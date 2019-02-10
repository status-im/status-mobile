from tests import base_user
from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView
import pytest


class TestProfile(BaseTestCase):

    @pytest.mark.testrail_id(5590)
    def test_copy_contact_code(self):
        sign_in = SignInView()
        sign_in.recover_access(base_user['passphrase'])
        profile = sign_in.profile_button.click()
        profile.share_my_code_button.click()
        profile.copy_code_button.click()
        if profile.get_clipboard() != base_user['public_key']:
            pytest.fail('Contact code was not copied to clipboard')

    @pytest.mark.skip('Test cases is not ready yet')
    def test_change_mail_server(self):
        sign_in = SignInView()
        sign_in.create_account()
        profile = sign_in.profile_button.click()
        profile.element_by_text('Advanced settings').click()
        s_names = profile.get_mail_servers_list()
        profile.get_mail_server(s_names[0]).is_active()

    @pytest.mark.testrail_id(5593)
    def test_log_out(self):
        sign_in = SignInView()
        username = 'test_log_out'
        sign_in.create_account(username=username)
        profile = sign_in.profile_button.click()
        profile.log_out_button.click()
        sign_in.password_input.find_element()
        for text in username, 'Sign in to Status', 'Other accounts':
            if not sign_in.element_by_text(text).is_visible():
                pytest.fail("Text '%s' is not shown" % text)

    @pytest.mark.skip
    # @pytest.mark.testrail_id(5648)
    def test_back_up_recovery_phrase(self):
        sign_in = SignInView()
        sign_in.create_account()
        profile = sign_in.profile_button.click()
        profile.back_up_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()
        profile.next_button.click()
        word_number = profile.get_recovery_phrase_word_number()
        profile.recovery_phrase_word_input.input_value(recovery_phrase[word_number])
        profile.next_button.click()
        word_number_1 = profile.get_recovery_phrase_word_number()
        profile.recovery_phrase_word_input.input_value(recovery_phrase[word_number_1])
        profile.done_button.click()
        profile.yes_button.click()
        profile.find_text("You're all set!")
