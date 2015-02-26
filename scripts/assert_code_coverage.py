#!/usr/bin/env python

import xml.etree.ElementTree as ElementTree
import os
import sys

# This parses buck-out/gen/jacoco/code-coverage/coverage.xml after
# `buck test --all --code-coverage --code-coverage-format xml --no-results-cache`
# has been run.
PATH_TO_CODE_COVERAGE_XML = 'buck-out/gen/jacoco/code-coverage/coverage.xml'

# If the code coverage for the project drops below this threshold,
# fail the build. This is designed to far enough below our current
# standards (80% coverage) that this should be possible to sustain
# given the inevitable ebb and flow of the code coverage level.
# Note: our Darwin test machines don't have all the packages
# installed that we do on Linux, so the code coverage there is
# naturally lower.
CODE_COVERAGE_GOAL = {
    'Linux': 78,
    'Darwin': 68,
}


def is_covered_package_name(package_name):
    """We exclude third-party code."""
    if not package_name.startswith('com/facebook/buck/'):
        return False
    return True


def is_covered_class_name(class_name):
    """We exclude classes (probably) generated by immutables."""
    if "/Immutable" in class_name:
        return False
    return True


def calculate_code_coverage():
    class CoverageTree:
        def __init__(self, name):
            self.name = name
            self.children = []

        def get_name(self):
            return self.name

        def add_child(self, child):
            self.children.append(child)

        def get_number_of_children(self):
            return len(self.children)

        def get_covered(self, name):
            return sum(map(lambda x: x.get_covered(name), self.children))

        def get_missed(self, name):
            return sum(map(lambda x: x.get_missed(name), self.children))

        def get_percentage(self, name):
            return round(
                100 *
                self.get_covered(name) /
                float(self.get_missed(name) + self.get_covered(name)),
                2)

    class CoverageLeaf:
        def __init__(self):
            self.missed = {}
            self.covered = {}

        def get_covered(self, name):
            return self.covered[name]

        def set_covered(self, name, value):
            self.covered[name] = value

        def get_missed(self, name):
            return self.missed[name]

        def set_missed(self, name, value):
            self.missed[name] = value

    root = ElementTree.parse(PATH_TO_CODE_COVERAGE_XML)

    # The length of the longest Java package included in the report.
    # Used for display purposes.
    max_package_name = 0

    # Coverage types measured by Jacoco.
    TYPES = set([
        'BRANCH',
        'CLASS',
        'COMPLEXITY',
        'INSTRUCTION',
        'LINE',
        'METHOD',
    ])

    # List determines column display order in final report.
    COLUMN_NAMES = [
        'INSTRUCTION',
        'LINE',
        'BRANCH',
        'METHOD',
        'CLASS',
        'LOC2FIX',
    ]

    # Column by which rows will be sorted in the final report.
    SORT_TYPE = 'INSTRUCTION'

    # Keys are values from TYPES; values are integers.
    total_covered_by_type = {}
    total_missed_plus_covered_type = {}
    for coverage_type in TYPES:
        total_covered_by_type[coverage_type] = 0
        total_missed_plus_covered_type[coverage_type] = 0

    # Values are dicts. Will have key 'package_name' as well as all values
    # from TYPES as keys. For entries from TYPES, values are the corresponding
    # coverage percentage for that type.
    coverage_by_package = []

    # Track count of untested lines to see which packages
    # have the largest amount of untested code.
    missed_lines_by_package = {}
    total_missed_lines = 0

    for package_element in root.findall('.//package'):
        package_name = package_element.attrib['name']
        if not is_covered_package_name(package_name):
            continue

        max_package_name = max(max_package_name, len(package_name))
        coverage = CoverageTree(package_name)
        coverage_by_package.append(coverage)

        for class_element in package_element.findall('./class'):
            class_name = class_element.attrib['name']
            if not is_covered_class_name(class_name):
                continue

            class_leaf = CoverageLeaf()
            coverage.add_child(class_leaf)
            for counter in class_element.findall('./counter'):
                counter_type = counter.attrib.get('type')
                missed = int(counter.attrib.get('missed'))
                class_leaf.set_missed(counter_type, missed)
                covered = int(counter.attrib.get('covered'))
                class_leaf.set_covered(counter_type, covered)

                total_covered_by_type[counter_type] += covered
                total_missed_plus_covered_type[counter_type] += missed + covered

                if counter_type == 'LINE':
                    missed_lines_by_package[package_name] = missed
                    total_missed_lines += missed

    def pair_compare(p1, p2):
        # High percentage should be listed first.

        diff1 = cmp(p2.get_percentage(SORT_TYPE), p1.get_percentage(SORT_TYPE))
        if diff1:
            return diff1
        # Ties are broken by lexicographic comparison.
        return cmp(p1.get_name(), p2.get_name)

    def label_with_padding(label):
        return label + ' ' * (max_package_name - len(label)) + ' '

    def column_format_str(column_name):
        if column_name == 'LOC2FIX':
            return '%(' + column_name + ')8d'
        else:
            return '%(' + column_name + ')7.2f%%'

    def print_separator(sep_len):
        print '-' * sep_len

    def get_color_for_percentage(percentage):
        # \033[92m is OKGREEN.
        # \033[93m is WARNING.
        platform = os.uname()[0]
        return '\033[92m' if percentage >= CODE_COVERAGE_GOAL[platform] else '\033[93m'

    # Print header.
    # Type column headers are right-justified and truncated to 7 characters.
    column_names = map(lambda x: x[0:7].rjust(7), COLUMN_NAMES)
    print label_with_padding('PACKAGE') + ' ' + ' '.join(column_names)
    separator_len = max_package_name + 1 + len(column_names) * 8
    print_separator(separator_len)

    # Create the format string to use for each row.
    format_string = '%(color)s%(label)s'
    for column in COLUMN_NAMES:
        format_string += column_format_str(column)
    format_string += '\033[0m'

    # Print rows sorted by line coverage then package name.
    coverage_by_package = filter(lambda x: x.get_number_of_children() > 0, coverage_by_package)
    coverage_by_package.sort(cmp=pair_compare)
    for item in coverage_by_package:
        info = {}
        pkg = item.get_name()
        for type_name in TYPES:
            try:
                info[type_name] = item.get_percentage(type_name)
            except KeyError:
                # It is possible to have a module of Java code with no branches.
                info['BRANCH'] = 100
        info['color'] = get_color_for_percentage(item.get_percentage(SORT_TYPE))
        info['label'] = label_with_padding(pkg)
        info['LOC2FIX'] = missed_lines_by_package[pkg]
        print format_string % info

    # Print aggregate numbers.
    overall_percentages = {}
    for coverage_type in TYPES:
        numerator = total_covered_by_type[coverage_type]
        denominator = total_missed_plus_covered_type[coverage_type]
        percentage = 100.0 * numerator / denominator
        overall_percentages[coverage_type] = percentage

    observed_percentage = overall_percentages[SORT_TYPE]
    overall_percentages['color'] = get_color_for_percentage(observed_percentage)
    overall_percentages['label'] = label_with_padding('TOTAL')
    overall_percentages['LOC2FIX'] = total_missed_lines
    print_separator(separator_len)
    print format_string % overall_percentages

    return observed_percentage


def main():
    """Exits with 0 or 1 depending on whether the code coverage goal is met."""
    coverage = calculate_code_coverage()
    platform = os.uname()[0]
    if coverage < CODE_COVERAGE_GOAL[platform]:
        data = {
            'expected': CODE_COVERAGE_GOAL[platform],
            'observed': coverage,
        }
        print '\033[91mFAIL: %(observed).2f%% does not meet goal of %(expected).2f%%\033[0m' % data
        sys.exit(1)


if __name__ == '__main__':
    main()
