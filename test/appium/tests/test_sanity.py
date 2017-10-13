import pytest
import time
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView
from tests.preconditions import set_password_as_new_user
from tests import basic_user


@pytest.mark.all
class TestAccess(SingleDeviceTestCase):

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
        home.find_full_text(verifications[verification]["outcome"], 10)

    @pytest.mark.parametrize("verification", ["short", "mismatch"])
    def test_password(self, verification):
        verifications = {"short":
                             {"input": "qwe1",
                              "outcome": "Password should be not less then 6 symbols."},
                         "mismatch":
                             {"input": "mismatch1234",
                              "outcome": "Password confirmation doesn\'t match password."}}
        home = HomeView(self.driver)
        home.request_password_icon.click()
        home.chat_request_input.send_keys(verifications[verification]["input"])
        home.confirm()
        if 'short' not in verification:
            home.chat_request_input.send_keys("qwerty1234")
            home.confirm()
        home.find_full_text(verifications[verification]["outcome"])
