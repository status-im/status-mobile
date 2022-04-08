import emoji
import random
import pytest

from time import sleep
from tests import marks, common_password
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten
from views.sign_in_view import SignInView
from views.send_transaction_view import SendTransactionView


@pytest.mark.xdist_group(name="1_1_chat_2")
@marks.critical
class TestOneToOneChatMultipleSharedDevices(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.home_1 = cls.device_1.create_user(enable_notifications=True)
        cls.home_2 = cls.device_2.create_user(enable_notifications=True)
        cls.profile_1 = cls.home_1.profile_button.click()
        cls.default_username_1 = cls.profile_1.default_username_text.text
        cls.profile_1.home_button.click()
        cls.public_key_2, cls.default_username_2 = cls.home_2.get_public_key_and_username(return_username=True)
        cls.chat_1 = cls.home_1.add_contact(cls.public_key_2)
        cls.chat_1.send_message('hey')
        cls.home_2.home_button.double_click()
        cls.chat_2 = cls.home_2.get_chat(cls.default_username_1).click()

    @marks.testrail_id(6315)
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
    def test_1_1_chat_text_message_with_push(self):
        self.home_2.just_fyi("Put app on background (to check Push notification received for audio message)")
        self.home_2.click_system_home_button()

        self.home_2.just_fyi("Sending audio message to device who is on background")
        self.chat_1.record_audio_message(message_length_in_seconds=125)
        if not self.chat_1.element_by_text("Maximum recording time reached").is_element_displayed():
            self.drivers[0].fail("Exceeded 2 mins limit of recording time.")

        self.chat_1.ok_button.click()
        if self.chat_1.audio_message_recorded_time.text != "1:59":
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
    def test_1_1_chat_emoji_and_link_send_and_open(self):
        self.home_1.just_fyi("Check that can send emoji in 1-1 chat")
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        self.chat_1.send_message(emoji.emojize(emoji_name))
        for chat in self.chat_1, self.chat_2:
            if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
                self.errors.append('Message with emoji was not sent or received in 1-1 chat')

        self.home_1.just_fyi("Check that link can be opened from 1-1 chat")
        url_message = 'http://status.im'
        self.chat_1.send_message(url_message)
        self.chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = self.chat_2.open_in_status_button.click()
        if not web_view.element_by_text('Private, Secure Communication').is_element_displayed(60):
            self.errors.append('URL was not opened from 1-1 chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(695843)
    def test_1_1_chat_text_message_edit_delete_push_disappear(self):
        self.device_2.just_fyi(
            "Device 1 sends text message and edits it in 1-1 chat. Device2 checks edited message is shown")
        message_before_edit_1_1, message_after_edit_1_1 = "Message before edit 1-1", "AFTER"
        self.chat_1.home_button.click()
        self.chat_2.home_button.click()
        self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.send_message(message_before_edit_1_1)

        self.chat_2.edit_message_in_chat(message_before_edit_1_1, message_after_edit_1_1)
        if not self.home_1.element_by_text_part(message_after_edit_1_1).is_element_present():
            self.errors.append('UNedited message version displayed on preview')
        self.home_1.get_chat(self.default_username_2).click()
        chat_element = self.chat_1.chat_element_by_text(message_after_edit_1_1)
        if not chat_element.is_element_present(30):
            self.errors.append('No edited message in 1-1 chat displayed')
        if not self.chat_1.element_by_text_part("⌫ Edited").is_element_present(30):
            self.errors.append('No mark in message bubble about this message was edited on receiver side')

        self.device_2.just_fyi("Verify Device1 can not edit and delete received message from Device2")
        chat_element.long_press_element()
        for action in ("edit", "delete"):
            if self.chat_1.element_by_translation_id(action).is_element_present():
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

        # TODO: disabled until https://github.com/status-im/status-react/issues/12936 fix
        # profile_1.just_fyi("Check timestamps for sender and receiver")
        # for chat in device_1_chat, device_2_chat:
        #     chat.verify_message_is_under_today_text(timestamp_message, self.errors)
        #     timestamp = chat.chat_element_by_text(timestamp_message).timestamp_message.text
        #     if timestamp not in sent_time_variants:
        #         self.errors.append(
        #             "Timestamp is not shown, expected '%s', in fact '%s'" % (sent_time_variants.join(","), timestamp))

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

        # TODO: Should be moved to group or test where no contact is added in prerequisites
        # self.device_2.just_fyi("Device 2: check there is no PN when receiving new message to activity centre")
        # self.device_2.put_app_to_background()
        # if not self.chat_1.chat_message_input.is_element_displayed():
        #     self.home_1.get_chat(username=self.default_username_2).click()
        # self.chat_1.send_message(message_no_pn)
        # self.device_2.open_notification_bar()
        # if self.home_2.element_by_text(message_no_pn).is_element_displayed():
        #     self.errors.append("Push notification with text was received for new message in activity centre")
        # self.device_2.get_app_from_background()
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
        self.chat_2.image_message_in_chat.click()
        self.chat_2.share_image_icon_button.click()
        self.chat_2.share_via_messenger()
        if not self.chat_2.image_in_android_messenger.is_element_present():
            self.errors.append("Can't share image")
        self.chat_2.click_system_back_button()
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


@pytest.mark.xdist_group(name="chats_contacts_keycard_2")
@marks.critical
class TestContactBlockMigrateKeycardMultipleSharedDevices(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.sender = transaction_senders['ETH_2']
        cls.nick = "FFOO_brak!1234"
        cls.message = cls.device_1.get_random_message()
        cls.pub_chat_name = cls.device_1.get_random_chat_name()
        cls.home_1 = cls.device_1.recover_access(cls.sender['passphrase'], keycard=True)
        cls.home_2 = cls.device_2.create_user()
        cls.profile_2 = cls.home_2.profile_button.click()
        cls.profile_2.privacy_and_security_button.click()
        cls.profile_2.backup_recovery_phrase_button.click()
        recovery_phrase = cls.profile_2.backup_recovery_phrase()
        cls.recovery_phrase = ' '.join(recovery_phrase.values())
        cls.public_key_2, cls.default_username_2 = cls.home_2.get_public_key_and_username(return_username=True)
        cls.chat_1 = cls.home_1.add_contact(cls.public_key_2, add_in_contacts=False)
        cls.chat_1.chat_options.click()
        cls.chat_1.view_profile_button.click()
        cls.chat_1.set_nickname(cls.nick)
        [home.home_button.click() for home in [cls.home_1, cls.home_2]]
        cls.home_2.add_contact(cls.sender['public_key'])
        cls.home_2.home_button.click()
        [home.join_public_chat(cls.pub_chat_name) for home in [cls.home_1, cls.home_2]]
        cls.chat_2 = cls.home_2.get_chat_view()
        cls.chat_2.send_message(cls.message)
        [home.home_button.click() for home in [cls.home_1, cls.home_2]]

    @marks.testrail_id(702186)
    def test_keycard_command_send_eth_1_1_chat(self):
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
        sleep(20)
        send_message = sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        updated_timestamp_sender = sender_message.timestamp_command_message.text
        if updated_timestamp_sender == timestamp_sender:
            self.errors.append("Timestamp of message is not updated after signing transaction")

        wallet_1 = self.chat_1.wallet_button.click()
        wallet_1.find_transaction_in_history(amount=amount)
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
                                                            typed_search_pattern=self.default_username_2[0:4])
            if self.chat_1.chat_message_input.text != '@' + self.default_username_2 + ' ':
                self.errors.append('Username is not resolved in chat input after selecting it in mention '
                               'suggestions list by default username!')
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
                    "'%s' from blocked user is fetched from offline in public chat" % (message))

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


class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):
    @marks.testrail_id(5362)
    @marks.medium
    def test_unread_messages_counter_preview_highlited_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_2 = home_2.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        home_2 = profile_2.home_button.click()
        public_key_1 = home_1.get_public_key_and_username()
        home_1.home_button.click()
        chat_2 = home_2.add_contact(public_key_1)

        message, message_2, message_3 = 'test message', 'test message2', 'test'
        chat_2.send_message(message)
        chat_element = home_1.get_chat(default_username_2)
        home_1.dapp_tab_button.click()
        chat_2.send_message(message_2)

        if home_1.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')
        device_1.home_button.click()
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')
        chat_1 = chat_element.click()
        chat_1.add_to_contacts.click()

        home_1.home_button.double_click()

        if home_1.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')
        if chat_element.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element for already seen message')
        home_1.delete_chat_long_press(default_username_2)

        home_1.just_fyi("Checking preview of message and chat highlighting")
        chat_2.send_message(message_3)
        chat_1_element = home_1.get_chat(default_username_2)
        if chat_1_element.chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is not hightligted or text is not shown! ")
        home_1.get_chat(default_username_2).click()
        home_1.home_button.double_click()
        if not home_1.get_chat(default_username_2).chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is still highlighted after opening ")
        self.errors.verify_no_errors()


