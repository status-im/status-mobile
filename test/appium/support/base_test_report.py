import json
import os
import re
import urllib

from support.lambda_test import get_session_info
from support.test_data import SingleTestData


class BaseTestReport:
    TEST_REPORT_DIR = "%s/../report" % os.path.dirname(os.path.abspath(__file__))

    def __init__(self):
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

    def save_logs(self, logs: dict):
        logs_paths = {}
        for log in logs.keys():
            log_path = os.path.join(self.TEST_REPORT_DIR, log)
            result = open(log_path, 'wb')
            result.write(logs[log])
            result.close()
            logs_paths[log] = log_path
        return logs_paths

    def save_test(self, test, geth: dict = None, requests_log: dict = None):
        for log in geth, requests_log:
            if log:
                logs_paths = self.save_logs(log)
            else:
                if hasattr(test, 'logs_paths'):
                    logs_paths = test.logs_paths
                else:
                    logs_paths = ''

        file_path = self.get_test_report_file_path(test.name)
        test_dict = {
            'testrail_case_id': test.testrail_case_id,
            'name': test.name,
            'logs_paths': logs_paths,
            'testruns': list(),
            'group_name': test.group_name,
            'secured': test.secured
        }
        for testrun in test.testruns:
            test_dict['testruns'].append(testrun.__dict__)
        json.dump(test_dict, open(file_path, 'w'))

    def get_all_tests(self):
        tests = list()
        file_list = [f for f in os.listdir(self.TEST_REPORT_DIR) if f.endswith('json')]
        for file_name in file_list:
            file_path = os.path.join(self.TEST_REPORT_DIR, file_name)
            test_data = json.load(open(file_path))
            testruns = list()
            for testrun_data in test_data['testruns']:
                testruns.append(SingleTestData.TestRunData(
                    steps=testrun_data['steps'],
                    jobs=testrun_data['jobs'],
                    error=testrun_data['error'],
                    first_commands=testrun_data['first_commands'],
                    xfail=testrun_data['xfail'],
                    run=testrun_data['run']))
            tests.append(SingleTestData(name=test_data['name'],
                                        logs_paths=test_data['logs_paths'],
                                        testruns=testruns,
                                        testrail_case_id=test_data['testrail_case_id'],
                                        grop_name=test_data['group_name'],
                                        secured=test_data['secured']))
        return tests

    def get_tests_by_status(self):
        tests = self.get_all_tests()
        passed, failed, xfailed = list(), list(), list()
        for test in tests:
            if self.is_test_successful(test):
                passed.append(test)
            else:
                if test.testruns[-1].xfail:
                    xfailed.append(test)
                else:
                    failed.append(test)
        return passed, failed, xfailed

    def get_lambda_test_job_url(self, job_id, first_command=0):
        return "https://appautomation.lambdatest.com/test?testID=" + get_session_info(job_id)['test_id']

    @staticmethod
    def get_jenkins_link_to_rerun_e2e(branch_name="develop", pr_id="", tr_case_ids=""):
        branch_name = urllib.parse.quote(branch_name)
        return 'https://ci.status.im/job/status-mobile/job/e2e/job/status-app-prs-rerun/parambuild/' \
               '?BRANCH_NAME=%s&PR_ID=%s&APK_NAME=%s.apk&TR_CASE_IDS=%s' % (branch_name, pr_id, pr_id, tr_case_ids)

    @staticmethod
    def is_test_successful(test):
        # Test passed if last testrun has passed
        return test.testruns[-1].error is None

    @staticmethod
    def separate_xfail_error(error):
        issue_id_list = re.findall(r'#\d+', error)
        issue_id = issue_id_list[0] if issue_id_list else ''

        xfail_error = re.findall(r'\[\[.*\]\]', error)
        if xfail_error:
            no_code_error_str = xfail_error[0]
            main_error = error.replace(no_code_error_str, '')
        else:
            no_code_error_str = ''
            main_error = error

        return main_error, no_code_error_str, issue_id
