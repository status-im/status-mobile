import asyncio
import base64
import logging
import re
import sys
from abc import ABCMeta, abstractmethod
from http.client import RemoteDisconnected

import pytest
import requests
from appium import webdriver
from appium.options.common import AppiumOptions
from appium.webdriver.common.mobileby import MobileBy
from appium.webdriver.connectiontype import ConnectionType
from selenium.common.exceptions import NoSuchElementException, WebDriverException
from urllib3.exceptions import MaxRetryError, ProtocolError

from support.api.network_api import NetworkApi
from tests import test_suite_data, start_threads, pytest_config_global, transl
from tests.conftest import github_report, run_name, lambda_test_username, lambda_test_access_key

executor_lambda_test = 'https://%s:%s@mobile-hub.lambdatest.com/wd/hub' % (lambda_test_username, lambda_test_access_key)

executor_local = 'http://localhost:4723/wd/hub'

implicit_wait = 5


def get_lambda_test_capabilities_real_device():
    capabilities = {
        "lt:options": {
            "w3c": True,
            "platformName": "android",
            "deviceName": "Pixel 8",
            "platformVersion": "14",
            "app": pytest_config_global['lt_apk_url'],
            "devicelog": True,
            "visual": True,
            "video": True,
            "build": run_name,
            "name": test_suite_data.current_test.group_name,
            "idleTimeout": 1000,
            "isRealMobile": True
        }
    }
    options = AppiumOptions()
    options.load_capabilities(capabilities)
    return options


def get_lambda_test_capabilities_emulator():
    capabilities = {
        "lt:options": {
            "w3c": True,
            "platformName": "android",
            "deviceName": "Pixel 6",
            "appiumVersion": "2.1.3",
            "platformVersion": "14",
            "app": pytest_config_global['lt_apk_url'],
            "devicelog": True,
            "visual": True,
            "video": True,
            "build": run_name,
            "name": test_suite_data.current_test.group_name,
            "idleTimeout": 1000,
            # "enableImageInjection": True,
            # "uploadMedia": ["lt://MEDIA2b3e34e2b0ee4928b9fc38c603f98191",], # "lt://MEDIAcfc1b4f1af0740759254404186bbe4f1"]
        },
        "appium:options": {
            "automationName": "UiAutomator2",
            "hideKeyboard": True
        }
    }
    options = AppiumOptions()
    options.load_capabilities(capabilities)
    return options


def get_app_path():
    app_folder = 'im.status.ethereum'
    apk = pytest_config_global['apk']
    if re.findall(r'pr\d\d\d\d\d', apk) or re.findall(r'\d\d\d\d\d.apk', apk):
        app_folder += '.pr'
    app_path = '/storage/emulated/0/Android/data/%s/files/Download/' % app_folder
    return app_path


def pull_geth(driver):
    result = driver.pull_file(get_app_path() + 'geth.log')
    return base64.b64decode(result)


def pull_requests_log(driver):
    result = driver.pull_file(get_app_path() + 'api.log')
    return base64.b64decode(result)


class AbstractTestCase:
    __metaclass__ = ABCMeta

    def print_lt_session_info(self, driver):
        sys.stdout = sys.stderr
        print("LambdaTestSessionID=%s job-name=%s" % (driver.session_id, run_name))

    def get_translation_by_key(self, key):
        return transl[key]

    @abstractmethod
    def setup_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')

    @abstractmethod
    def teardown_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')

    @property
    def environment(self):
        return pytest_config_global['env']

    network_api = NetworkApi()

    @staticmethod
    def get_alert_text(driver):
        try:
            return driver.find_element(MobileBy.ID, 'android:id/message').text
        except NoSuchElementException:
            return None

    def add_alert_text_to_report(self, driver):
        try:
            alert_text = self.get_alert_text(driver)
            if alert_text:
                test_suite_data.current_test.testruns[-1].error = "%s; also Unexpected Alert is shown: '%s'" % (
                    test_suite_data.current_test.testruns[-1].error, alert_text
                )
        except (RemoteDisconnected, ProtocolError):
            test_suite_data.current_test.testruns[-1].error = "%s; \n RemoteDisconnected" % \
                                                              test_suite_data.current_test.testruns[-1].error


class Driver(webdriver.Remote):

    @property
    def number(self):
        return test_suite_data.current_test.testruns[-1].jobs[self.session_id]

    def info(self, text: str, device=True):
        if device:
            text = 'Device %s: %s ' % (self.number, text)
        logging.info(text)
        test_suite_data.current_test.testruns[-1].steps.append(text)

    def fail(self, text: str):
        pytest.fail('Device %s: %s' % (self.number, text))

    def update_lt_session_status(self, index, status):
        data = {
            "action": "setTestStatus",
            "arguments": {
                "status": status,
                "remark": "Device %s" % index
            }
        }
        self.execute_script("lambda-hook: %s" % str(data).replace("'", "\""))


class Errors(object):
    def __init__(self):
        self.errors = list()

    def append(self, text=str()):
        self.errors.append(text)

    def verify_no_errors(self):
        if self.errors:
            pytest.fail('\n '.join([self.errors.pop(0) for _ in range(len(self.errors))]))


