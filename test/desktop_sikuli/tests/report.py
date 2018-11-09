import json
import os
import requests
import base64
from subprocess import check_output

TEST_REPORT_DIR_CONTAINER = '/var/log/takoe'


def get_test_report_file_path(test_name):
    file_name = "%s.json" % test_name
    if not os.path.exists(TEST_REPORT_DIR_CONTAINER):
        os.makedirs(TEST_REPORT_DIR_CONTAINER)
    return os.path.join(TEST_REPORT_DIR_CONTAINER, file_name)


def get_testrail_case_id(obj):
    if 'testrail_id' in obj.keywords._markers:
        return obj.keywords._markers['testrail_id'].args[0]


def save_test_result(test, report):
    test_name = test.name
    file_path = get_test_report_file_path(test_name)
    error = None
    if report.failed:
        check_output(['import', '-window', 'root', '%s/%s.png' % (TEST_REPORT_DIR_CONTAINER, test_name)])
        error = report.longrepr.reprcrash.message
    with open('%s/%s.log' % (TEST_REPORT_DIR_CONTAINER, test_name), 'r') as log:
        steps = [i for i in log]
    test_dict = {
        'testrail_case_id': get_testrail_case_id(test),
        'name': test_name,
        'steps': steps,
        'error': error,
        'screenshot': test_name + '.png'}
    json.dump(test_dict, open(file_path, 'w'))


class SingleTestData(object):
    def __init__(self, name, testruns, testrail_case_id):
        self.testrail_case_id = testrail_case_id
        self.name = name
        self.testruns = testruns

    class TestRunData(object):
        def __init__(self, steps, error):
            self.steps = steps
            self.error = error


class BaseReportDesktop:

    def __init__(self, test_report_dir_master):
        self.test_reports_dir = test_report_dir_master

    def init_report(self):
        # overridden in order to prevent deletion of all actual report files, do not remove!
        pass

    def get_all_tests(self):
        tests = list()
        file_list = [f for f in os.listdir(self.test_reports_dir)]
        for file_name in file_list:
            if file_name.endswith('json'):
                file_path = os.path.join(self.test_reports_dir, file_name)
                test_data = json.load(open(file_path))
                testruns = list()
                testruns.append(SingleTestData.TestRunData(
                    steps=test_data['steps'], error=test_data['error']))
                tests.append(SingleTestData(name=test_data['name'], testruns=testruns,
                                            testrail_case_id=test_data['testrail_case_id']))
        return tests


class TestrailReportDesktop(BaseReportDesktop):

    def __init__(self, test_report_dir_master):
        super(TestrailReportDesktop, self).__init__(test_report_dir_master)

        self.password = os.environ.get('TESTRAIL_PASS')
        self.user = os.environ.get('TESTRAIL_USER')

        self.outcomes = {
            'passed': 1,
            'undefined_fail': 10}

        self.headers = dict()
        self.headers['Authorization'] = 'Basic %s' % str(
            base64.b64encode(bytes('%s:%s' % (self.user, self.password), 'utf-8')), 'ascii').strip()
        self.headers['Content-Type'] = 'application/json'

        self.url = 'https://ethstatus.testrail.net/index.php?/'
        self.api_url = self.url + 'api/v2/'

        self.run_id = 1917
        self.suite_id = 52
        self.project_id = 16

    def add_run(self, run_name):
        request_body = {'suite_id': self.suite_id,
                        'name': run_name,
                        'case_ids': self.get_regression_cases(is_pr='PR-' in run_name),
                        'include_all': False}
        run = self.post('add_run/%s' % self.project_id, request_body)
        self.run_id = run['id']

    def get(self, method):
        return requests.get(self.api_url + method, headers=self.headers).json()

    def post(self, method, data):
        data = bytes(json.dumps(data), 'utf-8')
        return requests.post(self.api_url + method, data=data, headers=self.headers).json()

    def get_cases(self, section_id):
        return self.get('get_cases/%s&suite_id=%s&section_id=%s' % (self.project_id, self.suite_id, section_id))

    def get_regression_cases(self, is_pr=False):
        test_cases = dict()
        test_cases['critical'] = 822
        test_cases['high'] = 799
        test_cases['medium'] = 823
        test_cases['low'] = 801
        case_ids = list()
        if is_pr:
            case_ids = [case['id'] for case in self.get_cases(test_cases['critical'])]
        else:
            for phase in test_cases:
                for case in self.get_cases(test_cases[phase]):
                    case_ids.append(case['id'])
        return case_ids

    def get_screenshot_url(self, build_num, test_name):
        jenkins_url = 'https://jenkins.status.im'
        job_name = 'desktop-sandbox'
        artifacts = '/job/end-to-end-tests/job/%s/%s/artifact/test/desktop_sikuli/report/' % (job_name, build_num)
        return jenkins_url + artifacts + test_name + '.png'

    def add_results(self, build_num):
        for test in self.get_all_tests():
            test_steps = "\n# Steps: \n"
            method = 'add_result_for_case/%s/%s' % (self.run_id, test.testrail_case_id)
            last_testrun = test.testruns[-1]
            for step in last_testrun.steps:
                test_steps += step + "\n"
            data = {'status_id': self.outcomes['undefined_fail'] if last_testrun.error else self.outcomes['passed'],
                    'comment': '%s' % ('# Error: \n %s \n# Screenshot:\n "![](%s)"' % (
                        last_testrun.error + test_steps, self.get_screenshot_url(build_num, test.name))
                    if last_testrun.error else test_steps)}
            self.post(method, data=data)
