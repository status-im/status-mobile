import org.sikuli.script.SikulixForJython
from sikuli import *

from tests import base_user
from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestRecoverAccess(BaseTestCase):

    def test_recover_access(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_as_key_event(base_user['passphrase'])
        sign_in.recover_password_input.send_as_key_event('123456')
        sign_in.sign_in_button.click()
        sign_in.home_button.find_element()
        profile = sign_in.profile_button.click()
        profile.share_my_code_button.click()
        profile.find_text(base_user['public_key'])

    def test_recover_access_proceed_with_enter(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_as_key_event(base_user['passphrase'] + Key.TAB)
        type('123456' + Key.ENTER)
        sign_in.home_button.find_element()

    def test_recover_access_go_back(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.find_element()
        sign_in.back_button.click()
        sign_in.create_account_button.find_element()
