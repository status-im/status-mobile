import pytest
import sys
import re
import subprocess
import asyncio
from selenium.common.exceptions import WebDriverException
from tests import test_data, start_threads
from os import environ
from appium import webdriver
from abc import ABCMeta, abstractmethod


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
        desired_caps['fullReset'] = False
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

    def update_test_info_dict(self):
        test_data.test_info[test_data.test_name] = dict()
        test_data.test_info[test_data.test_name]['jobs'] = list()
        test_data.test_info[test_data.test_name]['steps'] = str()


class SingleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        self.update_test_info_dict()

        capabilities = {'local': {'executor': self.executor_local,
                                  'capabilities': self.capabilities_local},
                        'sauce': {'executor': self.executor_sauce_lab,
                                  'capabilities': self.capabilities_sauce_lab}}

        self.driver = webdriver.Remote(capabilities[self.environment]['executor'],
                                       capabilities[self.environment]['capabilities'])
        self.driver.implicitly_wait(self.implicitly_wait)
        test_data.test_info[test_data.test_name]['jobs'].append(self.driver.session_id)

    def teardown_method(self, method):
        if self.environment == 'sauce':
            self.print_sauce_lab_info(self.driver)
        try:
            self.driver.quit()
        except WebDriverException:
            pass


class LocalMultipleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        self.drivers = dict()

    def create_drivers(self, quantity):
        capabilities = self.add_local_devices_to_capabilities()
        for driver in range(quantity):
            self.drivers[driver] = webdriver.Remote(self.executor_local, capabilities[driver])
            self.drivers[driver].implicitly_wait(self.implicitly_wait)

    def teardown_method(self, method):
        for driver in self.drivers:
            try:
                self.drivers[driver].quit()
            except WebDriverException:
                pass


class SauceMultipleDeviceTestCase(AbstractTestCase):

    @classmethod
    def setup_class(cls):
        cls.loop = asyncio.get_event_loop()

    def setup_method(self, method):
        self.update_test_info_dict()
        self.drivers = dict()

    def create_drivers(self, quantity=2):
        self.drivers = self.loop.run_until_complete(start_threads(quantity, webdriver.Remote,
                                                    self.drivers,
                                                    self.executor_sauce_lab,
                                                    self.capabilities_sauce_lab))
        for driver in range(quantity):
            self.drivers[driver].implicitly_wait(self.implicitly_wait)
            test_data.test_info[test_data.test_name]['jobs'].append(self.drivers[driver].session_id)

    def teardown_method(self, method):
        for driver in self.drivers:
            self.print_sauce_lab_info(self.drivers[driver])
            try:
                self.drivers[driver].quit()
            except WebDriverException:
                pass

    @classmethod
    def teardown_class(cls):
        cls.loop.close()


environments = {'local': LocalMultipleDeviceTestCase,
                'sauce': SauceMultipleDeviceTestCase}


class MultipleDeviceTestCase(environments[pytest.config.getoption('env')]):

    pass