class TestMessagesOneToOneChatSingle(SingleDeviceTestCase):

    @marks.testrail_id(5322)
    @marks.medium
    def test_delete_cut_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(transaction_senders['N']['public_key'])

        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)

        message_input.delete_last_symbols(2)
        current_text = message_input.text
        if current_text != message_text[:-2]:
            self.driver.fail("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        message_input.cut_text()

        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2]).wait_for_visibility_of_element(2)

    @marks.testrail_id(6298)
    @marks.medium
    def test_can_scan_qr_with_chat_key_from_home_start_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(basic_user['passphrase'])

        url_data = {
            'ens_with_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s.stateofus.eth' % ens_user_ropsten['ens'],
                'username': '@%s' % ens_user_ropsten['ens']
            },
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user_ropsten['ens'],
                'username': '@%s' % ens_user_ropsten['ens']
            },
            'ens_another_domain_deep_link': {
                'url': 'status-im://u/%s' % ens_user['ens_another'],
                'username': '@%s' % ens_user['ens_another']
            },
            'own_profile_key_deep_link': {
                'url': 'https://join.status.im/u/%s' % basic_user['public_key'],
                'error': "That's you"
            },
            'other_user_profile_key_deep_link': {
                'url': 'https://join.status.im/u/%s' % transaction_senders['M']['public_key'],
                'username': transaction_senders['M']['username']
            },
            'other_user_profile_key_deep_link_invalid': {
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Please enter or scan a valid chat key'
            },
            'own_profile_key': {
                'url': basic_user['public_key'],
                'error': "That's you"
            },
            # 'ens_without_stateofus_domain': {
            #     'url': ens_user['ens'],
            #     'username': ens_user['username']
            # },
            'other_user_profile_key': {
                'url': transaction_senders['M']['public_key'],
                'username': transaction_senders['M']['username']
            },
            'other_user_profile_key_invalid': {
                'url': '%s123' % ens_user['public_key'],
                'error': 'Please enter or scan a valid chat key'
            },
        }

        for key in url_data:
            home.plus_button.click_until_presence_of_element(home.start_new_chat_button)
            contacts = home.start_new_chat_button.click()
            sign_in.just_fyi('Checking scanning qr for "%s" case' % key)
            contacts.scan_contact_code_button.click()
            if contacts.allow_button.is_element_displayed():
                contacts.allow_button.click()
            contacts.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            from views.chat_view import ChatView
            chat = ChatView(self.driver)
            if url_data[key].get('error'):
                if not chat.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat.ok_button.click()
            if url_data[key].get('username'):
                if not chat.chat_message_input.is_element_displayed():
                    self.errors.append(
                        'In "%s" case chat input is not found after scanning, so no redirect to 1-1' % key)
                if not chat.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In "%s" case "%s" not found after scanning' % (key, url_data[key]['username']))
                chat.get_back_to_home_view()
        self.errors.verify_no_errors()

    @marks.testrail_id(6322)
    @marks.medium
    def test_can_scan_different_links_with_universal_qr_scanner(self):
        user = transaction_senders['ETH_STT_3']
        home = SignInView(self.driver).recover_access(user['passphrase'])
        wallet = home.wallet_button.click()
        wallet.home_button.click()
        send_transaction = SendTransactionView(self.driver)

        url_data = {
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user_ropsten['ens'],
                'username': '@%s' % ens_user_ropsten['ens']
            },

            'other_user_profile_key_deep_link': {
                'url': 'status-im://u/%s' % basic_user['public_key'],
                'username': basic_user['username']
            },
            'other_user_profile_key_deep_link_invalid': {
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Unable to read this code'
            },
            'own_profile_key': {
                'url': user['public_key'],
            },
            'other_user_profile_key': {
                'url': transaction_senders['A']['public_key'],
                'username': transaction_senders['A']['username']
            },
            'wallet_validation_wrong_address_transaction': {
                'url': 'ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e@3/transfer?address=blablabla&uint256=1e10',
                'error': 'Invalid address',
            },
            'wallet_eip_ens_for_receiver': {
                'url': 'ethereum:0xc55cf4b03948d7ebc8b9e8bad92643703811d162@3/transfer?address=nastya.stateofus.eth&uint256=1e-1',
                'data': {
                    'asset': 'STT',
                    'amount': '0.1',
                    'address': '0x58d8…F2ff',
                },
            },
            'wallet_eip_payment_link': {
                'url': 'ethereum:pay-0xc55cf4b03948d7ebc8b9e8bad92643703811d162@3/transfer?address=0x3d597789ea16054a084ac84ce87f50df9198f415&uint256=1e1',
                'data': {
                    'amount': '10',
                    'asset': 'STT',
                    'address': '0x3D59…F415',
                },
            },
            'dapp_deep_link': {
                'url': 'https://join.status.im/b/simpledapp.eth',
            },
            'dapp_deep_link_https': {
                'url': 'https://join.status.im/b/https://simpledapp.eth',
            },
            'public_chat_deep_link': {
                'url': 'https://join.status.im/baga-ma-2020',
                'chat_name': 'baga-ma-2020'
            },
        }

        for key in url_data:
            home.plus_button.click_until_presence_of_element(home.start_new_chat_button)
            home.just_fyi('Checking %s case' % key)
            if home.universal_qr_scanner_button.is_element_displayed():
                home.universal_qr_scanner_button.click()
            if home.allow_button.is_element_displayed():
                home.allow_button.click()
            home.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            from views.chat_view import ChatView
            chat = ChatView(self.driver)
            if key == 'own_profile_key':
                from views.profile_view import ProfileView
                profile = ProfileView(self.driver)
                if not profile.default_username_text.is_element_displayed():
                    self.errors.append('In %s case was not redirected to own profile' % key)
                home.home_button.double_click()
            if url_data[key].get('error'):
                if not chat.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat.ok_button.click()
            if url_data[key].get('username'):
                if not chat.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In %s case username not shown' % key)
            if 'wallet' in key:
                if url_data[key].get('data'):
                    actual_data = send_transaction.get_values_from_send_transaction_bottom_sheet()
                    difference_in_data = url_data[key]['data'].items() - actual_data.items()
                    if difference_in_data:
                        self.errors.append(
                            'In %s case returned value does not match expected in %s' % (key, repr(difference_in_data)))
                    wallet.close_send_transaction_view_button.click()
                wallet.home_button.click()
            if 'dapp' in key:
                home.open_in_status_button.click()
                if not (chat.allow_button.is_element_displayed() or chat.element_by_text(
                        "Can't find web3 library").is_element_displayed()):
                    self.errors.append('No allow button is shown in case of navigating to Status dapp!')
                chat.dapp_tab_button.click()
                chat.home_button.click()
            if 'public' in key:
                if not chat.chat_message_input.is_element_displayed():
                    self.errors.append('No message input is shown in case of navigating to public chat via deep link!')
                if not chat.element_by_text_part(url_data[key]['chat_name']).is_element_displayed():
                    self.errors.append('Chat name is not shown in case of navigating to public chat via deep link!')
            chat.get_back_to_home_view()

        self.errors.verify_no_errors()
