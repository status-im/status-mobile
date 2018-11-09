import logging
try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass
import os
import pytest

from views.base_element import BaseElement, TextElement

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/base_view')


class ProfileButton(BaseElement):
    def __init__(self):
        super(ProfileButton, self).__init__(IMAGES_PATH + '/profile_button.png')

    def click(self, log=True):
        super(ProfileButton, self).click(log=log)
        from views.profile_view import ProfileView
        return ProfileView()


class BaseView(object):

    def __init__(self):
        super(BaseView, self).__init__()
        self.home_button = BaseElement(IMAGES_PATH + '/home_button.png')
        self.profile_button = ProfileButton()
        self.back_button = BaseElement(IMAGES_PATH + '/back_button.png')

    def find_text(self, expected_text):
        logging.info("Find text '%s'" % expected_text)
        for _ in range(3):
            current_text = text().encode('ascii', 'ignore').replace('\n', ' ')
            if expected_text in current_text:
                return
        pytest.fail("Could not find text '%s'" % expected_text)

    def get_clipboard(self):
        return Env.getClipboard()

    def element_by_text(self, text):
        return TextElement(text)

    def press_enter(self):
        logging.info('Press Enter button')
        type(Key.ENTER)

    def press_tab(self):
        logging.info('Press Tab button')
        type(Key.TAB)
