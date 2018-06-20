import json
import hmac
import os
from hashlib import md5
from support.test_data import SingleTestData


class BaseTestReport:

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

    def save_test(self, test):
        file_path = self.get_test_report_file_path(test.name)
        test_dict = {
            'testrail_case_id': test.testrail_case_id,
            'name': test.name,
            'testruns': list()
        }
        for testrun in test.testruns:
            test_dict['testruns'].append(testrun.__dict__)
        json.dump(test_dict, open(file_path, 'w'))

    def get_all_tests(self):
        tests = list()
        file_list = [f for f in os.listdir(self.TEST_REPORT_DIR)]
        for file_name in file_list:
            file_path = os.path.join(self.TEST_REPORT_DIR, file_name)
            test_data = json.load(open(file_path))
            testruns = list()
            for testrun_data in test_data['testruns']:
                testruns.append(SingleTestData.TestRunData(
                    steps=testrun_data['steps'], jobs=testrun_data['jobs'], error=testrun_data['error']))
            tests.append(SingleTestData(name=test_data['name'], testruns=testruns,
                                        testrail_case_id=test_data['testrail_case_id']))
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

    def get_sauce_job_url(self, job_id):
        token = hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                         bytes(job_id, 'latin-1'), md5).hexdigest()
        return "https://saucelabs.com/jobs/%s?auth=%s" % (job_id, token)

    @staticmethod
    def is_test_successful(test):
        # Test passed if last testrun has passed
        return test.testruns[-1].error is None
