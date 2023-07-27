import base64
import itertools
import json
import logging
import re
from json import JSONDecodeError
from os import environ
from sys import argv

import emoji
import requests

from support.base_test_report import BaseTestReport


class TestrailReport(BaseTestReport):

    def __init__(self):
        super(TestrailReport, self).__init__()

        self.password = environ.get('TESTRAIL_PASS')
        self.user = environ.get('TESTRAIL_USER')

        self.run_id = None
        # self.suite_id = 48
        self.suite_id = 5274
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
        print("Testrun: %sruns/view/%s" % (self.url, self.run_id))

    def get_cases(self, section_ids):
        test_cases = list()
        for section_id in section_ids:
            test_cases.append(
                self.get('get_cases/%s&suite_id=%s&section_id=%s' % (self.project_id, self.suite_id, section_id))[
                    'cases'])
        return itertools.chain.from_iterable(test_cases)

    def get_regression_cases(self):
        test_cases = dict()
        test_cases['pr'] = dict()
        test_cases['nightly'] = dict()
        test_cases['upgrade'] = dict()
        ## PR e2e old UI
        # test_cases['pr']['critical'] = 730
        # test_cases['pr']['contacts'] = 50831
        # test_cases['pr']['public_chat'] = 50654
        # test_cases['pr']['one_to_one_chat'] = 50655
        # test_cases['pr']['group_chat'] = 50656
        # test_cases['pr']['onboarding'] = 50659
        # test_cases['pr']['recovery'] = 50660
        # test_cases['pr']['wallet'] = 50661
        # test_cases['pr']['send_tx'] = 50662
        # test_cases['pr']['keycard_tx'] = 50663
        # test_cases['pr']['1_1_chat_commands'] = 50825
        # test_cases['pr']['ens'] = 50827
        # test_cases['pr']['sync'] = 50834
        # test_cases['pr']['browser'] = 50812

        test_cases['pr']['critical'] = 50955
        test_cases['pr']['one_to_one_chat'] = 50956
       # test_cases['pr']['deep_links'] = 50967
        test_cases['pr']['group_chat'] = 50964
        test_cases['pr']['community_single'] = 50983
        test_cases['pr']['community_multiple'] = 50982
        test_cases['pr']['activity_centre_contact_request'] = 50984
        test_cases['pr']['activity_centre_other'] = 51005

        ## Nightly e2e
        # test_cases['nightly']['medium'] = 736
        # test_cases['nightly']['chat'] = 50811
        # test_cases['nightly']['browser'] = 50826
        # test_cases['nightly']['profile'] = 50828
        # test_cases['nightly']['deep_link'] = 50836
        # test_cases['nightly']['share_profile'] = 50837
        # test_cases['nightly']['chat_2'] = 50838
        # test_cases['nightly']['group_chat'] = 50839
        # test_cases['nightly']['pairing'] = 50840
        # test_cases['nightly']['activity_center'] = 50833
        # test_cases['nightly']['timeline'] = 50842
        # test_cases['nightly']['community'] = 50841
        # test_cases['nightly']['permissions'] = 50843
        # test_cases['nightly']['scan qr'] = 50844
        # test_cases['nightly']['mentions'] = 50845
        # test_cases['nightly']['mutual_contact_requests'] = 50857
        # test_cases['nightly']['keycard'] = 50850
        # test_cases['nightly']['wallet'] = 50851

        ## Upgrade e2e
        # test_cases['upgrade']['general'] = 881

        case_ids = list()
        for arg in argv:
            if "run_testrail_ids" in arg:
                key, value = arg.split('=')
                case_ids = value.split(',')
        if len(case_ids) == 0:
            # if 'critical' in argv:
            if 'new_ui_critical' in argv:
                for category in test_cases['pr']:
                    for case in self.get_cases([test_cases['pr'][category]]):
                        case_ids.append(case['id'])
            elif 'upgrade' in argv and 'not upgrade' not in argv:
                for case in self.get_cases([test_cases['upgrade']['general']]):
                    case_ids.append(case['id'])
            else:
                for phase in test_cases:
                    if phase != 'upgrade':
                        for category in test_cases[phase]:
                            for case in self.get_cases([test_cases[phase][category]]):
                                case_ids.append(case['id'])
        return case_ids

    def add_results(self):
        data = list()
        all_tests = self.get_all_tests()
        for test in all_tests:
            test_steps = "# Steps: \n"
            devices = str()
            last_testrun = test.testruns[-1]
            for step in last_testrun.steps:
                test_steps += step + "\n"
            for i, device in enumerate(last_testrun.jobs):
                if last_testrun.first_commands:
                    devices += "# [Device %d](%s) \n" % (
                        i + 1, self.get_sauce_job_url(job_id=device, first_command=last_testrun.first_commands[device]))
                else:
                    devices += "# [Device %d](%s) \n" % (i + 1, self.get_sauce_job_url(job_id=device))
            comment = str()
            if test.group_name:
                comment += "# Class: %s \n" % test.group_name
            if last_testrun.error:
                full_error = last_testrun.error
                (code_error, no_code_error_str, issue_id) = self.separate_xfail_error(full_error)
                if issue_id:
                    test_rail_xfail = self.make_error_with_gh_issue_link(no_code_error_str, issue_id)
                    error = "%s %s" % (code_error, test_rail_xfail)
                else:
                    error = full_error
                error = error.replace("[[", "**").replace("]]", "**")
                comment += '%s' % ('# Error: \n %s \n' % emoji.demojize(error)) + devices + test_steps
            else:
                comment += devices + test_steps
            data.append(
                {'case_id': test.testrail_case_id,
                 'status_id': self.outcomes['undefined_fail'] if last_testrun.error else self.outcomes['passed'],
                 'comment': comment})

        results = self.post('add_results_for_cases/%s' % self.run_id, data={"results": data})
        try:
            results[0]
        except (IndexError, KeyError):
            print("Got TestRail error when adding results: \n%s" % results)

        for test in all_tests:
            last_testrun = test.testruns[-1]
            if last_testrun.error:
                try:
                    device = list(last_testrun.jobs.keys())[0]
                except IndexError:
                    continue
                for res in results:
                    if last_testrun.first_commands:
                        pattern = r"%s\?auth=.*#%s" % (device, str(last_testrun.first_commands[device]))
                    else:
                        pattern = device
                    if re.findall(pattern, res['comment']):
                        res_id = res['id']
                        try:
                            for geth in test.geth_paths.keys():
                                self.add_attachment(method='add_attachment_to_result/%s' % str(res_id),
                                                    path=test.geth_paths[geth])
                        except (AttributeError, FileNotFoundError):
                            pass
                        break

        self.change_test_run_description()

    def change_test_run_description(self):
        tests = self.get_all_tests()
        passed_tests = self.get_passed_tests()
        failed_tests = self.get_failed_tests()
        not_executed_tests = self.get_not_executed_tests(self.run_id)
        final_description = "Nothing to report this time..."
        if len(tests) > 0:
            description_title = "# %.0f%% of end-end tests have passed\n" % (len(passed_tests) / len(tests) * 100)
            description_title += "\n"
            description_title += "Total executed tests: %d\n" % len(tests)
            description_title += "Failed tests: %d\n" % len(failed_tests)
            description_title += "Passed tests: %d\n" % len(passed_tests)
            if not_executed_tests:
                description_title += "Not executed tests: %d\n" % len(not_executed_tests)
            description_title += "\n"
            ids_failed_test = []
            single_devices_block, group_blocks, case_info = str(), dict(), str()
            if failed_tests:
                for test in failed_tests:
                    if test.group_name:
                        group_blocks[test.group_name] = "\n-------\n## Class: %s:\n" % test.group_name
                for test in failed_tests:
                    last_testrun = test.testruns[-1]
                    test_rail_link = self.get_test_result_link(self.run_id, test.testrail_case_id)
                    ids_failed_test.append(test.testrail_case_id)
                    case_title = '\n'
                    case_title += '-------\n'
                    case_title += "### ID %s: [%s](%s) \n" % (test.testrail_case_id, test.name, test_rail_link)
                    full_error = last_testrun.error[-255:]
                    (code_error, no_code_error_str, issue_id) = self.separate_xfail_error(full_error)
                    if issue_id:
                        test_rail_xfail = self.make_error_with_gh_issue_link(no_code_error_str, issue_id)
                        error = "```%s```\n **%s**  \n" % (code_error, test_rail_xfail)
                    else:
                        error = "```%s```\n **%s**  \n" % (code_error, no_code_error_str)
                    for job_id, f in last_testrun.jobs.items():
                        if last_testrun.first_commands:
                            job_url = self.get_sauce_job_url(job_id=job_id,
                                                             first_command=last_testrun.first_commands[job_id])
                        else:
                            job_url = self.get_sauce_job_url(job_id=job_id)
                        case_info = "Logs for device %d: [steps](%s), [failure screenshot](%s)" \
                                    % (f, job_url, self.get_sauce_final_screenshot_url(job_id))

                    if test.group_name:
                        group_blocks[test.group_name] += case_title + error + case_info
                    else:
                        single_devices_block += case_title + error + case_info
                description_title += '## Failed tests: %s \n' % ','.join(map(str, ids_failed_test))
            if not_executed_tests:
                description_title += "## Not executed tests: %s\n" % ','.join([str(i) for i in not_executed_tests])
            final_description = description_title + single_devices_block + ''.join([i for i in group_blocks.values()])

        request_body = {'description': final_description}
        return self.post('update_run/%s' % self.run_id, request_body)

    def get_run_results(self, test_run_id=None):
        return self.get('get_results_for_run/%s' % (test_run_id if test_run_id else self.run_id))['results']

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
        except (KeyError, JSONDecodeError):
            print('Cannot extract result for %s e2e' % test_case_id)
            return None

    def get_not_executed_tests(self, test_run_id):
        try:
            results = self.get("get_tests/%s&status_id=3" % test_run_id)
            return [result['case_id'] for result in results["tests"]]
        except KeyError:
            print('Cannot extract result for %s' % test_run_id)
            pass

    @staticmethod
    def make_error_with_gh_issue_link(error, issue_id):
        return error.replace(issue_id,
                             '[%s](https://github.com/status-im/status-mobile/issues/%s)' % (issue_id, issue_id[1:]))
