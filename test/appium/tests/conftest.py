import requests
import pytest
import re
from _pytest.runner import runtestprotocol
from http.client import RemoteDisconnected
from support.device_stats_db import DeviceStatsDB
from support.test_rerun import should_rerun_test
from tests import test_suite_data, appium_container
from datetime import datetime
from os import environ
from io import BytesIO
from sauceclient import SauceClient
from support.api.network_api import NetworkApi
from support.github_report import GithubHtmlReport
from support.testrail_report import TestrailReport
from tests.users import transaction_senders

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')
github_token = environ.get('GIT_HUB_TOKEN')

sauce = SauceClient(sauce_username, sauce_access_key)
github_report = GithubHtmlReport()
testrail_report = TestrailReport()


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
    parser.addoption('--platform_version',
                     action='store',
                     default='8.0',
                     help='Android device platform version')
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
    parser.addoption('--rerun_count',
                     action='store',
                     default=0,
                     help='How many times tests should be re-run if failed')
    parser.addoption("--run_testrail_ids",
                     action="store",
                     metavar="NAME",
                     default=None,
                     help="only run tests matching the environment NAME.")

    # chat bot

    parser.addoption('--messages_number',
                     action='store',
                     default=20,
                     help='Messages number')
    parser.addoption('--public_keys',
                     action='store',
                     default='',
                     help='List of public keys for one-to-one chats')
    parser.addoption('--running_time',
                     action='store',
                     default=600,
                     help='Running time in seconds')

    # running tests using appium docker instance

    parser.addoption('--docker',
                     action='store',
                     default=False,
                     help='Are you using the appium docker container to run the tests?')
    parser.addoption('--docker_shared_volume',
                     action='store',
                     default=None,
                     help='Path to a directory with .apk that will be shared with docker instance. Test reports will be also saved there')
    parser.addoption('--device_ip',
                     action='store',
                     default=None,
                     help='Android device IP address used for battery tests')
    parser.addoption('--bugreport',
                     action='store',
                     default=False,
                     help='Should generate bugreport for each test?')
    parser.addoption('--stats_db_host',
                     action='store',
                     default=None,
                     help='Host address for device stats database')
    parser.addoption('--stats_db_port',
                     action='store',
                     default=8086,
                     help='Port for device stats db')
    parser.addoption('--stats_db_username',
                     action='store',
                     default=None,
                     help='Username for device stats db')
    parser.addoption('--stats_db_password',
                     action='store',
                     default=None,
                     help='Password for device stats db')
    parser.addoption('--stats_db_database',
                     action='store',
                     default='example9',
                     help='Database name for device stats db')


def is_master(config):
    return not hasattr(config, 'slaveinput')


def is_uploaded():
    stored_files = sauce.storage.get_stored_files()
    for i in range(len(stored_files['files'])):
        if stored_files['files'][i]['name'] == test_suite_data.apk_name:
            return True


def pytest_configure(config):
    config.addinivalue_line("markers", "testrail_id(name): empty")

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
        if config.getoption('testrail_report'):
            testrail_report.add_results()
        if config.getoption('pr_number'):
            from github import Github
            repo = Github(github_token).get_user('status-im').get_repo('status-react')
            pull = repo.get_pull(int(config.getoption('pr_number')))
            pull.create_issue_comment(github_report.build_html_report(testrail_report.run_id))


def should_save_device_stats(config):
    db_args = [config.getoption(option) for option in
               ('stats_db_host', 'stats_db_port', 'stats_db_username', 'stats_db_password', 'stats_db_database')]
    return all(db_args)


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    if report.when == 'call':
        is_sauce_env = item.config.getoption('env') == 'sauce'
        current_test = test_suite_data.current_test
        if report.failed:
            error = report.longreprtext
            exception = re.findall('E.*Message:|E.*Error:|E.*Failed:', error)
            if exception:
                error = error.replace(re.findall('E.*Message:|E.*Error:|E.*Failed:', report.longreprtext)[0], '')
            current_test.testruns[-1].error = error
        if is_sauce_env:
            update_sauce_jobs(current_test.name, current_test.testruns[-1].jobs, report.passed)
        if pytest.config.getoption('docker'):
            device_stats = appium_container.get_device_stats()
            if pytest.config.getoption('bugreport'):
                appium_container.generate_bugreport(item.name)

            build_name = pytest.config.getoption('apk')
            # Find type of tests that are run on the device
            if 'battery_consumption' in item.keywords._markers:
                test_group = 'battery_consumption'
            else:
                test_group = None

            if should_save_device_stats(item.config):
                device_stats_db = DeviceStatsDB(
                    item.config.getoption('stats_db_host'),
                    item.config.getoption('stats_db_port'),
                    item.config.getoption('stats_db_username'),
                    item.config.getoption('stats_db_password'),
                    item.config.getoption('stats_db_database'),
                )
                device_stats_db.save_stats(build_name, item.name, test_group, not report.failed, device_stats)


def update_sauce_jobs(test_name, job_ids, passed):
    for job_id in job_ids.keys():
        try:
            sauce.jobs.update_job(job_id, name=test_name, passed=passed)
        except RemoteDisconnected:
            pass


def get_testrail_case_id(item):
    testrail_id = item.get_closest_marker('testrail_id')
    if testrail_id:
        return testrail_id.args[0]


def pytest_runtest_setup(item):
    testrail_id = [mark.args[0] for mark in item.iter_markers(name='testrail_id')][0]
    run_testrail_ids = item.config.getoption("run_testrail_ids")
    if run_testrail_ids:
        if str(testrail_id) not in run_testrail_ids:
            pytest.skip("test requires testrail case id %s" % testrail_id)
    test_suite_data.set_current_test(item.name, testrail_case_id=get_testrail_case_id(item))
    test_suite_data.current_test.create_new_testrun()


def pytest_runtest_protocol(item, nextitem):
    rerun_count = int(item.config.getoption('rerun_count'))
    for i in range(rerun_count):
        reports = runtestprotocol(item, nextitem=nextitem)
        for report in reports:
            if report.failed and should_rerun_test(report.longreprtext):
                break  # rerun
        else:
            return True  # no need to rerun


@pytest.fixture(scope="session", autouse=False)
def faucet_for_senders():
    network_api = NetworkApi()
    for user in transaction_senders.values():
        network_api.faucet(address=user['address'])


@pytest.fixture
def messages_number(request):
    return int(request.config.getoption('messages_number'))


@pytest.fixture
def message_wait_time(request):
    return int(request.config.getoption('message_wait_time'))


@pytest.fixture
def participants_number(request):
    return int(request.config.getoption('participants_number'))


@pytest.fixture
def chat_name(request):
    return request.config.getoption('chat_name')


@pytest.fixture
def user_public_key(request):
    return request.config.getoption('user_public_key')
