import pytest

from utilities import passpharse_with_spaces
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

    @pytest.mark.testrail_id(5571)
    def test_recover_access_proceed_with_enter(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_keys(base_user['passphrase'])
        sign_in.press_tab()
        sign_in.recover_password_input.verify_is_focused()
        sign_in.recover_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.home_button.find_element()

    @pytest.mark.testrail_id(5569)
    def test_recover_access_go_back(self):
        sign_in = SignInView()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.find_element()
        sign_in.back_button.click()
        sign_in.create_account_button.find_element()

    @pytest.mark.testrail_id(5649)
    def test_recovery_phrase_for_recovered_account(self):
        sign_in = SignInView()
        sign_in.recover_access(base_user['passphrase'])
        profile = sign_in.profile_button.click()
        if profile.share_my_code_button.is_visible():
            profile.element_by_text('Backup').verify_element_is_not_present()
        else:
            pytest.fail('Profile view was not opened')

    @pytest.mark.testrail_id(5652)
    def test_recover_account_error_messages(self):
        errors = {'': 'Required field',
                  ' '.join(base_user['passphrase'].split()[::-1]): 'Some words might be misspelled',
                  ' '.join(base_user['passphrase'].split()[:-1]) + ' aaa': 'Some words might be misspelled',
                  'robot seed robot seed robot seed robot seed robot seed robot seed.': 'Recovery phrase is invalid',
                  'robot': 'Recovery phrase is invalid',
                  'seed seed seed seed seed seed seed seed seed seed seed': 'Recovery phrase is invalid',
                  'seed seed seed seed seed seed seed seed seed seed seed seed seed': 'Recovery phrase is invalid'
                  }
        sign_in = SignInView()
        for i in errors:
            sign_in.i_have_account_button.click()
            sign_in.recovery_phrase_input.send_keys(i)
            sign_in.recover_password_input.click()
            error_message = errors[i]
            if not sign_in.element_by_text(error_message).is_visible():
                self.errors.append("Error message '%s' is not shown for passphrase '%s'" % (error_message, i))
            sign_in.back_button.click()
        sign_in.i_have_account_button.click()
        sign_in.recovery_phrase_input.send_keys(base_user['passphrase'])
        sign_in.recover_password_input.click()
        sign_in.sign_in_button.click()
        if not sign_in.element_by_text('Required field').is_visible():
            self.errors.append("Error message 'Required field' is not shown for empty password input")
        self.verify_no_errors()

    @pytest.mark.testrail_id(5651)
    def test_recover_account_with_spaces(self):
        sign_in = SignInView()
        passphrase = passpharse_with_spaces(base_user['passphrase'])
        sign_in.recover_access(passphrase)
        profile = sign_in.profile_button.click()
        profile.find_text(base_user['username'])
