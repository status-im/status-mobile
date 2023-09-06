import os
import re
import signal
import urllib.request
from contextlib import contextmanager
from dataclasses import dataclass
from datetime import datetime
from http.client import RemoteDisconnected
from os import environ
from time import sleep

import pytest
from _pytest.runner import runtestprotocol
from requests.exceptions import ConnectionError as c_er

import tests
from support.device_stats_db import DeviceStatsDB
from support.test_rerun import should_rerun_test
from tests import test_suite_data, appium_container

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')
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
                     default='sauce',
                     help='Specify environment: local/sauce/api')
    parser.addoption('--datacenter',
                     action='store',
                     default='eu-central-1',
                     help='For sauce only: us-west-1, eu-central-1')
    parser.addoption('--platform_version',
                     action='store',
                     default='8.0',
                     help='Android device platform version')
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
    parser.addoption("--apk_upgrade",
                     action="store",
                     metavar="NAME",
                     default=None,
                     help='Url or local path to apk for upgrade')

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
    parser.addoption('--chat_name',
                     action='store',
                     default='test_chat',
                     help='Public chat name')
    parser.addoption('--device_number',
                     action='store',
                     default=2,
                     help='Public chat name')

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


@dataclass
class Option:
    datacenter: str = None


option = Option()
testrail_report = None
github_report = None
apibase = None
sauce = None


def is_master(config):
    return not hasattr(config, 'workerinput')


def is_uploaded():
    stored_files = sauce.storage.files()
    for i in range(len(stored_files)):
        if stored_files[i].name == test_suite_data.apk_name:
            return True


def pytest_configure(config):
    global option
    option = config.option
    from support.testrail_report import TestrailReport
    global testrail_report
    testrail_report = TestrailReport()
    from support.github_report import GithubHtmlReport
    global github_report
    from saucelab_api_client.saucelab_api_client import SauceLab
    github_report = GithubHtmlReport()
    tests.pytest_config_global = vars(config.option)
    config.addinivalue_line("markers", "testrail_id(name): empty")
    global apibase
    if config.getoption('datacenter') == 'us-west-1':
        apibase = 'us-west-1.saucelabs.com'
    elif config.getoption('datacenter') == 'eu-central-1':
        apibase = 'eu-central-1.saucelabs.com'
    else:
        raise NotImplementedError("Unknown SauceLabs datacenter")
    global sauce
    sauce = SauceLab('https://api.' + apibase + '/', sauce_username, sauce_access_key)
    if config.getoption('log_steps'):
        import logging
        logging.basicConfig(level=logging.INFO)
    if config.getoption('env') != 'api':
        test_suite_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
                                                 if '.apk' in i]])[0]
        if is_master(config):
            pr_number = config.getoption('pr_number')
            if config.getoption('testrail_report'):
                if pr_number:
                    run_number = len(testrail_report.get_runs(pr_number)) + 1
                    run_name = 'PR-%s run #%s' % (pr_number, run_number)
                else:
                    run_name = test_suite_data.apk_name
                testrail_report.add_run(run_name)
            if pr_number:
                from github import Github
                repo = Github(github_token).get_user('status-im').get_repo('status-mobile')
                pull = repo.get_pull(int(pr_number))
                pull.get_commits()[0].create_status(state='pending', context='Mobile e2e tests',
                                                    description='e2e tests are running')
            if config.getoption('env') == 'sauce':
                if not is_uploaded():
                    def _upload_and_check_response(apk_file_path):
                        @contextmanager
                        def _upload_time_limit(seconds):
                            def signal_handler(signum, frame):
                                raise TimeoutError("Apk upload took more than %s seconds" % seconds)

                            signal.signal(signal.SIGALRM, signal_handler)
                            signal.alarm(seconds)
                            try:
                                yield
                            finally:
                                signal.alarm(0)

                        with _upload_time_limit(600):
                            class UploadApkException(Exception):
                                pass

                            with open(apk_file_path, 'rb') as f:
                                resp = sauce.storage._session.request('post', '/v1/storage/upload',
                                                                      files={'payload': f})
                                try:
                                    if resp['item']['name'] != test_suite_data.apk_name:
                                        raise UploadApkException(
                                            "Incorrect apk was uploaded to Sauce storage, response:\n%s" % resp)
                                except KeyError:
                                    raise UploadApkException(
                                        "Error when uploading apk to Sauce storage, response:\n%s" % resp)

                    if 'http' in config.getoption('apk'):
                        # it works with just a file_name, but I've added full path because not sure how it'll behave on the remote run (Jenkins)
                        file_path, to_remove = os.path.join(os.path.dirname(__file__), test_suite_data.apk_name), True
                        urllib.request.urlretrieve(config.getoption('apk'),
                                                   filename=file_path)  # if url is not valid it raises an error
                    else:
                        file_path, to_remove = config.getoption('apk'), False

                    for _ in range(3):
                        try:
                            _upload_and_check_response(apk_file_path=file_path)
                            break
                        except (ConnectionError, RemoteDisconnected):
                            sleep(10)
                    if to_remove:
                        os.remove(file_path)


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