def create_shared_drivers(quantity):
    drivers = dict()
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    print('LT Executor: %s' % executor_lambda_test)
    try:
        drivers = loop.run_until_complete(start_threads(test_suite_data.current_test.name,
                                                        quantity,
                                                        Driver,
                                                        drivers,
                                                        command_executor=executor_lambda_test,
                                                        options=get_lambda_test_capabilities_emulator()))
        if len(drivers) < quantity:
            test_suite_data.current_test.testruns[-1].error = "Not all %s drivers are created" % quantity

        for i in range(quantity):
            test_suite_data.current_test.testruns[-1].jobs[drivers[i].session_id] = i + 1
            drivers[i].implicitly_wait(implicit_wait)
            drivers[i].update_settings({"enforceXPath1": True})
            drivers[i].set_network_connection(ConnectionType.WIFI_ONLY)
        return drivers, loop
    except (MaxRetryError, AttributeError) as e:
        test_suite_data.current_test.testruns[-1].error = str(e)
        for i, driver in drivers.items():
            try:
                driver.update_lt_session_status(i + 1, "failed")
                driver.quit()
            except (WebDriverException, AttributeError):
                pass
        raise e


class MultipleSharedDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        if not self.drivers:
            pytest.fail(test_suite_data.current_test.testruns[-1].error)
        for _, driver in self.drivers.items():
            driver.execute_script("lambda-testCase-start=%s" % method.__name__)
            driver.log_event("appium", "Started %s" % method.__name__)
        jobs = test_suite_data.current_test.testruns[-1].jobs
        if not jobs:
            for index, driver in self.drivers.items():
                jobs[driver.session_id] = index + 1
        self.errors = Errors()
        test_suite_data.current_test.group_name = self.__class__.__name__

    def teardown_method(self, method):
        log_names, log_contents = [], []
        for driver in self.drivers:
            try:
                self.print_lt_session_info(self.drivers[driver])
                self.add_alert_text_to_report(self.drivers[driver])
                log_names.append(
                    '%s_geth%s.log' % (test_suite_data.current_test.name, str(self.drivers[driver].number)))
                log_contents.append(pull_geth(self.drivers[driver]))
                log_names.append(
                    '%s_requests%s.log' % (test_suite_data.current_test.name, str(self.drivers[driver].number)))
                log_contents.append(pull_requests_log(self.drivers[driver]))
            except (WebDriverException, AttributeError, RemoteDisconnected, ProtocolError):
                pass
            finally:
                try:
                    logs = {log_names[i]: log_contents[i] for i in range(len(log_names))}
                    test_suite_data.current_test.logs_paths = github_report.save_logs(logs)
                except IndexError:
                    pass

    @pytest.fixture(scope='class', autouse=True)
    def prepare(self, request):
        test_suite_data.current_test.group_name = request.cls.__name__
        try:
            request.cls.prepare_devices(request)
        finally:
            for item, value in request.__dict__.items():
                setattr(request.cls, item, value)

    @classmethod
    def teardown_class(cls):
        requests_session = requests.Session()
        requests_session.auth = (lambda_test_username, lambda_test_access_key)
        if test_suite_data.tests[0].testruns[-1].error and 'setup failed' in test_suite_data.tests[0].testruns[
            -1].error:
            group_setup_failed = True
        else:
            group_setup_failed = False
        log_contents, log_names = list(), list()
        try:
            for i, driver in cls.drivers.items():
                if group_setup_failed:
                    log_contents.append(pull_geth(driver=driver))
                    log_names.append('%s_geth%s.log' % (cls.__name__, i))
                    log_contents.append(pull_requests_log(driver=driver))
                    log_names.append('%s_requests%s.log' % (cls.__name__, i))
                    lt_session_status = "failed"
                else:
                    lt_session_status = "passed"
                try:
                    driver.update_lt_session_status(i + 1, lt_session_status)
                    driver.quit()
                except (WebDriverException, RemoteDisconnected):
                    pass
                # url = 'https://api.%s/rest/v1/%s/jobs/%s/assets/%s' % (apibase, sauce_username, session_id, "log.json")
                # try:
                #     WebDriverWait(driver, 60, 2).until(lambda _: requests_session.get(url).status_code == 200)
                #     commands = requests_session.get(url).json()
                #     for command in commands:
                #         try:
                #             if command['message'].startswith("Started "):
                #                 for test in test_suite_data.tests:
                #                     if command['message'] == "Started %s" % test.name:
                #                         test.testruns[-1].first_commands[session_id] = commands.index(command) + 1
                #         except KeyError:
                #             continue
                # except (RemoteDisconnected, requests.exceptions.ConnectionError, TimeoutException):
                #     pass
        except AttributeError:
            pass
        finally:
            try:
                cls.loop.close()
            except AttributeError:
                pass

        logs = dict(zip(log_names, log_contents))
        logs_paths = github_report.save_logs(logs)

        for test in test_suite_data.tests:
            if group_setup_failed:
                test.logs_paths = logs_paths
            github_report.save_test(test)
