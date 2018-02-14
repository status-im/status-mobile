from appium.webdriver.common.mobileby import MobileBy
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions
from tests import info


class BaseElement(object):

    class Locator(object):

        def __init__(self, by, value):
            self.by = by
            self.value = value

        @classmethod
        def xpath_selector(locator, value):
            return locator(MobileBy.XPATH, value)

        @classmethod
        def accessibility_id(locator, value):
            return locator(MobileBy.ACCESSIBILITY_ID, value)

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
        info('Looking for %s' % self.name)
        try:
            return self.driver.find_element(self.locator.by, self.locator.value)
        except NoSuchElementException as exception:
            exception.msg = "'%s' is not found on screen, using: '%s'" % (self.name, self.locator)
            raise exception

    def find_elements(self):
        info('Looking for %s' % self.name)
        return self.driver.find_elements(self.locator.by, self.locator.value)

    def wait_for_element(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds)\
                .until(expected_conditions.presence_of_element_located((self.locator.by, self.locator.value)))
        except TimeoutException as exception:
            exception.msg = "'%s' is not found on screen, using: '%s', during '%s' seconds" % (self.name, self.locator,
                                                                                               seconds)
            raise exception

    def scroll_to_element(self):
        for _ in range(9):
            try:
                return self.find_element()
            except NoSuchElementException:
                info('Scrolling down to %s' % self.name)
                self.driver.swipe(500, 1000, 500, 500)

    def is_element_present(self, sec=5):
        try:
            info('Wait for %s' % self.name)
            self.wait_for_element(sec)
            return True
        except TimeoutException:
            return False

    @property
    def text(self):
        return self.find_element().text


class BaseEditBox(BaseElement):

    def __init__(self, driver):
        super(BaseEditBox, self).__init__(driver)

    def send_keys(self, value):
        self.find_element().send_keys(value)
        info("Type '%s' to %s" % (value, self.name))

    def set_value(self, value):
        self.find_element().set_value(value)
        info("Type '%s' to %s" % (value, self.name))

    def clear(self):
        self.find_element().clear()
        info('Clear text in %s' % self.name)

    def click(self):
        self.find_element().click()
        info('Tap on %s' % self.name)


class BaseText(BaseElement):

    def __init__(self, driver):
        super(BaseText, self).__init__(driver)

    @property
    def text(self):
        text = self.find_element().text
        info('%s is %s' % (self.name, text))
        return text


class BaseButton(BaseElement):

    def __init__(self, driver):
        super(BaseButton, self).__init__(driver)

    def click(self):
        self.find_element().click()
        info('Tap on %s' % self.name)
        return self.navigate()

    def click_until_presence_of_element(self, desired_element, attempts=5):
        counter = 0
        while not desired_element.is_element_present() and counter <= attempts:
            try:
                info('Tap on %s' % self.name)
                self.find_element().click()
                info('Wait for %s' % desired_element.name)
                desired_element.wait_for_element(5)
                return self.navigate()
            except (NoSuchElementException, TimeoutException):
                counter += 1
