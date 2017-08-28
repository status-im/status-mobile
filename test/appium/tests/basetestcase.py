import pytest
import sys
from tests import *
from os import environ
from appium import webdriver
from abc import ABCMeta, \
    abstractmethod
import hmac
from hashlib import md5


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
    def capabilities_sauce_lab(self):

        desired_caps = dict()
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.6.5'
        desired_caps['platformVersion'] = '6.0'
        desired_caps['deviceName'] = 'Android GoogleAPI Emulator'
        desired_caps['app'] = pytest.config.getoption('apk')
        desired_caps['browserName'] = ''
        desired_caps['deviceOrientation'] = "portrait"
        desired_caps['name'] = tests_data.name
        desired_caps['build'] = pytest.config.getoption('build')
        desired_caps['idleTimeout'] = 500
        return desired_caps

    def get_public_url(self, driver):
        token = hmac.new(bytes(self.sauce_username + ":" + self.sauce_access_key, 'latin-1'),
                         bytes(driver.session_id, 'latin-1'), md5).hexdigest()
        return "https://saucelabs.com/jobs/%s?auth=%s" % (driver.session_id, token)

    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest.config.getoption('build')))
        print(self.get_public_url(driver))

    @property
    def executor_local(self):
        return 'http://localhost:4723/wd/hub'

    @property
    def capabilities_local(self):
        desired_caps = dict()
        desired_caps['deviceName'] = 'takoe'
        desired_caps['platformName'] = 'Android'
        desired_caps['appiumVersion'] = '1.6.5'
        desired_caps['platformVersion'] = '6.0'
        desired_caps['app'] = pytest.config.getoption('apk')
        return desired_caps

    @abstractmethod
    def setup_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')

    @abstractmethod
    def teardown_method(self, method):
        raise NotImplementedError('Should be overridden from a child class')


class SingleDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):
        self.driver = webdriver.Remote(self.executor_sauce_lab,
                                       self.capabilities_sauce_lab)
        self.driver.implicitly_wait(10)

    def teardown_method(self, method):
        self.print_sauce_lab_info(self.driver)
        self.driver.quit()


class MultiplyDeviceTestCase(AbstractTestCase):

    def setup_method(self, method):

        loop = asyncio.get_event_loop()
        self.driver_1, \
        self.driver_2 = loop.run_until_complete(start_threads(2,
                                                              webdriver.Remote,
                                                              self.executor_sauce_lab,
                                                              self.capabilities_sauce_lab))
        loop.close()
        for driver in self.driver_1, self.driver_2:
            driver.implicitly_wait(10)

    def teardown_method(self, method):
        for driver in self.driver_1, self.driver_2:
            self.print_sauce_lab_info(driver)
            driver.quit()
