package com.facebook.buck.jvm.java;


import static com.facebook.buck.jvm.java.HasMavenCoordinates.NORMALIZE_COORDINATE;

import com.facebook.buck.graph.AbstractBreadthFirstTraversal;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

public interface RuleGatherer {
  /**
   * Gather the rules that should be packaged into a single JAR once this rule finishes executing.
   */
  GatheredDeps gatherRules(BuildRule root);

  public class GatheredDeps {
    private final Iterable<BuildRule> toPackage;
    private final ImmutableSortedSet<SourcePath> sources;
    private final Iterable<JavaLibrary> classpath;
    private final Iterable<HasMavenCoordinates> mavenDeps;

    private GatheredDeps(
        Iterable<JavaLibrary> toPackage,
        Iterable<? extends HasSources> sources,
        Iterable<JavaLibrary> classpath,
        Iterable<HasMavenCoordinates> mavenDeps) {
      this.toPackage = FluentIterable.from(toPackage).filter(BuildRule.class);
      this.sources = FluentIterable.from(sources)
          .transformAndConcat(
              new Function<HasSources, Iterable<SourcePath>>() {
                @Override
                public Iterable<SourcePath> apply(HasSources input) {
                  return input.getSources();
                }
              })
          .toSortedSet(Ordering.<SourcePath>natural());
      this.classpath = classpath;
      this.mavenDeps = mavenDeps;
    }

    public Iterable<BuildRule> getRulesToPackage() {
      return toPackage;
    }

    public ImmutableSortedSet<SourcePath> getSourceCode() {

      return sources;
    }

    public Iterable<JavaLibrary> getTransitiveClasspath() {
      return classpath;
    }

    public Iterable<HasMavenCoordinates> getMavenDeps() {
      return mavenDeps;
    }
  }

  /**
   * @return Just the root jar.
   */
  RuleGatherer SINGLE_JAR = new RuleGatherer() {
    @Override
    public GatheredDeps gatherRules(BuildRule root) {
      // Search the deps for the JavaLibrary that matches the root's target.
      final BuildTarget unflavored =
          BuildTarget.of(root.getBuildTarget().getUnflavoredBuildTarget());

      final Set<JavaLibrary> foundRules = new HashSet<>();
      new AbstractBreadthFirstTraversal<BuildRule>(root) {

        @Override
        public Iterable<BuildRule> visit(BuildRule rule) throws RuntimeException {
          if (unflavored.equals(rule.getBuildTarget()) && rule instanceof JavaLibrary) {
            foundRules.add((JavaLibrary) rule);
            return Collections.emptySet();
          }

          if (foundRules.isEmpty()) {
            return rule.getDeps();
          }
          return Collections.emptySet();
        }
      }.start();

      Preconditions.checkState(
          foundRules.size() == 1,
          "Unable to find java library rule for %s",
          root);
      JavaLibrary javaLibrary = Iterables.getOnlyElement(foundRules);
      return new GatheredDeps(
          Collections.singleton(javaLibrary),
          Collections.singleton(javaLibrary),
          javaLibrary.getTransitiveClasspathDeps(),
          // TODO: Perhaps gather the maven deps properly
          Collections.<HasMavenCoordinates>emptySet());
    }
  };

