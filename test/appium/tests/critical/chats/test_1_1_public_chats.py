import random
import time

import emoji
import pytest

from tests import marks, common_password, run_in_parallel
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user, ens_user_message_sender
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="four_2")
@marks.critical
class TestCommandsMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender = transaction_senders['ETH_STT_3']
        self.home_1 = self.device_1.recover_access(passphrase=self.sender['passphrase'], enable_notifications=True)
        self.home_2 = self.device_2.create_user()
        for home in self.home_1, self.home_2:
            profile = home.profile_button.click()
            profile.profile_notifications_button.scroll_and_click()
            profile.wallet_push_notifications.click()
        self.recipient_public_key, self.recipient_username = self.home_2.get_public_key_and_username(
            return_username=True)
        self.wallet_1, self.wallet_2 = self.home_1.wallet_button.click(), self.home_2.wallet_button.click()
        [wallet.home_button.click() for wallet in (self.wallet_1, self.wallet_2)]
        self.chat_1 = self.home_1.add_contact(self.recipient_public_key)
        self.chat_1.send_message("hello!")
        self.account_name_1 = self.wallet_1.status_account_name

    @marks.testrail_id(6253)
    def test_1_1_chat_command_send_tx_eth_outgoing_tx_push(self):
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

        # TODO: should be added PNs for receiver after getting more stable feature (rechecked 04.10.22, valid)
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
    def test_1_1_chat_command_request_and_send_tx_stt_in_1_1_chat_offline(self):
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


