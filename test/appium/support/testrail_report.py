import json
import requests
import base64
from os import environ
from support.base_test_report import BaseTestReport


class TestrailReport(BaseTestReport):

    def __init__(self, sauce_username, sauce_access_key):
        super(TestrailReport, self).__init__(sauce_username, sauce_access_key)

        self.password = environ.get('TESTRAIL_PASS')
        self.user = environ.get('TESTRAIL_USER')

        self.run_id = None
        self.suite_id = 42
        self.project_id = 9

        self.outcomes = {
            'passed': 1,
            'undefined_fail': 10}

        self.headers = dict()
        self.headers['Authorization'] = 'Basic %s' % str(
            base64.b64encode(bytes('%s:%s' % (self.user, self.password), 'utf-8')), 'ascii').strip()
        self.headers['Content-Type'] = 'application/json'

        self.url = 'https://ethstatus.testrail.net/index.php?/api/v2/'

    def get(self, method):
        raw_response = requests.get(self.url + method, headers=self.headers).text
        return json.loads(raw_response)

    def post(self, method, data):
        data = bytes(json.dumps(data), 'utf-8')
        raw_response = requests.post(self.url + method, data=data, headers=self.headers).text
        return json.loads(raw_response)

    def get_suites(self):
        return self.get('get_suites/%s' % self.project_id)

    def get_tests(self):
        return self.get('get_tests/%s' % self.run_id)

    def get_milestones(self):
        return self.get('get_milestones/%s' % self.project_id)

    @property
    def actual_milestone_id(self):
        return self.get_milestones()[-1]['id']

    def add_run(self, run_name):
        request_body = {'suite_id': self.suite_id,
                        'name': run_name,
                        'milestone_id': self.actual_milestone_id}
        run = self.post('add_run/%s' % self.project_id, request_body)
        self.run_id = run['id']

    def add_results(self):
        for test in self.get_all_tests():
            test_steps = "# Steps: \n"
            devices = str()
            method = 'add_result_for_case/%s/%s' % (self.run_id, test.testrail_case_id)
            for step in test.steps:
                test_steps += step + "\n"
            for i, device in enumerate(test.jobs):
                devices += "# [Device %d](%s) \n" % (i + 1, self.get_sauce_job_url(device))
            data = {'status_id': self.outcomes['undefined_fail'] if test.error else self.outcomes['passed'],
                    'comment': '%s' % ('# Error: \n %s \n' % test.error) + devices + test_steps if test.error
                    else devices + test_steps}
            self.post(method, data=data)
