import logging
import time
import pytest

import org.sikuli.script.SikulixForJython
from sikuli import *


class BaseElement(object):
    def __init__(self, screenshot):
        self.screenshot = screenshot

    def find_element(self):
        try:
            wait(self.screenshot, 10)
        except FindFailed:
            pytest.fail('%s was not found' % self.__class__.__name__)

    def click(self):
        logging.info('Click %s' % self.__class__.__name__)
        self.find_element()
        click(self.screenshot)

    def verify_element_is_not_present(self):
        try:
            wait(self.screenshot, 10)
            pytest.fail('%s is displayed but not expected' % self.__class__.__name__)
        except FindFailed:
            pass


class InputField(BaseElement):

    def input_value(self, value):
        self.click()
        type(value)

    def send_as_key_event(self, value):
        self.click()
        for i in str(value):
            type(i)
            time.sleep(0.5)


class TextElement(object):
    def __init__(self, text):
        self.element_line = None
        for _ in range(3):
            lines = collectLines()
            for line in lines:
                if text in line.getText().encode('ascii', 'ignore'):
                    self.element_line = line
                    return
            time.sleep(3)

    def click(self):
        self.element_line.click()
