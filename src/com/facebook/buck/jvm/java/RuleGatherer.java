package com.facebook.buck.jvm.java;


import com.facebook.buck.graph.AbstractBreadthFirstTraversal;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.UnflavoredBuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

public enum RuleGatherer {
  SINGLE_JAR {
    @Override
    public GatheredDeps gatherRules(BuildRule root) {
      // Search the deps for the JavaLibrary that matches the root's target.
      final BuildTarget unflavored =
          BuildTarget.of(root.getBuildTarget().getUnflavoredBuildTarget());

      Deque<BuildRule> toExplore = new ArrayDeque<>();
      toExplore.add(root);
      while (!toExplore.isEmpty()) {
        BuildRule rule = toExplore.remove();
        if (unflavored.equals(rule.getBuildTarget()) && rule instanceof JavaLibrary) {
          JavaLibrary javaLibrary = (JavaLibrary) rule;
          return new GatheredDeps(
              Collections.singleton(javaLibrary),
              javaLibrary.getTransitiveClasspathDeps(),
              Collections.<HasMavenCoordinates>emptySet());
        }
        toExplore.addAll(rule.getDeps());
      }
      throw new IllegalStateException(String.format(
          "Unable to find java library rule for %s",
          root));
    }
  },

  UBER_JAR {
    @Override
    public GatheredDeps gatherRules(BuildRule root) {
      ImmutableSortedSet<JavaLibrary> theWorld =
          ImmutableSortedSet.copyOf(((HasClasspathEntries) root).getTransitiveClasspathDeps());
      FirstOrderDeps collector = new FirstOrderDeps(root);
      collector.start();
      return new GatheredDeps(
          theWorld,
          theWorld,
          collector.getFirstOrderMavenDeps());
    }
  },

  /**
   * Gather all the rules required to build a maven jar. It is assumed that the root jar forms the
   * root of a "maven package" (that is, has its own maven_coords).
   */
  MAVEN_JAR {
    @Override
    public GatheredDeps gatherRules(BuildRule root) {
      if (!(root instanceof HasMavenCoordinates)) {
        throw new HumanReadableException(
            "Expected root to be publishable to maven: %s",
            root.getFullyQualifiedName());
      }

      FirstOrderDeps collector = new FirstOrderDeps(root);
      collector.start();

      return new GatheredDeps(
          collector.getPackagedLibraries(),
          collector.getTransitiveClasspath(),
          collector.getFirstOrderMavenDeps());
    }
  };

  /**
   * Gather the rules that should be packaged into a single JAR once this rule finishes executing.
   */
  public abstract GatheredDeps gatherRules(BuildRule root);

  public class GatheredDeps {
    private final Iterable<BuildRule> toPackage;
    private final Iterable<JavaLibrary> classpath;
    private final Iterable<HasMavenCoordinates> mavenDeps;

    private GatheredDeps(
        Iterable<JavaLibrary> toPackage,
        Iterable<JavaLibrary> classpath,
        Iterable<HasMavenCoordinates> mavenDeps) {
      this.toPackage = FluentIterable.from(toPackage).filter(BuildRule.class);
      this.classpath = classpath;
      this.mavenDeps = mavenDeps;
    }

    public Iterable<BuildRule> getRulesToPackage() {
      return toPackage;
    }

    public Iterable<JavaLibrary> getTransitiveClasspath() {
      return classpath;
    }

    public Iterable<HasMavenCoordinates> getMavenDeps() {
      return mavenDeps;
    }
  }

  private class FirstOrderDeps extends AbstractBreadthFirstTraversal<BuildRule> {

    private final BuildRule root;
    private final UnflavoredBuildTarget rootTarget;
    private final Set<JavaLibrary> firstOrder = new TreeSet<>();
    @Nullable
    private JavaLibrary rootLibrary;

    public FirstOrderDeps(BuildRule root) {
      super(root);
      this.root = root;

      // Make the assumption that the unflavored target is the root java library
      this.rootTarget = root.getBuildTarget().getUnflavoredBuildTarget();
    }

    @Override
    public Iterable<BuildRule> visit(BuildRule rule) throws RuntimeException {
      if (rootTarget.equals(rule.getBuildTarget().getUnflavoredBuildTarget())) {
        if (rule instanceof JavaLibrary) {
          rootLibrary = (JavaLibrary) rule;
        }
      }

      if (HasMavenCoordinates.MAVEN_COORDS_PRESENT_PREDICATE.apply(rule) &&
          !rule.equals(rootLibrary)) {
        if (rule instanceof JavaLibrary) {
          firstOrder.add((JavaLibrary) rule);
        }
      }

      Iterable<BuildRule> deps = rule.getDeps();

      firstOrder.addAll(FluentIterable.from(deps)
          .filter(HasMavenCoordinates.MAVEN_COORDS_PRESENT_PREDICATE)
          .filter(JavaLibrary.class)
          .toSet());

      return FluentIterable.from(deps)
          .filter(Predicates.not(HasMavenCoordinates.MAVEN_COORDS_PRESENT_PREDICATE))
          .toSet();
    }

    private ImmutableSortedSet<JavaLibrary> getFirstOrderJavaDeps() {
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

    Iterable<JavaLibrary> getPackagedLibraries() {
      ImmutableSortedSet<JavaLibrary> allJavaDeps = getFirstOrderJavaDeps();
      return FluentIterable.from(allJavaDeps)
          .filter(new Predicate<JavaLibrary>() {
            @Override
            public boolean apply(JavaLibrary input) {
              String name = HasMavenCoordinates.NORMALIZE_COORDINATE.apply(input);
              return name == null ||
                  rootTarget.equals(input.getBuildTarget().getUnflavoredBuildTarget());
            }
          });
    }

    Iterable<HasMavenCoordinates> getFirstOrderMavenDeps() {
      ImmutableSortedSet<JavaLibrary> allJavaDeps = getFirstOrderJavaDeps();
      return FluentIterable.from(allJavaDeps)
          .filter(HasMavenCoordinates.class)
          .filter(new Predicate<HasMavenCoordinates>() {
            @Override
            public boolean apply(HasMavenCoordinates input) {
              String name = HasMavenCoordinates.NORMALIZE_COORDINATE.apply(input);
              return name != null &&
                  !rootTarget.equals(input.getBuildTarget().getUnflavoredBuildTarget());
            }
          });
    }

    public Iterable<JavaLibrary> getTransitiveClasspath() {
      Preconditions.checkNotNull(rootLibrary);
      return rootLibrary.getTransitiveClasspathDeps();
    }
  }
}
