from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView
import pytest


class TestCreateAccount(BaseTestCase):

    @pytest.mark.testrail_id(5565)
    def test_create_account(self):
        sign_in = SignInView()
        sign_in.create_account_button.click()
        sign_in.create_password_input.input_value('123456')
        sign_in.next_button.click()
        sign_in.confirm_password_input.input_value('123456')
        sign_in.next_button.click()
        sign_in.username_input.input_value('test')
        sign_in.next_button.click()
        sign_in.home_button.find_element()

    @pytest.mark.testrail_id(5570)
    def test_create_account_proceed_with_enter(self):
        sign_in = SignInView()
        sign_in.create_account_button.click()
        sign_in.create_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.confirm_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.username_input.input_value('test')
        sign_in.press_enter()
        sign_in.home_button.find_element()

    @pytest.mark.testrail_id(5568)
    def test_create_account_go_back(self):
        sign_in = SignInView()
        sign_in.create_account_button.click()
        sign_in.create_password_input.find_element()
        sign_in.back_button.click()
        sign_in.create_account_button.click()
        sign_in.create_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.confirm_password_input.find_element()
        sign_in.back_button.click()
        sign_in.create_password_input.find_element()
        sign_in.create_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.confirm_password_input.input_value('123456')
        sign_in.press_enter()
        sign_in.username_input.find_element()
        sign_in.back_button.verify_element_is_not_present()

    @pytest.mark.testrail_id(5567)
    def test_switch_accounts(self):
        sign_in = SignInView()
        sign_in.create_account(username='user_1')
        profile = sign_in.profile_button.click()
        profile.log_out_button.click()
        sign_in.other_accounts_button.click()
        sign_in.create_account(username='user_2')
        sign_in.profile_button.click()
        profile.log_out_button.click()
        sign_in.other_accounts_button.click()
        sign_in.element_by_text('user_2').click()
        sign_in.password_input.input_value('qwerty')
        sign_in.press_enter()
        sign_in.profile_button.click()
        profile.find_text('user_2')

    @pytest.mark.testrail_id(5650)
    def test_status_log(self):
        sign_in = SignInView()
        sign_in.create_account()
        with open('/root/.local/share/Status/Status.log') as f:
            if 'mnemonic' in f.read():
                pytest.fail("'mnemonic' is in Status.log!")
