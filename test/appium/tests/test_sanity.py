import pytest
import time
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView
from tests.preconditions import set_password_as_new_user
from tests import basic_user


@pytest.mark.all
class TestAccess(SingleDeviceTestCase):

    @pytest.mark.profile
    def test_change_profile_name_and_status(self):
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        chats.back_button.click()
        chats.profile_button.click()
        profile = chats.profile_icon.click()

        new_status = '#newstatus'
        new_username = 'NewUserName!'

        profile.user_status_box.click()
        profile.user_status_input.clear()
        profile.user_status_input.send_keys(new_status)
        profile.username_input.clear()
        profile.username_input.send_keys(new_username)
        profile.save_button.click()
        profile.back_button.click()

        chats.profile_button.click()
        login = chats.switch_users_button.click()
        user = login.element_by_text(new_username, 'button')
        user.click()
        login.password_input.send_keys('qwerty1234')
        login.sign_in_button.click()
        home.find_full_text('Chats', 60)
        chats.profile_button.click()
        for text in new_status + ' ', new_username:
            chats.find_full_text(text, 5)

    @pytest.mark.recover
    def test_recover_access(self):
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        chats.back_button.click()
        chats.profile_button.click()
        login = chats.switch_users_button.click()
        login.recover_access_button.click()
        login.passphrase_input.send_keys(basic_user['passphrase'])
        login.password_input.send_keys(basic_user['password'])
        login.confirm_recover_access.click()
        recovered_user = login.element_by_text(basic_user['username'], 'button')
        recovered_user.click()
        login.password_input.send_keys(basic_user['password'])
        login.sign_in_button.click()
        home.find_full_text('Chats', 60)
        if basic_user['password'] in str(home.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @pytest.mark.sign_in
    @pytest.mark.parametrize("verification", ["invalid", "valid"])
    def test_sign_in(self, verification):

        verifications = {"valid":
                             {"input": "qwerty1234",
                              "outcome": "Chats"},
                         "invalid":
                             {"input": "12345ewq",
                              "outcome": "Wrong password"}}
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        chats.back_button.click()
        chats.profile_button.click()
        login = chats.switch_users_button.click()
        login.first_account_button.click()
        login.password_input.send_keys(verifications[verification]['input'])
        login.sign_in_button.click()
        home.find_full_text(verifications[verification]["outcome"], 60)
        if verifications[verification]["input"] in str(home.logcat):
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
        home = HomeView(self.driver)
        home.request_password_icon.click()
        home.chat_request_input.send_keys(verifications[verification]["input"])
        home.confirm()
        if 'short' not in verification:
            home.chat_request_input.send_keys("new_unique_password")
            home.confirm()
        home.find_full_text(verifications[verification]["outcome"])
        if verifications[verification]["input"] in str(home.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)
