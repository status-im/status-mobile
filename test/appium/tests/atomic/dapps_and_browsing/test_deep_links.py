from selenium.common.exceptions import NoSuchElementException

from tests import marks, test_dapp_url
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user, ens_user
from views.sign_in_view import SignInView


class TestDeepLinks(SingleDeviceTestCase):

    @marks.testrail_id(5396)
    @marks.high
    def test_open_public_chat_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        chat_name = sign_in_view.get_random_chat_name()
        deep_link = 'status-im://%s' % chat_name
        sign_in_view.open_weblink_and_login(deep_link)
        chat_view = sign_in_view.get_chat_view()
        try:
            assert chat_view.user_name_text.text == '#' + chat_name
        except (AssertionError, NoSuchElementException):
            self.driver.fail("Public chat '%s' is not opened" % chat_name)

    @marks.testrail_id(5441)
    @marks.medium
    def test_open_user_profile_using_deep_link(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        for user_ident in ens_user['ens'], ens_user['ens_another'], ens_user['public_key']:
            self.driver.close_app()
            deep_link = 'status-im://u/%s' % user_ident
            sign_in.open_weblink_and_login(deep_link)
            chat = sign_in.get_chat_view()
            for text in ens_user['username'], sign_in.get_translation_by_key("add-to-contacts"):
                if not chat.element_by_text(text).scroll_to_element(10):
                    self.driver.fail("User profile screen is not opened")

    @marks.testrail_id(5442)
    @marks.medium
    def test_open_dapp_using_deep_link(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        self.driver.close_app()
        dapp_name = test_dapp_url
        dapp_deep_link = 'status-im://b/%s' % dapp_name
        sign_in_view.open_weblink_and_login(dapp_deep_link)
        web_view = sign_in_view.get_chat_view()
        try:
            test_dapp_view = web_view.open_in_status_button.click()
            test_dapp_view.allow_button.is_element_present()
        except NoSuchElementException:
            self.driver.fail("DApp '%s' is not opened!" % dapp_name)

    @marks.testrail_id(5781)
    @marks.medium
    def test_deep_link_with_invalid_user_public_key_own_profile_key(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=basic_user['passphrase'])
        self.driver.close_app()

        sign_in_view.just_fyi('Check that no error when opening invalid deep link')
        deep_link = 'status-im://u/%s' % basic_user['public_key'][:-10]
        sign_in_view.open_weblink_and_login(deep_link)
        home_view = sign_in_view.get_home_view()
        home_view.plus_button.click_until_presence_of_element(home_view.start_new_chat_button)
        if not home_view.start_new_chat_button.is_element_present():
            self.errors.append(
                "Can't navigate to start new chat after app opened from deep link with invalid public key")
        self.driver.close_app()

        sign_in_view.just_fyi('Check that no error when opening invalid deep link')
        deep_link = 'status-im://u/%s' % basic_user['public_key']
        sign_in_view.open_weblink_and_login(deep_link)
        home_view.plus_button.click_until_presence_of_element(home_view.start_new_chat_button)
        if not home_view.start_new_chat_button.is_element_present():
            self.errors.append("Can't navigate to start new chat after app opened from deep link with own public key")
        self.errors.verify_no_errors()
