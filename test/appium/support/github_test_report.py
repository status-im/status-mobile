import json
from hashlib import md5
import hmac
import os

from tests import SingleTestData


class GithubHtmlReport:

    TEST_REPORT_DIR = "%s/../report" % os.path.dirname(os.path.abspath(__file__))

    def __init__(self, sauce_username, sauce_access_key):
        self.sauce_username = sauce_username
        self.sauce_access_key = sauce_access_key
        self.init_report()

    def init_report(self):
        if not os.path.exists(self.TEST_REPORT_DIR):
            os.makedirs(self.TEST_REPORT_DIR)
        # delete all old files in report dir
        file_list = [f for f in os.listdir(self.TEST_REPORT_DIR)]
        for f in file_list:
            os.remove(os.path.join(self.TEST_REPORT_DIR, f))

    def get_test_report_file_path(self, test_name):
        file_name = "%s.json" % test_name
        return os.path.join(self.TEST_REPORT_DIR, file_name)

    def build_html_report(self):
        tests = self.get_all_tests()
        passed_tests = self.get_passed_tests()
        failed_tests = self.get_failed_tests()

        if len(tests) > 0:
            title_html = "## %.0f%% of end-end tests have passed\n" % (len(passed_tests) / len(tests) * 100)
            summary_html = "```\n"
            summary_html += "Total executed tests: %d\n" % len(tests)
            summary_html += "Failed tests: %d\n" % len(failed_tests)
            summary_html += "Passed tests: %d\n" % len(passed_tests)
            summary_html += "```\n"
            failed_tests_html = str()
            passed_tests_html = str()
            if failed_tests:
                failed_tests_html = self.build_tests_table_html(failed_tests, failed_tests=True)
            if passed_tests:
                passed_tests_html = self.build_tests_table_html(passed_tests, failed_tests=False)
            return title_html + summary_html + failed_tests_html + passed_tests_html
        else:
            return None

    def save_test(self, test):
        file_path = self.get_test_report_file_path(test.name)
        json.dump(test.__dict__, open(file_path, 'w'))

    def get_all_tests(self):
        tests = list()
        file_list = [f for f in os.listdir(self.TEST_REPORT_DIR)]
        for file_name in file_list:
            file_path = os.path.join(self.TEST_REPORT_DIR, file_name)
            test_dict = json.load(open(file_path))
            tests.append(SingleTestData(name=test_dict['name'], steps=test_dict['steps'],
                                        jobs=test_dict['jobs'], error=test_dict['error']))
        return tests

    def get_failed_tests(self):
        tests = self.get_all_tests()
        failed = list()
        for test in tests:
            if test.error is not None:
                failed.append(test)
        return failed

    def get_passed_tests(self):
        tests = self.get_all_tests()
        passed = list()
        for test in tests:
            if test.error is None:
                passed.append(test)
        return passed

    def build_tests_table_html(self, tests, failed_tests=False):
        tests_type = "Failed tests" if failed_tests else "Passed tests"
        html = "<h3>%s (%d)</h3>" % (tests_type, len(tests))
        html += "<details>"
        html += "<summary>Click to expand</summary>"
        html += "<br/>"
        html += "<table style=\"width: 100%\">"
        html += "<colgroup>"
        html += "<col span=\"1\" style=\"width: 20%;\">"
        html += "<col span=\"1\" style=\"width: 80%;\">"
        html += "</colgroup>"
        html += "<tbody>"
        html += "<tr>"
        html += "</tr>"
        for i, test in enumerate(tests):
            html += self.build_test_row_html(i, test)
        html += "</tbody>"
        html += "</table>"
        html += "</details>"
        return html

    def build_test_row_html(self, index, test):
        html = "<tr><td><b>%d. %s</b></td></tr>" % (index+1, test.name)
        html += "<tr><td>"
        test_steps_html = list()
        for step in test.steps:
            test_steps_html.append("<div>%s</div>" % step)
        if test.error:
            if test_steps_html:
                html += "<p>"
                html += "<blockquote>"
                # last 2 steps as summary
                html += "%s" % ''.join(test_steps_html[-2:])
                html += "</blockquote>"
                html += "</p>"
            html += "<code>%s</code>" % test.error
            html += "<br/><br/>"
        if test.jobs:
            html += self.build_device_sessions_html(test.jobs)
        html += "</td></tr>"
        return html

    def get_sauce_job_url(self, job_id):
        token = hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                         bytes(job_id, 'latin-1'), md5).hexdigest()
        return "https://saucelabs.com/jobs/%s?auth=%s" % (job_id, token)

    def build_device_sessions_html(self, jobs):
        html = "<ins>Device sessions:</ins>"
        html += "<p><ul>"
        for i, job_id in enumerate(jobs):
            html += "<li><a href=\"%s\">Device %d</a></li>" % (self.get_sauce_job_url(job_id), i+1)
        html += "</ul></p>"
        return html
