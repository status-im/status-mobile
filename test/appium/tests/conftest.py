from tests import test_data
import requests
import re
import pytest
from datetime import datetime
from os import environ
from io import BytesIO
from sauceclient import SauceClient
from hashlib import md5
import hmac

storage = 'http://artifacts.status.im:8081/artifactory/nightlies-local/'

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')
github_token = environ.get('GIT_HUB_TOKEN')

sauce = SauceClient(sauce_username, sauce_access_key)


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
                     default='build_' + latest_nightly_apk['name'],
                     help="Specify build name")
    parser.addoption('--apk',
                     action='store',
                     default=latest_nightly_apk['url'],
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
        if stored_files['files'][i]['name'] == test_data.apk_name:
            return True


def pytest_configure(config):
    if config.getoption('log'):
        import logging
        logging.basicConfig(level=logging.INFO)
    test_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
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
                              + sauce_username + '/' + test_data.apk_name + '?overwrite=true',
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
        with open('github_comment.txt', 'r') as comment:
            pull.create_issue_comment('# Automated test results: \n' + comment.read())


def get_public_url(job_id):
    token = hmac.new(bytes(sauce_username + ":" + sauce_access_key, 'latin-1'),
                     bytes(job_id, 'latin-1'), md5).hexdigest()
    return "https://saucelabs.com/jobs/%s?auth=%s" % (job_id, token)


def make_github_report(error=None):
    if pytest.config.getoption('pr_number'):
        title = '### %s' % test_data.test_name
        outcome = '%s' % ':x:' if error else ':white_check_mark:' + ':\n'
        title += outcome
        steps = '\n\n <details>\n<summary>Test Steps & Error message:</summary>\n\n ```%s ```%s\n\n</details>\n' % \
                (test_data.test_info[test_data.test_name]['steps'], '\n```' + error + '```' if error else '')
        sessions = str()

        for job_id in test_data.test_info[test_data.test_name]['jobs']:
            sessions += '  - [Android Device Session](%s) \n' % get_public_url(job_id)
        with open('github_comment.txt', 'a') as comment:
            comment.write(title + '\n' + steps + '\n' + sessions + '---\n')


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    if pytest.config.getoption('env') == 'sauce':
        if report.when == 'call':
            if report.passed:
                for job_id in test_data.test_info[test_data.test_name]['jobs']:
                    sauce.jobs.update_job(job_id, name=test_data.test_name, passed=True)
                make_github_report()
            if report.failed:
                for job_id in test_data.test_info[test_data.test_name]['jobs']:
                    sauce.jobs.update_job(job_id, name=test_data.test_name, passed=False)
                make_github_report(error=report.longreprtext)


def pytest_runtest_setup(item):
    test_data.test_name = item.name
