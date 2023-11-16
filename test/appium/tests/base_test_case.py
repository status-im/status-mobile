import asyncio
import base64
import logging
import re
import subprocess
import sys
from abc import ABCMeta, abstractmethod
from http.client import RemoteDisconnected

import pytest
import requests
from appium import webdriver
from appium.options.common import AppiumOptions
from appium.webdriver.common.mobileby import MobileBy
from sauceclient import SauceException
from selenium.common.exceptions import NoSuchElementException, TimeoutException, WebDriverException
from selenium.webdriver.support.wait import WebDriverWait
from urllib3.exceptions import MaxRetryError, ProtocolError

from support.api.network_api import NetworkApi
from tests import test_suite_data, start_threads, appium_container, pytest_config_global, transl
from tests.conftest import sauce_username, sauce_access_key, apibase, github_report

executor_sauce_lab = 'https://%s:%s@ondemand.%s:443/wd/hub' % (sauce_username, sauce_access_key, apibase)

executor_local = 'http://localhost:4723/wd/hub'

implicit_wait = 5


def get_capabilities_local():
    desired_caps = dict()
    if pytest_config_global['docker']:
        # apk is in shared volume directory
        apk = '/root/shared_volume/%s' % pytest_config_global['apk']
    else:
        apk = pytest_config_global['apk']
    desired_caps['app'] = apk
    desired_caps['deviceName'] = 'nexus_5'
    desired_caps['platformName'] = 'Android'
    desired_caps['appiumVersion'] = '1.9.1'
    desired_caps['platformVersion'] = '10.0'
    desired_caps['newCommandTimeout'] = 600
    desired_caps['fullReset'] = False
    desired_caps['unicodeKeyboard'] = True
    desired_caps['automationName'] = 'UiAutomator2'
    desired_caps['setWebContentDebuggingEnabled'] = True
    return desired_caps


def add_local_devices_to_capabilities():
    updated_capabilities = list()
    raw_out = re.split(r'[\r\\n]+', str(subprocess.check_output(['adb', 'devices'])).rstrip())
    for line in raw_out[1:]:
        serial = re.findall(r"(([\d.\d:]*\d+)|\bemulator-\d+)", line)
        if serial:
            capabilities = get_capabilities_local()
            capabilities['udid'] = serial[0][0]
            updated_capabilities.append(capabilities)
    return updated_capabilities


def get_capabilities_sauce_lab():
    caps = dict()
    caps['platformName'] = 'Android'
    caps['idleTimeout'] = 1000
    caps['appium:app'] = 'sauce-storage:' + test_suite_data.apk_name
    caps['appium:deviceName'] = 'Android GoogleAPI Emulator'
    caps['appium:deviceOrientation'] = 'portrait'
    caps['appium:platformVersion'] = '10.0'
    caps['appium:automationName'] = 'UiAutomator2'
    caps['appium:newCommandTimeout'] = 600
    caps['appium:idleTimeout'] = 1000
    caps['appium:unicodeKeyboard'] = True
    caps['appium:automationName'] = 'UiAutomator2'
    caps['appium:setWebContentDebuggingEnabled'] = True
    caps['appium:ignoreUnimportantViews'] = False
    caps['ignoreUnimportantViews'] = False
    caps['appium:enableNotificationListener'] = True
    caps['enableNotificationListener'] = True
    caps['appium:enforceXPath1'] = True
    caps['enforceXPath1'] = True
    caps['sauce:options'] = dict()
    caps['sauce:options']['appiumVersion'] = '2.0.0'
    caps['sauce:options']['username'] = sauce_username
    caps['sauce:options']['accessKey'] = sauce_access_key
    caps['sauce:options']['build'] = pytest_config_global['build']
    caps['sauce:options']['name'] = test_suite_data.current_test.name
    caps['sauce:options']['maxDuration'] = 3600
    caps['sauce:options']['idleTimeout'] = 1000

    options = AppiumOptions()
    options.load_capabilities(caps)

    return options


# def update_capabilities_sauce_lab(new_capabilities: dict):
#     caps = get_capabilities_sauce_lab().copy()
#     caps.update(new_capabilities)
#     return caps


def get_app_path():
    app_folder = 'im.status.ethereum'
    apk = pytest_config_global['apk']
    if re.findall(r'pr\d\d\d\d\d', apk) or re.findall(r'\d\d\d\d\d.apk', apk):
        app_folder += '.pr'
    app_path = '/storage/emulated/0/Android/data/%s/files/Download/' % app_folder
    return app_path


def get_geth_path():
    return get_app_path() + 'geth.log'


def pull_geth(driver):
    result = driver.pull_file(get_geth_path())
    return base64.b64decode(result)


class AbstractTestCase:
    __metaclass__ = ABCMeta

    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest_config_global['build']))

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


