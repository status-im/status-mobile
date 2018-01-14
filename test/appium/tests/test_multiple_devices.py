import pytest
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from tests import api_requests
from tests.base_test_case import MultipleDeviceTestCase
from tests import user_flow, transaction_users, get_current_time
from views.console_view import ConsoleView


@pytest.mark.all
class TestMultipleDevices(MultipleDeviceTestCase):

    @pytest.mark.skip('Discover functionality is not available')
    @pytest.mark.discover
    def test_new_profile_name_and_status_on_discover(self):
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        for profile in device_1, device_2:
            user_flow.create_user(profile)
            profile.back_button.click()
        device_1_home, device_2_home = device_1.get_home_view(), device_2.get_home_view()

        device_2_public_key = user_flow.get_public_key(device_2_home)
        user_flow.add_contact(device_1_home, device_2_public_key)
        device_1_chat = device_1_home.get_chat_view()
        device_1_chat.chat_message_input.send_keys('test123')
        device_1_chat.send_message_button.click()
        device_1_chat.back_button.click()

        device_2_home.element_by_text('test123', 'button').click()
        device_2_chat = device_2_home.get_chat_view()
        device_2_chat.add_to_contacts.click()
        device_2_chat.back_button.click()

        device_1_profile, device_2_profile = device_1_chat.profile_button.click(), device_2_chat.profile_button.click()

        for profile in device_1_profile, device_2_profile:
            user_details = user_flow.get_new_username_and_status(profile)
            profile.back_button.click()
            discover = profile.discover_button.click()
            discover.all_popular.click()
            discover.find_full_text(user_details['name'])
            discover.find_full_text(' %s' % user_details['status'])
            discover.back_button.click()
            discover.all_recent.click()
            discover.find_full_text(user_details['name'])
            discover.find_full_text('%s ' % user_details['status'])

    @pytest.mark.chat
    def test_one_to_one_chat(self):
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
            device.back_button.click()
        device_1_home, device_2_home = device_1.get_home_view(), device_2.get_home_view()

        device_1_public_key = user_flow.get_public_key(device_1_home)
        user_flow.add_contact(device_2_home, device_1_public_key)
        device_2_chat = device_2_home.get_chat_view()
        message_1 = 'SOMETHING'
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()

        message_2 = 'another SOMETHING'
        device_1_home.home_button.click()
        device_1_home.find_full_text(message_1)
        device_1_home.element_by_text(message_1, 'button').click()
        device_1_chat = device_1_home.get_chat_view()
        device_1_chat.chat_message_input.send_keys(message_2)
        device_1_chat.send_message_button.click()
        device_2_chat.find_full_text(message_2)

    @pytest.mark.chat
    def test_group_chat_send_receive_messages_and_remove_user(self):
        device_1, device_2 = ConsoleView(self.driver_1), \
                             ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
            device.back_button.click()
        device_1_home = device_1.get_home_view()
        device_2_home = device_2.get_home_view()
        device_1_public_key = user_flow.get_public_key(device_1_home)
        user_flow.add_contact(device_2_home, device_1_public_key)
        device_2_chat = device_2_home.get_chat_view()
        device_1_user_name = device_2_chat.user_name_text.text
        device_2_home.back_button.click(times_to_click=3)
        chat_name = 'new_chat'
        message_1 = 'first SOMETHING'
        message_2 = 'second SOMETHING'
        message_3 = 'third SOMETHING'
        user_flow.create_group_chat(device_2_home, device_1_user_name, chat_name)

        # send_and_receive_messages
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()
        device_1.home_button.click()
        device_1_home.find_full_text(message_1)
        device_1_home.element_by_text(chat_name, 'button').click()
        group_chat_d1 = device_1_home.get_chat_view()
        group_chat_d1.chat_message_input.send_keys(message_2)
        group_chat_d1.send_message_button.click()
        device_2_chat.find_full_text(message_2)

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
        group_chat_d1.find_text_part("removed you from group chat")
        if group_chat_d1.element_by_text(message_3, 'text').is_element_present(20):
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
        device_2_home = device_2.get_home_view()
        device_1_home = device_1.get_home_view()
        user_flow.add_contact(device_1_home, sender['public_key'])
        device_1_home.back_button.click(times_to_click=3)
        if test == 'group_chat':
            group_chat_name = 'gtr_%s' % get_current_time()
            user_flow.create_group_chat(device_1_home, sender['username'], group_chat_name)
            device_2_home.element_by_text(group_chat_name, 'button').click()
        else:
            one_to_one_chat_device_1 = device_1_home.element_by_text_part(sender['username'][:25], 'button')
            one_to_one_chat_device_1.scroll_to_element()
            one_to_one_chat_device_1.click()
        device_1_chat = device_1_home.get_chat_view()
        device_2_chat = device_2_home.get_chat_view()
        device_1_chat.request_command.click()
        amount = device_1_chat.get_unique_amount()
        if test == 'group_chat':
            device_1_chat.first_recipient_button.click()
            device_1_chat.send_as_keyevent(amount)
        else:
            device_1_chat.chat_message_input.set_value(amount)
            one_to_one_chat_device_2 = device_2_chat.element_by_text_part(recipient['username'][:25], 'button')
            one_to_one_chat_device_2.click()
        device_1_chat.send_message_button.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        if test == 'group_chat':
            device_1_chat.find_full_text('from  ' + sender['username'], 20)
            device_2_chat.find_full_text('from  ' + sender['username'], 20)
        device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button').click()
        device_2_chat.send_message_button.click()
        device_2_send_transaction = device_2_chat.get_send_transaction_view()
        device_2_send_transaction.sign_transaction_button.click()
        device_2_send_transaction.enter_password_input.send_keys(sender['password'])
        device_2_send_transaction.sign_transaction_button.click()
        device_2_send_transaction.got_it_button.click()
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        device_2_chat.back_button.click()
        device_2_wallet = device_2_home.wallet_button.click()
        transactions_view = device_2_wallet.transactions_button.click()
        transaction_element = transactions_view.transactions_table.find_transaction(amount=amount)
        transaction_details_view = transaction_element.click()
        transaction_hash = transaction_details_view.get_transaction_hash()
        api_requests.find_transaction_on_ropsten(address=sender['address'],
                                                 transaction_hash=transaction_hash)
