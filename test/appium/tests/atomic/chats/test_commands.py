import time
import pytest
from tests import marks
from tests.users import transaction_senders, ens_user
from tests.base_test_case import MultipleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="commands_2")
@marks.critical
class TestCommandsMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.sender = transaction_senders['ETH_STT_3']
        cls.home_1 = cls.device_1.recover_access(passphrase=cls.sender['passphrase'], enable_notifications=True)
        cls.home_2 = cls.device_2.create_user()
        for home in cls.home_1, cls.home_2:
            profile = home.profile_button.click()
            profile.profile_notifications_button.scroll_and_click()
            profile.wallet_push_notifications.click()
        cls.recipient_public_key, cls.recipient_username = cls.home_2.get_public_key_and_username(return_username=True)
        cls.wallet_1, cls.wallet_2 = cls.home_1.wallet_button.click(), cls.home_2.wallet_button.click()
        [wallet.home_button.click() for wallet in (cls.wallet_1, cls.wallet_2)]
        cls.chat_1 = cls.home_1.add_contact(cls.recipient_public_key)
        cls.chat_1.send_message("hello!")
        cls.account_name_1 = cls.wallet_1.status_account_name

    @marks.testrail_id(6253)
    @marks.transaction
    def test_1_1_chat_command_send_eth_outgoing_tx_push(self):
        amount = self.chat_1.get_unique_amount()
        self.home_1.just_fyi('Send %s ETH in 1-1 chat and check it for sender and receiver: Address requested' % amount)
        self.chat_1.commands_button.click()
        send_transaction = self.chat_1.send_command.click()
        send_transaction.get_username_in_transaction_bottom_sheet_button(self.recipient_username).click()
        if send_transaction.scan_qr_code_button.is_element_displayed():
            self.drivers[0].fail('Recipient is editable in bottom sheet when send ETH from 1-1 chat')
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        sender_message = self.chat_1.get_outgoing_transaction(self.account_name_1)
        if not sender_message.is_element_displayed():
            self.drivers[0].fail('No message is shown after sending ETH in 1-1 chat for sender')
        sender_message.transaction_status.wait_for_element_text(sender_message.address_requested)

        chat_2 = self.home_2.get_chat(self.sender['username']).click()
        receiver_message = chat_2.get_incoming_transaction(self.account_name_1)
        timestamp_sender = sender_message.timestamp_command_message.text
        if not receiver_message.is_element_displayed():
            self.drivers[0].fail('No message about incoming transaction in 1-1 chat is shown for receiver')
        receiver_message.transaction_status.wait_for_element_text(receiver_message.address_requested)

        self.home_2.just_fyi('Accept and share address for sender and receiver')
        for option in (receiver_message.decline_transaction, receiver_message.accept_and_share_address):
            if not option.is_element_displayed():
                self.drivers[0].fail("Required options accept or share are not shown")

        select_account_bottom_sheet = receiver_message.accept_and_share_address.click()
        if not select_account_bottom_sheet.get_account_in_select_account_bottom_sheet_button(
                self.account_name_1).is_element_displayed():
            self.errors.append('Not expected value in "From" in "Select account": "Status" is expected')
        select_account_bottom_sheet.select_button.click()
        receiver_message.transaction_status.wait_for_element_text(receiver_message.shared_account)
        sender_message.transaction_status.wait_for_element_text(sender_message.address_request_accepted)

        self.home_1.just_fyi("Sign and send transaction and check that timestamp on message is updated")
        time.sleep(20)
        send_bottom_sheet = sender_message.sign_and_send.click()
        send_bottom_sheet.next_button.click()
        send_bottom_sheet.sign_transaction()
        updated_timestamp_sender = sender_message.timestamp_command_message.text
        if updated_timestamp_sender == timestamp_sender:
            self.errors.append("Timestamp of message is not updated after signing transaction")
        self.chat_1.wallet_button.click()
        self.wallet_1.find_transaction_in_history(amount=amount)

        [wallet.put_app_to_background() for wallet in (self.wallet_1, self.wallet_2)]
        self.device_1.open_notification_bar()
        self.network_api.wait_for_confirmation_of_transaction(self.sender['address'], amount)
        pn = self.home_1.get_pn('You sent %s ETH' % amount)
        if pn:
            pn.click()
            if not self.wallet_1.transaction_history_button.is_element_displayed():
                self.errors.append('Was not redirected to transaction history after tapping on PN')
        else:
            self.home_1.click_system_back_button()
            self.home_1.status_in_background_button.click_if_shown()
        self.wallet_1.home_button.click(desired_view="chat")

        self.home_1.just_fyi("Check 'Confirmed' state for sender and receiver(use pull-to-refresh to update history)")
        chat_2.status_in_background_button.click()
        chat_2.wallet_button.click()
        self.wallet_2.wait_balance_is_changed()
        self.wallet_2.find_transaction_in_history(amount=amount)
        self.wallet_2.home_button.click()
        self.home_2.get_chat(self.sender['username']).click()
        [message.transaction_status.wait_for_element_text(message.confirmed, 60) for message in
         (sender_message, receiver_message)]

        # TODO: should be added PNs for receiver after getting more stable feature (rechecked 23.11.21, valid)
        self.errors.verify_no_errors()

    @marks.testrail_id(6265)
    def test_1_1_chat_command_decline_eth_push_changing_state(self):
        [home.driver.background_app(3) for home in (self.home_1, self.home_2)]
        self.home_1.home_button.double_click()
        self.home_1.get_chat(username=self.recipient_username).click()

        self.home_1.just_fyi('Decline transaction before sharing address and check that state is changed')
        self.chat_1.commands_button.click()
        send_transaction = self.chat_1.send_command.click()
        amount = self.chat_1.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        chat_1_sender_message = self.chat_1.get_outgoing_transaction()
        self.home_1.click_system_home_button()

        self.home_2.home_button.double_click()
        chat_2 = self.home_2.get_chat(self.sender['username']).click()
        chat_2_receiver_message = chat_2.get_incoming_transaction()
        chat_2_receiver_message.decline_transaction.click()
        self.home_1.open_notification_bar()
        self.home_1.element_by_text_part('Request address for transaction declined').wait_and_click()

        [message.transaction_status.wait_for_element_text(message.declined) for message in
         (chat_1_sender_message, chat_2_receiver_message)]

        self.home_1.just_fyi('Decline transaction request and check that state is changed')
        request_amount = self.chat_1.get_unique_amount()
        self.chat_1.commands_button.click()
        request_transaction = self.chat_1.request_command.click()
        request_transaction.amount_edit_box.set_value(request_amount)
        request_transaction.confirm()
        request_transaction.request_transaction_button.click()
        chat_1_request_message = self.chat_1.get_incoming_transaction()
        chat_2_sender_message = chat_2.get_outgoing_transaction()
        chat_2_sender_message.decline_transaction.click()
        [message.transaction_status.wait_for_element_text(message.declined) for message in
         (chat_2_sender_message, chat_1_request_message)]

        self.errors.verify_no_errors()

    @marks.testrail_id(6263)
    @marks.transaction
    def test_1_1_chat_command_request_and_receive_stt_in_1_1_chat_offline(self):
        [home.driver.background_app(2) for home in (self.home_1, self.home_2)]
        asset_name = 'STT'
        amount = self.device_1.get_unique_amount()

        self.device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        self.home_2.get_back_to_home_view()
        self.home_2.wallet_button.click()
        self.wallet_2.select_asset(asset_name)
        self.wallet_2.home_button.click()
        self.home_1.wallet_button.double_click()
        initial_amount_stt = self.wallet_1.get_asset_amount_by_name('STT')
        self.home_1.driver.close_app()

        self.home_2.just_fyi('Request %s STT in 1-1 chat and check it is visible for sender and receiver' % amount)
        chat_2 = self.home_2.get_chat(username=self.sender['username']).click()
        chat_2.commands_button.click()
        request_transaction = chat_2.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        asset_button = request_transaction.asset_by_name(asset_name)
        request_transaction.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        request_transaction.request_transaction_button.click()
        chat_2_request_message = chat_2.get_incoming_transaction()
        if not chat_2_request_message.is_element_displayed():
            self.drivers[1].fail('No incoming transaction in 1-1 chat is shown for recipient after requesting STT')

        self.home_1.just_fyi('Check that transaction message is fetched from offline and sign transaction')
        self.device_1.driver.launch_app()
        self.device_1.sign_in()
        self.home_1.connection_offline_icon.wait_for_invisibility_of_element(30)
        self.home_1.get_chat(self.recipient_username).click()
        chat_1_sender_message = self.chat_1.get_outgoing_transaction()
        if not chat_1_sender_message.is_element_displayed():
            self.drivers[0].fail('No outgoing transaction in 1-1 chat is shown for sender after requesting STT')
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.address_received)
        send_message = chat_1_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction()

        self.home_2.just_fyi('Check that transaction message is updated with new status after offline')
        [chat.toggle_airplane_mode() for chat in (self.chat_1, chat_2)]
        self.network_api.wait_for_confirmation_of_transaction(self.sender['address'], amount, token=True)
        for home in (self.home_1, self.home_2):
            home.toggle_airplane_mode()
            home.home_button.double_click()
            home.connection_offline_icon.wait_for_invisibility_of_element(100)
        self.home_2.get_chat(self.sender['username']).click()
        self.home_1.get_chat(self.recipient_username).click()
        [message.transaction_status.wait_for_element_text(message.confirmed, wait_time=120) for message in
         (chat_1_sender_message, chat_2_request_message)]

        self.home_1.just_fyi('Check that can find tx in history and balance is updated after offline')
        self.home_1.wallet_button.click()
        self.wallet_1.wait_balance_is_changed('STT', initial_amount_stt)
        self.wallet_1.find_transaction_in_history(amount=amount, asset=asset_name)

        self.errors.verify_no_errors()


class TestCommandsMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(6257)
    @marks.medium
    @marks.transaction
    def test_network_mismatch_for_send_request_in_1_1_chat(self):
        sender = transaction_senders['ETH_1']
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sign_in_1.recover_access(passphrase=sender['passphrase'])
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        wallet_1 = home_1.wallet_button.click()
        wallet_1.home_button.click()
        profile_2 = home_2.profile_button.click()
        username_2 = profile_2.default_username_text.text
        profile_2.switch_network()

        chat_2 = home_2.add_contact(sender['public_key'])
        chat_2.send_message("Hey there!")
        amount = chat_2.get_unique_amount()

        chat_2.commands_button.click()
        request_transaction = chat_2.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        request_transaction.request_transaction_button.click()
        chat_2_request_message = chat_2.get_incoming_transaction()

        chat_2_request_message.long_press_element()
        if chat_2.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Incoming transaction message!')

        chat_1 = home_1.get_chat(username_2).click()
        chat_1_sender_message = chat_1.get_outgoing_transaction()
        chat_1_sender_message.long_press_element()
        if chat_1.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Outgoing transaction message!')
        send_message = chat_1_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)
        wallet_2 = chat_2.wallet_button.click()
        wallet_2.accounts_status_account.click()
        wallet_2.swipe_down()
        wallet_2.home_button.click(desired_view="chat")
        if chat_2_request_message.transaction_status == chat_1_sender_message.confirmed:
            self.errors.append("Transaction is shown as confirmed on mainnet, but was sent on ropsten!")
        self.errors.verify_no_errors()

    @marks.testrail_id(6279)
    @marks.high
    @marks.transaction
    def test_send_eth_to_ens_in_chat(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sender, reciever = transaction_senders['ETH_3'], ens_user
        home_1, home_2 = sign_in_1.recover_access(sender['passphrase']), sign_in_2.recover_access(
            reciever['passphrase'])

        home_2.just_fyi("Start chat with sender")
        profile_2 = home_2.profile_button.click()
        profile_2.connect_existing_ens(reciever['ens'])
        profile_2.home_button.click()
        chat_2 = home_2.add_contact(sender['public_key'])
        message_1, message_2 = 'hello', 'hey'
        chat_2.send_message(message_1)

        wallet_1 = home_1.wallet_button.click()
        wallet_1.wait_balance_is_changed()
        wallet_1.home_button.click()
        chat_1 = home_1.add_contact(reciever['ens'])
        chat_1.send_message(message_2)
        chat_1.commands_button.click()
        amount = chat_1.get_unique_amount()

        chat_1.just_fyi("Check sending assets to ENS name from sender side")
        send_message = chat_1.send_command.click()
        send_message.amount_edit_box.set_value(amount)
        send_message.confirm()
        send_message.next_button.click()
        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.drivers[0])
        send_transaction.ok_got_it_button.click()
        send_transaction.sign_transaction()
        chat_1_sender_message = chat_1.get_outgoing_transaction(transaction_value=amount)
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)

        chat_2.just_fyi("Check that message is fetched for receiver")
        chat_2_reciever_message = chat_2.get_incoming_transaction(transaction_value=amount)
        chat_2_reciever_message.transaction_status.wait_for_element_text(chat_2_reciever_message.confirmed)

