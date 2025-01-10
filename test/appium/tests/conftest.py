import json
import os
import re
import signal
import time
from contextlib import contextmanager
from dataclasses import dataclass
from datetime import datetime
from http.client import RemoteDisconnected
from os import environ

import pytest
import requests
from filelock import FileLock
from requests.exceptions import ConnectionError as c_er

import tests
from tests import test_suite_data

lambda_test_username = environ.get('LAMBDA_TEST_USERNAME')
lambda_test_access_key = environ.get('LAMBDA_TEST_ACCESS_KEY')

github_token = environ.get('GIT_HUB_TOKEN')


def pytest_addoption(parser):
    parser.addoption("--build",
                     action="store",
                     default=datetime.now().strftime('%Y-%m-%d-%H-%M'),
                     help="Specify build name")
    parser.addoption('--apk',
                     action='store',
                     default=None,
                     help='Url or local path to apk')
    parser.addoption('--env',
                     action='store',
                     default='lt',
                     help='Specify environment: local/lt/api')
    parser.addoption('--log_steps',
                     action='store',
                     default=False,
                     help='Display each test step in terminal as plain text: True/False')
    parser.addoption('--pr_number',
                     action='store',
                     default=None,
                     help='Pull Request number')
    parser.addoption('--testrail_report',
                     action='store',
                     default=False,
                     help='boolean; For creating testrail report per run')
    parser.addoption("--run_testrail_ids",
                     action="store",
                     metavar="NAME",
                     default=None,
                     help="only run tests matching the environment NAME.")
    parser.addoption("--apk_upgrade",
                     action="store",
                     metavar="NAME",
                     default=None,
                     help='Url or local path to apk for upgrade')


@dataclass
class Option:
    pass


option = Option()
testrail_report = None
github_report = None
run_name = None


def is_master(config):
    return not hasattr(config, 'workerinput')


@contextmanager
def _upload_time_limit(seconds):
    def signal_handler(signum, frame):
        raise TimeoutError("Apk upload took more than %s seconds" % seconds)

    signal.signal(signal.SIGALRM, signal_handler)
    signal.alarm(seconds)
    try:
        start_time = time.time()
        yield
        print("Apk upload took %s seconds" % round(time.time() - start_time))
    finally:
        signal.alarm(0)


class UploadApkException(Exception):
    pass


def _upload_and_check_response(apk_file_path):
    from support.lambda_test import upload_apk
    with _upload_time_limit(1000):
        return upload_apk(apk_file_path)


def _upload_and_check_response_with_retries(apk_file_path, retries=3):
    for _ in range(retries):
        try:
            return _upload_and_check_response(apk_file_path)
        except (ConnectionError, RemoteDisconnected, c_er):
            time.sleep(10)


def _download_apk(url):
    # Absolute path added to handle CI runs.
    apk_path = os.path.join(os.path.dirname(__file__), test_suite_data.apk_name)

    print('Downloading: %s' % url)
    try:
        resp = requests.get(url)
        resp.raise_for_status()
    except requests.RequestException as err:
        print(resp.text)
        raise err

    with open(apk_path, 'wb') as f:
        f.write(resp.content)

    return apk_path


def get_run_name(config, new_one=False):
    pr_number = config.getoption('pr_number')
    if config.getoption('testrail_report'):
        if pr_number:
            if new_one:
                run_number = len(testrail_report.get_runs(pr_number)) + 1
            else:
                run_number = len(testrail_report.get_runs(pr_number))
            return 'PR-%s run #%s (%s)' % (pr_number, run_number, test_suite_data.apk_name.split('-')[4])
        else:
            return test_suite_data.apk_name
    else:
        return config.getoption('build')


def pytest_configure(config):
    global option
    option = config.option
    from support.testrail_report import TestrailReport
    global testrail_report
    testrail_report = TestrailReport()
    from support.github_report import GithubHtmlReport
    global github_report
    github_report = GithubHtmlReport()
    tests.pytest_config_global = vars(config.option)
    config.addinivalue_line("markers", "testrail_id(name): empty")

    if config.getoption('log_steps'):
        import logging
        logging.basicConfig(level=logging.INFO)
    if config.getoption('env') == 'api':
        return

    test_suite_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
                                             if '.apk' in i]])[0]
    global run_name
    if is_master(config) and config.getoption('testrail_report'):
        run_name = get_run_name(config, new_one=True)
        testrail_report.add_run(run_name)
    else:
        run_name = get_run_name(config, new_one=False)

    if not is_master(config):
        return

    pr_number = config.getoption('pr_number')
    if pr_number:
        from github import Github
        repo = Github(github_token).get_user('status-im').get_repo('status-mobile')
        pull = repo.get_pull(int(pr_number))
        pull.get_commits()[0].create_status(
            state='pending',
            context='Mobile e2e tests',
            description='e2e tests are running'
        )


