import pytest
from selenium.common.exceptions import NoSuchElementException

from support.device_apps import start_web_browser
from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestDeepLinks(SingleDeviceTestCase):

    @marks.testrail_id(5396)
    @marks.high
    def test_open_public_chat_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        start_web_browser(self.driver)
        chat_name = sign_in_view.get_public_chat_name()
        sign_in_view.send_as_keyevent('https://get.status.im/chat/public/%s' % chat_name)
        sign_in_view.confirm()
        open_button = sign_in_view.element_by_xpath('//*[@text="Open in Status"] | //*[@content-desc="Open in Status"]')
        open_button.wait_for_visibility_of_element()
        open_button.click()
        sign_in_view.sign_in()
        chat_view = sign_in_view.get_chat_view()
        try:
            assert chat_view.user_name_text.text == '#' + chat_name
        except (AssertionError, NoSuchElementException):
            pytest.fail("Public chat '%s' is not opened" % chat_name)
