import pytest
from selenium.common.exceptions import NoSuchElementException

from apis.ropsten_api import get_balance, verify_balance_is_updated
from tests.basetestcase import MultiplyDeviceTestCase
from tests.preconditions import set_password_as_new_user, change_user_details, recover_access
from tests import transaction_users
from views.base_view import verify_transaction_in_ropsten
from views.chats import get_unique_amount
from views.home import HomeView


@pytest.mark.all
class TestMultiplyDevices(MultiplyDeviceTestCase):
    @pytest.mark.discover
    def test_new_profile_name_and_status_on_discover(self):
        device_1, device_2 = HomeView(self.driver_1), HomeView(self.driver_2)
        set_password_as_new_user(device_1, device_2)
        device_1.back_button.click()
        device_2.back_button.click()

        chats_d1, chats_d2 = device_1.get_chats(), device_2.get_chats()
        chats_d2.profile_button.click()
        profile_d2 = chats_d2.profile_icon.click()
        d2_public_key = profile_d2.public_key_text.text

        chats_d1.plus_button.click()
        chats_d1.add_new_contact.click()
        chats_d1.public_key_edit_box.send_keys(d2_public_key)
        chats_d1.confirm()
        chats_d1.confirm_public_key_button.click()
        chats_d1.chat_message_input.send_keys('test123')
        chats_d1.send_message_button.click()
        chats_d2.back_button.click()

        new_chat_d2 = chats_d2.element_by_text('test123', 'button')
        new_chat_d2.click()

        for _ in range(3):
            chats_d1.back_button.click()
        chats_d2.add_to_contacts.click()
        chats_d2.back_button.click()
        chats_d1.profile_button.click()
        chats_d2.profile_button.click()

        profile_d1, profile_d2 = chats_d1.profile_icon.click(), chats_d2.profile_icon.click()
        users_details = change_user_details(profile_d1, profile_d2)
        profile_d1.back_button.click()
        profile_d2.back_button.click()
        discover_d1 = profile_d1.discover_button.click()
        discover_d2 = profile_d2.discover_button.click()
        for device in discover_d1, discover_d2:
            device.all_popular.click()
            for k in users_details:
                device.find_full_text(users_details[k]['name'])
                device.find_full_text(' ' + users_details[k]['status'])
            device.back_button.click()
            device.all_recent.click()
            for k in users_details:
                device.find_full_text(users_details[k]['name'])
                device.find_full_text(users_details[k]['status'] + ' ')

    @pytest.mark.chat
    def test_one_to_one_chat(self):

        device_1, device_2 = HomeView(self.driver_1), HomeView(self.driver_2)
        set_password_as_new_user(device_1, device_2)
        device_1.back_button.click()

        chats_d1 = device_1.get_chats()
        chats_d1.profile_button.click()
        profile_d1 = chats_d1.profile_icon.click()
        key = profile_d1.public_key_text.text
        device_2.back_button.click()

        chats_d2 = device_2.get_chats()
        chats_d2.plus_button.click()
        chats_d2.add_new_contact.click()
        chats_d2.public_key_edit_box.send_keys(key)
        chats_d2.confirm()
        chats_d2.confirm_public_key_button.click()

        message_1 = 'SOMETHING'
        message_2 = 'another SOMETHING'
        user_d1_name = chats_d2.user_name_text.text

        chats_d2.chat_message_input.send_keys(message_1)
        chats_d2.send_message_button.click()

        chats_d1.back_button.click()
        chats_d1.find_full_text(message_1)
        one_to_one_chat_d1 = chats_d1.element_by_text(message_1, 'button')
        one_to_one_chat_d1.click()

        one_to_one_chat_d2 = chats_d2.element_by_text(user_d1_name, 'button')
        one_to_one_chat_d2.click()
        chats_d2.chat_message_input.send_keys(message_2)
        chats_d2.send_message_button.click()

        chats_d1.find_full_text(message_2)

    @pytest.mark.chat
    def test_group_chat_send_receive_messages_and_remove_user(self):

        device_1, device_2 = HomeView(self.driver_1), \
                             HomeView(self.driver_2)
        set_password_as_new_user(device_2, device_1)

        device_1.back_button.click()
        chats_d1 = device_1.get_chats()
        chats_d1.profile_button.click()
        profile_d1 = chats_d1.profile_icon.click()
        key = profile_d1.public_key_text.text

        device_2.back_button.click()
        chats_d2 = device_2.get_chats()
        chats_d2.plus_button.click()
        chats_d2.add_new_contact.click()
        chats_d2.public_key_edit_box.send_keys(key)
        chats_d2.confirm()
        chats_d2.confirm_public_key_button.click()
        user_name_d1 = chats_d2.user_name_text.text

        for _ in range(2):
            device_2.back_button.click()
        chats_d2.new_group_chat_button.click()

        user_contact = chats_d2.element_by_text(user_name_d1, 'button')
        user_contact.scroll_to_element()
        user_contact.click()
        chats_d2.next_button.click()

        chat_name = 'new_chat'
        message_1 = 'first SOMETHING'
        message_2 = 'second SOMETHING'
        message_3 = 'third SOMETHING'

        chats_d2.name_edit_box.send_keys(chat_name)
        chats_d2.save_button.click()

        # send_and_receive_messages

        chats_d2.chat_message_input.send_keys(message_1)
        chats_d2.send_message_button.click()

        profile_d1.back_button.click()
        chats_d1 = profile_d1.get_chats()
        chats_d1.find_full_text(message_1)
        group_chat_d1 = chats_d1.element_by_text(chat_name, 'button')
        group_chat_d1.click()

        chats_d2.chat_message_input.send_keys(message_2)
        chats_d2.send_message_button.click()

        chats_d1.find_full_text(message_2)

        # remove_user

        chats_d2.group_chat_options.click()
        chats_d2.chat_settings.click()
        chats_d2.confirm()
        chats_d2.user_options.click()
        chats_d2.remove_button.click()
        device_2.back_button.click()

        # chats_d2.find_full_text("You\'ve removed " + user_name_d1)

        chats_d2.chat_message_input.send_keys(message_3)
        chats_d2.send_message_button.click()

        chats_d1.find_text_part("removed you from group chat")
        message_text = chats_d1.element_by_text(message_3, 'text')
        if message_text.is_element_present(20):
            pytest.fail('Message is shown for the user which has been removed from the GroupChat', False)

    @pytest.mark.chat
    def test_send_funds_via_request_in_one_to_one_chat(self):
        recipient = transaction_users['A_USER']
        sender = transaction_users['B_USER']

        device_1, device_2 = HomeView(self.driver_1), HomeView(self.driver_2)
        recover_access(device_1,
                       passphrase=recipient['passphrase'],
                       password=recipient['password'],
                       username=recipient['username'])
        chats_d1 = device_1.get_chats()

        recover_access(device_2,
                       passphrase=sender['passphrase'],
                       password=sender['password'],
                       username=sender['username'])
        chats_d2 = device_2.get_chats()

        try:
            chats_d1.element_by_text_part(sender['username'][:25], 'button').click()
        except NoSuchElementException:
            chats_d1.plus_button.click()
            chats_d1.add_new_contact.click()
            chats_d1.public_key_edit_box.send_keys(sender['public_key'])
            chats_d1.confirm()
            chats_d1.confirm_public_key_button.click()

        chats_d1.request_funds_button.click()
        amount = get_unique_amount()
        chats_d1.chat_message_input.set_value(amount)
        chats_d1.send_message_button.click()

        initial_balance_recipient = get_balance(recipient['address'])

        chats_d2.element_by_text_part(recipient['username'][:25], 'button').click()
        chats_d2.element_by_text('Requesting  %s ETH' % amount, 'button').click()
        chats_d2.send_message_button.click()
        chats_d2.sign_transaction_button.click()
        chats_d2.enter_password_input.send_keys(sender['password'])
        chats_d2.sign_transaction_button.click()
        chats_d2.got_it_button.click()
        verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        chats_d2.verify_amount_is_sent(amount)

        chats_d2.back_button.click()
        wallet = chats_d2.wallet_button.click()
        tr_view = wallet.transactions_button.click()
        transaction = tr_view.transactions_table.find_transaction(amount=amount)
        details_view = transaction.click()
        transaction_hash = details_view.get_transaction_hash()
        verify_transaction_in_ropsten(address=sender['address'], transaction_hash=transaction_hash)
