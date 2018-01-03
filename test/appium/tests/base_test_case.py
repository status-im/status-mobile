import pytest
import sys
from tests import *
from os import environ
from appium import webdriver
from abc import ABCMeta, \
    abstractmethod
import hmac
import re
import subprocess
from hashlib import md5
from selenium.common.exceptions import WebDriverException


class AbstractTestCase:

    __metaclass__ = ABCMeta

    @property
    def sauce_access_key(self):
        return environ.get('SAUCE_ACCESS_KEY')

    @property
    def sauce_username(self):
        return environ.get('SAUCE_USERNAME')

    @property
    def executor_sauce_lab(self):
        return 'http://%s:%s@ondemand.saucelabs.com:80/wd/hub' % (self.sauce_username, self.sauce_access_key)

    @property
    def executor_local(self):
        return 'http://localhost:4723/wd/hub'

    def get_public_url(self, driver):
        token = hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                         bytes(driver.session_id, 'latin-1'), md5).hexdigest()
        return "https://saucelabs.com/jobs/%s?auth=%s" % (driver.session_id, token)

    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest.config.getoption('build')))
        print(self.get_public_url(driver))

    def add_local_devices_to_capabilities(self):
        updated_capabilities = list()
        raw_out = re.split(r'[\r\\n]+', str(subprocess.check_output(['adb', 'devices'])).rstrip())
        for line in raw_out[1:]:
            serial = re.findall(r"([\d.\d:]*\d+)", line)
            if serial:
                capabilities = self.capabilities_local
                capabilities['udid'] = serial[0]
                updated_capabilities.append(capabilities)
        return updated_capabilities

    @property
    def capabilities_sauce_lab(self):
        desired_caps = dict()
        desired_caps['app'] = 'sauce-storage:' + test_data.apk_name

        desired_caps['build'] = pytest.config.getoption('build')
        desired_caps['name'] = test_data.test_name
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.7.1'
        desired_caps['platformVersion'] = '6.0'
        desired_caps['deviceName'] = 'Android GoogleAPI Emulator'
        desired_caps['deviceOrientation'] = "portrait"
        desired_caps['commandTimeout'] = 600
        desired_caps['idleTimeout'] = 1000
        return desired_caps

    @property
    def capabilities_local(self):
        desired_caps = dict()
        desired_caps['app'] = pytest.config.getoption('apk')
        desired_caps['deviceName'] = 'nexus_5'
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.7.1'
        desired_caps['platformVersion'] = '6.0'
        desired_caps['newCommandTimeout'] = 600
        desired_caps['fullReset'] = True
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
        return 10


class LocalMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        capabilities = self.add_local_devices_to_capabilities()
        self.driver_1 = webdriver.Remote(self.executor_local, capabilities[0])
        self.driver_2 = webdriver.Remote(self.executor_local, capabilities[1])
        for driver in self.driver_1, self.driver_2:
            driver.implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.driver_1, self.driver_2:
            try:
                driver.quit()
            except WebDriverException:
                pass


class SauceMultipleDeviceTestCase(AbstractTestCase):

    @classmethod
    def setup_class(cls):
        cls.loop = asyncio.get_event_loop()

    def setup_method(self, method):
        self.driver_1, \
        self.driver_2 = self.loop.run_until_complete(start_threads(2,
                                                              webdriver.Remote,
                                                              self.executor_sauce_lab,
                                                              self.capabilities_sauce_lab))
        for driver in self.driver_1, self.driver_2:
            driver.implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.driver_1, self.driver_2:
            self.print_sauce_lab_info(driver)
            try:
                driver.quit()
            except WebDriverException:
                pass

    @classmethod
    def teardown_class(cls):
        cls.loop.close()


class SingleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):

        capabilities = {'local': {'executor': self.executor_local,
                                  'capabilities': self.capabilities_local},
                        'sauce': {'executor': self.executor_sauce_lab,
                                  'capabilities': self.capabilities_sauce_lab}}

        self.driver = webdriver.Remote(capabilities[self.environment]['executor'],
                                       capabilities[self.environment]['capabilities'])
        self.driver.implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        if self.environment == 'sauce':
            self.print_sauce_lab_info(self.driver)
        try:
            self.driver.quit()
        except WebDriverException:
            pass


environments = {'local': LocalMultipleDeviceTestCase,
                'sauce': SauceMultipleDeviceTestCase}


class MultipleDeviceTestCase(environments[pytest.config.getoption('env')]):

    pass
