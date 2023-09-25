import base64
import os
import time
from io import BytesIO
from timeit import timeit

import emoji
import imagehash
from PIL import Image, ImageChops, ImageStat
from appium.webdriver.common.mobileby import MobileBy
from appium.webdriver.common.touch_action import TouchAction
from selenium.common.exceptions import NoSuchElementException, StaleElementReferenceException, TimeoutException
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.wait import WebDriverWait

from tests import transl


class BaseElement(object):
    def __init__(self, driver, **kwargs):
        self.driver = driver
        self.by = MobileBy.XPATH
        self.locator = None
        self.xpath = None
        self.accessibility_id = None
        self.translation_id = None
        self.uppercase = None
        self.prefix = ''
        self.suffix = None
        self.id = None
        self.class_name = None
        self.AndroidUIAutomator = None
        self.webview = None

        self.__dict__.update(kwargs)
        self.set_locator()

    def set_locator(self):
        if self.xpath:
            self.locator = self.xpath
        elif self.accessibility_id:
            self.by = MobileBy.ACCESSIBILITY_ID
            self.locator = self.accessibility_id
        elif self.translation_id:
            text = transl[self.translation_id]
            self.locator = '//*[@text="%s"]' % text
            if self.uppercase:
                self.locator = '//*[@text="%s" or @text="%s"]' % (text, text.upper())
            if self.suffix:
                self.locator += self.suffix
        elif self.id:
            self.by = MobileBy.ID
            self.locator = self.id
        elif self.class_name:
            self.by = MobileBy.CLASS_NAME
            self.locator = self.class_name
        elif self.AndroidUIAutomator:
            self.by = MobileBy.ANDROID_UIAUTOMATOR
            self.locator = self.AndroidUIAutomator
        elif self.webview:
            self.locator = '//*[@text="{0}"] | //*[@content-desc="{desc}"]'.format(self.webview, desc=self.webview)
        if self.prefix:
            self.locator = self.prefix + self.locator
        return self

    @property
    def name(self):
        return self.__class__.__name__

    def navigate(self):
        return None

    def find_element(self):
        for _ in range(3):
            try:
                self.driver.info("Find `%s` by `%s`: `%s`" % (self.name, self.by, self.exclude_emoji(self.locator)))
                return self.driver.find_element(self.by, self.locator)
            except NoSuchElementException:
                raise NoSuchElementException(
                    "Device %s: %s by %s: `%s` is not found on the screen" % (
                        self.driver.number, self.name, self.by, self.locator)) from None
            except Exception as exception:
                # if 'Internal Server Error' in str(exception):
                raise exception

    def find_elements(self):
        return self.driver.find_elements(self.by, self.locator)

    def click(self):
        self.find_element().click()
        self.driver.info('Tap on found: %s' % self.name)
        return self.navigate()

    def wait_and_click(self, sec=30):
        self.driver.info("Wait for element `%s` for max %ss and click when it is available" % (self.name, sec))
        self.wait_for_visibility_of_element(sec)
        self.click()

    def click_until_presence_of_element(self, desired_element, attempts=4):
        counter = 0
        self.driver.info("Click until `%s` by `%s`: `%s` will be presented" % (
            desired_element.name, desired_element.by, desired_element.locator))
        while not desired_element.is_element_displayed(1) and counter <= attempts:
            try:
                self.find_element().click()
                return self.navigate()
            except (NoSuchElementException, TimeoutException):
                counter += 1
        else:
            self.driver.info("%s element not found" % desired_element.name)

    def double_click(self):
        self.driver.info('Double tap on: %s' % self.name)
        [self.find_element().click() for _ in range(2)]

    def wait_for_element(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds) \
                .until(expected_conditions.presence_of_element_located((self.by, self.locator)))
        except TimeoutException:
            raise TimeoutException(
                "Device `%s`: `%s` by` %s`: `%s` is not found on the screen after wait_for_element" % (
                    self.driver.number, self.name, self.by, self.locator)) from None

    def wait_for_elements(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds) \
                .until(expected_conditions.presence_of_all_elements_located((self.by, self.locator)))
        except TimeoutException:
            raise TimeoutException(
                "Device %s:  %s by %s:`%s` is not found on the screen after wait_for_elements" % (
                    self.driver.number, self.name, self.by, self.locator)) from None

    def wait_for_visibility_of_element(self, seconds=10, ignored_exceptions=None):
        try:
            return WebDriverWait(self.driver, seconds, ignored_exceptions=ignored_exceptions) \
                .until(expected_conditions.visibility_of_element_located((self.by, self.locator)))
        except TimeoutException:
            raise TimeoutException(
                "Device %s: %s by %s:`%s` is not found on the screen after wait_for_visibility_of_element" % (
                    self.driver.number, self.name, self.by, self.locator)) from None

    def wait_for_invisibility_of_element(self, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds) \
                .until(expected_conditions.invisibility_of_element_located((self.by, self.locator)))
        except TimeoutException:
            raise TimeoutException(
                "Device %s: %s by %s: `%s`  is still visible on the screen after %s seconds after wait_for_invisibility_of_element" % (
                    self.driver.number, self.name, self.by, self.locator, seconds)) from None

    def wait_for_rendering_ended_and_click(self, attempts=3):
        for i in range(attempts):
            try:
                self.click()
                self.driver.info("Attempt %s is successful clicking %s" % (i, self.locator))
                return
            except StaleElementReferenceException:
                time.sleep(1)
        raise StaleElementReferenceException(
            msg="Device %s: continuous rendering, can't click an element by %s: %s" % (
                self.driver.number, self.by, self.locator))

    def wait_for_element_text(self, text, wait_time=30, message=None):
        if not isinstance(text, str):
            text = str(text)
        self.driver.info("Wait for text element `%s` to be equal to `%s`" % (self.name, text))
        element_text = str()
        counter = 0
        while counter < wait_time:
            try:
                element_text = self.find_element().text.strip()
            except StaleElementReferenceException:
                time.sleep(1)
                element_text = self.find_element().text.strip()
            if element_text == text:
                self.driver.info('Element %s text is equal to %s' % (self.name, text))
                return
            counter += 10
            time.sleep(10)
        self.driver.fail(message if message else "`%s` is not equal to expected `%s` in %s sec" % (
            element_text, text, wait_time))

    def scroll_to_element(self, depth: int = 9, direction='down'):
        self.driver.info('Scrolling %s to %s' % (direction, self.name))
        for _ in range(depth):
            try:
                return self.find_element()
            except NoSuchElementException:
                size = self.driver.get_window_size()
                if direction == 'down':
                    self.driver.swipe(500, size["height"] * 0.4, 500, size["height"] * 0.05)
                else:
                    self.driver.swipe(500, size["height"] * 0.25, 500, size["height"] * 0.8)
        else:
            raise NoSuchElementException(
                "Device %s: %s by %s: `%s` is not found on the screen" % (
                    self.driver.number, self.name, self.by, self.locator)) from None

    def scroll_and_click(self, direction='down'):
        self.scroll_to_element(direction=direction)
        self.click()

    # def is_element_present(self, sec=5):
    #     try:
    #         return self.wait_for_element(sec)
    #     except TimeoutException:
    #         return False

    def is_element_displayed(self, sec=5, ignored_exceptions=None):
        try:
            return self.wait_for_visibility_of_element(sec, ignored_exceptions=ignored_exceptions)
        except TimeoutException:
            return False

    def click_if_shown(self, sec=5):
        if self.is_element_displayed(sec=sec):
            self.click()

    def is_element_disappeared(self, sec=20):
        try:
            return self.wait_for_invisibility_of_element(sec)
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

    def attribute_value(self, value):
        attribute_value = self.find_element().get_attribute(value)
        if attribute_value.lower() == 'true':
            attribute_state = True
        elif attribute_value.lower() == 'false':
            attribute_state = False
        else:
            attribute_state = attribute_value
        return attribute_state

    # Method-helper for renew screenshots in case if changed
    def save_new_screenshot_of_element(self, name: str):
        full_path_to_file = os.sep.join(__file__.split(os.sep)[:-1]) + '/elements_templates/%s' % name
        screen = Image.open(BytesIO(base64.b64decode(self.find_element().screenshot_as_base64)))
        screen.save(full_path_to_file)

    def is_element_image_equals_template(self, file_name: str = ''):
        if file_name:
            self.template = file_name
        return not ImageChops.difference(self.image, self.template).getbbox()

    def is_element_differs_from_template(self, file_name: str = '', diff: int = 0):
        if file_name:
            self.template = file_name
        result = False
        difference = ImageChops.difference(self.image, self.template)
        stat = ImageStat.Stat(difference)
        diff_ratio = sum(stat.mean) / (len(stat.mean) * 255)
        self.driver.info('Image differs from template to %s percents' % str(diff_ratio * 100))
        if diff_ratio * 100 > diff:
            result = True
        return result

    def is_element_image_similar_to_template(self, template_path: str = ''):
        image_template = os.sep.join(__file__.split(os.sep)[:-1]) + '/elements_templates/%s' % template_path
        template = imagehash.average_hash(Image.open(image_template))
        element_image = imagehash.average_hash(self.image)
        return not bool(template - element_image)

    def get_element_coordinates(self):
        element = self.find_element()
        location = element.location
        size = element.size
        return location, size

    def swipe_left_on_element(self):
        self.driver.info("Swiping left on element %s" % self.name)
        location, size = self.get_element_coordinates()
        x, y = location['x'], location['y']
        width, height = size['width'], size['height']
        self.driver.swipe(start_x=x + width * 0.75, start_y=y + height / 2, end_x=x, end_y=y + height / 2)

    def swipe_right_on_element(self):
        self.driver.info("Swiping right on element %s" % self.name)
        location, size = self.get_element_coordinates()
        x, y = location['x'], location['y']
        width, height = size['width'], size['height']
        self.driver.swipe(start_x=x, start_y=y + height / 2, end_x=x + width * 0.75, end_y=y + height / 2)

    def swipe_to_web_element(self, depth=700):
        element = self.find_element()
        location = element.location
        x, y = location['x'], location['y']
        self.driver.swipe(start_x=x, start_y=y, end_x=x, end_y=depth)

    def long_press_element(self):
        element = self.find_element()
        self.driver.info("Long press on `%s`" % self.name)
        action = TouchAction(self.driver)
        action.long_press(element).release().perform()

    def long_press_until_element_is_shown(self, expected_element):
        element = self.find_element()
        self.driver.info("Long press on `%s` until expected element is shown" % self.name)
        action = TouchAction(self.driver)
        for _ in range(3):
            action.long_press(element).release().perform()
            if expected_element.is_element_displayed():
                return

    def long_press_element_by_coordinate(self, rel_x=0.8, rel_y=0.8):
        element = self.find_element()
        location = element.location
        size = element.size
        x = int(location['x'] + size['width'] * rel_x)
        y = int(location['y'] + size['height'] * rel_y)
        action = TouchAction(self.driver)
        action.long_press(x=x, y=y).release().perform()

    def measure_time_before_element_appears(self, max_wait_time=30):
        def wrapper():
            return self.wait_for_visibility_of_element(max_wait_time)

        return timeit(wrapper, number=1)

    def measure_time_while_element_is_shown(self, max_wait_time=30):
        def wrapper():
            return self.wait_for_invisibility_of_element(max_wait_time)

        return timeit(wrapper, number=1)

    def click_inside_element_by_coordinate(self, rel_x=0.8, rel_y=0.8, times_to_click=1):
        location, size = self.get_element_coordinates()
        x = int(location['x'] + size['width'] * rel_x)
        y = int(location['y'] + size['height'] * rel_y)
        [self.driver.tap([(x, y)], 150) for _ in range(times_to_click)]

    @staticmethod
    def get_translation_by_key(key):
        return transl[key]

    @staticmethod
    def exclude_emoji(value):
        return 'emoji' if value in emoji.UNICODE_EMOJI else value


