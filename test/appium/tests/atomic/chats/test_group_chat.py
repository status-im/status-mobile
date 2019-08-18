from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from tests.users import chat_users
from views.sign_in_view import SignInView


def return_left_chat_system_message(username):
    return "*%s* left the group" % username


def return_created_chat_system_message(username, chat_name):
    return "*%s* created the group *%s*" % (username, chat_name)


def return_joined_chat_system_message(username):
    return "*%s* has joined the group" % username


def return_made_admin_system_message(username):
    return "*%s* has been made admin" % username


def create_users(driver_1, driver_2):
    device_1_sign_in, device_2_sign_in = SignInView(driver_1), SignInView(driver_2)
    return device_1_sign_in.create_user(), device_2_sign_in.create_user()


def get_username(device_home, default=True):
    device_profile_view = device_home.profile_button.click()
    if default:
        username = device_profile_view.default_username_text.text
    else:
        username = device_profile_view.username_set_by_user_text.text
    device_home.home_button.click()
    return username


def create_new_group_chat(device_1_home, device_2_home, chat_name):
    # device 2: get public key and default username
    device_2_public_key = device_2_home.get_public_key()
    device_2_default_username = get_username(device_2_home)

    # device 1: add device 2 as contact
    device_1_chat = device_1_home.add_contact(device_2_public_key)
    device_1_chat.get_back_to_home_view()

    # device 1: create group chat with some user
    device_1_chat = device_1_home.create_group_chat([device_2_default_username], chat_name)

    # device 2: open group chat
    device_2_chat = device_2_home.get_chat_with_user(chat_name).click()

    return device_1_chat, device_2_chat


