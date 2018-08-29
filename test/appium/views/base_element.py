import base64
from io import BytesIO
import os

import time
from timeit import timeit

from PIL import Image, ImageChops
from appium.webdriver.common.mobileby import MobileBy
from appium.webdriver.common.touch_action import TouchAction
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions


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

        @classmethod
        def text_selector(locator, text):
            return BaseElement.Locator.xpath_selector('//*[@text="' + text + '"]')

        @classmethod
        def text_part_selector(locator, text):
            return BaseElement.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')

        @classmethod
        def id(locator, value):
            return locator(MobileBy.ID, value)

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
        for _ in range(3):
            try:
                return self.driver.find_element(self.locator.by, self.locator.value)
            except NoSuchElementException:
                raise NoSuchElementException(
                    "Device %s: '%s' is not found on the screen" % (self.driver.number, self.name)) from None
            except Exception as exception:
                if 'Internal Server Error' in str(exception):
                    continue

    def find_elements(self):
        return self.driver.find_elements(self.locator.by, self.locator.value)

    def click(self):
        self.find_element().click()
        self.driver.info('Tap on %s' % self.name)

    def wait_for_element(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds) \
                .until(expected_conditions.presence_of_element_located((self.locator.by, self.locator.value)))
        except TimeoutException:
            raise TimeoutException(
                "Device %s: '%s' is not found on the screen" % (self.driver.number, self.name)) from None

    def wait_for_visibility_of_element(self, seconds=10, ignored_exceptions=None):
        try:
            return WebDriverWait(self.driver, seconds, ignored_exceptions=ignored_exceptions) \
                .until(expected_conditions.visibility_of_element_located((self.locator.by, self.locator.value)))
        except TimeoutException:
            raise TimeoutException(
                "Device %s: '%s' is not found on the screen" % (self.driver.number, self.name)) from None

    def wait_for_invisibility_of_element(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds) \
                .until(expected_conditions.invisibility_of_element_located((self.locator.by, self.locator.value)))
        except TimeoutException:
            raise TimeoutException("Device %s: '%s' is still visible on the screen after %s seconds" % (
                self.driver.number, self.name, seconds)) from None

    def scroll_to_element(self):
        for _ in range(9):
            try:
                return self.find_element()
            except NoSuchElementException:
                self.driver.info('Scrolling down to %s' % self.name)
                self.driver.swipe(500, 1000, 500, 500)

    def is_element_present(self, sec=5):
        try:
            self.driver.info('Wait for %s' % self.name)
            return self.wait_for_element(sec)
        except TimeoutException:
            return False

    def is_element_displayed(self, sec=5, ignored_exceptions=None):
        try:
            self.driver.info('Wait for %s' % self.name)
            return self.wait_for_visibility_of_element(sec, ignored_exceptions=ignored_exceptions)
        except TimeoutException:
            return False

    @property
    def text(self):
        return self.find_element().text

    @property
    def template(self):
        try:
            return self.__template
        except FileNotFoundError:
            raise FileNotFoundError('Please add %s image as template' % self.name)

    @template.setter
    def template(self, value):
        self.__template = Image.open(os.sep.join(__file__.split(os.sep)[:-1]) + '/elements_templates/%s' % value)

    @property
    def image(self):
        return Image.open(BytesIO(base64.b64decode(self.find_element().screenshot_as_base64)))

    def is_element_image_equals_template(self, file_name: str = ''):
        if file_name:
            self.template = file_name
        return not ImageChops.difference(self.image, self.template).getbbox()

    def swipe_element(self):
        element = self.find_element()
        location, size = element.location, element.size
        x, y = location['x'], location['y']
        width, height = size['width'], size['height']
        self.driver.swipe(start_x=x + width * 0.75, start_y=y + height / 2, end_x=x, end_y=y + height / 2)

    def long_press_element(self):
        element = self.find_element()
        self.driver.info('Long press %s' % self.name)
        action = TouchAction(self.driver)
        action.long_press(element).release().perform()

    def measure_time_before_element_appears(self, max_wait_time=30):
        def wrapper():
            return self.wait_for_visibility_of_element(max_wait_time)

        return timeit(wrapper, number=1)

    def measure_time_while_element_is_shown(self, max_wait_time=30):
        def wrapper():
            return self.wait_for_invisibility_of_element(max_wait_time)

        return timeit(wrapper, number=1)


class BaseEditBox(BaseElement):

    def __init__(self, driver):
        super(BaseEditBox, self).__init__(driver)

    def send_keys(self, value):
        self.find_element().send_keys(value)
        self.driver.info("Type '%s' to %s" % (value, self.name))

    def set_value(self, value):
        self.find_element().set_value(value)
        self.driver.info("Type '%s' to %s" % (value, self.name))

    def clear(self):
        self.find_element().clear()
        self.driver.info('Clear text in %s' % self.name)

    def delete_last_symbols(self, number_of_symbols_to_delete: int):
        self.driver.info('Delete last %s symbols from %s' % (number_of_symbols_to_delete, self.name))
        self.click()
        for _ in range(number_of_symbols_to_delete):
            time.sleep(1)
            self.driver.press_keycode(67)

    def paste_text_from_clipboard(self):
        self.driver.info('Paste text from clipboard into %s' % self.name)
        self.long_press_element()
        time.sleep(2)
        action = TouchAction(self.driver)
        location = self.find_element().location
        x, y = location['x'], location['y']
        action.press(x=x + 100, y=y - 50).release().perform()

    def cut_text(self):
        self.driver.info('Cut text in %s' % self.name)
        location = self.find_element().location
        x, y = location['x'], location['y']
        action = TouchAction(self.driver)
        action.long_press(x=x, y=y).release().perform()
        time.sleep(2)
        action.press(x=x + 50, y=y - 50).release().perform()


class BaseText(BaseElement):

    def __init__(self, driver):
        super(BaseText, self).__init__(driver)

    @property
    def text(self):
        text = self.find_element().text
        self.driver.info('%s is %s' % (self.name, text))
        return text


class BaseButton(BaseElement):

    def __init__(self, driver):
        super(BaseButton, self).__init__(driver)

    def click(self):
        self.find_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()

    def click_until_presence_of_element(self, desired_element, attempts=3):
        counter = 0
        while not desired_element.is_element_present(1) and counter <= attempts:
            try:
                self.driver.info('Tap on %s' % self.name)
                self.find_element().click()
                self.driver.info('Wait for %s' % desired_element.name)
                desired_element.wait_for_element(5)
                return self.navigate()
            except (NoSuchElementException, TimeoutException):
                counter += 1