@pytest.mark.xdist_group(name="one_2")
@marks.critical
class TestOneToOneChatMultipleSharedDevices(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1 = self.device_1.create_user(enable_notifications=True)
        self.home_2 = self.device_2.create_user(enable_notifications=True)
        self.profile_1 = self.home_1.profile_button.click()
        self.default_username_1 = self.profile_1.default_username_text.text
        self.profile_1.home_button.click()
        self.public_key_2, self.default_username_2 = self.home_2.get_public_key_and_username(return_username=True)
        self.chat_1 = self.home_1.add_contact(self.public_key_2)
        self.chat_1.send_message('hey')
        self.home_2.home_button.double_click()
        self.chat_2 = self.home_2.get_chat(self.default_username_1).click()

    @marks.testrail_id(6315)
    # moved
    def test_1_1_chat_message_reaction(self):
        message_from_sender = "Message sender"
        self.device_1.just_fyi("Sender start 1-1 chat, set emoji and check counter")
        self.chat_1.send_message(message_from_sender)
        self.chat_1.set_reaction(message_from_sender)
        message_sender = self.chat_1.chat_element_by_text(message_from_sender)
        if message_sender.emojis_below_message() != 1:
            self.errors.append("Counter of reaction is not updated on your own message!")

        self.device_2.just_fyi("Receiver  set own emoji and verifies counter on received message in 1-1 chat")
        message_receiver = self.chat_2.chat_element_by_text(message_from_sender)
        if message_receiver.emojis_below_message(own=False) != 1:
            self.errors.append("Counter of reaction is not updated on received message!")
        self.chat_2.set_reaction(message_from_sender)
        for counter in message_sender.emojis_below_message(), message_receiver.emojis_below_message():
            if counter != 2:
                self.errors.append('Counter is not updated after setting emoji from receiver!')

        self.device_2.just_fyi("Receiver pick the same emoji and verify that counter will decrease for both users")
        self.chat_2.set_reaction(message_from_sender)
        for counter in message_sender.emojis_below_message(), message_receiver.emojis_below_message(own=False):
            if counter != 1:
                self.errors.append('Counter is not decreased after re-tapping  emoji from receiver!')
        self.errors.verify_no_errors()

    @marks.testrail_id(6316)
    def test_1_1_chat_audio_message_with_push(self):
        self.home_2.just_fyi("Put app on background (to check Push notification received for audio message)")
        self.home_2.click_system_home_button()

        self.home_2.just_fyi("Sending audio message to device who is on background")
        self.chat_1.record_audio_message(message_length_in_seconds=65)
        if not self.chat_1.element_by_text("Maximum recording time reached").is_element_displayed():
            self.drivers[0].fail("Exceeded 1 min limit of recording time.")

        self.chat_1.ok_button.click()
        if self.chat_1.audio_message_recorded_time.text != "0:59":
            self.errors.append("Timer exceed 2 minutes")
        self.chat_1.send_message_button.click()

        self.device_2.open_notification_bar()
        chat_2 = self.home_2.click_upon_push_notification_by_text("Audio")

        listen_time = 5

        self.device_2.home_button.click()
        self.home_2.get_chat(self.default_username_1).click()
        chat_2.play_audio_message(listen_time)
        if chat_2.audio_message_in_chat_timer.text not in ("00:05", "00:06", "00:07", "00:08"):
            self.errors.append("Listened 5 seconds but timer shows different listened time in audio message")

        self.errors.verify_no_errors()

    @marks.testrail_id(5373)
    def test_1_1_chat_emoji_send_reply_and_open_link(self):
        self.home_1.just_fyi("Check that can send emoji in 1-1 chat")
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        self.chat_1.send_message(emoji.emojize(emoji_name))
        for chat in self.chat_1, self.chat_2:
            if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
                self.errors.append('Message with emoji was not sent or received in 1-1 chat')
        self.chat_1.quote_message(emoji_unicode)
        if self.chat_1.quote_username_in_message_input.text != "↪ You":
            self.errors.append("'You' is not displayed in reply quote snippet replying to own message")

        self.chat_1.just_fyi("Clear quote and check there is not snippet anymore")
        self.chat_1.cancel_reply_button.click()
        if self.chat_1.cancel_reply_button.is_element_displayed():
            self.errors.append("Message quote kept in public chat input after it was cancellation")

        self.chat_1.just_fyi("Send reply")
        self.chat_1.quote_message(emoji_unicode)
        reply_to_message_from_sender = "hey, reply"
        self.chat_1.send_message(reply_to_message_from_sender)

        self.chat_1.just_fyi("Receiver verifies received reply...")
        if self.chat_2.chat_element_by_text(reply_to_message_from_sender).replied_message_text != emoji_unicode:
            self.errors.append("No reply received in 1-1 chat")

        self.home_1.just_fyi("Check that link can be opened and replied from 1-1 chat")
        reply = 'reply to link'
        url_message = 'Test with link: https://status.im/ here should be nothing unusual.'
        self.chat_1.send_message(url_message)
        self.chat_2.chat_element_by_text(url_message).wait_for_element(20)
        self.chat_2.quote_message(url_message)
        self.chat_2.send_message(reply)
        replied_message = self.chat_1.chat_element_by_text(reply)
        if replied_message.replied_message_text != url_message:
            self.errors.append("Reply for '%s' not present in message received in public chat" % url_message)

        url_message = 'http://status.im'
        self.chat_1.send_message(url_message)
        self.chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = self.chat_2.open_in_status_button.click()
        if not web_view.element_by_text('Private, Secure Communication').is_element_displayed(60):
            self.errors.append('URL was not opened from 1-1 chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(695843)
    # moved without edit
    def test_1_1_chat_text_message_edit_delete_push_disappear(self):
        self.device_2.just_fyi(
            "Device 1 sends text message and edits it in 1-1 chat. Device2 checks edited message is shown")
        message_before_edit_1_1, message_after_edit_1_1 = "Message before edit 1-1", "AFTER"
        self.chat_1.home_button.click()
        self.chat_2.home_button.click()
        self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.send_message(message_before_edit_1_1)

        self.chat_2.edit_message_in_chat(message_before_edit_1_1, message_after_edit_1_1)
        if not self.home_1.element_by_text_part(message_after_edit_1_1).is_element_displayed():
            self.errors.append('UNedited message version displayed on preview')
        self.home_1.get_chat(self.default_username_2).click()
        chat_element = self.chat_1.chat_element_by_text(message_after_edit_1_1)
        if not chat_element.is_element_displayed(30):
            self.errors.append('No edited message in 1-1 chat displayed')
        if not self.chat_1.element_by_text_part("⌫ Edited").is_element_displayed(30):
            self.errors.append('No mark in message bubble about this message was edited on receiver side')

        self.device_2.just_fyi("Verify Device1 can not edit and delete received message from Device2")
        chat_element.long_press_element()
        for action in ("edit", "delete"):
            if self.chat_1.element_by_translation_id(action).is_element_displayed():
                self.errors.append('Option to %s someone else message available!' % action)
        self.home_1.click_system_back_button()

        self.device_2.just_fyi("Delete message and check it is not shown in chat preview on home")
        self.chat_2.delete_message_in_chat(message_after_edit_1_1)
        for chat in (self.chat_2, self.chat_1):
            if chat.chat_element_by_text(message_after_edit_1_1).is_element_displayed(30):
                self.errors.append("Deleted message is shown in chat view for 1-1 chat")
        self.chat_1.home_button.double_click()
        if self.home_1.element_by_text(message_after_edit_1_1).is_element_displayed(30):
            self.errors.append("Deleted message is shown on chat element on home screen")

        self.device_2.just_fyi("Send one more message and check that PN will be deleted with message deletion")
        message_to_delete = 'DELETE ME'
        self.home_1.put_app_to_background()
        self.chat_2.send_message(message_to_delete)
        self.home_1.open_notification_bar()
        if not self.home_1.get_pn(message_to_delete):
            self.errors.append("Push notification doesn't appear")
        self.chat_2.delete_message_in_chat(message_to_delete)
        pn_to_disappear = self.home_1.get_pn(message_to_delete)
        if pn_to_disappear:
            if not pn_to_disappear.is_element_disappeared(30):
                self.errors.append("Push notification was not removed after initial message deletion")

        self.errors.verify_no_errors()

    @marks.testrail_id(5315)
    # moved
    def test_1_1_chat_non_latin_message_to_newly_added_contact_with_profile_picture_on_different_networks(self):
        self.home_1.get_app_from_background()
        self.home_2.get_app_from_background()
        self.home_1.profile_button.click()
        self.profile_1.edit_profile_picture('sauce_logo.png')
        self.profile_1.switch_network()
        self.profile_1.home_button.click()
        self.home_1.get_chat(self.default_username_2).click()

        self.profile_1.just_fyi("Send messages on different languages")
        messages = ['hello', '¿Cómo estás tu año?', 'ё, доброго вечерочка', '®	æ ç ♥']
        timestamp_message = messages[3]
        for message in messages:
            self.chat_1.send_message(message)
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.chat_2.home_button.click()
            self.home_2.get_chat(self.default_username_1).click()
        sent_time_variants = self.chat_1.convert_device_time_to_chat_timestamp()
        for message in messages:
            if not self.chat_2.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % message)
        if not self.chat_2.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts button is not shown')
        if self.chat_2.user_name_text.text != self.default_username_1:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % self.default_username_1)

        self.chat_2.just_fyi("Add user to contact and verify his default username")
        self.chat_2.add_to_contacts.click()
        self.chat_2.chat_options.click()
        self.chat_2.view_profile_button.click()
        if not self.chat_2.remove_from_contacts.is_element_displayed():
            self.errors.append("Remove from contacts in not shown after adding contact from 1-1 chat bar")
        self.chat_2.close_button.click()
        self.chat_2.home_button.double_click()
        self.home_2.plus_button.click()
        device_2_contacts = self.home_2.start_new_chat_button.click()
        if not device_2_contacts.element_by_text(self.default_username_1).is_element_displayed():
            self.errors.append('%s is not added to contacts' % self.default_username_1)
        if self.chat_1.user_name_text.text != self.default_username_2:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % self.default_username_2)

        if not self.chat_2.contact_profile_picture.is_element_image_equals_template('sauce_logo_profile_2.png'):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(6283)
    def test_1_1_chat_push_emoji(self):
        message_no_pn, message = 'No PN', 'Text push notification'

        self.device_2.home_button.click()
        self.home_2.get_chat(self.default_username_1).click()
        self.home_2.profile_button.click()

        self.device_2.just_fyi("Device 2 puts app on background being on Profile view to receive PN with text")
        self.device_2.click_system_home_button()
        self.chat_1.send_message(message)

        self.device_1.just_fyi("Device 1 puts app on background to receive emoji push notification")
        self.device_1.profile_button.click()
        self.device_1.click_system_home_button()

        self.device_2.just_fyi("Check text push notification and tap it")
        self.device_2.open_notification_bar()
        if not self.home_2.get_pn(message):
            self.device_2.driver.fail("Push notification with text was not received")
        chat_2 = self.device_2.click_upon_push_notification_by_text(message)

        self.device_2.just_fyi("Send emoji message to Device 1 while it's on background")
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        chat_2.send_message(emoji.emojize(emoji_message))

        self.device_1.just_fyi("Device 1 checks PN with emoji")
        self.device_1.open_notification_bar()
        if not self.device_1.element_by_text_part(emoji_unicode).is_element_displayed(10):
            self.device_1.driver.fail("Push notification with emoji was not received")
        chat_1 = self.device_1.click_upon_push_notification_by_text(emoji_unicode)

        self.device_1.just_fyi("Check Device 1 is actually on chat")
        if not (chat_1.element_by_text_part(message).is_element_displayed()
                and chat_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            self.device_1.driver.fail("Failed to open chat view after tap on PN")

        self.device_1.just_fyi("Checks there are no PN after message was seen")
        [device.click_system_home_button() for device in (self.device_1, self.device_2)]
        [device.open_notification_bar() for device in (self.device_1, self.device_2)]
        if (self.device_2.element_by_text_part(message).is_element_displayed()
                or self.device_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            self.errors.append("PN are keep staying after message was seen by user")
        self.errors.verify_no_errors()

    @marks.testrail_id(6305)
    def test_1_1_chat_image_send_save_reply(self):
        self.home_1.get_app_from_background()
        self.home_2.get_app_from_background()

        self.home_1.home_button.click()
        self.home_1.get_chat(username=self.default_username_2).click()

        self.home_1.just_fyi('send image in 1-1 chat from Gallery, check options for sender')
        image_description = 'description'
        self.chat_1.show_images_button.click()
        self.chat_1.allow_button.click_if_shown()
        self.chat_1.first_image_from_gallery.click()
        if not self.chat_1.cancel_send_image_button.is_element_displayed():
            self.errors.append("Can't cancel sending images, expected image preview is not shown!")
        self.chat_1.chat_message_input.set_value(image_description)
        self.chat_1.send_message_button.click()
        self.chat_1.chat_message_input.click()
        for message in self.chat_1.image_message_in_chat, self.chat_1.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for sender')
        self.chat_1.image_message_in_chat.long_press_element()
        for element in self.chat_1.reply_message_button, self.chat_1.save_image_button:
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on own image messages')
        if self.chat_1.view_profile_button.is_element_displayed():
            self.errors.append('"View profile" is shown on long-press on own message')

        self.home_2.just_fyi('check image, description and options for receiver')
        self.home_2.get_chat(self.default_username_1).click()
        for message in self.chat_2.image_message_in_chat, self.chat_2.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for receiver')

        self.home_2.just_fyi('check options on long-press image for receiver')
        self.chat_2.image_message_in_chat.long_press_element()
        for element in (self.chat_2.reply_message_button, self.chat_2.save_image_button):
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on received image messages')

        self.home_1.just_fyi('save image')
        self.chat_1.save_image_button.click_until_presence_of_element(self.chat_1.show_images_button)
        self.chat_1.show_images_button.click_until_presence_of_element(self.chat_1.image_from_gallery_button)
        self.chat_1.image_from_gallery_button.click_until_presence_of_element(self.chat_1.recent_image_in_gallery)
        if not self.chat_1.recent_image_in_gallery.is_element_displayed():
            self.errors.append('Saved image is not shown in Recent')
        self.home_1.click_system_back_button(2)

        self.home_2.just_fyi('reply to image message')
        self.chat_2.reply_message_button.click()
        if self.chat_2.quote_username_in_message_input.text != "↪ Replying to %s" % self.default_username_1:
            self.errors.append("Username is not displayed in reply quote snippet replying to image message")
        reply_to_message_from_receiver = "image reply"
        self.chat_2.send_message(reply_to_message_from_receiver)
        reply_message = self.chat_2.chat_element_by_text(reply_to_message_from_receiver)
        if not reply_message.image_in_reply.is_element_displayed():
            self.errors.append("Image is not displayed in reply")

        self.home_2.just_fyi('check share and save options on opened image')
        self.chat_2.image_message_in_chat.scroll_to_element(direction='up')
        self.chat_2.image_message_in_chat.click_until_presence_of_element(self.chat_2.share_image_icon_button)
        self.chat_2.share_image_icon_button.click()
        self.chat_2.share_via_messenger()
        if not self.chat_2.image_in_android_messenger.is_element_displayed():
            self.errors.append("Can't share image")
        self.chat_2.click_system_back_button_until_element_is_shown(element=self.chat_2.save_image_icon_button)
        self.chat_2.save_image_icon_button.click()
        self.chat_2.show_images_button.click()
        self.chat_2.allow_button.wait_and_click()

        if not self.chat_2.first_image_from_gallery.is_element_image_similar_to_template('saved.png'):
            self.errors.append("New picture was not saved!")

        self.errors.verify_no_errors()

    @marks.testrail_id(5310)
    def test_1_1_chat_is_shown_message_sent_delivered_from_offline(self):

        self.home_1.home_button.click()
        self.home_2.home_button.click()

        self.home_1.just_fyi('turn on airplane mode and check that offline status is shown on home view')
        self.home_1.toggle_airplane_mode()
        self.home_1.connection_offline_icon.wait_and_click(20)
        for element in self.home_1.not_connected_to_node_text, self.home_1.not_connected_to_peers_text:
            if not element.is_element_displayed():
                self.errors.append(
                    'Element "%s" is not shown in Connection status screen if device is offline' % element.locator)
        self.home_1.click_system_back_button()

        message_1 = 'test message'

        self.home_2.just_fyi("check sent status")
        self.home_2.get_chat(username=self.default_username_1).click()
        self.chat_2.send_message(message_1)
        chat_element = self.chat_2.chat_element_by_text(message_1)
        if chat_element.status != 'sent':
            self.errors.append('Message status is not sent, it is %s!' % chat_element.status)
        self.chat_2.toggle_airplane_mode()

        self.home_1.just_fyi('go back online and check that 1-1 chat will be fetched')
        self.home_1.toggle_airplane_mode()
        chat_element = self.home_1.get_chat(self.default_username_2, wait_time=60)
        chat_element.click()
        self.chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(20)

        self.home_1.just_fyi('checking offline fetching for another message, check delivered status for first message')
        self.chat_2.toggle_airplane_mode()
        if self.chat_2.chat_element_by_text(message_1).status != 'delivered':
            self.errors.append(
                'Message status is not delivered, it is %s!' % self.chat_2.chat_element_by_text(message_1).status)
        self.home_1.toggle_airplane_mode()
        message_2 = 'one more message'
        self.chat_2.send_message(message_2)
        self.home_1.toggle_airplane_mode()
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)
        self.errors.verify_no_errors()

    @marks.testrail_id(5387)
    def test_1_1_chat_delete_via_delete_button_relogin(self):
        self.home_1.driver.quit()
        self.home_2.home_button.click()
        self.home_2.get_chat(username=self.default_username_1).click()

        self.home_2.just_fyi("Deleting chat via delete button and check it will not reappear after relaunching app")
        self.chat_2.delete_chat()
        self.chat_2.get_back_to_home_view()

        if self.home_2.get_chat_from_home_view(self.default_username_1).is_element_displayed():
            self.errors.append('Deleted %s chat is shown, but the chat has been deleted' % self.default_username_1)
        self.home_2.reopen_app()
        if self.home_2.get_chat_from_home_view(self.default_username_1).is_element_displayed():
            self.errors.append(
                'Deleted chat %s is shown after re-login, but the chat has been deleted' % self.default_username_1)
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="two_2")
@marks.critical
class TestContactBlockMigrateKeycardMultipleSharedDevices(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender = transaction_senders['ETH_2']
        self.nick = "FFOO_brak!1234"
        self.message = self.device_1.get_random_message()
        self.pub_chat_name = self.device_1.get_random_chat_name()
        self.home_1 = self.device_1.recover_access(self.sender['passphrase'], keycard=True)
        self.home_2 = self.device_2.create_user()
        self.profile_2 = self.home_2.profile_button.click()
        self.profile_2.privacy_and_security_button.click()
        self.profile_2.backup_recovery_phrase_button.click()
        recovery_phrase = self.profile_2.backup_recovery_phrase()
        self.recovery_phrase = ' '.join(recovery_phrase.values())
        self.public_key_2, self.default_username_2 = self.home_2.get_public_key_and_username(return_username=True)
        self.chat_1 = self.home_1.add_contact(self.public_key_2, add_in_contacts=False)
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.set_nickname(self.nick)
        [home.home_button.click() for home in [self.home_1, self.home_2]]
        self.home_2.add_contact(self.sender['public_key'])
        self.home_2.home_button.click()
        [home.join_public_chat(self.pub_chat_name) for home in [self.home_1, self.home_2]]
        self.chat_2 = self.home_2.get_chat_view()
        self.chat_2.send_message(self.message)
        [home.home_button.click() for home in [self.home_1, self.home_2]]

    @marks.testrail_id(702186)
    def test_keycard_command_send_tx_eth_1_1_chat(self):
        self.home_2.get_chat(self.sender['username']).click()
        self.chat_2.send_message("hey on kk!")
        self.chat_2.home_button.click()

        amount = self.chat_1.get_unique_amount()
        account_name = self.chat_1.status_account_name

        self.chat_1.just_fyi('Send %s ETH in 1-1 chat and check it for sender and receiver: Address requested' % amount)
        self.home_1.get_chat(self.nick).click()
        self.chat_1.send_message("hello again!")
        self.chat_1.commands_button.click()
        send_transaction = self.chat_1.send_command.click()
        send_transaction.get_username_in_transaction_bottom_sheet_button(self.default_username_2).click()
        if send_transaction.scan_qr_code_button.is_element_displayed():
            self.chat_1.driver.fail('Recipient is editable in bottom sheet when send ETH from 1-1 chat')
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        sender_message = self.chat_1.get_outgoing_transaction()
        if not sender_message.is_element_displayed():
            self.chat_1.driver.fail('No message is shown after sending ETH in 1-1 chat for sender')
        sender_message.transaction_status.wait_for_element_text(sender_message.address_requested)

        self.home_2.get_chat(self.sender['username']).click()
        receiver_message = self.chat_2.get_incoming_transaction()
        timestamp_sender = sender_message.timestamp_command_message.text
        if not receiver_message.is_element_displayed(30):
            self.chat_2.driver.fail('No message about incoming transaction in 1-1 chat is shown for receiver')
        receiver_message.transaction_status.wait_for_element_text(receiver_message.address_requested)

        self.chat_1.just_fyi('Accept and share address for sender and receiver')
        for option in (receiver_message.decline_transaction, receiver_message.accept_and_share_address):
            if not option.is_element_displayed():
                self.drivers[0].fail("Required options accept or share are not shown")

        select_account_bottom_sheet = receiver_message.accept_and_share_address.click()
        if not select_account_bottom_sheet.get_account_in_select_account_bottom_sheet_button(
                account_name).is_element_displayed():
            self.errors.append('Not expected value in "From" in "Select account": "Status" is expected')
        select_account_bottom_sheet.select_button.click()
        receiver_message.transaction_status.wait_for_element_text(receiver_message.shared_account)
        sender_message.transaction_status.wait_for_element_text(sender_message.address_request_accepted)

        self.chat_1.just_fyi("Sign and send transaction and check that timestamp on message is updated")
        time.sleep(20)
        send_message = sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        updated_timestamp_sender = sender_message.timestamp_command_message.text
        if updated_timestamp_sender == timestamp_sender:
            self.errors.append("Timestamp of message is not updated after signing transaction")

        wallet_1 = self.chat_1.wallet_button.click()
        wallet_1.find_transaction_in_history(amount=amount)
        self.home_2.put_app_to_background_and_back()
        self.network_api.wait_for_confirmation_of_transaction(self.sender['address'], amount, confirmations=3)
        wallet_1.home_button.click(desired_view='chat')

        self.home_1.just_fyi("Check 'Confirmed' state for sender and receiver(use pull-to-refresh to update history)")
        wallet_2 = self.chat_2.wallet_button.click()
        wallet_2.find_transaction_in_history(amount=amount)
        sender_message.transaction_status.wait_for_element_text(sender_message.confirmed, 120)
        self.errors.verify_no_errors()

    @marks.testrail_id(702175)
    def test_contact_add_remove_mention_default_username_nickname_public_chat(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]
        self.chat_1.just_fyi('check that can mention user with 3-random name in public chat')
        self.home_1.get_chat('#%s' % self.pub_chat_name).click()

        self.chat_1.just_fyi('Set nickname for user without adding him to contacts, check it in public chat')
        chat_element = self.chat_1.chat_element_by_text(self.message)
        expected_username = '%s %s' % (self.nick, self.default_username_2)
        if chat_element.username.text != expected_username:
            self.errors.append('Username %s in public chat does not match expected %s' % (
                chat_element.username.text, expected_username))

        self.chat_1.just_fyi('Add user to contacts, mention it by nickname check contact list in Profile')
        chat_element.member_photo.click()
        self.chat_1.profile_add_to_contacts.click()
        if not self.chat_1.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts'")
        self.chat_1.close_button.click()

        self.chat_1.just_fyi('check that can mention user with nickname or default username in public chat')
        self.chat_1.select_mention_from_suggestion_list(username_in_list=self.nick + ' ' + self.default_username_2,
                                                        typed_search_pattern=self.nick[0:2])
        if self.chat_1.chat_message_input.text != '@' + self.default_username_2 + ' ':
            self.errors.append('Username is not resolved in chat input after selecting it in mention '
                               'suggestions list by nickname!')
        self.chat_1.chat_message_input.clear()
        for pattern in (self.nick[0:2], self.default_username_2[0:4]):
            self.chat_1.select_mention_from_suggestion_list(username_in_list=self.nick + ' ' + self.default_username_2,
                                                            typed_search_pattern=pattern)
            if self.chat_1.chat_message_input.text != '@' + self.default_username_2 + ' ':
                self.errors.append('Username is not resolved in chat input after selecting it in mention suggestions '
                                   'list by default username!')
        additional_text = 'and more'
        self.chat_1.send_as_keyevent(additional_text)
        self.chat_1.send_message_button.click()
        if not self.chat_1.chat_element_by_text('%s %s' % (self.nick, additional_text)).is_element_displayed():
            self.errors.append("Nickname is not resolved on send message")
        self.chat_1.get_back_to_home_view()

        self.chat_1.just_fyi('check contact list in Profile after setting nickname')
        profile_1 = self.chat_1.profile_button.click()
        userprofile = profile_1.open_contact_from_profile(self.nick)
        if not userprofile.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts' in profile contacts")
        profile_1.close_button.click()
        profile_1.home_button.double_click()

        self.chat_1.just_fyi(
            'Check that user is added to contacts below "Start new chat" and you redirected to 1-1 on tap')
        self.home_1.plus_button.click()
        self.home_1.start_new_chat_button.click()
        if not self.home_1.element_by_text(self.nick).is_element_displayed():
            self.home_1.driver.fail('List of contacts below "Start new chat" does not contain added user')
        self.home_1.element_by_text(self.nick).click()
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.chat_1.driver.fail('No redirect to 1-1 chat if tap on Contact below "Start new chat"')
        for element in (self.chat_1.chat_message_input, self.chat_1.element_by_text(self.nick)):
            if not element.is_element_displayed():
                self.errors.append('Expected element is not found in 1-1 after adding user to contacts from profile')
        if self.chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is shown in 1-1 after adding user to contacts from profile')

        self.chat_1.just_fyi('Remove user from contacts')
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.remove_from_contacts.click_until_absense_of_element(self.chat_1.remove_from_contacts)
        if self.chat_1.profile_nickname.text != self.nick:
            self.errors.append("Nickname is changed after removing user from contacts")

        self.chat_1.just_fyi('Check that user is removed from contact list in profile')
        self.chat_1.close_button.click()
        if not self.chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is not shown in 1-1 after removing user from contacts')
        self.chat_1.profile_button.double_click()
        profile_1.contacts_button.click()
        if profile_1.element_by_text(self.nick).is_element_displayed():
            self.errors.append('Contact is shown in Profile after removing user from contacts')
        self.errors.verify_no_errors()

    @marks.testrail_id(702176)
    def test_contact_block_unblock_public_chat_offline(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.chat_1.just_fyi('Block user')
        self.home_1.get_chat("#%s" % self.pub_chat_name).click()
        chat_element = self.chat_1.chat_element_by_text(self.message)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.chat_1.block_contact()

        self.chat_1.just_fyi('messages from blocked user are hidden in public chat and close app')
        if self.chat_1.chat_element_by_text(self.message).is_element_displayed():
            self.errors.append("Messages from blocked user is not cleared in public chat ")
        self.chat_1.home_button.click()
        if self.home_1.element_by_text(self.nick).is_element_displayed():
            self.errors.append("1-1 chat from blocked user is not removed!")
        self.chat_1.toggle_airplane_mode()

        self.home_2.just_fyi('send message to public chat while device 1 is offline')
        message_blocked, message_unblocked = "Message from blocked user", "Hurray! unblocked"
        self.home_2.get_chat("#%s" % self.pub_chat_name).click()
        self.chat_2.send_message(message_blocked)

        self.chat_1.just_fyi('check that new messages from blocked user are not delivered')
        self.chat_1.toggle_airplane_mode()
        self.home_1.get_chat("#%s" % self.pub_chat_name).click()
        for message in self.message, message_blocked:
            if self.chat_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    "'%s' from blocked user is fetched from offline in public chat" % message)

        self.home_1.just_fyi('Verify input field is disabled in 1-1 chat with blocked user')
        self.home_1.home_button.double_click()
        chat_1_1 = self.home_1.add_contact(self.public_key_2, add_in_contacts=False)
        if chat_1_1.chat_message_input.is_element_displayed():
            self.errors.append("Chat input field is displayed in chat with blocked user")
        self.home_1.home_button.double_click()
        self.home_1.get_chat("#%s" % self.pub_chat_name).click()

        self.chat_2.just_fyi('Unblock user and check that can see further messages')
        profile_1 = self.home_1.get_profile_view()
        self.chat_1.profile_button.double_click()
        profile_1.contacts_button.wait_and_click()
        profile_1.blocked_users_button.wait_and_click()
        profile_1.element_by_text(self.nick).click()
        self.chat_1.unblock_contact_button.click()
        self.chat_1.close_button.click()
        [home.home_button.click(desired_view='chat') for home in [self.home_1, self.home_2]]
        self.chat_2.send_message(message_unblocked)
        self.chat_2.home_button.double_click()
        self.home_2.add_contact(self.sender['public_key'])
        self.chat_2.send_message(message_unblocked)
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed():
            self.errors.append("Message was not received in public chat after user unblock!")
        self.chat_1.home_button.click()
        self.home_1.get_chat(self.nick, wait_time=30).click()
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed():
            self.errors.append("Message was not received in 1-1 chat after user unblock!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702188)
    @marks.xfail(
        reason="flaky; issue when sometimes history is not fetched from offline for public chat, needs investigation")
    def test_cellular_settings_on_off_public_chat_fetching_history(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]
        public_chat_name, public_chat_message = 'e2e-started-before', 'message to pub chat'
        public_1 = self.home_1.join_public_chat(public_chat_name)
        public_1.send_message(public_chat_message)

        self.home_2.just_fyi('set mobile data to "OFF" and check that peer-to-peer connection is still working')
        self.home_2.set_network_to_cellular_only()
        self.home_2.mobile_connection_off_icon.wait_for_visibility_of_element(20)
        for element in (self.home_2.continue_syncing_button, self.home_2.stop_syncing_button,
                        self.home_2.remember_my_choice_checkbox):
            if not element.is_element_displayed(10):
                self.drivers[0].fail(
                    'Element %s is not not shown in "Syncing mobile" bottom sheet' % element.locator)
        self.home_2.stop_syncing_button.click()
        if not self.home_2.mobile_connection_off_icon.is_element_displayed():
            self.drivers[0].fail('No mobile connection OFF icon is shown')
        self.home_2.mobile_connection_off_icon.click()
        for element in self.home_2.connected_to_n_peers_text, self.home_2.waiting_for_wi_fi:
            if not element.is_element_displayed():
                self.errors.append("Element '%s' is not shown in Connection status bottom sheet" % element.locator)
        self.home_2.click_system_back_button()
        public_2 = self.home_2.join_public_chat(public_chat_name)
        if public_2.chat_element_by_text(public_chat_message).is_element_displayed(30):
            self.errors.append("Chat history was fetched with mobile data fetching off")
        public_chat_new_message = 'new message'
        public_1.send_message(public_chat_new_message)
        if not public_2.chat_element_by_text(public_chat_new_message).is_element_displayed(30):
            self.errors.append("Peer-to-peer connection is not working when  mobile data fetching is off")

        self.home_2.just_fyi('set mobile data to "ON"')
        self.home_2.home_button.click()
        self.home_2.mobile_connection_off_icon.click()
        self.home_2.use_mobile_data_switch.wait_and_click(30)
        if not self.home_2.connected_to_node_text.is_element_displayed(10):
            self.errors.append("Not connected to history node after enabling fetching on mobile data")
        self.home_2.click_system_back_button()
        self.home_2.mobile_connection_on_icon.wait_for_visibility_of_element(10)
        self.home_2.get_chat('#%s' % public_chat_name).click()
        if not public_2.chat_element_by_text(public_chat_message).is_element_displayed(180):
            self.errors.append("Chat history was not fetched with mobile data fetching ON")

        self.home_2.just_fyi('check redirect to sync settings by tapping on "Sync" in connection status bottom sheet')
        self.home_2.home_button.click()
        self.home_2.mobile_connection_on_icon.click()
        self.home_2.connection_settings_button.click()
        if not self.home_2.element_by_translation_id("mobile-network-use-mobile").is_element_displayed():
            self.errors.append(
                "Was not redirected to sync settings after tapping on Settings in connection bottom sheet")

        self.home_2.just_fyi("Check default preferences in Sync settings")
        profile_1 = self.home_1.get_profile_view()
        self.home_1.profile_button.double_click()
        profile_1.sync_settings_button.click()
        if not profile_1.element_by_translation_id("mobile-network-use-wifi").is_element_displayed():
            self.errors.append("Mobile data is enabled by default")
        profile_1.element_by_translation_id("mobile-network-use-wifi").click()
        if profile_1.ask_me_when_on_mobile_network.text != "ON":
            self.errors.append("'Ask me when on mobile network' is not enabled by default")

        profile_1.just_fyi("Disable 'ask me when on mobile network' and check that it is not shown")
        profile_1.ask_me_when_on_mobile_network.click()
        profile_1.set_network_to_cellular_only()
        if profile_1.element_by_translation_id("mobile-network-start-syncing").is_element_displayed(20):
            self.errors.append("Popup is shown, but 'ask me when on mobile network' is disabled")

        profile_1.just_fyi("Check 'Restore default' setting")
        profile_1.element_by_text('Restore Defaults').click()
        if profile_1.use_mobile_data.attribute_value("checked"):
            self.errors.append("Mobile data is enabled by default")
        if not profile_1.ask_me_when_on_mobile_network.attribute_value("checked"):
            self.errors.append("'Ask me when on mobile network' is not enabled by default")
        self.errors.verify_no_errors()

    @marks.testrail_id(702177)
    def test_restore_account_migrate_multiaccount_to_keycard_db_saved(self):
        self.home_1.driver.quit()
        self.home_2.profile_button.double_click()
        self.profile_2.logout()

        self.device_2.just_fyi("Checking migration to keycard: db saved (1-1 chat, nickname, messages)")
        self.device_2.options_button.click()
        self.device_2.manage_keys_and_storage_button.click()
        self.device_2.move_keystore_file_option.click()
        self.device_2.enter_seed_phrase_next_button.click()
        self.device_2.seedphrase_input.set_value(self.recovery_phrase)
        self.device_2.choose_storage_button.click()
        self.device_2.keycard_required_option.click()
        self.device_2.confirm_button.click()
        self.device_2.migration_password_input.set_value(common_password)
        self.device_2.confirm_button.click()
        from views.keycard_view import KeycardView
        keycard = KeycardView(self.device_2.driver)
        keycard.begin_setup_button.click()
        keycard.connect_card_button.wait_and_click()
        keycard.enter_default_pin()
        keycard.enter_default_pin()
        if not self.device_2.element_by_translation_id("migration-successful").is_element_displayed(30):
            self.driver.fail("No popup about successfull migration is shown!")
        self.device_2.ok_button.click()
        self.home_2.home_button.wait_for_element(30)
        if not self.home_2.element_by_text_part(self.pub_chat_name).is_element_displayed():
            self.errors.append("Public chat was removed from home after migration to kk")
        self.home_2.get_chat(self.sender['username']).click()
        if self.chat_2.add_to_contacts.is_element_displayed():
            self.errors.append("User was removed from contacts after migration to kk")
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="three_2")
@marks.critical
class TestEnsStickersMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender, self.reciever = transaction_senders['ETH_3'], ens_user
        self.home_1 = self.device_1.recover_access(passphrase=self.sender['passphrase'])
        self.home_2 = self.device_2.recover_access(ens_user['passphrase'], enable_notifications=True)
        self.ens = '@%s' % self.reciever['ens']
        self.pub_chat_name = self.home_1.get_random_chat_name()
        self.chat_1 = self.home_1.join_public_chat(self.pub_chat_name)
        self.chat_2 = self.home_2.join_public_chat(self.pub_chat_name)
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.profile_2 = self.home_2.profile_button.click()
        self.profile_2.connect_existing_ens(self.reciever['ens'])
        self.home_1.add_contact(self.reciever['ens'])
        self.home_2.home_button.click()
        self.home_2.add_contact(self.sender['public_key'])
        # To avoid activity centre for restored users
        [chat.send_message("hey!") for chat in (self.chat_1, self.chat_2)]

        self.home_1.just_fyi("Close the ENS banner")
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        [home.ens_banner_close_button.click_if_shown() for home in (self.home_1, self.home_2)]

    @marks.testrail_id(702152)
    def test_ens_purchased_in_profile(self):
        self.home_2.profile_button.double_click()
        ens_name_after_adding = self.profile_2.default_username_text.text
        if ens_name_after_adding != '@%s' % ens_user['ens']:
            self.errors.append('ENS name is not shown as default in user profile after adding, "%s" instead' %
                               ens_name_after_adding)

        self.home_2.just_fyi('check ENS name wallet address and public key')
        self.home_2.element_by_text(self.reciever['ens']).click()
        self.home_2.element_by_text(self.reciever['ens']).click()
        for text in (self.reciever['address'].lower(), self.reciever['public_key']):
            if not self.home_2.element_by_text_part(text).is_element_displayed(40):
                self.errors.append('%s text is not shown' % text)
        self.home_2.profile_button.double_click()

        self.home_2.just_fyi('check ENS name is shown on share my profile window')
        self.profile_2.share_my_profile_button.click()
        if self.profile_2.ens_name_in_share_chat_key_text.text != '%s' % ens_user['ens']:
            self.errors.append('No ENS name is shown on tapping on share icon in Profile')
        self.profile_2.close_share_popup()
        self.errors.verify_no_errors()

    @marks.testrail_id(702153)
    def test_ens_command_send_tx_eth_1_1_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        wallet_1 = self.home_1.wallet_button.click()
        wallet_1.wait_balance_is_changed()
        wallet_1.home_button.click()
        self.home_1.get_chat(self.ens).click()
        self.chat_1.commands_button.click()
        amount = self.chat_1.get_unique_amount()

        self.chat_1.just_fyi("Check sending assets to ENS name from sender side")
        send_message = self.chat_1.send_command.click()
        send_message.amount_edit_box.set_value(amount)
        send_message.confirm()
        send_message.next_button.click()
        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.drivers[0])
        send_transaction.ok_got_it_button.click()
        send_transaction.sign_transaction()
        chat_1_sender_message = self.chat_1.get_outgoing_transaction(transaction_value=amount)
        self.home_2.put_app_to_background_and_back()
        self.network_api.wait_for_confirmation_of_transaction(self.sender['address'], amount, confirmations=3)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)

        self.chat_2.just_fyi("Check that message is fetched for receiver")
        self.home_2.get_chat(self.sender['username']).click()
        chat_2_reciever_message = self.chat_2.get_incoming_transaction(transaction_value=amount)
        chat_2_reciever_message.transaction_status.wait_for_element_text(chat_2_reciever_message.confirmed,
                                                                         wait_time=60)

    @marks.testrail_id(702155)
    def test_ens_mention_nickname_1_1_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi('Mention user by ENS in 1-1 chat')
        message, message_ens_owner = '%s hey!' % self.ens, '%s hey!' % self.reciever['ens']
        self.home_1.get_chat(self.ens).click()
        self.chat_1.send_message(message)

        self.home_1.just_fyi('Set nickname and mention user by nickname in 1-1 chat')
        russian_nickname = 'МОЙ дорогой ДРУх'
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.set_nickname(russian_nickname)
        self.chat_1.select_mention_from_suggestion_list(russian_nickname + ' ' + self.ens)

        self.chat_1.just_fyi('Check that nickname is shown in preview for 1-1 chat')
        updated_message = '%s hey!' % russian_nickname
        self.chat_1.home_button.double_click()
        if not self.chat_1.element_by_text(updated_message).is_element_displayed():
            self.errors.append('"%s" is not show in chat preview on home screen!' % message)
        self.home_1.get_chat(russian_nickname).click()

        self.chat_1.just_fyi('Check redirect to user profile on mention by nickname tap')
        self.chat_1.chat_element_by_text(updated_message).click()
        if not self.chat_1.profile_block_contact.is_element_displayed():
            self.errors.append(
                'No redirect to user profile after tapping on message with mention (nickname) in 1-1 chat')
        else:
            self.chat_1.profile_send_message.click()

        self.chat_2.just_fyi("Check message with mention for ENS owner")
        self.home_2.get_chat(self.sender['username']).click()
        if not self.chat_2.chat_element_by_text(message_ens_owner).is_element_displayed():
            self.errors.append('Expected %s message is not shown for ENS owner' % message_ens_owner)

        self.chat_1.just_fyi('Check if after deleting nickname ENS is shown again')
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.profile_nickname_button.click()
        self.chat_1.nickname_input_field.clear()
        self.chat_1.element_by_text('Done').click()
        self.chat_1.close_button.click()
        if self.chat_1.user_name_text.text != self.ens:
            self.errors.append("Username '%s' is not updated to ENS '%s' after deleting nickname" %
                               (self.chat_1.user_name_text.text, self.ens))

        self.errors.verify_no_errors()

    @marks.testrail_id(702156)
    def test_ens_mention_push_highlighted_public_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.home_2.get_chat('#%s' % self.pub_chat_name).click()
        text = "From ENS name user!"
        self.chat_2.send_message(text)
        self.chat_2.home_button.click()

        self.home_2.put_app_to_background()
        self.home_2.open_notification_bar()

        self.home_1.just_fyi('check that can mention user with ENS name')
        self.home_1.get_chat('#%s' % self.pub_chat_name).click()
        self.chat_1.wait_ens_name_resolved_in_chat(message=text, username_value='@%s' % self.reciever['ens'])
        self.chat_1.select_mention_from_suggestion_list(self.reciever['ens'])
        if self.chat_1.chat_message_input.text != self.ens + ' ':
            self.errors.append(
                'ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        self.chat_1.send_message_button.click()

        self.home_2.just_fyi(
            'check that PN is received and after tap you are redirected to chat, mention is highligted')
        pn = self.home_2.get_pn(self.reciever['username'])
        if pn:
            pn.click()
        else:
            self.errors.append('No PN on mention in public chat! ')
            self.home_2.click_system_back_button(2)
        if self.home_2.element_starts_with_text(self.reciever['ens']).is_element_differs_from_template('ment_new_1.png',
                                                                                                       2):
            self.errors.append('Mention is not highlighted!')
        self.errors.verify_no_errors()

    @marks.testrail_id(702157)
    def test_sticker_1_1_public_chat_mainnet(self):
        self.home_2.status_in_background_button.click_if_shown()
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        profile_2 = self.home_2.profile_button.click()
        profile_2.switch_network()

        self.home_2.just_fyi('Check that can use purchased stickerpack on Mainnet')
        self.home_2.get_chat('#%s' % self.pub_chat_name).click()
        self.chat_2.install_sticker_pack_by_name('Tozemoon')
        self.chat_2.sticker_icon.click()
        if not self.chat_2.chat_item.is_element_displayed():
            self.errors.append('Cannot use purchased stickers')
        self.home_2.profile_button.click()
        profile_2.switch_network('Goerli with upstream RPC')

        self.home_1.just_fyi('Install free sticker pack and use it in 1-1 chat on Goerli')
        self.home_1.get_chat(self.ens).click()
        self.chat_1.chat_message_input.clear()
        self.chat_1.install_sticker_pack_by_name()
        self.chat_1.sticker_icon.click()
        if not self.chat_1.sticker_message.is_element_displayed():
            self.errors.append('Sticker was not sent')
        self.chat_1.swipe_right()
        if not self.chat_1.sticker_icon.is_element_displayed():
            self.errors.append('Sticker is not shown in recently used list')
        self.chat_1.get_back_to_home_view()

        self.home_1.just_fyi('Send stickers in public chat from Recent')
        self.home_1.join_public_chat(self.home_1.get_random_chat_name())
        self.chat_1.show_stickers_button.click()
        self.chat_1.sticker_icon.click()
        if not self.chat_1.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent from Recent')

        # self.home_2.just_fyi('Check that can install stickers by tapping on sticker message')
        # TODO: disabled because of #13683 (rechecked 04.10.22, valid)
        self.home_2.home_button.double_click()
        self.home_2.get_chat(self.sender['username']).click()
        # self.chat_2.chat_item.click()
        # self.chat_2.element_by_text_part('Free').wait_and_click(40)
        # if self.chat_2.element_by_text_part('Free').is_element_displayed():
        #     self.errors.append('Stickerpack was not installed')

        self.chat_2.just_fyi('Check that can navigate to another user profile via long tap on sticker message')
        # self.chat_2.close_sticker_view_icon.click()
        self.chat_2.chat_item.long_press_element()
        self.chat_2.element_by_text('View Details').click()
        self.chat_2.profile_send_message.wait_and_click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702158)
    def test_start_new_chat_public_key_validation(self):
        [home.get_back_to_home_view() for home in (self.home_1, self.home_2)]
        self.home_2.driver.quit()
        public_key = basic_user['public_key']
        self.home_1.plus_button.click()
        chat = self.home_1.start_new_chat_button.click()

        self.home_1.just_fyi("Validation: invalid public key and invalid ENS")
        for invalid_chat_key in (basic_user['public_key'][:-1], ens_user_message_sender['ens'][:-2]):
            chat.public_key_edit_box.clear()
            chat.public_key_edit_box.set_value(invalid_chat_key)
            chat.confirm()
            if not self.home_1.element_by_translation_id("profile-not-found").is_element_displayed():
                self.errors.append('Error is not shown for invalid public key')

        self.home_1.just_fyi("Check that valid ENS is resolved")
        chat.public_key_edit_box.clear()
        chat.public_key_edit_box.set_value(ens_user_message_sender['ens'])
        resolved_ens = '%s.stateofus.eth' % ens_user_message_sender['ens']
        if not chat.element_by_text(resolved_ens).is_element_displayed(10):
            self.errors.append('ENS name is not resolved after pasting chat key')
        self.home_1.close_button.click()

        self.home_1.just_fyi("Check that can paste public key from keyboard and start chat")
        self.home_1.get_chat('#%s' % self.pub_chat_name).click()
        chat.send_message(public_key)
        chat.copy_message_text(public_key)
        chat.back_button.click()
        self.home_1.plus_button.click()
        self.home_1.start_new_chat_button.click()
        chat.public_key_edit_box.paste_text_from_clipboard()
        if chat.public_key_edit_box.text != public_key:
            self.errors.append('Public key is not pasted from clipboard')
        if not chat.element_by_text(basic_user['username']).is_element_displayed():
            self.errors.append('3 random-name is not resolved after pasting chat key')

        self.home_1.just_fyi('My_profile button at Start new chat view opens own QR code with public key pop-up')
        self.home_1.my_profile_on_start_new_chat_button.click()
        account = self.home_1.get_profile_view()
        if not (account.public_key_text.is_element_displayed() and account.share_button.is_element_displayed()
                and account.qr_code_image.is_element_displayed()):
            self.errors.append('No self profile pop-up data displayed after My_profile button tap')
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="one_2")
@marks.new_ui_critical
class TestOneToOneChatMultipleSharedDevicesNewUi(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.profile_1 = self.home_1.get_profile_view()
        users = self.loop.run_until_complete(run_in_parallel(
            ((self.home_1.get_public_key_and_username, True),
             (self.home_2.get_public_key_and_username, True))
        ))
        self.public_key_1, self.default_username_1 = users[0]
        self.public_key_2, self.default_username_2 = users[1]

        self.profile_1.switch_push_notifications()

        self.profile_1.just_fyi("Sending contact request via Profile > Contacts")
        self.profile_1.click_system_back_button_until_element_is_shown(self.profile_1.contacts_button)
        self.profile_1.add_contact_via_contacts_list(self.public_key_2)
        self.chat_1 = self.profile_1.open_contact_from_profile(self.default_username_2)

        self.home_2.just_fyi("Accepting contact request from activity centre")
        self.home_2.chats_tab.click()
        self.home_2.handle_contact_request(self.default_username_1)

        self.profile_1.just_fyi("Sending message to contact via Profile > Contacts > Send message")
        self.chat_1.profile_send_message.click()
        self.chat_1.send_message('hey')
        self.home_2.click_system_back_button_until_element_is_shown()
        self.chat_2 = self.home_2.get_chat(self.default_username_1).click()
        self.message_1, self.message_2, self.message_3, self.message_4 = \
            "Message 1", "Message 2", "Message 3", "Message 4"

    @marks.testrail_id(702730)
    def test_1_1_chat_message_reaction(self):
        message_from_sender = "Message sender"
        self.device_1.just_fyi("Sender start 1-1 chat, set emoji and check counter")
        self.chat_1.send_message(message_from_sender)
        self.chat_1.set_reaction(message_from_sender)

        message_sender = self.chat_1.chat_element_by_text(message_from_sender)
        message_sender.emojis_below_message().wait_for_element_text(1)

        self.device_2.just_fyi("Receiver sets own emoji and verifies counter on received message in 1-1 chat")
        message_receiver = self.chat_2.chat_element_by_text(message_from_sender)
        message_receiver.emojis_below_message().wait_for_element_text(1, 90)
        self.chat_2.set_reaction(message_from_sender)

        self.device_2.just_fyi("Receiver pick the same emoji and verify that counter will decrease for both users")
        self.chat_2.set_reaction(message_from_sender)
        message_sender.emojis_below_message().wait_for_element_text(1)
        message_receiver.emojis_below_message().wait_for_element_text(1, 90)
        self.errors.verify_no_errors()

    @marks.testrail_id(702731)
    def test_1_1_chat_pin_messages(self):
        self.home_1.just_fyi("Check that Device1 can pin own message in 1-1 chat")
        self.chat_1.send_message(self.message_1)
        self.chat_1.send_message(self.message_2)
        self.chat_1.pin_message(self.message_1, 'pin-to-chat')
        if not self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed():
            self.drivers[0].fail("Message is not pinned!")

        self.home_1.just_fyi("Check that Device2 can pin Device1 message in 1-1 chat and two pinned "
                             "messages are in Device1 profile")
        self.chat_2.pin_message(self.message_2, 'pin-to-chat')
        for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
            chat.pinned_messages_count.wait_for_element_text("2",
                                                             message="Pinned messages count is not 2 as expected!")

            self.home_1.just_fyi("Check pinned message are visible in Pinned panel for user %s" % (chat_number + 1))
            chat.pinned_messages_count.click()
            for message in self.message_1, self.message_2:
                pinned_by = chat.pinned_messages_list.get_message_pinned_by_text(message)
                if pinned_by.is_element_displayed():
                    text = pinned_by.text.strip()
                    if chat_number == 0:
                        expected_text = "You" if message == self.message_1 else self.default_username_2
                    else:
                        expected_text = "You" if message == self.message_2 else self.default_username_1
                    if text != expected_text:
                        self.errors.append(
                            "Pinned by '%s' doesn't match expected '%s' for user %s" % (
                                text, expected_text, chat_number + 1)
                        )
                else:
                    self.errors.append(
                        "Message '%s' is missed on Pinned messages list for user %s" % (message, chat_number + 1)
                    )
            # workaround for 14672
            chat.tap_by_coordinates(500, 100)
        # Part of the test is blocked by #14637
        #     chat.click_system_back_button()
        #
        # self.home_1.just_fyi("Check that Device1 can not pin more than 3 messages and 'Unpin' dialog appears")
        # self.chat_1.send_message(self.message_3)
        # self.chat_1.send_message(self.message_4)
        # self.chat_1.pin_message(self.message_3, 'pin-to-chat')
        # self.chat_1.pin_message(self.message_4, 'pin-to-chat')
        # if self.chat_1.pin_limit_popover.is_element_displayed(30):
        #     self.chat_1.view_pinned_messages_button.click()
        #     self.chat_1.pinned_messages_list.message_element_by_text(
        #         self.message_2).click_inside_element_by_coordinate()
        #     self.home_1.just_fyi("Unpin one message so that another could be pinned")
        #     self.chat_1.element_by_translation_id('unpin-from-chat').double_click()
        #     self.chat_1.chat_element_by_text(self.message_4).click()
        #     self.chat_1.pin_message(self.message_4, 'pin-to-chat')
        #     if not (self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30) and
        #             self.chat_2.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30)):
        #         self.errors.append("Message 4 is not pinned in chat after unpinning previous one")
        # else:
        #     self.errors.append("Can pin more than 3 messages in chat")
        #
        # self.home_1.just_fyi("Check pinned messages are visible in Pinned panel for both users")
        # for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
        #     count = chat.pinned_messages_count.text
        #     if count != '3':
        #         self.errors.append("Pinned messages count is not 3 for user %s" % (chat_number + 1))
        #
        # self.home_1.just_fyi("Unpin one message and check it's unpinned for another user")
        # self.chat_2.chat_element_by_text(self.message_4).long_press_element()
        # self.chat_2.element_by_translation_id("unpin-from-chat").click()
        # self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.wait_for_invisibility_of_element()
        # if self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed():
        #     self.errors.append("Message_4 is not unpinned!")
        #
        # for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
        #     count = chat.pinned_messages_count.text
        #     if count != '2':
        #         self.errors.append(
        #             "Pinned messages count is not 2 after unpinning the last pinned message for user %s" % (
        #                     chat_number + 1)
        #         )
        self.errors.verify_no_errors()

    @marks.testrail_id(702745)
    @marks.xfail(reason="On profile picture failed due to #14718")
    def test_1_1_chat_non_latin_messages_stack_update_profile_photo(self):
        self.home_1.click_system_back_button_until_element_is_shown()
        self.home_1.browser_tab.click()  # temp, until profile is on browser tab
        self.profile_1.edit_profile_picture('sauce_logo.png')
        self.profile_1.chats_tab.click()

        self.chat_2.just_fyi("Send messages with non-latin symbols")
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.chat_1.click_system_back_button_until_element_is_shown()
            self.home_1.get_chat(self.default_username_2).click()
        self.chat_1.send_message("workaround for 14637")
        messages = ['hello', '¿Cómo estás tu año?', 'ё, доброго вечерочка', '®	æ ç ♥']
        [self.chat_2.send_message(message) for message in messages]
        for message in messages:
            if not self.chat_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % message)

        self.chat_2.just_fyi("Checking updated member photo, timestamp and username on message")
        timestamp = self.chat_2.chat_element_by_text(messages[0]).timestamp
        sent_time_variants = self.chat_2.convert_device_time_to_chat_timestamp()
        if timestamp not in sent_time_variants:
            self.errors.append(
                'Timestamp on message %s does not correspond expected [%s]' % (timestamp, *sent_time_variants))
        for message in [messages[1], messages[2]]:
            if self.chat_2.chat_element_by_text(message).member_photo.is_element_displayed():
                self.errors.append('%s is not stack to 1st(they are sent in less than 5 minutes)!' % message)

        self.chat_1.just_fyi("Sending message while user is still not in contacts")
        message = 'profile_photo'
        self.chat_1.send_message(message)
        self.chat_2.chat_element_by_text(message).wait_for_visibility_of_element(30)
        # Should be checked in CR flow, as for now no way to start chat with user until he is added to contacts
        # if not self.chat_2.chat_element_by_text(message).member_photo.is_element_differs_from_template("member2.png",
        #                                                                                                diff=5):
        #     self.errors.append("Image of user in 1-1 chat is updated even when user is not added to contacts!")

        self.chat_1.just_fyi("Users add to contacts each other")
        [home.click_system_back_button_until_element_is_shown() for home in (self.home_1, self.home_2)]
        [home.browser_tab.click() for home in (self.home_1, self.home_2)]
        self.profile_1.add_contact_via_contacts_list(self.public_key_2)
        self.profile_2 = self.home_2.get_profile_view()
        self.profile_2.add_contact_via_contacts_list(self.public_key_1)

        self.chat_1.just_fyi("Go back to chat view and checking that profile photo is updated")
        [home.chats_tab.click() for home in (self.home_1, self.home_2)]
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.home_2.get_chat(self.default_username_1).click()
        if self.chat_2.chat_element_by_text(message).member_photo.is_element_differs_from_template("member3.png",
                                                                                                   diff=5):
            self.errors.append("Image of user in 1-1 chat is too different from template!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702733)
    def test_1_1_chat_text_message_delete_push_disappear(self):
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.home_1.get_chat(self.default_username_2).click()
        self.device_2.just_fyi("Verify Device1 can not edit and delete received message from Device2")
        message_after_edit_1_1 = 'smth I should edit'
        self.chat_2.send_message(message_after_edit_1_1)
        chat_1_element = self.chat_1.chat_element_by_text(message_after_edit_1_1)
        chat_1_element.long_press_element()
        for action in ("edit", "delete-for-everyone"):
            if self.chat_1.element_by_translation_id(action).is_element_displayed():
                self.errors.append('Option to %s someone else message available!' % action)
        self.home_1.click_system_back_button()

        self.device_2.just_fyi("Delete message for everyone and check it is not shown in chat preview on home")
        self.chat_2.delete_message_in_chat(message_after_edit_1_1)
        for chat in (self.chat_2, self.chat_1):
            if chat.chat_element_by_text(message_after_edit_1_1).is_element_displayed(30):
                self.errors.append("Deleted message is shown in chat view for 1-1 chat")
        self.chat_1.click_system_back_button_until_element_is_shown()
        if self.home_1.element_by_text(message_after_edit_1_1).is_element_displayed(30):
            self.errors.append("Deleted message is shown on chat element on home screen")

        self.device_2.just_fyi("Send one more message and check that PN will be deleted with message deletion")
        message_to_delete = 'DELETE ME'
        self.home_1.put_app_to_background()
        self.chat_2.send_message(message_to_delete)
        self.home_1.open_notification_bar()
        if not self.home_1.get_pn(message_to_delete):
            self.errors.append("Push notification doesn't appear")
        self.chat_2.delete_message_in_chat(message_to_delete)
        pn_to_disappear = self.home_1.get_pn(message_to_delete)
        if pn_to_disappear:
            if not pn_to_disappear.is_element_disappeared(30):
                self.errors.append("Push notification was not removed after initial message deletion")

        self.errors.verify_no_errors()
