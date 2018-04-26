import json
import hmac
import os
from hashlib import md5
from tests import SingleTestData


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
        json.dump(test.__dict__, open(file_path, 'w'))

    def get_all_tests(self):
        tests = list()
        file_list = [f for f in os.listdir(self.TEST_REPORT_DIR)]
        for file_name in file_list:
            file_path = os.path.join(self.TEST_REPORT_DIR, file_name)
            test_dict = json.load(open(file_path))
            tests.append(SingleTestData(name=test_dict['name'], steps=test_dict['steps'],
                                        jobs=test_dict['jobs'], error=test_dict['error'],
                                        testrail_case_id=test_dict['testrail_case_id']))
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

    def get_sauce_job_url(self, job_id):
        token = hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                         bytes(job_id, 'latin-1'), md5).hexdigest()
        return "https://saucelabs.com/jobs/%s?auth=%s" % (job_id, token)