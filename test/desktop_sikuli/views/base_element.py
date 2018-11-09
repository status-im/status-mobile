import logging
import time
import pytest
import re
try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass


class BaseElement(object):
    def __init__(self, screenshot):
        self.screenshot = screenshot
        self.name = re.findall('([^\/]+)(?=.png)', self.screenshot)[0].replace('_', ' ').title()

    def find_element(self, log=True):
        if log:
            logging.info('Find %s' % self.name)
        try:
            wait(self.screenshot, 10)
        except FindFailed:
            pytest.fail('%s was not found' % self.name)

    def click(self, log=True):
        if log:
            logging.info('Click %s' % self.name)
        self.find_element(log=False)
        click(self.screenshot)

    def verify_element_is_not_present(self):
        logging.info('Verify: %s is not present' % self.name)
        try:
            wait(self.screenshot, 10)
            pytest.fail('%s is displayed but not expected' % self.name)
        except FindFailed:
            pass


class InputField(BaseElement):

    def input_value(self, value):
        logging.info("%s field: set value '%s'" % (self.name, value))
        self.click(log=False)
        type(value)

    def send_keys(self, value):
        logging.info("Type '%s' to %s field" % (value, self.name))
        self.click(log=False)
        for i in str(value):
            type(i)
            time.sleep(0.5)

    def is_focused(self):
        self.find_element(log=False)
        return find(self.screenshot).getTarget() == Env.getMouseLocation()

    def verify_is_focused(self):
        logging.info('Verify %s is focused' % self.name)
        if not self.is_focused():
            pytest.fail('%s is not focused' % self.name)


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
