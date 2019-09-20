from selenium.common.exceptions import NoSuchElementException

from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


class TestDeepLinks(SingleDeviceTestCase):

    @marks.testrail_id(5396)
    @marks.high
    def test_open_public_chat_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        chat_name = sign_in_view.get_public_chat_name()
        deep_link = 'https://get.status.im/chat/public/%s' % chat_name
        sign_in_view.open_weblink_and_login(deep_link)
        chat_view = sign_in_view.get_chat_view()
        try:
            assert chat_view.user_name_text.text == '#' + chat_name
        except (AssertionError, NoSuchElementException):
            self.driver.fail("Public chat '%s' is not opened" % chat_name)

    @marks.testrail_id(5441)
    @marks.medium
    def test_open_user_profile_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        deep_link = 'https://get.status.im/user/%s' % basic_user['public_key']
        sign_in_view.open_weblink_and_login(deep_link)
        chat_view = sign_in_view.get_chat_view()
        for text in basic_user['username'], 'Add to contacts':
            if not chat_view.element_by_text(text).scroll_to_element(10):
                self.driver.fail("User profile screen is not opened")

    @marks.testrail_id(5442)
    @marks.medium
    def test_open_dapp_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        dapp_name = 'status-im.github.io/dapp'
        dapp_deep_link = 'https://get.status.im/browse/%s' % dapp_name
        sign_in_view.open_weblink_and_login(dapp_deep_link)
        web_view = sign_in_view.get_chat_view()
        try:
            test_dapp_view = web_view.open_in_status_button.click()
            test_dapp_view.allow_button.is_element_present()
        except NoSuchElementException:
            self.driver.fail("DApp '%s' is not opened!" % dapp_name)

    @marks.testrail_id(5780)
    @marks.medium
    def test_open_own_user_profile_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=basic_user['passphrase'])
        self.driver.close_app()
        deep_link = 'https://get.status.im/user/%s' % basic_user['public_key']
        sign_in_view.open_weblink_and_login(deep_link)
        profile_view = sign_in_view.get_profile_view()
        if profile_view.default_username_text.text != basic_user['username'] \
                or not profile_view.contacts_button.is_element_displayed() \
                or not profile_view.share_my_profile_button.is_element_displayed():
            self.driver.fail("Own profile screen is not opened!")

    @marks.testrail_id(5781)
    @marks.medium
    def test_deep_link_with_invalid_user_public_key(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        deep_link = 'https://get.status.im/user/%s' % basic_user['public_key'][:-10]
        sign_in_view.open_weblink_and_login(deep_link)
        home_view = sign_in_view.get_home_view()
        home_view.plus_button.click_until_presence_of_element(home_view.start_new_chat_button)
        if not home_view.start_new_chat_button.is_element_present():
            self.driver.fail("Can't navigate to start new chat after app opened from deep link with invalid public key")