class EditBox(BaseElement):

    def __init__(self, driver, **kwargs):
        super(EditBox, self).__init__(driver, **kwargs)

    def send_keys(self, value):
        self.find_element().send_keys(value)
        self.driver.info("Type `%s` to `%s`" % (self.exclude_emoji(value), self.name))

    def clear(self):
        self.find_element().clear()
        self.driver.info("Clear text in `%s`" % self.name)

    def delete_last_symbols(self, number_of_symbols_to_delete: int):
        self.driver.info("Delete last `%s` symbols from `%s`" % (number_of_symbols_to_delete, self.name))
        self.click()
        for _ in range(number_of_symbols_to_delete):
            time.sleep(1)
            self.driver.press_keycode(67)

    def paste_text_from_clipboard(self):
        self.driver.info("Paste text from clipboard into `%s`" % self.name)
        self.long_press_element()
        time.sleep(2)
        action = TouchAction(self.driver)
        location = self.find_element().location
        x, y = location['x'], location['y']
        action.press(x=x + 25, y=y - 50).release().perform()

    def cut_text(self):
        self.driver.info("Cut text in `%s`" % self.name)
        location = self.find_element().location
        x, y = location['x'], location['y']
        action = TouchAction(self.driver)
        action.long_press(x=x, y=y).release().perform()
        time.sleep(2)
        action.press(x=x + 50, y=y - 50).release().perform()


