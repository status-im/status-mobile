from tests import marks, get_current_time
from tests.base_test_case import MultipleDeviceTestCase
from tests.users import chat_users
from views.sign_in_view import SignInView


def create_users(driver_1, driver_2, username_1=None, username_2=None):
    device_1_sign_in, device_2_sign_in = SignInView(driver_1), SignInView(driver_2)
    if username_1 is not None and username_2 is not None:
        return device_1_sign_in.create_user(username_1), device_2_sign_in.create_user(username_1)
    return device_1_sign_in.create_user(), device_2_sign_in.create_user()


def create_and_join_group_chat(device_1_home, device_2_home, chat_name):
    # device 2: get public key and default username
    device_2_public_key = device_2_home.get_public_key()
    device_2_profile_view = device_2_home.profile_button.click()
    device_2_default_username = device_2_profile_view.default_username_text.text
    device_2_home.home_button.click()

    # device 1: add device 2 as contact
    device_1_chat = device_1_home.add_contact(device_2_public_key)
    device_1_chat.get_back_to_home_view()

    # device 1: create group chat with some user
    device_1_chat = device_1_home.create_group_chat([device_2_default_username], chat_name)

    # device 2: open group chat
    device_2_chat = device_2_home.get_chat_with_user(chat_name).click()
    device_2_chat.join_chat_button.click()

    return device_1_chat, device_2_chat


@marks.chat
class TestGroupChatMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3994)
    @marks.high
    def test_create_new_group_chat(self):
        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        for chat in (device_1_chat, device_2_chat):
            if chat.user_name_text.text != chat_name:
                self.errors.append('Oops! Chat screen does not match the entered chat name %s' % chat_name)

        self.verify_no_errors()

    @marks.testrail_id(3993)
    @marks.critical
    def test_send_message_in_group_chat(self):
        message_from_device_1 = 'Hello from device 1'
        message_from_device_2 = 'Hi there! Sent from device 2'

        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        device_1_chat.send_message(message_from_device_1)
        device_2_chat.send_message(message_from_device_2)

        if not device_2_chat.chat_element_by_text(message_from_device_1).is_element_displayed():
            self.errors.append("Message with test '%s' was not received" % message_from_device_1)
        if not device_1_chat.chat_element_by_text(message_from_device_2).is_element_displayed():
            self.errors.append("Message with test '%s' was not received" % message_from_device_2)
        self.verify_no_errors()

    @marks.testrail_id(5674)
    @marks.high
    def test_group_chat_system_messages(self):
        username_1 = 'user1_%s' % get_current_time()
        username_2 = 'user2_%s' % get_current_time()

        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1], username_1, username_2)
        chat_name = device_1_home.get_public_chat_name()
        device_2_profile = device_2_home.profile_button.click()
        device_2_default_username = device_2_profile.default_username_text.text

        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        admin_created_chat_system_message = "*%s* created the group *%s*" % (username_1, chat_name)
        user2_joined_chat_system_message = "*%s* has joined the group" % device_2_default_username
        user2_left_chat_system_message = "*%s* left the group" % device_2_default_username

        # device 2: delete group chat
        device_2_chat = device_2_home.get_chat_with_user(chat_name).click()
        device_2_chat.delete_chat()

        # device 1: check system messages in the group chat
        if not device_1_chat.element_by_text(admin_created_chat_system_message).is_element_displayed():
            self.errors.append("System message: '%s' in group chat is not shown" % admin_created_chat_system_message)
        if not device_1_chat.element_by_text(user2_joined_chat_system_message).is_element_displayed():
            self.errors.append(
                "System message: '%s' in group chat is not shown" % user2_joined_chat_system_message)
        if not device_1_chat.element_by_text(user2_left_chat_system_message).is_element_displayed():
            self.errors.append(
                "System message: '%s' in group chat is not shown" % user2_left_chat_system_message)
        self.verify_no_errors()

    @marks.testrail_id(3997)
    @marks.high
    def test_delete_group_chat_via_delete_button(self):
        message_from_device_1 = 'Hello from device 1'
        message_from_device_2 = 'Hi there! Sent from device 2'

        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        # send some messages and delete chat
        device_1_chat.send_message(message_from_device_1)
        device_2_chat.send_message(message_from_device_2)
        device_1_chat.delete_chat()
        device_2_chat.send_message(message_from_device_2)

        # device_1: check if chat is was deleted
        if device_1_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Public chat '%s' is shown, but the chat has been deleted" % chat_name)
        self.verify_no_errors()

    @marks.testrail_id(3998)
    @marks.high
    def test_add_new_group_chat_member(self):
        username_1 = 'user1_%s' % get_current_time()
        username_2 = 'user2_%s' % get_current_time()
        message_for_device_2 = 'This message should be visible for device 2'
        chat_member = chat_users['A']

        self.create_drivers(2)

        # create accounts on each device
        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1], username_1, username_2)
        chat_name = device_1_home.get_public_chat_name()

        # device 2: get public key and default username
        device_2_public_key = device_2_home.get_public_key()
        device_2_profile_view = device_2_home.profile_button.click()
        device_2_default_username = device_2_profile_view.default_username_text.text
        device_2_home.home_button.click()

        # device 1: add contacts
        device_1_home.add_contact(chat_member['public_key'])
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.add_contact(device_2_public_key)
        device_1_chat.get_back_to_home_view()

        # device 1: create group chat with some user
        device_1_chat = device_1_home.create_group_chat([chat_member['username']], chat_name)

        # device 1: add device 2 as a new member of the group chat
        device_1_chat.add_members_to_group_chat([device_2_default_username])

        # device 1: send a message that should be visible for device 2
        device_1_chat.send_message(message_for_device_2)

        # device 2: open the chat and check messages
        device_2_chat = device_2_home.get_chat_with_user(chat_name).click()
        device_2_chat.join_chat_button.click()
        if device_2_chat.chat_element_by_text(message_for_device_2).is_element_displayed():
            self.errors.append('Message that was sent after device 2 has joined is visible')

        self.verify_no_errors()
