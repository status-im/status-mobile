import pytest
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView
from tests.preconditions import set_password_as_new_user, recover_access
from tests import basic_user, transaction_users


@pytest.mark.sanity
class TestSanity(SingleDeviceTestCase):

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
        home.find_full_text('Chats', 10)

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

    @pytest.mark.parametrize("test, recipient, sender", [('group_chat', 'A_USER', 'B_USER'),
                                                         ('one_to_one_chat', 'B_USER', 'A_USER')],
                             ids=['group_chat', 'one_to_one_chat'])
    def test_send_transaction(self, test, recipient, sender):
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        recover_access(chats,
                       transaction_users[sender]['passphrase'],
                       transaction_users[sender]['password'],
                       transaction_users[sender]['username'])
        chats.wait_for_syncing_complete()

        sender_address = transaction_users[sender]['address']
        recipient_address = transaction_users[recipient]['address']
        recipient_key = transaction_users[recipient]['public_key']
        initial_balance_recipient = chats.get_balance(recipient_address)

        if chats.get_balance(sender_address) < 1000000000000000000:
            chats.get_donate(sender_address)

        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        if test == 'group_chat':
            user_name = chats.user_name_text.text
            chats.back_button.click()
            chats.new_group_chat_button.click()
            user_contact = chats.element_by_text(user_name, 'button')
            user_contact.scroll_to_element()
            user_contact.click()
            chats.next_button.click()
            chats.name_edit_box.send_keys('chat_send_transaction')
            chats.save_button.click()

        chats.send_funds_button.click()
        chats.first_recipient_button.click()
        chats.send_int_as_keyevent(0)
        chats.send_dot_as_keyevent()
        chats.send_int_as_keyevent(1)
        chats.send_message_button.click()
        chats.confirm_transaction_button.wait_for_element(60)
        chats.confirm_transaction_button.click()
        chats.password_input.send_keys(transaction_users[sender]['password'])
        chats.confirm_button.click()
        chats.got_it_button.click()

        chats.find_full_text('0.1')
        chats.find_full_text('Sent', 60)
        if test == 'group_chat':
            chats.find_full_text('to  ' + transaction_users[recipient]['username'], 60)
        chats.verify_balance_is_updated(initial_balance_recipient, recipient_address)
