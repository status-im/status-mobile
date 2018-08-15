import time
import requests
import pytest
import re
from _pytest.runner import runtestprotocol
from support.test_rerun import should_rerun_test
from tests import test_suite_data
from datetime import datetime
from os import environ
from io import BytesIO
from sauceclient import SauceClient
from support.github_report import GithubHtmlReport
from support.testrail_report import TestrailReport

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')
github_token = environ.get('GIT_HUB_TOKEN')

sauce = SauceClient(sauce_username, sauce_access_key)
github_report = GithubHtmlReport(sauce_username, sauce_access_key)
testrail_report = TestrailReport(sauce_username, sauce_access_key)


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
                     default='sauce',
                     help='Specify environment: local/sauce/api')
    parser.addoption('--log',
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
    parser.addoption('--network',
                     action='store',
                     default='ropsten',
                     help='string; ropsten or rinkeby')

    # message reliability
    parser.addoption('--rerun_count',
                     action='store',
                     default=0,
                     help='How many times tests should be re-run if failed')
    parser.addoption('--messages_number',
                     action='store',
                     default=20,
                     help='Messages number')
    parser.addoption('--message_wait_time',
                     action='store',
                     default=20,
                     help='Max time to wait for a message to be received')
    parser.addoption('--participants_number',
                     action='store',
                     default=5,
                     help='Public chat participants number')
    parser.addoption('--chat_name',
                     action='store',
                     default=None,
                     help='Public chat name')
    parser.addoption('--user_public_key',
                     action='store',
                     default=None,
                     help='Public key of user for 1-1 chat')


def get_rerun_count():
    return int(pytest.config.getoption('rerun_count'))


def is_master(config):
    return not hasattr(config, 'slaveinput')


def is_uploaded():
    stored_files = sauce.storage.get_stored_files()
    for i in range(len(stored_files['files'])):
        if stored_files['files'][i]['name'] == test_suite_data.apk_name:
            return True


def pytest_configure(config):
    if config.getoption('log'):
        import logging
        logging.basicConfig(level=logging.INFO)
    if config.getoption('env') != 'api':
        test_suite_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
                                                 if '.apk' in i]])[0]
        if is_master(config):
            if config.getoption('testrail_report'):
                pr_number = config.getoption('pr_number')
                if pr_number:
                    run_number = len(testrail_report.get_runs(pr_number)) + 1
                    run_name = 'PR-%s run #%s' % (pr_number, run_number)
                else:
                    run_name = test_suite_data.apk_name
                testrail_report.add_run(run_name)
            if config.getoption('env') == 'sauce':
                if not is_uploaded():
                    if 'http' in config.getoption('apk'):
                        response = requests.get(config.getoption('apk'), stream=True)
                        response.raise_for_status()
                        file = BytesIO(response.content)
                        del response
                        requests.post('http://saucelabs.com/rest/v1/storage/'
                                      + sauce_username + '/' + test_suite_data.apk_name + '?overwrite=true',
                                      auth=(sauce_username, sauce_access_key),
                                      data=file,
                                      headers={'Content-Type': 'application/octet-stream'})
                    else:
                        sauce.storage.upload_file(config.getoption('apk'))


def pytest_unconfigure(config):
    if is_master(config):
        if config.getoption('pr_number'):
            from github import Github
            repo = Github(github_token).get_user('status-im').get_repo('status-react')
            pull = repo.get_pull(int(config.getoption('pr_number')))
            pull.create_issue_comment(github_report.build_html_report())
        if config.getoption('testrail_report'):
            testrail_report.add_results()


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    if report.when == 'call':
        is_sauce_env = pytest.config.getoption('env') == 'sauce'
        current_test = test_suite_data.current_test
        if report.failed:
            error = report.longreprtext
            exception = re.findall('E.*:', error)
            if exception:
                error = error.replace(re.findall('E.*Message:|E.*Error:|E.*Failed:', report.longreprtext)[0], '')
            current_test.testruns[-1].error = error
        if is_sauce_env:
            update_sauce_jobs(current_test.name, current_test.testruns[-1].jobs, report.passed)


def update_sauce_jobs(test_name, job_ids, passed):
    for job_id in job_ids.keys():
        sauce.jobs.update_job(job_id, name=test_name, passed=passed)


def get_testrail_case_id(obj):
    if 'testrail_id' in obj.keywords._markers:
        return obj.keywords._markers['testrail_id'].args[0]


def pytest_runtest_setup(item):
    test_suite_data.set_current_test(item.name, testrail_case_id=get_testrail_case_id(item))
    test_suite_data.current_test.create_new_testrun()


def pytest_runtest_protocol(item, nextitem):
    for i in range(get_rerun_count()):
        reports = runtestprotocol(item, nextitem=nextitem)
        for report in reports:
            if report.failed and should_rerun_test(report.longreprtext):
                break  # rerun
        else:
            return True  # no need to rerun


@pytest.fixture
def messages_number():
    return int(pytest.config.getoption('messages_number'))


@pytest.fixture
def message_wait_time():
    return int(pytest.config.getoption('message_wait_time'))


@pytest.fixture
def participants_number():
    return int(pytest.config.getoption('participants_number'))


@pytest.fixture
def chat_name():
    return pytest.config.getoption('chat_name')


@pytest.fixture
def user_public_key():
    return pytest.config.getoption('user_public_key')
