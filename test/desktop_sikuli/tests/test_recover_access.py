from tests import base_user
from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestRecoverAccess(BaseTestCase):

    def test_recover_access(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_keys(base_user['passphrase'])
        sign_in.recover_password_input.send_keys('123456')
        sign_in.sign_in_button.click()
        sign_in.home_button.find_element()
        profile = sign_in.profile_button.click()
        profile.share_my_code_button.click()
        profile.find_text(base_user['public_key'])

    def test_recover_access_proceed_with_enter(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_keys(base_user['passphrase'])
        sign_in.press_tab()
        sign_in.recover_password_input.verify_is_focused()
        sign_in.recover_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.home_button.find_element()

    def test_recover_access_go_back(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.find_element()
        sign_in.back_button.click()
        sign_in.create_account_button.find_element()