def should_save_device_stats(config):
    db_args = [config.getoption(option) for option in
               ('stats_db_host', 'stats_db_port', 'stats_db_username', 'stats_db_password', 'stats_db_database')]
    return all(db_args)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()

    is_sauce_env = item.config.getoption('env') == 'sauce'
    case_ids_set = item.config.getoption("run_testrail_ids")

    def catch_error():
        error = report.longreprtext
        failure_pattern = 'E.*Message:|E.*Error:|E.*Failed:'
        exception = re.findall(failure_pattern, error)
        if exception:
            error = error.replace(re.findall(failure_pattern, report.longreprtext)[0], '')
        return error

    if report.when == 'setup':
        is_group = "xdist_group" in item.keywords._markers or "xdist_group" in item.parent.keywords._markers
        error_intro, error = 'Test setup failed:', ''
        final_error = '%s %s' % (error_intro, error)
        if (hasattr(report, 'wasxfail') and not case_ids_set) or (hasattr(report, 'wasxfail') and (
                str([mark.args[0] for mark in item.iter_markers(name='testrail_id')][0]) in str(case_ids_set))):
            if '[NOTRUN]' in report.wasxfail:
                test_suite_data.set_current_test(item.name, testrail_case_id=get_testrail_case_id(item))
                test_suite_data.current_test.create_new_testrun()
                if is_group:
                    test_suite_data.current_test.group_name = item.instance.__class__.__name__
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
                if is_sauce_env:
                    update_sauce_jobs(test_suite_data.current_test.group_name,
                                      test_suite_data.current_test.testruns[-1].jobs,
                                      report.passed)
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
            if error:
                current_test.testruns[-1].error = '%s [[%s]]' % (error, report.wasxfail)
        if is_sauce_env:
            update_sauce_jobs(current_test.name, current_test.testruns[-1].jobs, report.passed)
        if item.config.getoption('docker'):
            device_stats = appium_container.get_device_stats()
            if item.config.getoption('bugreport'):
                appium_container.generate_bugreport(item.name)

            build_name = item.config.getoption('apk')
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
    from sauceclient import SauceException
    for job_id in job_ids.keys():
        try:
            sauce.jobs.update_job(username=sauce_username, job_id=job_id, name=test_name, passed=passed)
        except (RemoteDisconnected, SauceException, c_er):
            pass


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
    test_suite_data.set_current_test(item.name, testrail_case_id=get_testrail_case_id(item))
    test_suite_data.current_test.create_new_testrun()


def pytest_runtest_protocol(item, nextitem):
    rerun_count = int(item.config.getoption('rerun_count'))
    for i in range(rerun_count):
        reports = runtestprotocol(item, nextitem=nextitem)
        for report in reports:
            is_in_group = [i for i in item.iter_markers(name='xdist_group')]
            if report.failed and should_rerun_test(report.longreprtext) and not is_in_group:
                break  # rerun
        else:
            return True  # no need to rerun


# @pytest.fixture(scope="session", autouse=False)
# def faucet_for_senders():
#     network_api = NetworkApi()
#     for user in transaction_senders.values():
#         network_api.faucet(address=user['address'])


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
