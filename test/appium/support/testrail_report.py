import json
import requests
import logging
import itertools
import emoji
import base64
from os import environ
from support.base_test_report import BaseTestReport
from sys import argv
from json import JSONDecodeError


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
        self.headers['x-api-ident'] = 'beta'

        self.url = 'https://ethstatus.testrail.net/index.php?/'
        self.api_url = self.url + 'api/v2/'

    def get(self, method):
        rval = requests.get(self.api_url + method, headers=self.headers).json()
        if 'error' in rval:
            logging.error("Failed TestRail request: %s" % rval['error'])
        return rval

    def post(self, method, data):
        data = bytes(json.dumps(data), 'utf-8')
        return requests.post(self.api_url + method, data=data, headers=self.headers).json()

    def add_attachment(self, method, path):
        files = {'attachment': (open(path, 'rb'))}
        result = requests.post(self.api_url + method, headers={'Authorization': self.headers['Authorization']},
                               files=files)
        files['attachment'].close()
        try:
            return result.json()
        except JSONDecodeError:
            pass

    def get_suites(self):
        return self.get('get_suites/%s' % self.project_id)

    def get_tests(self):
        return self.get('get_tests/%s' % self.run_id)['tests']

    def get_milestones(self):
        return self.get('get_milestones/%s' % self.project_id)['milestones']

    def get_runs(self, pr_number):
        return [i for i in self.get('get_runs/%s' % self.project_id)['runs'] if 'PR-%s ' % pr_number in i['name']]

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
                        'case_ids': self.get_regression_cases(),
                        'include_all': False}
        run = self.post('add_run/%s' % self.project_id, request_body)
        self.run_id = run['id']

    def get_cases(self, section_ids):
        test_cases = list()
        for section_id in section_ids:
            test_cases.append(
                self.get('get_cases/%s&suite_id=%s&section_id=%s' % (self.project_id, self.suite_id, section_id))[
                    'cases'])
        return itertools.chain.from_iterable(test_cases)

    def get_regression_cases(self):
        test_cases = dict()
        test_cases['critical'] = 730
        test_cases['medium'] = 736
        test_cases['upgrade'] = 881
        test_cases['public_chat'] = 50654
        test_cases['one_to_one_chat'] = 50655
        case_ids = list()
        for arg in argv:
            if "run_testrail_ids" in arg:
                key, value = arg.split('=')
                case_ids = value.split(',')
        if len(case_ids) == 0:
            if 'critical or high' in argv:
                for case in self.get_cases([test_cases['critical'],  test_cases['public_chat'], test_cases['one_to_one_chat']]):
                    case_ids.append(case['id'])
            elif 'upgrade' in argv and 'not upgrade' not in argv:
                for case in self.get_cases([test_cases['upgrade']]):
                    case_ids.append(case['id'])
            else:
                for phase in test_cases:
                    if phase != 'upgrade':
                        for case in self.get_cases([test_cases[phase]]):
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
                if last_testrun.first_commands:
                    devices += "# [Device %d](%s) \n" % (
                        i + 1, self.get_sauce_job_url(job_id=device, first_command=last_testrun.first_commands[device]))
                else:
                    devices += "# [Device %d](%s) \n" % (i + 1, self.get_sauce_job_url(job_id=device))
            data = {'status_id': self.outcomes['undefined_fail'] if last_testrun.error else self.outcomes['passed'],
                    'comment': '%s' % ('# Error: \n %s \n' % emoji.demojize(
                        last_testrun.error)) + devices + test_steps if last_testrun.error
                    else devices + test_steps}
            try:
                result_id = self.post(method, data=data)['id']
            except KeyError:
                result_id = ''
            if last_testrun.error:
                for geth in test.geth_paths.keys():
                    self.add_attachment(method='add_attachment_to_result/%s' % str(result_id),
                                        path=test.geth_paths[geth])
        self.change_test_run_description()

    def change_test_run_description(self):
        tests = self.get_all_tests()
        passed_tests = self.get_passed_tests()
        failed_tests = self.get_failed_tests()
        final_description = "Nothing to report this time..."
        if len(tests) > 0:
            description_title = "# %.0f%% of end-end tests have passed\n" % (len(passed_tests) / len(tests) * 100)
            description_title += "\n"
            description_title += "Total executed tests: %d\n" % len(tests)
            description_title += "Failed tests: %d\n" % len(failed_tests)
            description_title += "Passed tests: %d\n" % len(passed_tests)
            description_title += "\n"
            ids_failed_test = []
            description, case_info = '', ''
            if failed_tests:
                for i, test in enumerate(failed_tests):
                    last_testrun = test.testruns[-1]
                    test_rail_link = self.get_test_result_link(self.run_id, test.testrail_case_id)
                    ids_failed_test.append(test.testrail_case_id)
                    case_title = '\n'
                    case_title += '-------\n'
                    case_title += "## %s) ID %s: [%s](%s) \n" % (
                    i + 1, test.testrail_case_id, test.name, test_rail_link)
                    error = "```%s```\n" % last_testrun.error[:255]
                    for job_id, f in last_testrun.jobs.items():
                        if last_testrun.first_commands:
                            job_url = self.get_sauce_job_url(job_id=job_id,
                                                             first_command=last_testrun.first_commands[job_id])
                        else:
                            job_url = self.get_sauce_job_url(job_id=job_id)
                        case_info = "Logs for device %d: [steps](%s), [failure screenshot](%s)" \
                                    % (f, job_url, self.get_sauce_final_screenshot_url(job_id))

                    description += case_title + error + case_info
            description_title += '## Failed tests: %s \n' % ','.join(map(str, ids_failed_test))
            final_description = description_title + description

        request_body = {'description': final_description}
        return self.post('update_run/%s' % self.run_id, request_body)

    def get_run_results(self):
        return self.get('get_results_for_run/%s' % self.run_id)['results']

    def is_run_successful(self):
        for test in self.get_run_results():
            if test['status_id'] != 1:
                return False
        else:
            return True

    def get_test_result_link(self, test_run_id, test_case_id):
        try:
            test_id = self.get('get_results_for_case/%s/%s' % (test_run_id, test_case_id))['results'][0]['test_id']
            return '%stests/view/%s' % (self.url, test_id)
        except KeyError:
            return None
