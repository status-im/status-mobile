import time

import emoji
import random
import string
from selenium.common.exceptions import TimeoutException

from tests import marks, get_current_time
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from tests.users import transaction_senders, transaction_recipients, basic_user, ens_user
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(5305)
    @marks.critical
    def test_text_message_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()
        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_2_chat = device_2_home.get_chat(default_username_1).click()
        device_2_chat.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(5310)
    @marks.critical
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        public_key_1 = home_1.get_public_key_and_username()
        home_1.home_button.click()

        home_1.toggle_airplane_mode()  # airplane mode on primary device

        profile_2 = home_2.profile_button.click()
        username_2 = profile_2.default_username_text.text
        profile_2.get_back_to_home_view()
        chat_2 = home_2.add_contact(public_key_1)
        message_1 = 'test message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_2.toggle_airplane_mode()  # airplane mode on secondary device

        home_1.toggle_airplane_mode()  # turning on WiFi connection on primary device

        home_1.connection_status.wait_for_invisibility_of_element(30)
        chat_element = home_1.get_chat(username_2)
        chat_element.wait_for_visibility_of_element(30)
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(2)

        chat_2.toggle_airplane_mode()  # turning on WiFi connection on secondary device
        home_1.toggle_airplane_mode()  # airplane mode on primary device

        chat_2.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(60)
        chat_2.connection_status.wait_for_invisibility_of_element(60)
        message_2 = 'one more message'
        chat_2.chat_message_input.send_keys(message_2)
        chat_2.send_message_button.click_until_absense_of_element(chat_2.send_message_button)

        home_1.toggle_airplane_mode()  # turning on WiFi connection on primary device

        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)

    @marks.testrail_id(5338)
    @marks.critical
    def test_messaging_in_different_networks(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        public_key_2 = home_2.get_public_key_and_username()
        profile_2 = home_2.get_profile_view()
        profile_2.switch_network('Mainnet with upstream RPC')

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2 = home_2.get_chat(default_username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

        public_chat_name = home_1.get_random_chat_name()
        chat_1.get_back_to_home_view()
        home_1.join_public_chat(public_chat_name)
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(public_chat_name)

        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(5315)
    @marks.high
    def test_send_non_english_message_to_newly_added_contact(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()

        # Skip until edit-profile feature returned

        # profile_1 = device_1_home.profile_button.click()
        # profile_1.edit_profile_picture('sauce_logo.png')
        # profile_1.home_button.click()

        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        messages = ['hello', '¿Cómo estás tu año?', 'ё, доброго вечерочка', '®	æ ç ♥']
        for message in messages:
            device_1_chat.send_message(message)

        chat_element = device_2_home.get_chat(default_username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        for message in messages:
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % message)
        if not device_2_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts button is not shown')
        if device_2_chat.user_name_text.text != default_username_1:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % default_username_1)
        device_2_chat.chat_options.click()
        device_2_chat.view_profile_button.click()

        # Skip until edit-profile feature returned

        # if not device_2_chat.contact_profile_picture.is_element_image_equals_template('sauce_logo.png'):
        #     self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(5782)
    @marks.critical
    def test_install_pack_and_send_sticker(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()

        device_1_home.just_fyi('join public chat and check that stickers are not available on Ropsten')
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_1_public_chat = device_1_home.get_chat_view()
        if device_1_public_chat.show_stickers_button.is_element_displayed():
            self.errors.append('Sticker button is shown while on Ropsten')

        device_1_home.just_fyi('switch to mainnet')
        device_1_public_chat.get_back_to_home_view()
        device_1_profile, device_2_profile = device_1_home.profile_button.click(), device_2_home.profile_button.click()
        device_2_public_key = device_2_profile.get_public_key_and_username()
        device_1_public_key, device_1_username = device_1_profile.get_public_key_and_username(return_username=True)

        for device in device_2_profile, device_1_profile:
            device.switch_network('Mainnet with upstream RPC')
        device_1_home.get_chat('#' + chat_name).click()

        device_1_home.just_fyi('install free sticker pack and use it in public chat')
        device_1_public_chat.show_stickers_button.click()
        device_1_public_chat.get_stickers.click()
        device_1_public_chat.install_sticker_pack_by_name('Status Cat')
        device_1_public_chat.back_button.click()
        time.sleep(2)
        device_1_public_chat.swipe_left()
        device_1_public_chat.sticker_icon.click()
        if not device_1_public_chat.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent')
        device_1_public_chat.swipe_right()
        if not device_1_public_chat.sticker_icon.is_element_displayed():
            self.errors.append('Sticker is not shown in recently used list')
        device_1_public_chat.get_back_to_home_view()

        device_1_home.just_fyi('send stickers in 1-1 chat from Recent')
        device_1_one_to_one_chat = device_1_home.add_contact(device_2_public_key)
        device_1_one_to_one_chat.show_stickers_button.click()
        device_1_one_to_one_chat.sticker_icon.click()
        if not device_1_one_to_one_chat.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent from Recent')

        device_2_home.just_fyi('check that can install stickers by tapping on sticker message')
        device2_one_to_one_chat = device_2_home.get_chat(device_1_username).click()
        device2_one_to_one_chat.chat_item.click()
        if not device2_one_to_one_chat.element_by_text_part('Status Cat').is_element_displayed():
            self.errors.append('Stickerpack is not available for installation after tapping on sticker message')
        device2_one_to_one_chat.element_by_text_part('Free').click()
        if device2_one_to_one_chat.element_by_text_part('Free').is_element_displayed():
            self.errors.append('Stickerpack was not installed')

        self.errors.verify_no_errors()

    @marks.testrail_id(6305)
    @marks.critical
    def test_image_in_one_to_one_send_save_reply(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()

        device_1_home.just_fyi('start 1-1 chat')
        device_1_profile, device_2_profile = device_1_home.profile_button.click(), device_2_home.profile_button.click()
        device_2_public_key = device_2_profile.get_public_key_and_username()
        device_1_public_key, device_1_username = device_1_profile.get_public_key_and_username(return_username=True)
        image_description = 'description'
        [home.click() for home in [device_1_profile.home_button, device_2_profile.home_button]]
        device_1_chat = device_1_home.add_contact(device_2_public_key)

        device_1_home.just_fyi('send image in 1-1 chat from Gallery, check options for sender')
        device_1_chat.show_images_button.click()
        device_1_chat.image_from_gallery_button.click()
        device_1_chat.allow_button.click()
        device_1_chat.click_system_back_button()
        device_1_chat.chat_message_input.click()
        device_1_chat.show_images_button.click()
        device_1_chat.first_image_from_gallery.click()
        if not device_1_chat.cancel_reply_button.is_element_displayed():
            self.errors.append("Can't cancel sending images, expected image preview is not shown!")
        device_1_chat.chat_message_input.set_value(image_description)
        device_1_chat.send_message_button.click()
        device_1_chat.chat_message_input.click()
        for message in device_1_chat.image_chat_item, device_1_chat.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for sender')
        if not device_1_chat.image_chat_item.is_element_image_equals_template('message_image_sender.png'):
            self.errors.append("Image doesn't match expected template for sender")
        device_1_chat.show_images_button.click()
        device_1_chat.image_from_gallery_button.click()
        device_1_chat.click_system_back_button()
        device_1_chat.image_chat_item.long_press_element()
        for element in device_1_chat.reply_message_button, device_1_chat.save_image_button:
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on own image messages')
        if device_1_chat.view_profile_button.is_element_displayed():
            self.errors.append('Options are not shown on long-press on image messages')

        device_2_home.just_fyi('check image, description and options for receiver')
        device_2_chat = device_2_home.get_chat(device_1_username).click()
        for message in device_2_chat.image_chat_item, device_2_chat.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for receiver')
        if not device_2_chat.image_chat_item.is_element_image_equals_template('message_image_receiver.png'):
            self.errors.append("Image doesn't match expected template for receiver")
        device_2_chat.image_chat_item.long_press_element()
        for element in device_2_chat.reply_message_button, device_2_chat.save_image_button, device_2_chat.view_profile_button:
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on own image messages')

        device_1_home.just_fyi('save image')
        device_1_chat.save_image_button.click()
        device_1_chat.show_images_button.click_until_presence_of_element(device_1_chat.image_from_gallery_button)
        device_1_chat.image_from_gallery_button.click()
        device_1_chat.wait_for_element_starts_with_text('Recent')
        if not device_1_chat.recent_image_in_gallery.is_element_displayed():
            self.errors.append('Saved image is not shown in Recent')

        device_2_home.just_fyi('reply to image message')
        device_2_chat.reply_message_button.click()
        if device_2_chat.quote_username_in_message_input.text != "↪ %s" % device_1_username:
            self.errors.append("Username is not displayed in reply quote snippet replying to image message")
        reply_to_message_from_receiver = "image reply"
        device_2_chat.send_message(reply_to_message_from_receiver)
        reply_message = device_2_chat.chat_element_by_text(reply_to_message_from_receiver)
        if not reply_message.image_in_reply.is_element_displayed():
            self.errors.append("Image is not displayed in reply")

        self.errors.verify_no_errors()

    @marks.testrail_id(5316)
    @marks.critical
    def test_add_to_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()

        device_2_public_key = device_2_home.get_public_key_and_username()
        profile_2 = device_2_home.get_profile_view()
        # TODO: skip until edit image profile is enabled
        # file_name = 'sauce_logo.png'
        # profile_2.edit_profile_picture(file_name)
        default_username_2 = profile_2.default_username_text.text
        profile_2.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key + ' ')
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat(default_username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message with text '%s' was not received" % message)
        device_2_chat.connection_status.wait_for_invisibility_of_element(60)
        device_2_chat.add_to_contacts.click()

        device_2_chat.get_back_to_home_view()
        device_2_home.plus_button.click()
        device_2_contacts = device_2_home.start_new_chat_button.click()
        if not device_2_contacts.element_by_text(default_username_1).is_element_displayed():
            self.errors.append('%s is not added to contacts' % default_username_1)

        if device_1_chat.user_name_text.text != default_username_2:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % default_username_2)
        device_1_chat.chat_options.click()
        device_1_chat.view_profile_button.click()
        # TODO: skip until edit image profile is enabled
        # if not device_1_chat.contact_profile_picture.is_element_image_equals_template(file_name):
        #     self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(5373)
    @marks.high
    def test_send_and_open_links(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        public_key_2 = home_2.get_public_key_and_username()
        home_2.home_button.click()

        chat_1 = home_1.add_contact(public_key_2)
        url_message = 'http://status.im'
        chat_1.chat_message_input.send_keys(url_message)
        chat_1.send_message_button.click()
        chat_1.get_back_to_home_view()
        chat_2 = home_2.get_chat(default_username_1).click()
        chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = chat_2.open_in_status_button.click()
        try:
            web_view.find_full_text('Private, Secure Communication')
        except TimeoutException:
            self.errors.append('Device 2: URL was not opened from 1-1 chat')
        web_view.back_to_home_button.click()
        chat_2.home_button.click()
        chat_2.back_button.click()

        chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        home_1.join_public_chat(chat_name)
        home_2.join_public_chat(chat_name)
        chat_2.chat_message_input.send_keys(url_message)
        chat_2.send_message_button.click()
        chat_1.element_starts_with_text(url_message, 'button').click()
        web_view = chat_1.open_in_status_button.click()
        try:
            web_view.find_full_text('Private, Secure Communication')
        except TimeoutException:
            self.errors.append('Device 1: URL was not opened from 1-1 chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(5362)
    @marks.medium
    def test_unread_messages_counter_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_2 = device_2_home.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        device_2_home = profile_2.get_back_to_home_view()
        device_1_public_key = device_1_home.get_public_key_and_username()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        message = 'test message'
        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()

        if device_1_home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')

        chat_element = device_1_home.get_chat(default_username_2)
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')

        chat_element.click()
        device_1_home.get_back_to_home_view()

        if device_1_home.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')

        if chat_element.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element for already seen message')
        self.errors.verify_no_errors()

    @marks.testrail_id(5425)
    @marks.medium
    # TODO: should be completed with quoting after fix 9480
    def test_markdown_support_in_messages(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user()
        profile_2 = device_2_home.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        device_2_home = profile_2.get_back_to_home_view()
        device_1_public_key = device_1_home.get_public_key_and_username()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)
        device_2_chat.chat_message_input.send_keys('test message')
        device_2_chat.send_message_button.click()
        device_1_chat = device_1_home.get_chat(default_username_2).click()

        markdown = {
            'bold text in asterics' : '**',
            'bold text in underscores' : '__',
            'italic text in asteric': '*',
            'italic text in underscore' : '_',
            'inline code' : '`',
            'code blocks' : '```',
            # 'quote reply (one row)' : '>',
        }

        for message, symbol in markdown.items():
            device_1_home.just_fyi('checking that "%s" is applied (%s) in 1-1 chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            device_2_chat.chat_message_input.send_keys(message_to_send)
            device_2_chat.send_message_button.click()
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the sender \n' % message)

            if not device_1_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the recipient \n' % message)

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        for message, symbol in markdown.items():
            device_1_home.just_fyi('checking that "%s" is applied (%s) in public chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            device_2_chat.chat_message_input.send_keys(message_to_send)
            device_2_chat.send_message_button.click()
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in public chat for the sender \n' % message)

            if not device_1_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in public chat for the recipient \n' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(5385)
    @marks.high
    def test_timestamp_in_chats(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user()
        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()
        device_1_profile = device_1_home.profile_button.click()
        default_username_1 = device_1_profile.default_username_text.text
        device_1_profile.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        device_1_chat.just_fyi('check user picture and timestamps in chat for sender and recipient in 1-1 chat')
        message = 'test text'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        sent_time = device_1_chat.convert_device_time_to_chat_timestamp()

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the sender')
        if device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the sender')

        device_2_chat = device_2_home.get_chat(default_username_1).click()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the recipient')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the recipient')
        # TODO: disabled due to issue 'yesterday' is shown, can't emulate manually
        # for chat in device_1_chat, device_2_chat:
        #     chat.verify_message_is_under_today_text(message, self.errors)

        device_1_chat.just_fyi('check user picture and timestamps in chat for sender and recipient in public chat')
        chat_name = device_1_home.get_random_chat_name()
        for chat in device_1_chat, device_2_chat:
            home_view = chat.get_back_to_home_view()
            home_view.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()
        sent_time = device_2_chat.convert_device_time_to_chat_timestamp()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the sender')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the recipient')
        if not device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in public chat for the recipient')
        # TODO: disabled due to issue 'yesterday' is shown, can't emulate manually
        # for chat in device_1_chat, device_2_chat:
        #     chat.verify_message_is_under_today_text(message, self.errors)

        self.errors.verify_no_errors()


@marks.all
@marks.chat
class TestMessagesOneToOneChatSingle(SingleDeviceTestCase):

    @marks.testrail_id(5317)
    @marks.critical
    def test_copy_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        chat = sign_in.get_chat_view()
        message_text = 'mmmeowesage_text'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        chat.element_by_text_part(message_text).long_press_element()
        chat.element_by_text('Copy').click()

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text:
            self.errors.append('Message text was not copied in a public chat')

        chat.get_back_to_home_view()
        home.add_contact(transaction_senders['M']['public_key'])
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text).long_press_element()
        chat.element_by_text('Copy').click()

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text:
            self.errors.append('Message text was not copied in 1-1 chat')
        self.errors.verify_no_errors()

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

    @marks.testrail_id(5328)
    @marks.critical
    @marks.battery_consumption
    def test_send_emoji(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(home.get_random_chat_name())
        chat = sign_in.get_chat_view()
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in public chat')

        chat.get_back_to_home_view()
        home.add_contact(transaction_senders['O']['public_key'])
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in 1-1 chat')
        self.errors.verify_no_errors()


    @marks.testrail_id(5783)
    @marks.critical
    def test_can_use_purchased_stickers_on_recovered_account(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(ens_user['passphrase'])

        sign_in_view.just_fyi('switch to Mainnet')
        profile_view = home_view.profile_button.click()
        profile_view.switch_network('Mainnet with upstream RPC')

        sign_in_view.just_fyi('join to public chat, buy and install stickers')
        chat = home_view.join_public_chat(home_view.get_random_chat_name())
        chat.show_stickers_button.click()
        chat.get_stickers.click()
        chat.install_sticker_pack_by_name('Tozemoon')
        chat.back_button.click()

        sign_in_view.just_fyi('check that can use installed pack')
        time.sleep(2)
        chat.swipe_left()
        chat.sticker_icon.click()
        if not chat.chat_item.is_element_displayed():
            self.driver.fail('Sticker was not sent')

    @marks.testrail_id(5403)
    @marks.critical
    def test_start_chat_with_ens(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        chat = home.add_contact(ens_user['ens'])
        if not chat.element_by_text("@" + ens_user['ens']).is_element_displayed():
            self.driver.fail('Wrong user is resolved from username when starting 1-1 chat.')

    @marks.testrail_id(5326)
    @marks.critical
    def test_offline_status(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()

        home_view.airplane_mode_button.click()

        chat = home_view.add_contact(transaction_senders['C']['public_key'])
        chat.element_by_text('Offline').wait_for_visibility_of_element(15)
        if chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in 1-1 chat')
        chat.get_back_to_home_view()

        if home_view.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in home screen')

        public_chat = home_view.join_public_chat(home_view.get_random_chat_name())
        if public_chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in a public chat')
        self.errors.verify_no_errors()

