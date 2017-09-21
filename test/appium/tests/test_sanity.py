import pytest
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView
from tests.preconditions import set_password_as_new_user


@pytest.mark.sanity
class TestSanity(SingleDeviceTestCase):

    def test_transaction(self):
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        chats.wait_for_syncing_complete()
        chats.back_button.click()
        chats.profile_button.click()
        profile = chats.profile_icon.click()

        sender_address = profile.profile_address_text.text
        recipient_key = '0x040e016b940e067997be8d91298d893ff2bc3580504b4ccb155ea03d183b85f1' \
                        '8e771a763d99f60fec70edf637eb6bad9f96d3e8a544168d3ad144f83b4cf7625c'
        recipient_address = '67a50ef1d26de6d65dbfbb88172ac1e7017e766d'

        profile.get_donate(sender_address)
        initial_balance = profile.get_balance(recipient_address)

        profile.back_button.click()
        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        chats.send_funds_button.click()
        chats.first_recipient_button.click()
        chats.send_int_as_keyevent(0)
        chats.send_dot_as_keyevent()
        chats.send_int_as_keyevent(1)
        chats.send_message_button.click()
        chats.confirm_transaction_button.click()
        chats.password_input.send_keys('qwerty1234')
        chats.confirm_button.click()
        chats.got_it_button.click()
        chats.find_full_text('0.1')
        chats.find_full_text('Sent', 60)
        chats.verify_balance_is_updated(initial_balance, recipient_address)

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

    @pytest.mark.parametrize("verification", ["short", "mismatch", "valid"])
    def test_password(self, verification):

        verifications = {"short":
                             {"input": "qwe1",
                              "outcome": "Password should be not less then 6 symbols."},
                         "valid":
                             {"input": "qwerty1234",
                              "outcome": "Tap here to enter your phone number & I\'ll find your friends"},
                         "mismatch":
                             {"input": "mismatch1234",
                              "outcome": "Password confirmation doesn\'t match password."}}
        home = HomeView(self.driver)
        home.request_password_icon.click()
        home.type_message_edit_box.send_keys(verifications[verification]["input"])
        home.confirm()
        if 'short' not in verification:
            home.type_message_edit_box.send_keys(verifications["valid"]["input"])
            home.confirm()
        home.find_full_text(verifications[verification]["outcome"])
