import time
import json
import hmac
import os
from hashlib import md5
from sauceclient import SauceException
import re
from support.test_data import SingleTestData


class BaseTestReport:
    TEST_REPORT_DIR = "%s/../report" % os.path.dirname(os.path.abspath(__file__))

    def __init__(self):
        self.sauce_username = os.environ.get('SAUCE_USERNAME')
        self.sauce_access_key = os.environ.get('SAUCE_ACCESS_KEY')
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

    def save_geth(self, geth: dict):
        geth_paths = {}
        for log in geth.keys():
            geth_path = os.path.join(self.TEST_REPORT_DIR, log)
            result = open(geth_path, 'wb')
            result.write(geth[log])
            result.close()
            geth_paths[log] = geth_path
        return geth_paths

    def save_test(self, test, geth: dict = None):
        if geth:
            geth_paths = self.save_geth(geth)
        else:
            if hasattr(test, 'geth_paths'):
                geth_paths = test.geth_paths
            else:
                geth_paths = ''
        file_path = self.get_test_report_file_path(test.name)
        test_dict = {
            'testrail_case_id': test.testrail_case_id,
            'name': test.name,
            'geth_paths': geth_paths,
            'testruns': list(),
            'group_name': test.group_name
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
                    xfail=testrun_data['xfail']))
            tests.append(SingleTestData(name=test_data['name'],
                                        geth_paths=test_data['geth_paths'],
                                        testruns=testruns,
                                        testrail_case_id=test_data['testrail_case_id'],
                                        grop_name=test_data['group_name']))
        return tests

    def get_failed_tests(self):
        tests = self.get_all_tests()
        failed = list()
        for test in tests:
            if not self.is_test_successful(test):
                failed.append(test)
        return failed

    def get_passed_tests(self):
        tests = self.get_all_tests()
        passed = list()
        for test in tests:
            if self.is_test_successful(test):
                passed.append(test)
        return passed

    def get_sauce_token(self, job_id):
        return hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                        bytes(job_id, 'latin-1'), md5).hexdigest()

    def get_sauce_job_url(self, job_id, first_command=0):
        token = self.get_sauce_token(job_id)
        from tests.conftest import apibase
        url = 'https://%s/jobs/%s?auth=%s' % (apibase, job_id, token)
        if first_command > 0:
            url += "#%s" % first_command
        return url

    @staticmethod
    def get_jenkins_link_to_rerun_e2e(branch_name="develop", pr_id="", tr_case_ids=""):
        return 'https://ci.status.im/job/status-mobile/job/e2e/job/status-app-prs-rerun/parambuild/' \
               '?BRANCH_NAME=%s&PR_ID=%s&TR_CASE_IDS=%s' % (branch_name, pr_id, tr_case_ids)

    def get_sauce_final_screenshot_url(self, job_id):
        return 'https://media.giphy.com/media/9M5jK4GXmD5o1irGrF/giphy.gif'
        # temp blocked, no sense with groups
        # from tests.conftest import sauce, apibase
        # token = self.get_sauce_token(job_id)
        # username = sauce.accounts.account_user.get_active_user().username
        # for _ in range(10):
        #     try:
        #         scr_number = sauce.jobs.get_job_assets(username=username, job_id=job_id)['screenshots'][-1]
        #         return 'https://assets.%s/jobs/%s/%s?auth=%s' % (apibase, job_id, scr_number, token)
        #     except SauceException:
        #         time.sleep(3)

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
