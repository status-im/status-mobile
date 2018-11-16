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
    def __init__(self, screenshot, x=0, y=0, w=1024, h=768):
        self.screenshot = screenshot
        self.name = re.findall('([^\/]+)(?=.png)', self.screenshot)[0].replace('_', ' ').title()
        self.region = Region(x, y, w, h)

    def find_element(self, log=True):
        if log:
            logging.info('Find %s' % self.name)
        try:
            self.region.wait(self.screenshot, 10)
        except FindFailed:
            pytest.fail('%s was not found' % self.name)

    def click(self, log=True):
        if log:
            logging.info('Click %s' % self.name)
        self.find_element(log=False)
        self.region.click(self.screenshot)

    def is_visible(self):
        try:
            self.region.wait(self.screenshot, 10)
            return True
        except FindFailed:
            return False

    def verify_element_is_not_present(self):
        logging.info('Verify: %s is not present' % self.name)
        try:
            self.region.wait(self.screenshot, 10)
            pytest.fail('%s is displayed but not expected' % self.name)
        except FindFailed:
            pass

    def get_target(self):
        self.find_element(log=False)
        return self.region.find(self.screenshot).getTarget()


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
        return self.get_target() == Env.getMouseLocation()

    def verify_is_focused(self):
        logging.info('Verify %s is focused' % self.name)
        if not self.is_focused():
            pytest.fail('%s is not focused' % self.name)


class TextElement(object):
    def __init__(self, text):
        self.text = text
        self.element_line = None

    def find_element(self):
        for _ in range(3):
            lines = collectLines()
            for line in lines:
                if self.text in line.getText().encode('ascii', 'ignore'):
                    self.element_line = line
                    return
            time.sleep(3)
        pytest.fail("Element with text '%s' was not found" % self.text)

    def click(self):
        logging.info("Click %s button" % self.text)
        self.find_element()
        self.element_line.click()

    def get_whole_text(self):
        self.find_element()
        return self.element_line.getText().encode('ascii', 'ignore')

    def is_visible(self):
        from _pytest.runner import Failed
        try:
            self.find_element()
            return True
        except Failed:
            return False

    def verify_element_is_not_present(self):
        if self.is_visible():
            pytest.fail("'%s text is displayed but not expected'" % self.text)
