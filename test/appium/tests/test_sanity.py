import pytest
from tests import transaction_users, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from views.console_view import ConsoleView
from tests import basic_user


@pytest.mark.all
class TestSanity(SingleDeviceTestCase):

    @pytest.mark.profile
    def test_change_user_name(self):
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        profile_view = console_view.profile_button.click()
        profile_view.edit_button.click()
        profile_view.username_input.clear()
        new_username = 'NewUserName!'
        profile_view.username_input.send_keys(new_username)
        profile_view.confirm_button.click()
        sign_in_view = profile_view.logout_button.click()
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.send_keys('qwerty1234')
        home_view = sign_in_view.sign_in_button.click()
        home_view.find_full_text('Wallet', 60)
        home_view.profile_button.click()
        profile_view.edit_button.click()
        profile_view.find_full_text(new_username, 5)

    @pytest.mark.recover
    def test_recover_access(self):
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        profile_view = console_view.profile_button.click()
        sign_in_view = profile_view.logout_button.click()
        recover_access_view = sign_in_view.recover_access_button.click()
        recover_access_view.passphrase_input.send_keys(basic_user['passphrase'])
        recover_access_view.password_input.send_keys(basic_user['password'])
        recover_access_view.confirm_recover_access.click()
        recovered_user = sign_in_view.element_by_text(basic_user['username'], 'button')
        recovered_user.click()
        sign_in_view.password_input.send_keys(basic_user['password'])
        sign_in_view.sign_in_button.click()
        console_view.find_full_text('Wallet', 60)
        if basic_user['password'] in str(console_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.gorup_chat
    def test_group_chat_members(self):
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        home_view = console_view.get_home_view()

        users = [transaction_users_wallet['A_USER'], transaction_users_wallet['B_USER'],
                 transaction_users['A_USER'], transaction_users['B_USER'], basic_user]
        user_names = sorted([user['username'] for user in users])

        for user in users:
            home_view.add_contact(user['public_key'])
            console_view.back_button.click(2)
        home_view.create_group_chat(sorted([user['username'] for user in users]))
        group_chat = home_view.get_chat_view()
        group_chat.chat_options.click()
        group_chat.chat_settings.click()
        group_chat.confirm()
        group_chat.more_users_button.click()
        for username in user_names:
            group_chat.find_full_text(username, 10)

    def test_commands_on_second_app_run(self):
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        home_view = console_view.get_home_view()
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.add_new_contact.click()
        contact_jarrad = home_view.element_by_text('Jarrad', 'button')
        contact_jarrad.scroll_to_element()
        contact_jarrad.click()
        chat_view = home_view.get_chat_view()

        commands = '/request', '/send'

        for command in commands:
            chat_view.find_full_text(command, 2)
        self.driver.close_app()
        console_view.apps_button.click()
        console_view.status_app_icon.scroll_to_element()
        console_view.status_app_icon.click()
        console_view.ok_button_apk.click()
        sign_in_view = console_view.get_sign_in_view()
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        contact_jarrad.wait_for_element(30)
        contact_jarrad.click()
        for command in commands:
            chat_view.find_full_text(command, 2)
        chat_view.back_button.click()
        home_view.create_group_chat(['Jarrad'])

    @pytest.mark.sign_in
    @pytest.mark.parametrize("verification", ["invalid", "valid"])
    def test_sign_in(self, verification):
        verifications = {"valid":
                             {"input": "qwerty1234",
                              "outcome": "Wallet"},
                         "invalid":
                             {"input": "12345ewq",
                              "outcome": "Wrong password"}}
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        profile_view = console_view.profile_button.click()
        profile_view.logout_button.scroll_to_element()
        sign_in_view = profile_view.logout_button.click()
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.send_keys(verifications[verification]['input'])
        sign_in_view.sign_in_button.click()
        sign_in_view.find_full_text(verifications[verification]["outcome"], 60)
        if verifications[verification]["input"] in str(console_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.password
    @pytest.mark.parametrize("verification", ["logcat", "mismatch", "short"])
    def test_password(self, verification):
        verifications = {
            "short": {"input": "qwe1",
                      "outcome": "Password should be not less then 6 symbols."},

            "mismatch": {"input": "mismatch1234",
                         "outcome": "Password confirmation doesn\'t match password."},

            "logcat": {"input": "new_unique_password",
                       "outcome": "Here is your signing phrase. "
                                  "You will use it to verify your transactions. "
                                  "Write it down and keep it safe!"}}
        console = ConsoleView(self.driver)
        console.request_password_icon.click()
        console.chat_request_input.send_keys(verifications[verification]["input"])
        console.confirm()
        if 'short' not in verification:
            console.chat_request_input.send_keys("new_unique_password")
            console.confirm()
        console.find_full_text(verifications[verification]["outcome"])
        if verifications[verification]["input"] in str(console.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)
