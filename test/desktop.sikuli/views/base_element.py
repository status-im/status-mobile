import logging
import org.sikuli.script.SikulixForJython
import pytest
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


class InputField(BaseElement):

    def input_value(self, value):
        self.click()
        type(value)
