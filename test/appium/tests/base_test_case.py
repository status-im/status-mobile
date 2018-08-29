import logging
import pytest
import sys
import re
import subprocess
import asyncio

from support.message_reliability_report import create_one_to_one_chat_report, create_public_chat_report
from support.api.network_api import NetworkApi
from os import environ
from appium import webdriver
from abc import ABCMeta, abstractmethod
from selenium.common.exceptions import WebDriverException
from tests import test_suite_data, start_threads
from appium.webdriver.common.mobileby import MobileBy
from selenium.common.exceptions import NoSuchElementException
from support.github_report import GithubHtmlReport


class AbstractTestCase:
    __metaclass__ = ABCMeta

    @property
    def sauce_username(self):
        return environ.get('SAUCE_USERNAME')

    @property
    def sauce_access_key(self):
        return environ.get('SAUCE_ACCESS_KEY')

    @property
    def executor_sauce_lab(self):
        return 'http://%s:%s@ondemand.saucelabs.com:80/wd/hub' % (self.sauce_username, self.sauce_access_key)

    @property
    def executor_local(self):
        return 'http://localhost:4723/wd/hub'

    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest.config.getoption('build')))

    def add_local_devices_to_capabilities(self):
        updated_capabilities = list()
        raw_out = re.split(r'[\r\\n]+', str(subprocess.check_output(['adb', 'devices'])).rstrip())
        for line in raw_out[1:]:
            serial = re.findall(r"(([\d.\d:]*\d+)|\bemulator-\d+)", line)
            if serial:
                capabilities = self.capabilities_local
                capabilities['udid'] = serial[0][0]
                updated_capabilities.append(capabilities)
        return updated_capabilities

    @property
    def capabilities_sauce_lab(self):
        desired_caps = dict()
        desired_caps['app'] = 'sauce-storage:' + test_suite_data.apk_name

        desired_caps['build'] = pytest.config.getoption('build')
        desired_caps['name'] = test_suite_data.current_test.name
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.7.2'
        desired_caps['platformVersion'] = '7.1'
        desired_caps['deviceName'] = 'Android GoogleAPI Emulator'
        desired_caps['deviceOrientation'] = "portrait"
        desired_caps['commandTimeout'] = 600
        desired_caps['idleTimeout'] = 1000
        desired_caps['unicodeKeyboard'] = True
        desired_caps['automationName'] = 'UiAutomator2'
        desired_caps['setWebContentDebuggingEnabled'] = True
        desired_caps['ignoreUnimportantViews'] = False
        desired_caps['enableNotificationListener'] = True
        return desired_caps

    def update_capabilities_sauce_lab(self, new_capabilities: dict):
        caps = self.capabilities_sauce_lab.copy()
        caps.update(new_capabilities)
        return caps

    @property
    def capabilities_local(self):
        desired_caps = dict()
        desired_caps['app'] = pytest.config.getoption('apk')
        desired_caps['deviceName'] = 'nexus_5'
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.7.2'
        desired_caps['platformVersion'] = '7.1'
        desired_caps['newCommandTimeout'] = 600
        desired_caps['fullReset'] = False
        desired_caps['unicodeKeyboard'] = True
        desired_caps['automationName'] = 'UiAutomator2'
        desired_caps['setWebContentDebuggingEnabled'] = True
        return desired_caps

    @abstractmethod
    def setup_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')

    @abstractmethod
    def teardown_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')

    @property
    def environment(self):
        return pytest.config.getoption('env')

    @property
    def implicitly_wait(self):
        return 2

    errors = []

    network_api = NetworkApi()
    github_report = GithubHtmlReport(sauce_username, sauce_access_key)

    def verify_no_errors(self):
        if self.errors:
            pytest.fail('. '.join([self.errors.pop(0) for _ in range(len(self.errors))]))

    def is_alert_present(self, driver):
        try:
            return driver.find_element(MobileBy.ID, 'android:id/message')
        except NoSuchElementException:
            return False

    def get_alert_text(self, driver):
        return driver.find_element(MobileBy.ID, 'android:id/message').text

    def add_alert_text_to_report(self, driver):
        if self.is_alert_present(driver):
            test_suite_data.current_test.testruns[-1].error += ", also Unexpected Alert is shown: '%s'" \
                                                                       % self.get_alert_text(driver)