class Errors(object):
    def __init__(self):
        self.errors = list()

    def append(self, text=str()):
        self.errors.append(text)

    def verify_no_errors(self):
        if self.errors:
            pytest.fail('\n '.join([self.errors.pop(0) for _ in range(len(self.errors))]))


class SingleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method, **kwargs):
        if pytest_config_global['docker']:
            appium_container.start_appium_container(pytest_config_global['docker_shared_volume'])
            appium_container.connect_device(pytest_config_global['device_ip'])

        (executor, capabilities) = (executor_sauce_lab, get_capabilities_sauce_lab()) if \
            self.environment == 'sauce' else (executor_local, get_capabilities_local())
        for key, value in kwargs.items():
            capabilities[key] = value
        self.driver = Driver(executor, capabilities)
        test_suite_data.current_test.testruns[-1].jobs[self.driver.session_id] = 1
        self.driver.implicitly_wait(implicit_wait)
        self.errors = Errors()

        if pytest_config_global['docker']:
            appium_container.reset_battery_stats()

    def teardown_method(self, method):
        if self.environment == 'sauce':
            self.print_sauce_lab_info(self.driver)
        try:
            self.add_alert_text_to_report(self.driver)
            geth_content = pull_geth(self.driver)
            self.driver.quit()
            if pytest_config_global['docker']:
                appium_container.stop_container()
        except (WebDriverException, AttributeError):
            pass
        finally:
            github_report.save_test(test_suite_data.current_test,
                                    {'%s_geth.log' % test_suite_data.current_test.name: geth_content})


class LocalMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        self.drivers = dict()
        self.errors = Errors()

    def create_drivers(self, quantity):
        capabilities = self.add_local_devices_to_capabilities()
        for driver in range(quantity):
            self.drivers[driver] = Driver(self.executor_local, capabilities[driver])
            test_suite_data.current_test.testruns[-1].jobs[self.drivers[driver].session_id] = driver + 1
            self.drivers[driver].implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.drivers:
            try:
                self.add_alert_text_to_report(self.drivers[driver])
                self.drivers[driver].quit()
            except WebDriverException:
                pass


class SauceMultipleDeviceTestCase(AbstractTestCase):

    @classmethod
    def setup_class(cls):
        cls.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(cls.loop)

    def setup_method(self, method):
        self.drivers = dict()
        self.errors = Errors()

    def create_drivers(self, quantity=2, max_duration=1800, custom_implicitly_wait=None):
        self.drivers = self.loop.run_until_complete(start_threads(quantity,
                                                                  Driver,
                                                                  self.drivers,
                                                                  executor_sauce_lab,
                                                                  get_capabilities_sauce_lab()))
        for driver in range(quantity):
            test_suite_data.current_test.testruns[-1].jobs[self.drivers[driver].session_id] = driver + 1
            self.drivers[driver].implicitly_wait(
                custom_implicitly_wait if custom_implicitly_wait else implicit_wait)

    def teardown_method(self, method):
        geth_names, geth_contents = [], []
        for driver in self.drivers:
            try:
                self.print_sauce_lab_info(self.drivers[driver])
                self.add_alert_text_to_report(self.drivers[driver])
                geth_names.append(
                    '%s_geth%s.log' % (test_suite_data.current_test.name, str(self.drivers[driver].number)))
                geth_contents.append(pull_geth(self.drivers[driver]))
                self.drivers[driver].quit()
            except (WebDriverException, AttributeError):
                pass
        geth = {geth_names[i]: geth_contents[i] for i in range(len(geth_names))}
        github_report.save_test(test_suite_data.current_test, geth)

    @classmethod
    def teardown_class(cls):
        cls.loop.close()


def create_shared_drivers(quantity):
    drivers = dict()
    if pytest_config_global['env'] == 'local':
        capabilities = add_local_devices_to_capabilities()
        for i in range(quantity):
            driver = Driver(executor_local, capabilities[i])
            test_suite_data.current_test.testruns[-1].jobs[driver.session_id] = i + 1
            driver.implicitly_wait(implicit_wait)
            drivers[i] = driver
        loop = None
        return drivers, loop
    else:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        print('SC Executor: %s' % executor_sauce_lab)
        try:
            drivers = loop.run_until_complete(start_threads(test_suite_data.current_test.name,
                                                            quantity,
                                                            Driver,
                                                            drivers,
                                                            command_executor=executor_sauce_lab,
                                                            options=get_capabilities_sauce_lab()))
            for i in range(quantity):
                test_suite_data.current_test.testruns[-1].jobs[drivers[i].session_id] = i + 1
                drivers[i].implicitly_wait(implicit_wait)
            if len(drivers) < quantity:
                test_suite_data.current_test.testruns[-1].error = "Not all %s drivers are created" % quantity
            return drivers, loop
        except (MaxRetryError, AttributeError) as e:
            test_suite_data.current_test.testruns[-1].error += str(e)
            raise e


class LocalSharedMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        jobs = test_suite_data.current_test.testruns[-1].jobs
        if not jobs:
            for index, driver in self.drivers.items():
                jobs[driver.session_id] = index + 1
        self.errors = Errors()

    def teardown_method(self, method):
        for driver in self.drivers:
            try:
                self.add_alert_text_to_report(self.drivers[driver])
            except WebDriverException:
                pass

    @pytest.fixture(scope='class', autouse=True)
    def prepare(self, request):
        try:
            request.cls.prepare_devices(request)
        finally:
            for item, value in request.__dict__.items():
                setattr(request.cls, item, value)

    @classmethod
    def teardown_class(cls):
        for driver in cls.drivers:
            try:
                cls.drivers[driver].quit()
            except WebDriverException:
                pass


class SauceSharedMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        if not self.drivers:
            pytest.fail(test_suite_data.current_test.testruns[-1].error)
        for _, driver in self.drivers.items():
            driver.execute_script("sauce:context=Started %s" % method.__name__)
        jobs = test_suite_data.current_test.testruns[-1].jobs
        if not jobs:
            for index, driver in self.drivers.items():
                jobs[driver.session_id] = index + 1
        self.errors = Errors()
        test_suite_data.current_test.group_name = self.__class__.__name__

    def teardown_method(self, method):
        geth_names, geth_contents = [], []
        for driver in self.drivers:
            try:
                self.print_sauce_lab_info(self.drivers[driver])
                self.add_alert_text_to_report(self.drivers[driver])
                geth_names.append(
                    '%s_geth%s.log' % (test_suite_data.current_test.name, str(self.drivers[driver].number)))
                geth_contents.append(pull_geth(self.drivers[driver]))

            except (WebDriverException, AttributeError, RemoteDisconnected, ProtocolError):
                pass
            finally:
                try:
                    geth = {geth_names[i]: geth_contents[i] for i in range(len(geth_names))}
                    test_suite_data.current_test.geth_paths = github_report.save_geth(geth)
                except IndexError:
                    pass

    @pytest.fixture(scope='class', autouse=True)
    def prepare(self, request):
        try:
            request.cls.prepare_devices(request)
        finally:
            for item, value in request.__dict__.items():
                setattr(request.cls, item, value)

    @classmethod
    def teardown_class(cls):
        from tests.conftest import sauce
        requests_session = requests.Session()
        requests_session.auth = (sauce_username, sauce_access_key)
        if test_suite_data.tests[0].testruns[-1].error and 'setup failed' in test_suite_data.tests[0].testruns[
            -1].error:
            group_setup_failed = True
        else:
            group_setup_failed = False
        geth_contents = list()
        try:
            for _, driver in cls.drivers.items():
                if group_setup_failed:
                    geth_contents.append(pull_geth(driver=driver))
                session_id = driver.session_id
                try:
                    sauce.jobs.update_job(username=sauce_username, job_id=session_id, name=cls.__name__)
                except (RemoteDisconnected, SauceException, requests.exceptions.ConnectionError):
                    pass
                try:
                    driver.quit()
                except WebDriverException:
                    pass
                url = 'https://api.%s/rest/v1/%s/jobs/%s/assets/%s' % (apibase, sauce_username, session_id, "log.json")
                try:
                    WebDriverWait(driver, 60, 2).until(lambda _: requests_session.get(url).status_code == 200)
                    commands = requests_session.get(url).json()
                    for command in commands:
                        try:
                            if command['message'].startswith("Started "):
                                for test in test_suite_data.tests:
                                    if command['message'] == "Started %s" % test.name:
                                        test.testruns[-1].first_commands[session_id] = commands.index(command) + 1
                        except KeyError:
                            continue
                except (RemoteDisconnected, requests.exceptions.ConnectionError, TimeoutException):
                    pass
        except AttributeError:
            pass
        finally:
            try:
                cls.loop.close()
            except AttributeError:
                pass

        geth_names = ['%s_geth%s.log' % (cls.__name__, i) for i in range(len(geth_contents))]
        geth = dict(zip(geth_names, geth_contents))
        geth_paths = github_report.save_geth(geth)

        for test in test_suite_data.tests:
            if group_setup_failed:
                test.geth_paths = geth_paths
            github_report.save_test(test)


if pytest_config_global['env'] == 'local':
    MultipleDeviceTestCase = LocalMultipleDeviceTestCase
    MultipleSharedDeviceTestCase = LocalSharedMultipleDeviceTestCase
else:
    MultipleDeviceTestCase = SauceMultipleDeviceTestCase
    MultipleSharedDeviceTestCase = SauceSharedMultipleDeviceTestCase


class NoDeviceTestCase(AbstractTestCase):

    def setup_method(self, method, **kwargs):
        pass

    def teardown_method(self, method):
        github_report.save_test(test_suite_data.current_test)
