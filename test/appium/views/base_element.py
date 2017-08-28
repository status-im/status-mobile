from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions
import pytest


class BaseElement(object):

    class Locator(object):

        def __init__(self, by, value):
            self.by = by
            self.value = value

        @classmethod
        def xpath_selector(locator, value):
            return locator(By.XPATH, value)

        def __str__(self, *args, **kwargs):
            return "%s:%s" % (self.by, self.value)

    def __init__(self, driver):
        self.driver = driver
        self.locator = None

    @property
    def name(self):
        return self.__class__.__name__

    def navigate(self):
        return None

    def find_element(self):
        try:
            return self.wait_for_element()
        except (NoSuchElementException, TimeoutException):
            pytest.fail("'%s' not found by %s '%s'" % (
                self.name, self.locator.by, self.locator.value), pytrace=False)

    def wait_for_element(self, seconds=30):
        return WebDriverWait(self.driver, seconds)\
            .until(expected_conditions.presence_of_element_located((self.locator.by, self.locator.value)))


class BaseEditBox(BaseElement):

    def __init__(self, driver):
        super(BaseEditBox, self).__init__(driver)
        self.driver = driver

    def send_keys(self, value):
        self.find_element().send_keys(value)


class BaseText(BaseElement):

    def __init__(self, driver):
        super(BaseText, self).__init__(driver)
        self.driver = driver

    @property
    def text(self):
        return self.find_element().text


class BaseButton(BaseElement):

    def __init__(self, driver):
        super(BaseButton, self).__init__(driver)
        self.driver = driver

    def click(self):
        self.find_element().click()
        return self.navigate()