class Driver(webdriver.Remote):

    @property
    def number(self):
        return test_suite_data.current_test.testruns[-1].jobs[self.session_id]

    def info(self, text: str):
        if "Base" not in text:
            text = 'Device %s: %s' % (self.number, text)
            logging.info(text)
            test_suite_data.current_test.testruns[-1].steps.append(text)

    def fail(self, text: str):
        pytest.fail('Device %s: %s' % (self.number, text))


class SingleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        capabilities = {'local': {'executor': self.executor_local,
                                  'capabilities': self.capabilities_local},
                        'sauce': {'executor': self.executor_sauce_lab,
                                  'capabilities': self.capabilities_sauce_lab}}

        self.driver = Driver(capabilities[self.environment]['executor'],
                             capabilities[self.environment]['capabilities'])
        test_suite_data.current_test.testruns[-1].jobs[self.driver.session_id] = 1
        self.driver.implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        if self.environment == 'sauce':
            self.print_sauce_lab_info(self.driver)
        try:
            self.add_alert_text_to_report(self.driver)
            self.driver.quit()
        except (WebDriverException, AttributeError):
            pass
        finally:
            self.github_report.save_test(test_suite_data.current_test)


class LocalMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        self.drivers = dict()

    def create_drivers(self, quantity):
        capabilities = self.add_local_devices_to_capabilities()
        for driver in range(quantity):
            self.drivers[driver] = Driver(self.executor_local, capabilities[driver])
            test_suite_data.current_test.testruns[-1].jobs[self.drivers[driver].session_id] = driver + 1
            self.drivers[driver].implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.drivers:
            try:
                self.add_alert_text_to_report(driver)
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

    def create_drivers(self, quantity=2, max_duration=1800, custom_implicitly_wait=None, offline_mode=False):
        capabilities = {'maxDuration': max_duration}
        if offline_mode:
            capabilities['platformVersion'] = '6.0'
        self.drivers = self.loop.run_until_complete(start_threads(quantity,
                                                                  Driver,
                                                                  self.drivers,
                                                                  self.executor_sauce_lab,
                                                                  self.update_capabilities_sauce_lab(capabilities)))
        for driver in range(quantity):
            test_suite_data.current_test.testruns[-1].jobs[self.drivers[driver].session_id] = driver + 1
            self.drivers[driver].implicitly_wait(
                custom_implicitly_wait if custom_implicitly_wait else self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.drivers:
            try:
                self.print_sauce_lab_info(self.drivers[driver])
                self.add_alert_text_to_report(self.drivers[driver])
                self.drivers[driver].quit()
            except (WebDriverException, AttributeError):
                pass
            finally:
                self.github_report.save_test(test_suite_data.current_test)

    @classmethod
    def teardown_class(cls):
        cls.loop.close()


environment = LocalMultipleDeviceTestCase if pytest.config.getoption('env') == 'local' else SauceMultipleDeviceTestCase


class MultipleDeviceTestCase(environment):

    def setup_method(self, method):
        super(MultipleDeviceTestCase, self).setup_method(method)
        self.senders = dict()

    def teardown_method(self, method):
        for user in self.senders:
            self.network_api.faucet(address=self.senders[user]['address'])
        super(MultipleDeviceTestCase, self).teardown_method(method)


class MessageReliabilityTestCase(MultipleDeviceTestCase):

    def setup_method(self, method):
        super(MessageReliabilityTestCase, self).setup_method(method)
        self.one_to_one_chat_data = dict()
        self.public_chat_data = dict()

    def teardown_method(self, method):
        if self.one_to_one_chat_data:
            create_one_to_one_chat_report(self.one_to_one_chat_data)
        if self.public_chat_data:
            create_public_chat_report(self.public_chat_data)
        super(MultipleDeviceTestCase, self).teardown_method(method)