  /**
   * Gather all the rules required to build a maven jar. It is assumed that the root jar forms the
   * root of a "maven package" (that is, has its own maven_coords).
   *
   * @return The transitive set of rules that do not cross a maven boundary.
   */
  RuleGatherer MAVEN_JAR = new RuleGatherer() {
    @Override
    public GatheredDeps gatherRules(final BuildRule root) {
      if (!(root instanceof HasMavenCoordinates)) {
        throw new HumanReadableException(
            "Expected root to be publishable to maven: %s",
            root.getFullyQualifiedName());
      }

      class FirstOrderDeps extends AbstractBreadthFirstTraversal<BuildRule> {

        private final String rootCoord;
        private final Set<JavaLibrary> firstOrder = new TreeSet<>();
        private final Set<JavaLibrary> packageable = new TreeSet<>();
        @Nullable
        private JavaLibrary rootLibrary;

        public FirstOrderDeps(BuildRule root) {
          super(root);
          rootCoord = Preconditions.checkNotNull(NORMALIZE_COORDINATE.apply(root));
        }

        @Override
        public Iterable<BuildRule> visit(BuildRule rule) throws RuntimeException {
          String coord = NORMALIZE_COORDINATE.apply(rule);

          Set<BuildRule> nonMavenDeps = FluentIterable.from(rule.getDeps())
              .filter(Predicates.not(HasMavenCoordinates.MAVEN_COORDS_PRESENT_PREDICATE))
              .toSet();

          if (HasMavenCoordinates.MAVEN_COORDS_PRESENT_PREDICATE.apply(rule)) {
            if (rootCoord.equals(coord)) {
              if (rule instanceof JavaLibrary) {
                rootLibrary = (JavaLibrary) rule;
              }
              return rule.getDeps();
            }

            if (rule instanceof JavaLibrary) {
              firstOrder.add((JavaLibrary) rule);
            }
          } else {
            packageable.addAll(FluentIterable.from(nonMavenDeps).filter(JavaLibrary.class).toSet());
          }

          return nonMavenDeps;
        }

        public ImmutableSortedSet<JavaLibrary> getFirstOrderJavaDeps() {
          Preconditions.checkState(
              rootLibrary != null,
              "Unable to determine root library. None of this will work: %s", root);

          Set<JavaLibrary> toReturn = new HashSet<>();
          toReturn.addAll(rootLibrary.getTransitiveClasspathDeps());

          // Remove all transitive classpath entries of first order deps
          toReturn.removeAll(
              FluentIterable.from(firstOrder)
                  .transformAndConcat(new Function<HasClasspathEntries, Iterable<JavaLibrary>>() {
                    @Override
                    public Iterable<JavaLibrary> apply(HasClasspathEntries input) {
                      return Sets.difference(
                          input.getTransitiveClasspathDeps(),
                          Collections.singleton(input));
                    }
                  })
                  .toSet());

          return ImmutableSortedSet.copyOf(toReturn);
        }

        public GatheredDeps getGatheredDeps() {
          ImmutableSortedSet<JavaLibrary> allJavaDeps = getFirstOrderJavaDeps();
          FluentIterable<JavaLibrary> ownJava = FluentIterable.from(allJavaDeps)
              .filter(new Predicate<JavaLibrary>() {
                @Override
                public boolean apply(JavaLibrary input) {
                  String name = HasMavenCoordinates.NORMALIZE_COORDINATE.apply(input);
                  return name == null || rootCoord.equals(name);
                }
              });
          return new GatheredDeps(
              allJavaDeps,
              ownJava.filter(HasSources.class),
              rootLibrary.getTransitiveClasspathDeps(),
              FluentIterable.from(allJavaDeps)
                  .filter(HasMavenCoordinates.class)
                  .filter(new Predicate<HasMavenCoordinates>() {
                @Override
                public boolean apply(HasMavenCoordinates input) {
                  String name = HasMavenCoordinates.NORMALIZE_COORDINATE.apply(input);
                  return name != null && !rootCoord.equals(name);
                }
              }));
        }
      }

      FirstOrderDeps collector = new FirstOrderDeps(root);
      collector.start();

      return collector.getGatheredDeps();
    }
  };

  /**
   * @return All transitive dependencies of the root jar.
   */
  RuleGatherer UBER_JAR = new RuleGatherer() {
    @Override
    public GatheredDeps gatherRules(BuildRule root) {
      ImmutableSortedSet<JavaLibrary> theWorld =
          ImmutableSortedSet.copyOf(((HasClasspathEntries) root).getTransitiveClasspathDeps());
      return new GatheredDeps(
          theWorld,
          FluentIterable.from(theWorld).filter(HasSources.class),
          theWorld,
          // TODO: Perhaps gather the maven deps properly?
          Collections.<HasMavenCoordinates>emptySet());
    }
  };
}
