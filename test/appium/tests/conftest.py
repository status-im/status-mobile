from tests import test_suite_data, SingleTestData
import requests
import re
import pytest
from datetime import datetime
from os import environ
from io import BytesIO
from sauceclient import SauceClient
from support.github_test_report import GithubHtmlReport

storage = 'http://artifacts.status.im:8081/artifactory/nightlies-local/'

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')
github_token = environ.get('GIT_HUB_TOKEN')

sauce = SauceClient(sauce_username, sauce_access_key)
github_report = GithubHtmlReport(sauce_username, sauce_access_key)


def get_latest_apk():
    raw_data = requests.request('GET', storage).text
    dates = re.findall("\d{2}-[a-zA-Z]{3}-\d{4} \d{2}:\d{2}", raw_data)
    dates.sort(key=lambda date: datetime.strptime(date, "%d-%b-%Y %H:%M"), reverse=True)
    return re.findall('>(.*k)</a>\s*%s' % dates[0], raw_data)[0]


latest_nightly_apk = dict()
latest_nightly_apk['name'] = get_latest_apk()
latest_nightly_apk['url'] = storage + latest_nightly_apk['name']


def pytest_addoption(parser):
    parser.addoption("--build",
                     action="store",
                     default=datetime.now().strftime('%Y-%m-%d-%H-%M'),  # 'build_' + latest_nightly_apk['name'],
                     help="Specify build name")
    parser.addoption('--apk',
                     action='store',
                     default=None,
                     help='Url or local path to apk')
    parser.addoption('--env',
                     action='store',
                     default='sauce',
                     help='Specify environment: local/sauce')
    parser.addoption('--log',
                     action='store',
                     default=False,
                     help='Display each test step in terminal as plain text: True/False')
    parser.addoption('--pr_number',
                     action='store',
                     default=None,
                     help='Pull Request number')


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
    test_suite_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
                                             if '.apk' in i]])[0]
    if is_master(config) and config.getoption('env') == 'sauce':
        if config.getoption('pr_number'):
            with open('github_comment.txt', 'w') as _:
                pass
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
    if is_master(config) and config.getoption('pr_number'):
        from github import Github
        repo = Github(github_token).get_user('status-im').get_repo('status-react')
        pull = repo.get_pull(int(config.getoption('pr_number')))
        pull.create_issue_comment(github_report.build_html_report())


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    is_sauce_env = pytest.config.getoption('env') == 'sauce'
    current_test = test_suite_data.current_test
    if report.when == 'call':
        if report.failed:
            current_test.error = report.longreprtext
        if is_sauce_env:
            update_sauce_jobs(current_test.name, current_test.jobs, report.passed)
        github_report.save_test(current_test)


def update_sauce_jobs(test_name, job_ids, passed):
    for job_id in job_ids:
        sauce.jobs.update_job(job_id, name=test_name, passed=passed)


def pytest_runtest_setup(item):
    test_suite_data.add_test(SingleTestData(item.name))
