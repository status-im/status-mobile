import pytest
from tests import transaction_users, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from tests import basic_user
from views.sign_in_view import SignInView


@pytest.mark.all
class TestSanity(SingleDeviceTestCase):

    @pytest.mark.profile
    def test_change_user_name(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.edit_button.click()
        profile_view.username_input.clear()
        new_username = 'NewUserName!'
        profile_view.username_input.send_keys(new_username)
        profile_view.confirm_button.click()
        profile_view.relogin()
        sign_in_view.profile_button.click()
        profile_view.edit_button.click()
        profile_view.find_full_text(new_username, 5)

    @pytest.mark.recover
    def test_recover_access(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.logout()
        recover_access_view = sign_in_view.add_existing_account_button.click()
        recover_access_view.passphrase_input.send_keys(basic_user['passphrase'])
        recover_access_view.password_input.send_keys(basic_user['password'])
        home_view = recover_access_view.sign_in_button.click()
        home_view.find_full_text('Wallet', 60)
        if basic_user['password'] in str(home_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.group_chat
    def test_group_chat_members(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()

        users = [transaction_users_wallet['A_USER'], transaction_users_wallet['B_USER'],
                 transaction_users['A_USER'], transaction_users['B_USER'], basic_user]
        user_names = sorted([user['username'] for user in users])

        for user in users:
            home_view.add_contact(user['public_key'])
            home_view.back_button.click(2)
        home_view.create_group_chat(sorted([user['username'] for user in users]))
        group_chat = home_view.get_chat_view()
        group_chat.chat_options.click()
        group_chat.chat_settings.click()
        group_chat.confirm()
        group_chat.more_users_button.click()
        for username in user_names:
            group_chat.find_full_text(username, 10)

    def test_commands_on_second_app_run(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        contact_user = basic_user
        home_view.add_contact(contact_user['public_key'])
        home_view.get_back_to_home_view()
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.start_new_chat_button.click()
        contact_name = home_view.element_by_text(contact_user['username'], 'button')
        contact_name.scroll_to_element()
        contact_name.click()
        chat_view = home_view.get_chat_view()

        commands = '/request', '/send'

        for command in commands:
            chat_view.find_full_text(command, 2)
        self.driver.close_app()
        sign_in_view.apps_button.click()
        sign_in_view.status_app_icon.scroll_to_element()
        sign_in_view.status_app_icon.click()
        sign_in_view.ok_button.click()
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        contact_name.wait_for_element(30)
        contact_name.click()
        for command in commands:
            chat_view.find_full_text(command, 2)
        chat_view.back_button.click()
        home_view.create_group_chat([contact_user['username']])

    @pytest.mark.sign_in
    @pytest.mark.parametrize("input_text,outcome",
                             [("qwerty1234", "Wallet"), ("12345ewq", "Wrong password")],
                             ids=['Sign on with valid password', 'Sign in with wrong password'])
    def test_sign_in(self, input_text, outcome):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.logout_button.scroll_to_element()
        sign_in_view = profile_view.logout_button.click()
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.send_keys(input_text)
        sign_in_view.sign_in_button.click()
        sign_in_view.find_full_text(outcome, 60)
        if input_text in str(sign_in_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.password
    def test_password_logcat(self):
        password = 'qwerty1234'
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user(password=password)
        sign_in_view.home_button.wait_for_visibility_of_element()
        if password in str(sign_in_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.password
    def test_password_mismatch(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_account_button.click()
        sign_in_view.password_input.send_keys('password1')
        sign_in_view.next_button.click()
        sign_in_view.confirm_password_input.send_keys("password2")
        sign_in_view.next_button.click()
        sign_in_view.find_full_text('Password confirmation doesn\'t match password.')
        logcat = str(sign_in_view.logcat)
        if 'password1' in logcat or 'password2' in logcat:
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.password
    def test_password_too_short(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_account_button.click()
        sign_in_view.password_input.send_keys('qwe1')
        sign_in_view.next_button.click()
        sign_in_view.password_input.find_element()
        assert not sign_in_view.confirm_password_input.is_element_present()