@pytest.fixture(scope='session', autouse=True)
def upload_apk(tmp_path_factory):
    fn = tmp_path_factory.getbasetemp().parent / "lt_apk.json"
    with FileLock(str(fn) + ".lock"):
        if fn.is_file():
            data = json.loads(fn.read_text())
            tests.pytest_config_global['lt_apk_url'] = data['lambda_test_apk_url']
        else:
            apk_src = tests.pytest_config_global['apk']
            if apk_src.startswith('http'):
                apk_path = _download_apk(apk_src)
            else:
                apk_path = apk_src
            tests.pytest_config_global['lt_apk_url'] = _upload_and_check_response(apk_path)
            fn.write_text(json.dumps({'lambda_test_apk_url': tests.pytest_config_global['lt_apk_url']}))
            if apk_src.startswith('http'):
                os.remove(apk_path)


def pytest_unconfigure(config):
    if is_master(config):
        if config.getoption('testrail_report'):
            testrail_report.add_results()
        if config.getoption('pr_number'):
            from github import Github
            repo = Github(github_token).get_user('status-im').get_repo('status-mobile')
            pull = repo.get_pull(int(config.getoption('pr_number')))
            comment = pull.create_issue_comment(github_report.build_html_report(testrail_report.run_id))
            if not testrail_report.is_run_successful():
                pull.get_commits()[0].create_status(state='failure', context='Mobile e2e tests',
                                                    description='Failure - e2e tests are failed',
                                                    target_url=comment.html_url)
            else:
                pull.get_commits()[0].create_status(state='success', context='Mobile e2e tests',
                                                    description='Success - e2e tests are passed',
                                                    target_url=comment.html_url)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()

    case_ids_set = item.config.getoption("run_testrail_ids")

    def catch_error():
        error = report.longreprtext
        failure_pattern = 'E.*Message:|E.*Error:|E.*Failed:'
        exception = re.findall(failure_pattern, error)
        if exception:
            error = error.replace(re.findall(failure_pattern, report.longreprtext)[0], '')
        return error

    secured_test = "secured" in item.keywords._markers or "secured" in item.parent.keywords._markers

    if report.when == 'setup':
        is_group = "xdist_group" in item.keywords._markers or "xdist_group" in item.parent.keywords._markers
        error_intro, error = 'Test setup failed:', ''
        final_error = '%s %s' % (error_intro, error)
        if (hasattr(report, 'wasxfail') and not case_ids_set) or (hasattr(report, 'wasxfail') and (
                str([mark.args[0] for mark in item.iter_markers(name='testrail_id')][0]) in str(case_ids_set))):
            if '[NOTRUN]' in report.wasxfail:
                test_suite_data.set_current_test(test_name=item.name, testrail_case_id=get_testrail_case_id(item),
                                                 secured=secured_test)
                test_suite_data.current_test.create_new_testrun()
                if is_group:
                    test_suite_data.current_test.group_name = item.instance.__class__.__name__
                test_suite_data.current_test.testruns[-1].xfail = report.wasxfail
                test_suite_data.current_test.testruns[-1].run = False
                error_intro, error = 'Test is not run, e2e blocker ', report.wasxfail
                final_error = "%s [[%s]]" % (error_intro, error)
            else:
                if is_group:
                    test_suite_data.current_test.group_name = item.instance.__class__.__name__
                error = catch_error()
                final_error = '%s %s [[%s]]' % (error_intro, error, report.wasxfail)
        else:
            if is_group and report.failed:
                test_suite_data.current_test.group_name = item.instance.__class__.__name__
                error = catch_error()
                final_error = '%s %s' % (error_intro, error)
        if error:
            test_suite_data.current_test.testruns[-1].error = final_error
            github_report.save_test(test_suite_data.current_test)

    if report.when == 'call':
        current_test = test_suite_data.current_test
        error = catch_error()
        if report.failed:
            current_test.testruns[-1].error = error
        if (hasattr(report, 'wasxfail') and not case_ids_set) or (hasattr(report, 'wasxfail') and (
                str([mark.args[0] for mark in item.iter_markers(name='testrail_id')][0]) in str(case_ids_set))):
            current_test.testruns[-1].xfail = report.wasxfail
            if '[NOTRUN]' in report.wasxfail:
                current_test.testruns[-1].run = False
            if error:
                current_test.testruns[-1].error = '%s [[%s]]' % (error, report.wasxfail)


def get_testrail_case_id(item):
    testrail_id = item.get_closest_marker('testrail_id')
    if testrail_id:
        return testrail_id.args[0]


def pytest_runtest_setup(item):
    try:
        testrail_id = [mark.args[0] for mark in item.iter_markers(name='testrail_id')][0]
    except IndexError:
        pass
    run_testrail_ids = item.config.getoption("run_testrail_ids")
    if run_testrail_ids:
        if str(testrail_id) not in list(run_testrail_ids.split(",")):
            pytest.skip("test requires testrail case id %s" % testrail_id)
    secured = bool([mark for mark in item.iter_markers(name='secured')])
    test_suite_data.set_current_test(test_name=item.name, testrail_case_id=get_testrail_case_id(item), secured=secured)
    test_suite_data.current_test.create_new_testrun()
