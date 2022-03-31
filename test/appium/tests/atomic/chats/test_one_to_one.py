import emoji
import random
import pytest

from tests import marks
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


class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(5782)
    @marks.critical
    def test_install_pack_and_send_sticker(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        home_1.just_fyi('Join public chat and check that stickers are not available on Ropsten')
        chat_name = home_1.get_random_chat_name()
        home_1.join_public_chat(chat_name)
        public_chat_1 = home_1.get_chat_view()
        if public_chat_1.show_stickers_button.is_element_displayed():
            self.errors.append('Sticker button is shown while on Ropsten')

        home_1.just_fyi('Switch to mainnet on both devices')
        public_chat_1.get_back_to_home_view()
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        public_key_2 = profile_2.get_public_key_and_username()
        public_key_1, username_1 = profile_1.get_public_key_and_username(return_username=True)
        [profile.switch_network() for profile in (profile_2, profile_1)]
        home_1.get_chat('#' + chat_name).click()

        home_1.just_fyi('Install free sticker pack and use it in public chat')
        public_chat_1.install_sticker_pack_by_name()
        public_chat_1.sticker_icon.click()
        if not public_chat_1.sticker_message.is_element_displayed():
            self.errors.append('Sticker was not sent')
        public_chat_1.swipe_right()
        if not public_chat_1.sticker_icon.is_element_displayed():
            self.errors.append('Sticker is not shown in recently used list')
        public_chat_1.get_back_to_home_view()

        home_1.just_fyi('Send stickers in 1-1 chat from Recent')
        private_chat_1 = home_1.add_contact(public_key_2)
        private_chat_1.show_stickers_button.click()
        private_chat_1.sticker_icon.click()
        if not private_chat_1.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent from Recent')

        home_2.just_fyi('Check that can install stickers by tapping on sticker message')
        private_chat_2 = home_2.get_chat(username_1).click()
        private_chat_2.chat_item.click()
        if not private_chat_2.element_by_text_part('Status Cat').is_element_displayed():
            self.errors.append('Stickerpack is not available for installation after tapping on sticker message')
        private_chat_2.element_by_text_part('Free').click()
        if private_chat_2.element_by_text_part('Free').is_element_displayed():
            self.errors.append('Stickerpack was not installed')

        home_2.just_fyi('Check that can navigate to another user profile via long tap on sticker message')
        private_chat_2.close_sticker_view_icon.click()
        private_chat_2.chat_item.long_press_element()
        private_chat_2.element_by_text('View Details').click()
        if not private_chat_2.profile_add_to_contacts.is_element_displayed():
            self.errors.append('No navigate to user profile after tapping View Details on sticker message')

        self.errors.verify_no_errors()

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

    @marks.testrail_id(5783)
    @marks.critical
    def test_can_use_purchased_stickers_on_recovered_account(self):
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(ens_user['passphrase'])
        profile = home.profile_button.click()
        profile.switch_network()

        sign_in.just_fyi('join to public chat, buy and install stickers')
        chat = home.join_public_chat(home.get_random_chat_name())
        chat.install_sticker_pack_by_name('Tozemoon')

        sign_in.just_fyi('check that can use installed pack')
        chat.sticker_icon.click()
        if not chat.chat_item.is_element_displayed():
            self.driver.fail('Sticker was not sent')

    @marks.testrail_id(5403)
    @marks.critical
    def test_start_chat_with_ens_mention_in_one_to_one(self):
        home = SignInView(self.driver).create_user()

        home.just_fyi('Start new chat with public key and check user profile from 1-1 header > options')
        chat = home.add_contact(ens_user_ropsten['public_key'])
        chat.chat_options.click_until_presence_of_element(chat.view_profile_button)
        chat.view_profile_button.click()
        for element in (chat.profile_block_contact, chat.remove_from_contacts, chat.profile_send_message):
            if not element.is_element_displayed():
                self.errors.append('Expected %s is not visible' % element.locator)
        chat.close_button.click()
        chat.home_button.click()

        home.just_fyi('Start new chat with ENS and check that ENS is resolved')
        ens = ens_user_ropsten['ens']
        home.add_contact(ens, add_in_contacts=False)
        if not chat.element_by_text("@" + ens).is_element_displayed():
            self.driver.fail('Wrong user is resolved from username when starting 1-1 chat.')

        home.just_fyi('Mention user by ENS in 1-1 chat')
        message = '@%s hey!' % ens
        chat.send_message(message)

        home.just_fyi('Check that ENS is shown in preview for 1-1 chat')
        chat.home_button.double_click()
        if not home.element_by_text(message).is_element_displayed():
            self.errors.append('"%s" is not show in chat preview on home screen!' % message)
        home.get_chat('@%s' % ens).click()

        home.just_fyi('Set nickname and mention user by nickname in 1-1 chat')
        russian_nickname = 'МОЙ дорогой ДРУх'
        chat.chat_options.click()
        chat.view_profile_button.click()
        chat.set_nickname(russian_nickname)
        chat.select_mention_from_suggestion_list(russian_nickname + ' @' + ens)

        home.just_fyi('Check that nickname is shown in preview for 1-1 chat')
        updated_message = '%s hey!' % russian_nickname
        chat.home_button.double_click()
        if not home.element_by_text(updated_message).is_element_displayed():
            self.errors.append('"%s" is not show in chat preview on home screen!' % message)
        home.get_chat(russian_nickname).click()

        home.just_fyi('Check redirect to user profile on mention by nickname tap')
        chat.chat_element_by_text(updated_message).click()
        if not chat.profile_block_contact.is_element_displayed():
            self.errors.append(
                'No redirect to user profile after tapping on message with mention (nickname) in 1-1 chat')

        home.just_fyi('My_profile button at Start new chat view opens own QR code with public key pop-up')
        chat.close_button.click()
        home.home_button.double_click()
        home.plus_button.click()
        home.start_new_chat_button.click()
        home.my_profile_on_start_new_chat_button.click()
        account = home.get_profile_view()
        if not (account.public_key_text.is_element_displayed() and account.share_button.is_element_displayed()
                and account.qr_code_image.is_element_displayed()):
            self.errors.append('No self profile pop-up data displayed after My_profile button tap')

        self.errors.verify_no_errors()

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
