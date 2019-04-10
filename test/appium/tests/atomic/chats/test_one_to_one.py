import pytest
import random
import string
import emoji
from datetime import datetime
from selenium.common.exceptions import TimeoutException
from tests import marks, get_current_time
from tests.users import transaction_senders, transaction_recipients, basic_user
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(5305)
    @marks.critical
    def test_text_message_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_2_chat = device_2_home.get_chat_with_user(username_1).click()
        device_2_chat.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(5310)
    @marks.skip
    @marks.critical
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2, offline_mode=True)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_2'
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user(username=username_2)
        public_key_1 = home_1.get_public_key()
        home_1.home_button.click()

        home_1.driver.set_network_connection(1)  # airplane mode on primary device

        chat_2 = home_2.add_contact(public_key_1)
        message_1 = 'test message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_2.driver.set_network_connection(1)  # airplane mode on secondary device

        home_1.driver.set_network_connection(2)  # turning on WiFi connection on primary device

        home_1.connection_status.wait_for_invisibility_of_element(20)
        chat_element = home_1.get_chat_with_user(username_2)
        chat_element.wait_for_visibility_of_element(20)
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(2)

        chat_2.driver.set_network_connection(2)  # turning on WiFi connection on secondary device
        home_1.driver.set_network_connection(1)  # airplane mode on primary device

        chat_2.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(60)
        message_2 = 'one more message'
        chat_2.chat_message_input.send_keys(message_2)
        chat_2.send_message_button.click()

        home_1.driver.set_network_connection(2)  # turning on WiFi connection on primary device

        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)

    @marks.testrail_id(5338)
    @marks.critical
    def test_messaging_in_different_networks(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        home_1, home_2 = sign_in_1.create_user(username_1), sign_in_2.create_user()
        public_key_2 = home_2.get_public_key()
        profile_2 = home_2.get_profile_view()
        profile_2.switch_network('Mainnet with upstream RPC')

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

        public_chat_name = home_1.get_public_chat_name()
        chat_1.get_back_to_home_view()
        home_1.join_public_chat(public_chat_name)
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(public_chat_name)

        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(5315)
    @marks.high
    def test_send_message_to_newly_added_contact(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()

        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user()

        profile_1 = device_1_home.profile_button.click()
        file_name = 'sauce_logo.png'
        profile_1.edit_profile_picture(file_name)
        profile_1.home_button.click()

        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat_with_user(username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message with test '%s' was not received" % message)
        if not device_2_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts button is not shown')
        if device_2_chat.user_name_text.text != username_1:
            self.errors.append("Real username '%s' is not shown in one-to-one chat" % username_1)
        device_2_chat.chat_options.click()
        device_2_chat.view_profile_button.click()
        if not device_2_chat.contact_profile_picture.is_element_image_equals_template(file_name):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.verify_no_errors()

    @marks.testrail_id(5316)
    @marks.critical
    def test_add_to_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'

        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user(
            username=username_2)

        device_2_public_key = device_2_home.get_public_key()
        profile_2 = device_2_home.get_profile_view()
        file_name = 'sauce_logo.png'
        profile_2.edit_profile_picture(file_name)
        profile_2.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat_with_user(username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message with text '%s' was not received" % message)
        device_2_chat.connection_status.wait_for_invisibility_of_element(60)
        device_2_chat.add_to_contacts.click()

        device_2_chat.get_back_to_home_view()
        start_new_chat = device_2_home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        if not start_new_chat.element_by_text(username_1).is_element_displayed():
            self.errors.append('%s is not added to contacts' % username_1)

        if device_1_chat.user_name_text.text != username_2:
            self.errors.append("Real username '%s' is not shown in one-to-one chat" % username_2)
        device_1_chat.chat_options.click()
        device_1_chat.view_profile_button.click()
        if not device_1_chat.contact_profile_picture.is_element_image_equals_template(file_name):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.verify_no_errors()

    @marks.testrail_id(5373)
    @marks.high
    def test_send_and_open_links(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'

        home_1, home_2 = device_1.create_user(username=username_1), device_2.create_user(username=username_2)
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        chat_1 = home_1.add_contact(public_key_2)
        url_message = 'status.im'
        chat_1.chat_message_input.send_keys(url_message)
        chat_1.send_message_button.click()
        chat_1.get_back_to_home_view()
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = chat_2.open_in_status_button.click()
        try:
            web_view.find_full_text('Access a Better Web, Anywhere')
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
            web_view.find_full_text('Access a Better Web, Anywhere')
        except TimeoutException:
            self.errors.append('Device 1: URL was not opened from 1-1 chat')
        self.verify_no_errors()

    @marks.testrail_id(5326)
    @marks.critical
    def test_offline_status(self):
        self.create_drivers(1)
        sign_in = SignInView(self.drivers[0])
        home_view = sign_in.create_user()

        # Dismiss "Welcome to Status" placeholder.
        # When the placeholder is visible, the offline status bar does not appear
        wallet_view = home_view.wallet_button.click()
        wallet_view.home_button.click()
        home_view.toggle_airplane_mode()
        home_view.accept_agreements()
        home_view = sign_in.sign_in()

        chat = home_view.add_contact(transaction_senders['C']['public_key'])
        if chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in 1-1 chat')
        chat.get_back_to_home_view()

        if home_view.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in home screen')

        public_chat = home_view.join_public_chat(home_view.get_public_chat_name())
        if public_chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in a public chat')
        self.verify_no_errors()

    @marks.testrail_id(5374)
    @marks.high
    def test_message_marked_as_sent_in_1_1_chat(self):
        self.create_drivers(1)
        sign_in_view = SignInView(self.drivers[0])
        home_view = sign_in_view.create_user()
        chat_view = home_view.add_contact(basic_user['public_key'])
        message = 'test message'
        chat_view.chat_message_input.send_keys(message)
        chat_view.send_message_button.click()
        if chat_view.chat_element_by_text(message).status.text != 'Sent':
            self.errors.append("'Sent' status is not shown under the sent text message")
        self.verify_no_errors()

    @marks.testrail_id(5362)
    @marks.critical
    def test_unread_messages_counter_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user(username=username_2)
        device_1_public_key = device_1_home.get_public_key()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        message = 'test message'
        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()

        if device_1_home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')

        chat_element = device_1_home.get_chat_with_user(username_2)
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')

        chat_element.click()
        device_1_home.get_back_to_home_view()

        if device_1_home.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')

        if chat_element.new_messages_counter.is_element_displayed():
            self.errors.append('New messages counter is shown on chat element for already seen message')
        self.verify_no_errors()

    @marks.testrail_id(5425)
    @marks.medium
    def test_bold_and_italic_text_in_messages(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user(username=username_2)
        device_1_public_key = device_1_home.get_public_key()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        bold_text = 'bold text'
        device_2_chat.chat_message_input.send_keys('*%s*' % bold_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in 1-1 chat for the sender')

        device_1_chat = device_1_home.get_chat_with_user(username_2).click()
        if not device_1_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in 1-1 chat for the recipient')

        italic_text = 'italic text'
        device_2_chat.chat_message_input.send_keys('~%s~' % italic_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the sender')

        if not device_1_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the recipient')

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_public_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys('*%s*' % bold_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in public chat for the recipient')

        device_2_chat.chat_message_input.send_keys('~%s~' % italic_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the recipient')

        self.verify_no_errors()

    @marks.skip
    @marks.testrail_id(5385)
    @marks.high
    def test_timestamp_in_chats(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = sign_in_1.create_user(username=username_1), sign_in_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'test text'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        sent_time = datetime.strptime(device_1_chat.driver.device_time, '%a %b %d %H:%M:%S GMT %Y').strftime("%I:%M %p")
        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the sender')
        if device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the sender')

        device_2_chat = device_2_home.get_chat_with_user(username_1).click()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the recipient')
        if not device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in 1-1 chat for the recipient')

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_public_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()
        sent_time = datetime.strptime(device_2_chat.driver.device_time, '%a %b %d %H:%M:%S GMT %Y').strftime("%I:%M %p")
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the sender')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the recipient')
        if not device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in 1-1 chat for the recipient')

        self.verify_no_errors()

    @marks.testrail_id(5405)
    @marks.high
    def test_fiat_value_is_correctly_calculated_on_recipient_side(self):
        sender = transaction_senders['Y']
        recipient = transaction_recipients['I']

        self.create_drivers(2)
        signin_view1, signin_view2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_view1, home_view2 = signin_view1.recover_access(sender['passphrase']), signin_view2.recover_access(
            recipient['passphrase'])

        devices = [
            {'home_view': home_view1, 'currency': 'AUD'},
            {'home_view': home_view2, 'currency': 'EUR'},
        ]

        # changing currency for both devices
        for device in devices:
            profile_view = device['home_view'].profile_button.click()
            profile_view.set_currency(device['currency'])
            profile_view.get_back_to_home_view()

        device1 = devices[0]
        device2 = devices[1]

        # setting up device1 wallet
        wallet1 = device1['home_view'].wallet_button.click()
        wallet1.set_up_wallet()
        wallet1.get_back_to_home_view()

        # sending ETH to device2 in 1*1 chat
        device1_chat = device1['home_view'].add_contact(recipient['public_key'])
        send_amount = device1_chat.get_unique_amount()
        device1_chat.send_transaction_in_1_1_chat('ETHro', send_amount)

        sent_message = device1_chat.chat_element_by_text(send_amount)
        if not sent_message.is_element_displayed() and not sent_message.contains_text(device1['currency']):
            self.errors.append('Wrong currency fiat value while sending ETH in 1*1 chat.')

        device2_chat = device2['home_view'].get_chat_with_user(sender['username']).click()
        received_message = device2_chat.chat_element_by_text(send_amount)
        if not received_message.is_element_displayed() and not received_message.contains_text(device2['currency']):
            self.errors.append('Wrong currency fiat value while receiving ETH in 1*1 chat.')

        device1_chat.get_back_to_home_view()
        wallet1 = device1['home_view'].wallet_button.click()
        send_amount = device1_chat.get_unique_amount()

        # Send and request some ETH from wallet and check whether the fiat currency value of
        # the new messages is equal to user-selected
        wallet1.send_transaction(asset_name='ETHro', recipient=recipient['username'], amount=send_amount)
        wallet1.get_back_to_home_view()
        device1_chat = device1['home_view'].get_chat_with_user(recipient['username']).click()

        sent_message = device1_chat.chat_element_by_text(send_amount)
        received_message = device2_chat.chat_element_by_text(send_amount)

        if not sent_message.is_element_displayed() and not sent_message.contains_text(device1['currency']):
            self.errors.append('Wrong currency fiat value while sending ETH from wallet.')

        if not received_message.is_element_displayed() and not sent_message.contains_text(device2['currency']):
            self.errors.append('Wrong currency fiat value while receiving ETH sent via wallet.')

        self.verify_no_errors()


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
        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text).long_press_element()
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
        self.verify_no_errors()

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
            pytest.fail("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        message_input.cut_text()

        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2] + ' ').wait_for_visibility_of_element(2)

    @marks.testrail_id(5328)
    @marks.critical
    @marks.battery_consumption
    def test_send_emoji(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(home.get_public_chat_name())
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
        self.verify_no_errors()

    @marks.testrail_id(5393)
    @marks.high
    def test_that_fiat_value_is_correct_for_token_transactions(self):
        sender_passphrase = transaction_senders['X']['passphrase']
        recipient_public_key = transaction_recipients['H']['public_key']
        recipient_user_name = transaction_recipients['H']['username']
        default_currency = 'USD'
        user_currency = 'EUR'
        sigin_view = SignInView(self.driver)
        home_view = sigin_view.recover_access(sender_passphrase)
        wallet = home_view.wallet_button.click()
        wallet.set_up_wallet()

        wallet.get_back_to_home_view()

        chat = home_view.add_contact(recipient_public_key)
        send_amount, request_amount = [chat.get_unique_amount() for _ in range(2)]
        # Send and request some tokens in 1x1 chat and check whether the fiat currency value of the messages is equal
        # to default
        chat.send_transaction_in_1_1_chat('STT', send_amount)
        chat.request_transaction_in_1_1_chat('STT', request_amount)

        send_message = chat.chat_element_by_text(send_amount)
        if not send_message.is_element_displayed() and not send_message.contains_text(default_currency):
            self.errors.append('Wrong fiat value while sending assets in 1-1 chat with default currency.')

        request_message = chat.chat_element_by_text(request_amount)
        if not request_message.is_element_displayed() and not request_message.contains_text(default_currency):
            self.errors.append('Wrong fiat value while requesting assets in 1-1 chat with default currency.')

        chat.get_back_to_home_view()

        # Switch default currency to user-selected
        profile_view = sigin_view.profile_button.click()
        profile_view.set_currency(user_currency)
        profile_view.get_back_to_home_view()

        chat = home_view.get_chat_with_user(recipient_user_name).click()

        # Check whether the fiat currency value of the messages sent is not changed to user-selected
        send_message = chat.chat_element_by_text(send_amount)
        if not send_message.is_element_displayed() and not send_message.contains_text(default_currency):
            self.errors.append('Wrong fiat value while sending assets in 1-1 chat with default currency.')

        request_message = chat.chat_element_by_text(request_amount)
        if not request_message.is_element_displayed() and not request_message.contains_text(default_currency):
            self.errors.append('Wrong fiat value while requesting assets in 1-1 chat with default currency.')

        # Send and request some tokens in 1x1 chat and check whether the fiat currency value of
        # the new messages is equal to user-selected
        send_amount, request_amount = [chat.get_unique_amount() for _ in range(2)]
        chat.send_transaction_in_1_1_chat('STT', send_amount)
        chat.request_transaction_in_1_1_chat('STT', request_amount)

        send_message = chat.chat_element_by_text(send_amount)
        if not send_message.is_element_displayed() and not send_message.contains_text(user_currency):
            self.errors.append('Wrong fiat value while sending assets in 1-1 chat with user selected currency.')

        request_message = chat.chat_element_by_text(request_amount)
        if not request_message.is_element_displayed() and not request_message.contains_text(user_currency):
            self.errors.append('Wrong fiat value while requesting assets in 1-1 chat with user selected currency.')

        chat.get_back_to_home_view()

        wallet = home_view.wallet_button.click()
        send_amount, request_amount = [chat.get_unique_amount() for _ in range(2)]

        # Send and request some tokens from wallet and check whether the fiat currency value of
        # the new messages is equal to user-selected
        wallet.send_transaction(asset_name='STT', recipient=recipient_user_name, amount=send_amount)
        wallet.receive_transaction(asset_name='STT', recipient=recipient_user_name, amount=request_amount)

        wallet.get_back_to_home_view()
        chat = home_view.get_chat_with_user(recipient_user_name).click()

        send_message = chat.chat_element_by_text(send_amount)
        if not send_message.is_element_displayed() and not send_message.contains_text(user_currency):
            self.errors.append('Wrong fiat value while sending assets from wallet with user selected currency.')

        request_message = chat.chat_element_by_text(request_amount)
        if not request_message.is_element_displayed() and not request_message.contains_text(user_currency):
            self.errors.append('Wrong fiat value while requesting assets from wallet with user selected currency.')

        self.verify_no_errors()
