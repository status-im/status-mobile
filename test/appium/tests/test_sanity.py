import pytest
import time
from tests.base_test_case import SingleDeviceTestCase
from views.console_view import ConsoleView
from tests import user_flow
from tests import basic_user


@pytest.mark.all
class TestSanity(SingleDeviceTestCase):

    @pytest.mark.sign_in
    @pytest.mark.parametrize("verification", ["invalid", "valid"])
    def test_sign_in(self, verification):
        verifications = {"valid":
                             {"input": "qwerty1234",
                              "outcome": "Chats"},
                         "invalid":
                             {"input": "12345ewq",
                              "outcome": "Wrong password"}}
        console_view = ConsoleView(self.driver)
        user_flow.create_user(console_view)
        chats_view = console_view.get_chat_view()
        chats_view.back_button.click()
        profile_drawer = chats_view.profile_button.click()
        sign_in_view = profile_drawer.switch_users_button.click()
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.send_keys(verifications[verification]['input'])
        sign_in_view.sign_in_button.click()
        console_view.find_full_text(verifications[verification]["outcome"], 60)
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

    @pytest.mark.profile
    def test_change_profile_name_and_status(self):
        new_status = '#newstatus'
        new_username = 'NewUserName!'
        console_view = ConsoleView(self.driver)
        user_flow.create_user(console_view)
        chats_view = console_view.get_chat_view()
        chats_view.back_button.click()
        profile_drawer = chats_view.profile_button.click()
        profile_view = profile_drawer.profile_icon.click()
        profile_view.user_status_box.click()
        profile_view.user_status_input.clear()
        profile_view.user_status_input.send_keys(new_status)
        profile_view.username_input.clear()
        profile_view.username_input.send_keys(new_username)
        profile_view.save_button.click()
        profile_view.back_button.click()
        chats_view.profile_button.click()
        sign_in_view = profile_drawer.switch_users_button.click()
        user = sign_in_view.element_by_text(new_username, 'button')
        user.click()
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        chats_view.find_full_text('Chats', 60)
        chats_view.profile_button.click()
        for text in new_status + ' ', new_username:
            chats_view.find_full_text(text, 5)

    @pytest.mark.recover
    def test_recover_access(self):
        console_view = ConsoleView(self.driver)
        user_flow.create_user(console_view)
        chats_view = console_view.get_chat_view()
        chats_view.back_button.click()
        profile_drawer = chats_view.profile_button.click()
        sign_in_view = profile_drawer.switch_users_button.click()
        recover_access_view = sign_in_view.recover_access_button.click()
        recover_access_view.passphrase_input.send_keys(basic_user['passphrase'])
        recover_access_view.password_input.send_keys(basic_user['password'])
        recover_access_view.confirm_recover_access.click()
        recovered_user = sign_in_view.element_by_text(basic_user['username'], 'button')
        recovered_user.click()
        sign_in_view.password_input.send_keys(basic_user['password'])
        sign_in_view.sign_in_button.click()
        console_view.find_full_text('Chats', 60)
        if basic_user['password'] in str(console_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)
