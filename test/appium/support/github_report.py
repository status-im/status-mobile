import os
from support.base_test_report import BaseTestReport
from support.testrail_report import TestrailReport
import re


class GithubHtmlReport(BaseTestReport):
    TEST_REPORT_DIR = "%s/../report" % os.path.dirname(os.path.abspath(__file__))

    def __init__(self):
        super(GithubHtmlReport, self).__init__()

    def list_of_failed_testrail_ids(self, tests_data):
        ids_failed_test = []
        for i, test in enumerate(tests_data):
            if test.testrail_case_id:
                ids_failed_test.append(test.testrail_case_id)
        return ','.join(map(str, ids_failed_test))

    def build_html_report(self, run_id):
        tests = self.get_all_tests()
        passed_tests = self.get_passed_tests()
        failed_tests = self.get_failed_tests()
        not_executed_tests = TestrailReport().get_not_executed_tests(run_id)

        if len(tests) > 0:
            title_html = "## %.0f%% of end-end tests have passed\n" % (len(passed_tests) / len(tests) * 100)
            summary_html = "```\n"
            summary_html += "Total executed tests: %d\n" % len(tests)
            summary_html += "Failed tests: %d\n" % len(failed_tests)
            summary_html += "Passed tests: %d\n" % len(passed_tests)
            if not_executed_tests:
                summary_html += "Not executed tests: %d\n" % len(not_executed_tests)
            summary_html += "```\n"
            not_executed_tests_html = str()
            failed_tests_html = str()
            passed_tests_html = str()
            if not_executed_tests:
                not_executed_tests_html = self.build_tests_table_html(not_executed_tests, run_id,
                                                                      not_executed_tests=True)
                summary_html += "```\n"
                summary_html += 'IDs of not executed tests: %s \n' % ','.join([str(i) for i in not_executed_tests])
                summary_html += "```\n"
            if failed_tests:
                failed_tests_html = self.build_tests_table_html(failed_tests, run_id, failed_tests=True)
                summary_html += "```\n"
                summary_html += 'IDs of failed tests: %s \n' % self.list_of_failed_testrail_ids(failed_tests)
                summary_html += "```\n"
            if passed_tests:
                passed_tests_html = self.build_tests_table_html(passed_tests, run_id, failed_tests=False)
            return title_html + summary_html + not_executed_tests_html + failed_tests_html + passed_tests_html
        else:
            return None

    def build_tests_table_html(self, tests, run_id, failed_tests=False, not_executed_tests=False):
        if failed_tests:
            tests_type = "Failed tests"
        elif not_executed_tests:
            tests_type = "Not executed tests"
        else:
            tests_type = "Passed tests"
        html = "<h3>%s (%d)</h3>" % (tests_type, len(tests))
        html += "<details>"
        html += "<summary>Click to expand</summary>"
        html += "<br/>"

        from tests import pytest_config_global
        pr_id = pytest_config_global['pr_number']
        apk_name = pytest_config_global['apk']

        if not_executed_tests:
            html += "<li><a href=\"%s\">Rerun not executed tests</a></li>" % self.get_jenkins_link_to_rerun_e2e(
                pr_id=pr_id,
                tr_case_ids=','.join([str(i) for i in tests]))

        if failed_tests:
            html += "<li><a href=\"%s\">Rerun failed tests</a></li>" % self.get_jenkins_link_to_rerun_e2e(
                pr_id=pr_id,
                tr_case_ids=','.join([str(test.testrail_case_id) for test in tests]))

        if not not_executed_tests:
            groups = {i: list() for i in set([test.group_name for test in tests])}
            for i in tests:
                groups[i.group_name].append(i)

            html += "<br/>"

            for class_name, tests_list in groups.items():
                if class_name:
                    html += "<h4>Class %s:</h4>" % class_name
                else:
                    html += "<h4>Single device tests:</h4>"
                html += "<table style=\"width: 100%\">"
                html += "<colgroup>"
                html += "<col span=\"1\" style=\"width: 20%;\">"
                html += "<col span=\"1\" style=\"width: 80%;\">"
                html += "</colgroup>"
                html += "<tbody>"
                html += "<tr>"
                html += "</tr>"
                for i, test in enumerate(tests_list):
                    html += self.build_test_row_html(i, test, run_id)
                html += "</tbody>"
                html += "</table>"
        html += "</details>"
        return html

    def build_test_row_html(self, index, test, run_id):
        test_rail_link = TestrailReport().get_test_result_link(run_id, test.testrail_case_id)
        if test_rail_link:
            html = "<tr><td><b>%s. <a href=\"%s\">%s</a>, id: %s </b></td></tr>" % (
                index + 1, test_rail_link, test.name, test.testrail_case_id)
        else:
            html = "<tr><td><b>%d. %s</b> (TestRail link is not found)</td></tr>" % (index + 1, test.name)
        html += "<tr><td>"
        test_steps_html = list()
        last_testrun = test.testruns[-1]
        for step in last_testrun.steps:
            test_steps_html.append("<div>%s</div>" % step)
        if last_testrun.error:
            error = last_testrun.error
            if test_steps_html:
                html += "<p>"
                html += "<blockquote>"
                # last 2 steps as summary
                html += "%s" % ''.join(test_steps_html[-2:])
                html += "</blockquote>"
                html += "</p>"
            code_error, no_code_error_str, _ = self.separate_xfail_error(error)
            if no_code_error_str:
                html += "\n\n```\n%s\n```\n\n" % code_error
                html += "<b>%s</b>" % no_code_error_str
            else:
                html += "\n\n```\n%s\n```\n\n" % error.replace("[[", "<b>[[").replace("]]", "]]</b>")
            html += "<br/><br/>"
        if last_testrun.jobs:
            html += self.build_device_sessions_html(last_testrun)
        html += "</td></tr>"
        return html

    def build_device_sessions_html(self, test_run):
        html = "<ins>Device sessions</ins>"
        html += "<p><ul>"
        for job_id, i in test_run.jobs.items():
            html += "<p>"
            html += "Device %d:" % i
            html += "<ul>"
            if test_run.first_commands:
                html += "<li><a href=\"%s\">Steps, video, logs</a></li>" % \
                        self.get_sauce_job_url(job_id, test_run.first_commands[job_id])
            else:
                html += "<li><a href=\"%s\">Steps, video, logs</a></li>" % self.get_sauce_job_url(job_id)
            if test_run.error:
                html += "<li><a href=\"%s\">Failure screenshot</a></li>" % self.get_sauce_final_screenshot_url(job_id)
            html += "</ul></p>"
        html += "</ul></p>"
        return html
