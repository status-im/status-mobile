import json
import requests
import emoji
import base64
from os import environ
from support.base_test_report import BaseTestReport


class TestrailReport(BaseTestReport):

    def __init__(self):
        super(TestrailReport, self).__init__()

        self.password = environ.get('TESTRAIL_PASS')
        self.user = environ.get('TESTRAIL_USER')

        self.run_id = None
        self.suite_id = 48
        self.project_id = 14

        self.outcomes = {
            'passed': 1,
            'undefined_fail': 10}

        self.headers = dict()
        self.headers['Authorization'] = 'Basic %s' % str(
            base64.b64encode(bytes('%s:%s' % (self.user, self.password), 'utf-8')), 'ascii').strip()
        self.headers['Content-Type'] = 'application/json'

        self.url = 'https://ethstatus.testrail.net/index.php?/'
        self.api_url = self.url + 'api/v2/'

    def get(self, method):
        return requests.get(self.api_url + method, headers=self.headers).json()

    def post(self, method, data):
        data = bytes(json.dumps(data), 'utf-8')
        return requests.post(self.api_url + method, data=data, headers=self.headers).json()

    def get_suites(self):
        return self.get('get_suites/%s' % self.project_id)

    def get_tests(self):
        return self.get('get_tests/%s' % self.run_id)

    def get_milestones(self):
        return self.get('get_milestones/%s' % self.project_id)

    def get_runs(self, pr_number):
        return [i for i in self.get('get_runs/%s' % self.project_id) if 'PR-%s ' % pr_number in i['name']]

    def get_run(self, run_id: int):
        return self.get('get_run/%s' % run_id)

    def get_last_pr_run(self, pr_number):
        run_id = max([run['id'] for run in self.get_runs(pr_number)])
        return self.get_run(run_id=run_id)

    @property
    def actual_milestone_id(self):
        return self.get_milestones()[-1]['id']

    def add_run(self, run_name):
        request_body = {'suite_id': self.suite_id,
                        'name': run_name,
                        'milestone_id': self.actual_milestone_id,
                        'case_ids': self.get_regression_cases(is_pr='PR-' in run_name),
                        'include_all': False}
        run = self.post('add_run/%s' % self.project_id, request_body)
        self.run_id = run['id']

    def get_cases(self, section_id):
        return self.get('get_cases/%s&suite_id=%s&section_id=%s' % (self.project_id, self.suite_id, section_id))

    def get_regression_cases(self, is_pr=False):
        test_cases = dict()
        test_cases['critical'] = 734
        test_cases['high'] = 735
        test_cases['medium'] = 736
        test_cases['low'] = 737
        case_ids = list()
        if is_pr:
            case_ids = [case['id'] for case in self.get_cases(test_cases['critical'])]
        else:
            for phase in test_cases:
                for case in self.get_cases(test_cases[phase]):
                    case_ids.append(case['id'])
        return case_ids

    def add_results(self):
        for test in self.get_all_tests():
            test_steps = "# Steps: \n"
            devices = str()
            method = 'add_result_for_case/%s/%s' % (self.run_id, test.testrail_case_id)
            last_testrun = test.testruns[-1]
            for step in last_testrun.steps:
                test_steps += step + "\n"
            for i, device in enumerate(last_testrun.jobs):
                devices += "# [Device %d](%s) \n" % (i + 1, self.get_sauce_job_url(device))
            data = {'status_id': self.outcomes['undefined_fail'] if last_testrun.error else self.outcomes['passed'],
                    'comment': '%s' % ('# Error: \n %s \n' % emoji.demojize(last_testrun.error)) + devices + test_steps if last_testrun.error
                    else devices + test_steps}
            self.post(method, data=data)

    def get_run_results(self):
        return self.get('get_results_for_run/%s' % self.run_id)

    def is_run_successful(self):
        for test in self.get_run_results():
            if test['status_id'] != 1:
                return False
        else:
            return True

    def get_test_result_link(self, test_run_id, test_case_id):
        try:
            test_id = self.get('get_results_for_case/%s/%s' % (test_run_id, test_case_id))[0]['test_id']
            return '%stests/view/%s' % (self.url, test_id)
        except KeyError:
            return None
