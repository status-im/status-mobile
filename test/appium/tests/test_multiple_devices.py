import pytest
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from tests import api_requests
from tests.base_test_case import MultipleDeviceTestCase
from tests import user_flow, transaction_users, get_current_time
from views.console_view import ConsoleView


@pytest.mark.all
class TestMultipleDevices(MultipleDeviceTestCase):

    @pytest.mark.discover
    def test_new_profile_name_and_status_on_discover(self):
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
        device_1.back_button.click()
        device_2.back_button.click()
        device_1_chat, device_2_chat = device_1.get_chat_view(), device_2.get_chat_view()
        device_2_public_key = user_flow.get_public_key(device_2_chat)
        user_flow.add_contact(device_1_chat, device_2_public_key)
        device_1_chat.chat_message_input.send_keys('test123')
        device_1_chat.send_message_button.click()
        device_1_chat.back_button.click()
        device_2_chat.back_button.click()
        new_chat_d2 = device_2_chat.element_by_text('test123', 'button')
        new_chat_d2.click()
        device_2_chat.add_to_contacts.click()
        for _ in range(2):
            device_1_chat.back_button.click()
        device_2_chat.back_button.click()
        device_1_profile_drawer = device_1_chat.profile_button.click()
        device_2_profile_drawer = device_2_chat.profile_button.click()
        device_1_profile, device_2_profile = \
            device_1_profile_drawer.profile_icon.click(), device_2_profile_drawer.profile_icon.click()
        users_details = user_flow.get_new_username_and_status(device_1_profile,
                                                              device_2_profile)
        device_1_profile.back_button.click()
        device_2_profile.back_button.click()
        device_1_discover = device_1_profile.discover_button.click()
        device_2_discover = device_2_profile.discover_button.click()
        for device in device_1_discover, device_2_discover:
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
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
        device_1.back_button.click()
        device_2.back_button.click()
        device_1_chat = device_1.get_chat_view()
        device_2_chat = device_2.get_chat_view()
        device_1_public_key = user_flow.get_public_key(device_1_chat)
        user_flow.add_contact(device_2_chat, device_1_public_key)
        message_1 = 'SOMETHING'
        message_2 = 'another SOMETHING'
        user_d1_name = device_2_chat.user_name_text.text
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()
        device_1_chat.back_button.click()
        device_1_chat.find_full_text(message_1)
        one_to_one_chat_d1 = device_1_chat.element_by_text(message_1, 'button')
        one_to_one_chat_d1.click()
        one_to_one_chat_d2 = device_2_chat.element_by_text(user_d1_name, 'button')
        one_to_one_chat_d2.click()
        device_2_chat.chat_message_input.send_keys(message_2)
        device_2_chat.send_message_button.click()
        device_1_chat.find_full_text(message_2)

    @pytest.mark.chat
    def test_group_chat_send_receive_messages_and_remove_user(self):
        device_1, device_2 = ConsoleView(self.driver_1), \
                             ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
        device_1.back_button.click()
        device_2.back_button.click()
        device_1_chat = device_1.get_chat_view()
        device_2_chat = device_2.get_chat_view()
        device_1_public_key = user_flow.get_public_key(device_1_chat)
        user_flow.add_contact(device_2_chat, device_1_public_key)
        device_1_user_name = device_2_chat.user_name_text.text
        for _ in range(3):
            device_2.back_button.click()
        chat_name = 'new_chat'
        message_1 = 'first SOMETHING'
        message_2 = 'second SOMETHING'
        message_3 = 'third SOMETHING'
        user_flow.create_group_chat(device_2_chat, device_1_user_name, chat_name)

        # send_and_receive_messages
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()
        device_1.back_button.click()
        device_1_chat = device_1.get_chat_view()
        device_1_chat.find_full_text(message_1)
        group_chat_d1 = device_1_chat.element_by_text(chat_name, 'button')
        group_chat_d1.click()
        device_2_chat.chat_message_input.send_keys(message_2)
        device_2_chat.send_message_button.click()
        device_1_chat.find_full_text(message_2)

        # remove user
        device_2_chat.group_chat_options.click()
        device_2_chat.chat_settings.click()
        for _ in range(2):
            try:
                device_2_chat.user_options.click()
            except (NoSuchElementException, TimeoutException):
                pass
        device_2_chat.remove_button.click()
        device_2_chat.confirm()
        device_2.back_button.click()

        # verify removed user receives no messages
        device_2_chat.chat_message_input.send_keys(message_3)
        device_2_chat.send_message_button.click()
        device_1_chat.find_text_part("removed you from group chat")
        message_text = device_1_chat.element_by_text(message_3, 'text')
        if message_text.is_element_present(20):
            pytest.fail('Message is shown for the user which has been removed from the GroupChat', False)

    @pytest.mark.transaction
    @pytest.mark.parametrize("test, recipient, sender", [('group_chat',
                                                          transaction_users['A_USER'], transaction_users['B_USER']),
                                                         ('one_to_one_chat',
                                                          transaction_users['B_USER'], transaction_users['A_USER'])
                                                         ],
                             ids=['group_chat', 'one_to_one_chat'])
    def test_send_funds_via_request(self, test, recipient, sender):
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        user_flow.recover_access(device_1,
                                 passphrase=recipient['passphrase'],
                                 password=recipient['password'],
                                 username=recipient['username'])
        user_flow.recover_access(device_2,
                                 passphrase=sender['passphrase'],
                                 password=sender['password'],
                                 username=sender['username'])
        device_2_chat = device_2.get_chat_view()
        device_1_chat = device_1.get_chat_view()
        if test == 'group_chat':
            user_flow.add_contact(device_1_chat, sender['public_key'])
            for _ in range(2):
                device_1_chat.back_button.click()
            group_chat_name = 'gtr_%s' % get_current_time()
            user_flow.create_group_chat(device_1_chat, sender['username'], group_chat_name)
            group_chat_d2 = device_2_chat.element_by_text(group_chat_name, 'button')
            group_chat_d2.click()
        else:
            device_1_chat.element_by_text_part(sender['username'][:25], 'button').click()
        device_1_chat.request_command.click()
        amount = device_1_chat.get_unique_amount()
        if test == 'group_chat':
            device_1_chat.first_recipient_button.click()
            device_1_chat.send_as_keyevent(amount)
        else:
            device_1_chat.chat_message_input.set_value(amount)
        device_1_chat.send_message_button.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        if test == 'group_chat':
            device_1_chat.find_full_text('from  ' + sender['username'], 20)
            device_2_chat.find_full_text('from  ' + sender['username'], 20)
        device_2_chat.element_by_text_part(recipient['username'][:25], 'button').click()
        device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button').click()
        device_2_chat.send_message_button.click()
        device_2_send_transaction = device_2_chat.get_send_transaction_view()
        device_2_send_transaction.sign_transaction_button.click()
        device_2_send_transaction.enter_password_input.send_keys(sender['password'])
        device_2_send_transaction.sign_transaction_button.click()
        device_2_send_transaction.got_it_button.click()
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        device_2_chat.back_button.click()
        device_2_wallet = device_2_chat.wallet_button.click()
        transactions_view = device_2_wallet.transactions_button.click()
        transaction_element = transactions_view.transactions_table.find_transaction(amount=amount)
        transaction_details_view = transaction_element.click()
        transaction_hash = transaction_details_view.get_transaction_hash()
        api_requests.find_transaction_on_ropsten(address=sender['address'],
                                                 transaction_hash=transaction_hash)
