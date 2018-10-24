from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestCreateAccount(BaseTestCase):

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
