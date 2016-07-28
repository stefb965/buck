package com.facebook.buck.jvm.java;


import com.facebook.buck.graph.AbstractBreadthFirstTraversal;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public interface RuleGatherer {
  /**
   * Gather the rules that should be packaged into a single JAR once this rule finishes executing.
   */
  ImmutableSortedSet<JavaLibrary> gatherRules(BuildRule root);

  /**
   * @return Just the root jar.
   */
  RuleGatherer SINGLE_JAR = new RuleGatherer() {
    @Override
    public ImmutableSortedSet<JavaLibrary> gatherRules(BuildRule root) {
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
      return ImmutableSortedSet.copyOf(foundRules);
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
    public ImmutableSortedSet<JavaLibrary> gatherRules(BuildRule root) {
      Preconditions.checkState(
          root instanceof MavenPublishable,
          "Expected %s to be publishable to maven.",
          root.getFullyQualifiedName());

      class Walker extends AbstractBreadthFirstTraversal<BuildRule> {

        private final String rootCoords;
        private JavaLibrary mavenRoot;
        private Set<HasMavenCoordinates> mavenDeps = new HashSet<>();
        private Set<BuildRule> everyDep = new HashSet<>();

        public Walker(BuildRule initialNode) {
          super(initialNode);
          this.rootCoords = Preconditions.checkNotNull(getCoords(initialNode));
        }

        @Override
        public Iterable<BuildRule> visit(BuildRule rule) throws RuntimeException {
          everyDep.add(rule);
          if (rule instanceof HasMavenCoordinates &&
              ((HasMavenCoordinates) rule).getMavenCoords().isPresent()) {
            HasMavenCoordinates publishable = (HasMavenCoordinates) rule;
            if (rootCoords.equals(getCoords(rule))) {
              if (rule instanceof JavaLibrary) {
                Preconditions.checkState(mavenRoot == null, "Attempting to set maven root twice");
                mavenRoot = (JavaLibrary) rule;
                return mavenRoot.getDepsForTransitiveClasspathEntries();
              }
              return rule.getDeps();
            }
            mavenDeps.add(publishable);
            return Collections.emptySet();
          }

          if (rule instanceof JavaLibrary) {
            return rule.getDeps();
          }
          return Collections.emptySet();
        }

        public Iterable<JavaLibrary> getDeps() {
          Set<BuildRule> deps = new HashSet<>();
          if (mavenRoot != null) {
            deps.addAll(mavenRoot.getDepsForTransitiveClasspathEntries());
            deps.add(mavenRoot);
          } else {
            deps.addAll(everyDep);
          }

          ImmutableSet<BuildRule> mavenJavaDeps = FluentIterable.from(mavenDeps)
              .filter(JavaLibrary.class)
              .transformAndConcat(new Function<JavaLibrary, Iterable<BuildRule>>() {
                @Override
                public Iterable<BuildRule> apply(JavaLibrary input) {
                  return ImmutableSet.<BuildRule>builder()
                      .add(input)
                      .addAll(input.getDepsForTransitiveClasspathEntries())
                      .build();
                }
              })
              .toSet();

          deps.removeAll(mavenJavaDeps);
          return FluentIterable.from(deps).filter(JavaLibrary.class);
        }

        @Nullable
        private String getCoords(BuildRule deriveFrom) {
          if (!(deriveFrom instanceof HasMavenCoordinates)) {
            return null;
          }

          HasMavenCoordinates mavenCoords = (HasMavenCoordinates) deriveFrom;

          if (!mavenCoords.getMavenCoords().isPresent()) {
            return null;
          }

          String coords = mavenCoords.getMavenCoords().get();
          Pattern p = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");
          Matcher m = p.matcher(coords);

          Preconditions.checkState(m.matches(), "Unable to parse maven coordinates: %s", coords);

          return
              m.group(1) + ':' + // group id
              m.group(2) + ':' + // artifact id
              m.group(7);        // version
        }
      }

      Walker walker = new Walker(root);
      walker.start();
      return FluentIterable.from(walker.getDeps()).toSortedSet(Ordering.natural());
    }
  };

  /**
   * @returns All transitive dependencies of the root jar.
   */
  RuleGatherer UBER_JAR = new RuleGatherer() {
    @Override
    public ImmutableSortedSet<JavaLibrary> gatherRules(BuildRule root) {
      return ImmutableSortedSet.copyOf(((HasClasspathEntries) root).getTransitiveClasspathDeps());
    }
  };

}