class Text(BaseElement):
    def __init__(self, driver, **kwargs):
        super(Text, self).__init__(driver, **kwargs)

    @property
    def text(self):
        text = self.find_element().text
        self.driver.info("`%s` is `%s`" % (self.name, text))
        return text


class Button(BaseElement):

    def __init__(self, driver, **kwargs):
        super(Button, self).__init__(driver, **kwargs)

    def click_until_absense_of_element(self, desired_element, attempts=3, timeout=1):
        counter = 0
        self.driver.info("Click until `%s` by `%s`: `%s` is NOT presented" % (
            desired_element.name, desired_element.by, desired_element.locator))
        while desired_element.is_element_displayed(timeout) and counter <= attempts:
            try:
                self.find_element().click()
                counter += 1
            except (NoSuchElementException, TimeoutException, StaleElementReferenceException):
                return self.navigate()


class SilentButton(Button):
    def find_element(self):
        for _ in range(3):
            try:
                return self.driver.find_element(self.by, self.locator)
            except NoSuchElementException:
                raise NoSuchElementException(
                    "Device %s: `%s` by `%s`:`%s` not found on the screen" % (
                        self.driver.number, self.name, self.by, self.locator)) from None
            except Exception as exception:
                if 'Internal Server Error' in str(exception):
                    continue

    def click(self):
        self.find_element().click()
        return self.navigate()

    @property
    def text(self):
        text = self.find_element().text
        return text


class CheckBox(Button):
    def __init__(self, driver, **kwargs):
        super(Button, self).__init__(driver, **kwargs)

    def __define_desired_element(self, elem_accessibility):
        desired_element_accessibility_id = elem_accessibility
        if self.accessibility_id is not None and ':' in self.accessibility_id:
            desired_element_accessibility_id = ':%s' % elem_accessibility
        return desired_element_accessibility_id

    def enable(self):
        self.click_until_presence_of_element(Button(self.driver,
                                                    accessibility_id=self.__define_desired_element("checkbox-on")))
        return self.navigate()

    def disable(self):
        self.click_until_presence_of_element(Button(self.driver,
                                                    accessibility_id=self.__define_desired_element("checkbox-off")))
        return self.navigate()