def create_and_join_group_chat(device_1_home, device_2_home, chat_name):
    device_1_chat, device_2_chat = create_new_group_chat(device_1_home, device_2_home, chat_name)
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

        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)
        for chat in (device_1_chat, device_2_chat):
            chat.send_message("Message from device: %s" % chat.driver.number)

        for chat in (device_1_chat, device_2_chat):
            for chat_driver in (device_1_chat, device_2_chat):
                if not chat.chat_element_by_text(
                        "Message from device: %s" % chat_driver.driver.number).is_element_displayed():
                    self.errors.append("Message from device '%s' was not received" % chat_driver.driver.number)

        self.verify_no_errors()

    @marks.testrail_id(5674)
    @marks.high
    def test_group_chat_system_messages(self):

        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_default_username = get_username(device_1_home)
        device_2_default_username = get_username(device_2_home)
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        # device 2: delete group chat
        device_2_chat = device_2_home.get_chat_with_user(chat_name).click()
        device_2_chat.delete_chat()

        # device 1: check system messages in the group chat

        system_messages = [
            return_created_chat_system_message(device_1_default_username, chat_name),
            return_joined_chat_system_message(device_2_default_username),
            return_left_chat_system_message(device_2_default_username)
        ]
        for message in system_messages:
            if not device_1_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % message)

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
            self.errors.append("Group chat '%s' is shown, but the chat has been deleted" % chat_name)

        self.verify_no_errors()

    @marks.testrail_id(3998)
    @marks.high
    def test_add_new_group_chat_member(self):
        message_for_device_2 = 'This message should be visible for device 2'
        chat_member = chat_users['A']

        self.create_drivers(2)

        # create accounts on each device
        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()

        # device 2: get public key and default username
        device_2_public_key = device_2_home.get_public_key()
        device_2_default_username = get_username(device_2_home)

        # device 1: add contacts
        device_1_home.add_contact(chat_member['public_key'])
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.add_contact(device_2_public_key)
        device_1_chat.get_back_to_home_view()

        # device 1: create group chat with some user
        device_1_chat = device_1_home.create_group_chat([chat_member['username']], chat_name)

        # device 1: add device 2 as a new member of the group chat
        device_1_chat.add_members_to_group_chat([device_2_default_username])

        # device 2: open the chat
        device_2_chat = device_2_home.get_chat_with_user(chat_name).click()
        device_2_chat.join_chat_button.click()

        # device 1: send a message that should be visible for device 2
        device_1_chat.send_message(message_for_device_2)

        if not device_2_chat.chat_element_by_text(message_for_device_2).is_element_displayed(30):
            self.errors.append('Message that was sent after device 2 has joined is not visible')
        self.verify_no_errors()

    @marks.testrail_id(5756)
    @marks.high
    def test_decline_invitation_to_group_chat(self):
            self.create_drivers(2)
            message_for_device_2 = 'This message should not be visible for device 2'

            device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
            chat_name = device_1_home.get_public_chat_name()
            device_1_chat, device_2_chat = create_new_group_chat(device_1_home, device_2_home, chat_name)
            device_2_chat.decline_invitation_button.click()

            # device 2: check that chat is deleted
            if device_2_home.element_by_text(chat_name).is_element_displayed():
                self.errors.append("Group chat '%s' is shown, but the chat has been deleted" % chat_name)

            # device 1: check system message about leaving a group chat
            device_2_default_username = get_username(device_2_home)
            user2_left_chat_system_message = return_left_chat_system_message(device_2_default_username)
            if not device_1_chat.chat_element_by_text(user2_left_chat_system_message).is_element_displayed():
                self.errors.append("Message with text '%s' was not received" % user2_left_chat_system_message)

            # device 1: send some message to group chat
            device_1_chat.send_message(message_for_device_2)

            # device 2: check that chat doesn't reappear
            if device_2_home.element_by_text(chat_name).is_element_displayed():
                self.errors.append("Group chat '%s' is shown, but the chat has been deleted" % chat_name)
            self.verify_no_errors()

    @marks.testrail_id(4001)
    @marks.high
    def test_remove_member_from_group_chat(self):
        self.create_drivers(2)
        message_for_device_2 = 'This message should not be visible for device 2'

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)
        device_2_default_username = get_username(device_2_home)
        device_2_custom_username = get_username(device_2_home, False)

        # device 1: get options for device 2 in group chat and remove him
        options = device_1_chat.get_user_options(device_2_default_username)
        options.remove_user_button.click()

        # device 2: check that removed user can see that he is removed
        user2_left_chat_system_message_for_user_2 = return_left_chat_system_message(device_2_custom_username)

        # TODO: should be reworked after https://github.com/status-im/status-react/pull/8487: replaced with default username
        # if not device_1_chat.chat_element_by_text(user2_left_chat_system_message_for_user_2).is_element_displayed():
        #     self.errors.append("Message with text '%s' was not received" % user2_left_chat_system_message_for_user_2)

        # if not device_2_chat.chat_element_by_text(user2_left_chat_system_message_for_user_2).is_element_displayed():
        #     self.errors.append("Message with text '%s' was not received" % user2_left_chat_system_message_for_user_2)

        # device 2: check there is no message input so user can't send new message in group chat
        if device_2_chat.chat_message_input.is_element_displayed():
            self.errors.append("Message input is still available for removed user")

        # device 1: send some message to group chat
        device_1_chat.send_message(message_for_device_2)

        # device 2: check that message is not received
        if device_2_chat.chat_element_by_text(message_for_device_2).is_element_displayed():
            self.errors.append("Message with text '%s' was received" % message_for_device_2)

        self.verify_no_errors()

    @marks.testrail_id(5694)
    @marks.high
    def test_make_admin_member_of_group_chat(self):
        self.create_drivers(2)
        chat_member = chat_users['A']

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()
        device_2_default_username = get_username(device_2_home)
        device_2_custom_username = get_username(device_2_home, False)

        # device 2: add contacts
        device_2_home.add_contact(chat_member['public_key'])
        device_2_home.get_back_to_home_view()

        # create and join group chat
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)

        # device 1: get options for device 2 in group chat and make him admin
        options = device_1_chat.get_user_options(device_2_default_username)
        options.make_admin_button.click()

        # device 2: check presence of system message
        # TODO: should be reworked after https://github.com/status-im/status-react/pull/8487: replaced with default username
        # user2_made_admin_system_message_for_user_2 = return_made_admin_system_message(device_2_custom_username)
        # if not device_2_chat.chat_element_by_text(user2_made_admin_system_message_for_user_2).is_element_displayed():
        #     self.errors.append("Message with test '%s' was not received" % user2_made_admin_system_message_for_user_2)

        # device 2: check that as admin can add new members to group chat
        device_2_chat.add_members_to_group_chat([chat_member['username']])

        self.verify_no_errors()

    @marks.testrail_id(5681)
    @marks.high
    def test_clear_history_of_group_chat_via_group_view(self):
        self.create_drivers(2)

        device_1_home, device_2_home = create_users(self.drivers[0], self.drivers[1])
        chat_name = device_1_home.get_public_chat_name()

        # create and join group chat
        device_1_chat, device_2_chat = create_and_join_group_chat(device_1_home, device_2_home, chat_name)
        messages = []

        # device 1, device 2: send messages and clear history on device 1
        for chat in (device_1_chat, device_2_chat):
            message = "Message from device: %s" % chat.driver.number
            chat.send_message(message)
            messages.append(message)

        device_1_chat.clear_history_via_group_info()

        # device 1: check that history is deleted
        for message in messages:
            if device_1_chat.element_starts_with_text(message).is_element_present():
                device_1_chat.driver.fail(
                    "Message '%s' is shown after re-login, but group chat history has been cleared" % message)

        device_1_home.relogin()
        device_1_home.element_by_text(chat_name).click()

        for message in messages:
            if device_1_chat.element_starts_with_text(message).is_element_present():
                device_1_chat.driver.fail(
                    "Message '%s' is shown after re-login, but group chat history has been cleared" % message)

        self.verify_no_errors()