from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from tests.users import transaction_senders, basic_user
from views.sign_in_view import SignInView
from views.chat_view import ChatView
from time import sleep


class TestGroupChatMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3994)
    @marks.high
    def test_create_new_group_chat_messaging_pn_delived(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user(enable_notifications=True)
        device_1_key, device_1_username = device_1.get_public_key_and_username(True)
        device_1.home_button.click()
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.plus_button.click()

        device_1_home.just_fyi('Check default placeholder when trying to create group chat without contacts')
        device_1_home.new_group_chat_button.click()
        if not device_1_home.element_by_translation_id("invite-friends").is_element_displayed():
             self.errors.append("No placeholder is shown when there are no contacts")
        device_1_home.get_back_to_home_view()

        device_2.just_fyi('Create group chat with new user, check system messages for sender')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()

        device_1_chat = device_1_home.create_group_chat([device_2_username], chat_name)
        create_system_message = device_1_chat.create_system_message(device_1_username, chat_name)
        invite_system_message = device_1_chat.invite_system_message(device_1_username, device_2_username)
        join_system_message = device_1_chat.join_system_message(device_2_username)
        invited_to_join = device_1_chat.invited_to_join_system_message(device_1_username, chat_name)
        create_for_admin_system_message = device_1_chat.create_for_admin_system_message(chat_name)
        for message in [create_for_admin_system_message, create_system_message, invite_system_message]:
            if not device_1_chat.chat_element_by_text(message):
                self.errors.append('%s system message is not shown' % message)

        device_2.just_fyi('Navigate to group chat, check system messages for member')
        if not device_2_home.get_chat(chat_name).is_element_displayed():
            self.drivers[0].fail('Group chat was not created!')
        device_2_chat = device_2_home.get_chat(chat_name).click()
        for element in device_2_chat.join_chat_button, device_2_chat.decline_invitation_button:
            if not element.is_element_displayed():
                self.drivers[0].fail('"Join Chat" or "Decline" is not shown for member of group chat')
        for message in [invited_to_join, create_system_message, invite_system_message]:
            if not device_2_chat.chat_element_by_text(message):
                self.errors.append('%s system message is not shown' % message)

        device_2.just_fyi('Join to group chat, check system messages and send messages to group chat, check message status is delivered')
        device_2_chat.join_chat_button.click()
        for chat in (device_1_chat, device_2_chat):
            if not chat.chat_element_by_text(join_system_message).is_element_displayed():
                self.errors.append('System message after joining group chat is not shown')
        device_2_chat.home_button.click(desired_view="home")
        message_1 = "Message from device: %s" % device_1_chat.driver.number
        device_1_chat.send_message(message_1)
        if device_1_chat.chat_element_by_text(message_1).status != 'delivered':
            self.errors.append('Message status is not delivered, it is %s!' % device_1_chat.chat_element_by_text(message_1).status)

        device_2_home.put_app_to_background()

        device_2_home.just_fyi('check that PN is received and after tap you are redirected to public chat')
        device_2_home.open_notification_bar()
        device_2_home.element_by_text_part("Message from device: %s" % device_1_chat.driver.number).click()
        device_2_chat.send_message("Message from device: %s" % device_2_chat.driver.number)
        for chat in (device_1_chat, device_2_chat):
            for chat_driver in (device_1_chat, device_2_chat):
                if not chat.chat_element_by_text(
                        "Message from device: %s" % chat_driver.driver.number).is_element_displayed():
                    self.errors.append("Message from device '%s' was not received" % chat_driver.driver.number)

        self.errors.verify_no_errors()

    @marks.testrail_id(3998)
    @marks.high
    def test_offline_add_new_group_chat_member(self):
        message_before_adding = 'message before adding new user'
        message_after_adding = 'message from new member'
        message_from_old_member_after_adding = 'message from old member'

        self.create_drivers(3)
        devices_home, devices_key, devices_username, devices_chat = {}, {}, {}, {}
        for key in self.drivers:
            sign_in_view = SignInView(self.drivers[key])
            devices_home[key] = sign_in_view.create_user()
            devices_key[key], devices_username[key] = sign_in_view.get_public_key_and_username(True)
            sign_in_view.home_button.click()

        chat_name = devices_home[0].get_random_chat_name()
        for i in range(1, 3):
            devices_home[0].add_contact(devices_key[i])
            devices_home[0].get_back_to_home_view()
        devices_chat[0] = devices_home[0].create_group_chat([devices_username[1]], chat_name)
        devices_chat[0].send_message(message_before_adding)

        devices_home[1].just_fyi('Join to chat as chat member')
        devices_chat[1] = devices_home[1].get_chat(chat_name).click()
        devices_chat[1].join_chat_button.click()

        devices_home[2].just_fyi('Put not added member device to offline and check that invite will be fetched')
        invite_system_message = devices_chat[0].invite_system_message(devices_username[0], devices_username[1])
        devices_home[2].toggle_airplane_mode()
        devices_chat[0].add_members_to_group_chat([devices_username[2]])
        devices_home[2].toggle_airplane_mode()
        devices_home[2].connection_offline_icon.wait_for_invisibility_of_element(60)
        if not devices_home[2].get_chat(chat_name).is_element_displayed():
            self.drivers[0].fail('Invite to group chat was not fetched from offline')
        devices_chat[2] = devices_home[2].get_chat(chat_name).click()
        if not devices_chat[2].element_by_text(invite_system_message).is_element_displayed():
            self.errors.append('Message about adding first chat member is not shown for new added member')
        if devices_chat[2].element_by_text(message_before_adding).is_element_displayed():
            self.errors.append('Message sent before adding user is shown')

        devices_chat[0].just_fyi('Put admin device to offline and check that message from new member will be fetched')
        devices_chat[0].toggle_airplane_mode()
        devices_chat[2].join_chat_button.click()
        devices_chat[2].send_message(message_after_adding)
        devices_chat[0].toggle_airplane_mode()
        for key in devices_chat:
            if not devices_chat[key].chat_element_by_text(message_after_adding).is_element_displayed(
                    40):
                self.errors.append("Message with text '%s' was not received" % message_after_adding)

        devices_chat[0].just_fyi('Send message from old member and check that it is fetched')
        devices_chat[1].send_message(message_from_old_member_after_adding)
        for key in devices_chat:
            if not devices_chat[key].chat_element_by_text(message_from_old_member_after_adding).is_element_displayed(
                    20):
                self.errors.append("Message with text '%s' was not received" % message_from_old_member_after_adding)

        self.errors.verify_no_errors()

    @marks.testrail_id(3997)
    @marks.medium
    def test_leave_group_chat_via_group_info(self):

        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        chat_name = device_1_home.get_random_chat_name()

        device_2.just_fyi('Create and join group chat')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.create_group_chat([device_2_username], chat_name)
        left_system_message = device_1_chat.leave_system_message(device_2_username)
        device_2_chat = device_2_home.get_chat(chat_name).click()
        device_2_chat.join_chat_button.click()

        device_2.just_fyi('Send several message and leave chat')
        for chat in device_1_chat, device_2_chat:
            chat.send_message('sent before leaving')
        device_2_chat.leave_chat_via_group_info()
        if not device_1_chat.chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('No system message after user left the group chat')
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' is shown, but user has left" % chat_name)

        device_2.just_fyi('Send message after user is left and check that it is not reappeared')
        message = 'sent after leaving'
        device_1_chat.send_message(message)
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' reappeared when new message is sent" % chat_name)
        self.errors.verify_no_errors()

    @marks.testrail_id(5756)
    @marks.medium
    def test_decline_invitation_to_group_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.plus_button.click()

        device_2.just_fyi('Create group chat with new user')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.create_group_chat([device_2_username], chat_name)
        device_2_chat = device_2_home.get_chat(chat_name).click()
        device_2_chat.decline_invitation_button.click()
        left_system_message = device_2_chat.leave_system_message(device_2_username)
        if not device_1_chat.chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('No system message after user left the group chat')
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' is shown, but user has left" % chat_name)

        device_2.just_fyi('Send message after invite is declined and check that it is not reappeared')
        message = 'sent after leaving'
        device_1_chat.send_message(message)
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' reappeared when new message is sent" % chat_name)

        self.errors.verify_no_errors()

    @marks.testrail_id(4001)
    @marks.medium
    def test_remove_member_from_group_chat(self):
        self.create_drivers(3)
        devices_home, devices_key, devices_username, devices_chat = {}, {}, {}, {}
        for key in self.drivers:
            sign_in_view = SignInView(self.drivers[key])
            devices_home[key] = sign_in_view.create_user()
            devices_key[key], devices_username[key] = sign_in_view.get_public_key_and_username(True)
            sign_in_view.home_button.click()

        chat_name = devices_home[0].get_random_chat_name()
        for i in range(1,3):
            devices_home[0].add_contact(devices_key[i])
            devices_home[0].get_back_to_home_view()
        devices_chat[0] = devices_home[0].create_group_chat([devices_username[1],
                                                             devices_username[2]], chat_name)

        devices_chat[0].just_fyi('Member_1, member_2: both users join to group chat')
        for i in range(1,3):
            devices_chat[i] = devices_home[i].get_chat(chat_name).click()
            devices_chat[i].join_chat_button.click()

        devices_chat[0].just_fyi("Admin: get options for device 2 in group chat and remove him")
        options = devices_chat[0].get_user_options(devices_username[1])
        options.remove_user_button.click()
        left_message = devices_chat[0].leave_system_message(devices_username[1])
        for key in devices_chat:
            if not devices_chat[key].chat_element_by_text(left_message).is_element_displayed():
                self.errors.append("Message with text '%s' was not received" % left_message)

        devices_chat[0].just_fyi("Check that input field is not available after removing")
        if devices_chat[1].chat_message_input.is_element_displayed():
            self.errors.append("Message input is still available for removed user")

        devices_chat[0].just_fyi("Send message and check that it is available only for remaining users")
        message = 'after removing member'
        devices_chat[0].send_message(message)
        for chat in (devices_chat[0], devices_chat[2]):
            if not chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message '%s' was not received after removing member" % message)

        if devices_chat[1].chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message '%s' was received by removed member" % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(6324)
    @marks.medium
    def test_invite_to_group_chat_handling(self):
        self.create_drivers(3)
        devices_sign_in, devices_home, devices_key, devices_username, devices_chat = {}, {}, {}, {}, {}
        for key in self.drivers:
            devices_sign_in[key] = SignInView(self.drivers[key])
            devices_home[key] = devices_sign_in[key].create_user()
            devices_key[key], devices_username[key] = devices_sign_in[key].get_public_key_and_username(True)
            devices_sign_in[key].home_button.click()
        [driver.close_app() for driver in (self.drivers[1], self.drivers[2])]

        chat_name = devices_home[0].get_random_chat_name()

        devices_home[0].just_fyi('Create group chats without members')
        devices_chat[0] = devices_home[0].create_group_chat([], chat_name)
        link = devices_chat[0].get_group_invite_via_group_info()
        devices_chat[0].get_back_to_home_view()


        devices_chat[0].just_fyi('Member_1, member_2: both users send requests to join group chat')
        [sign_in.open_weblink_and_login(link) for sign_in in (devices_sign_in[1], devices_sign_in[2])]
        introduction_messages = ['message for retrying']
        for i in range(1,3):
            devices_home[i].element_by_text_part(chat_name).click()
            devices_chat[i] = ChatView(self.drivers[i])
            introduction_messages.append('Please add me, member_%s to your gorgeous group chat' % str(i))
            devices_chat[i].request_membership_for_group_chat(introduction_messages[i])

        devices_chat[0].just_fyi('Admin: accept request for Member_1 and decline for Member_2')
        devices_home[0].get_chat(chat_name).click()
        devices_chat[0].group_membership_request_button.click()
        devices_chat[0].element_by_text(devices_username[1]).click()
        if not devices_chat[0].element_by_text_part(introduction_messages[1]).is_element_displayed():
            self.errors.append('Introduction message is not shown!')
        devices_chat[0].accept_group_invitation_button.click()
        devices_chat[0].accept_membership_for_group_chat_via_chat_view(devices_username[2], accept=False)
        devices_chat[0].click_system_back_button()

        devices_chat[1].just_fyi('Member_1: join chat')
        devices_chat[1].join_chat_button.click()

        devices_chat[2].just_fyi('Member_2: retry request')
        devices_chat[2].retry_group_invite_button.click()
        devices_chat[2].request_membership_for_group_chat(introduction_messages[0])

        devices_chat[2].just_fyi('Admin: decline request for Member_2')
        devices_chat[0].group_membership_request_button.click()
        devices_chat[0].element_by_text(devices_username[2]).click()
        if not devices_chat[0].element_by_text_part(introduction_messages[0]).is_element_displayed():
            self.errors.append('Introduction message that was set after retrying attempt is not shown for admin!')
        devices_chat[0].decline_group_invitation_button.click()
        devices_chat[0].click_system_back_button()

        devices_chat[2].just_fyi('Member_2: remove chat')
        devices_chat[2].remove_group_invite_button.click()

        devices_chat[2].just_fyi('Double check after relogin')
        if devices_chat[0].group_membership_request_button.is_element_displayed():
            self.errors.append('Group membership request is still shown when there are no pending requests anymore')
        [devices_home[i].relogin() for i in range(0,3)]
        if devices_home[2].element_by_text_part(chat_name).is_element_displayed():
            self.errors.append('Group chat was not removed when removing after declining group invite')
        [home.get_chat(chat_name).click() for home in (devices_home[0], devices_home[1])]
        if devices_chat[0].group_membership_request_button.is_element_displayed():
            self.errors.append('Group membership request is shown after relogin when there are no pending requests anymore')
        join_system_message = devices_chat[0].join_system_message(devices_username[1])
        for chat in (devices_chat[1], devices_chat[0]):
            if not chat.chat_element_by_text(join_system_message).is_element_displayed():
                self.errors.append('%s is not shown after joining to group chat via invite' % join_system_message)

        self.errors.verify_no_errors()

    @marks.testrail_id(5694)
    @marks.medium
    def test_make_admin_member_of_group_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        device_1_key, device_1_username = device_1.get_public_key_and_username(True)
        device_1.home_button.click()
        chat_name = device_1_home.get_random_chat_name()

        device_2.just_fyi('Create and join to group chat')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.create_group_chat([device_2_username], chat_name)
        device_2_chat = device_2_home.get_chat(chat_name).click()
        device_2_chat.join_chat_button.click()

        device_1.just_fyi('Check group info view and options of users')
        device_1_chat.chat_options.click()
        group_info_view = device_1_chat.group_info.click()
        if not group_info_view.user_admin(device_1_username).is_element_displayed():
            self.errors.append("Admin user is not marked as admin")
        group_info_view.get_user_from_group_info(device_1_username).click()
        if device_1_chat.profile_block_contact.is_element_displayed():
            self.errors.append("Admin is redirected to own profile on tapping own username from group info")
        group_info_view.get_user_from_group_info(device_2_username).click()
        if not device_1_chat.profile_block_contact.is_element_displayed():
            self.errors.append("Admin is not redirected to user profile on tapping member username from group info")
        device_1_chat.back_button.click()

        device_1.just_fyi('Made admin another user and check system message')
        options = group_info_view.get_username_options(device_2_username).click()
        options.make_admin_button.click()
        admin_system_message = device_1_chat.has_been_made_admin_system_message(device_1_username, device_2_username)
        for chat in (device_1_chat, device_2_chat):
            if not chat.chat_element_by_text(admin_system_message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % admin_system_message)

        device_2.just_fyi('Check Admin in group info and that "add members" is available')
        device_2_chat.chat_options.click()
        group_info_view = device_2_chat.group_info.click()
        for username in (device_1_username, device_2_username):
            if not group_info_view.user_admin(username).is_element_displayed():
                self.errors.append("Admin user is not marked as admin")
        if not group_info_view.add_members.is_element_displayed():
            self.errors.append("Add member button is not available for new admin")

        self.errors.verify_no_errors()

    @marks.testrail_id(6280)
    @marks.medium
    def test_rename_group_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        device_1_key, device_1_username = device_1.get_public_key_and_username(True)
        device_1.home_button.click()
        initial_chat_name = device_1_home.get_random_chat_name()

        device_2.just_fyi('Create and join group chat')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.create_group_chat([device_2_username], initial_chat_name)
        device_2_chat = device_2_home.get_chat(initial_chat_name).click()
        device_2_chat.join_chat_button.click()

        device_2.just_fyi('Rename chat and check system messages')
        new_chat_name = device_1_chat.get_random_chat_name()
        device_1_chat.rename_chat_via_group_info(new_chat_name)
        for chat in (device_1_chat, device_2_chat):
            if not chat.element_by_text(chat.create_system_message(device_1_username, initial_chat_name)).is_element_displayed():
                self.errors.append('Initial system message about creating chta was changed!')
            if not chat.element_by_text(chat.changed_group_name_system_message(device_1_username, new_chat_name)).is_element_displayed():
                self.errors.append('Message about changing chat name is not shown')

        device_2.just_fyi('Check that you can navigate to renamed chat')
        device_2_chat.back_button.click()
        device_2_home.get_chat(new_chat_name).click()

        self.errors.verify_no_errors()

    @marks.testrail_id(5752)
    @marks.medium
    def test_block_and_unblock_user_from_group_chat_via_group_info(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        initial_chat_name = device_1_home.get_random_chat_name()

        device_2.just_fyi('Create and join group chat')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        device_1_home.add_contact(device_2_key)
        device_1_home.get_back_to_home_view()
        device_1_chat = device_1_home.create_group_chat([device_2_username], initial_chat_name)
        device_2_chat = device_2_home.get_chat(initial_chat_name).click()
        device_2_chat.join_chat_button.click()

        device_2.just_fyi('Send message and block user via Group Info')
        message_before_block = 'message from device2'
        device_2_chat.send_message(message_before_block)
        device_2_options = device_1_chat.get_user_options(device_2_username)
        device_2_options.view_profile_button.click()
        device_2_options.block_contact()
        device_2_options.back_button.click()
        if device_1_chat.chat_element_by_text(message_before_block).is_element_displayed(10):
            self.errors.append('User was blocked, but past message are shown')
        message_after_block = 'message from device2 after block'
        device_2_chat.send_message(message_after_block)
        if device_1_chat.chat_element_by_text(message_after_block).is_element_displayed(10):
            self.errors.append('User was blocked, but new messages still received')

        device_1.just_fyi('Unblock user via group info and check that new messages will arrive')
        device_2_options = device_1_chat.get_user_options(device_2_username)
        device_2_options.view_profile_button.click()
        device_2_options.unblock_contact_button.click()
        device_2_options.back_button.click(2)
        message_after_unblock = 'message from device2 after unblock'
        device_2_chat.send_message(message_after_unblock)
        if not device_1_chat.chat_element_by_text(message_after_unblock).is_element_displayed(20):
            self.errors.append('User was unblocked, but new messages are not received')

        self.errors.verify_no_errors()

    @marks.testrail_id(6317)
    @marks.medium
    def test_pair_devices_group_chat_different_messages_nicknames(self):
        self.create_drivers(3)
        device_1, device_2, device_3 = SignInView(self.drivers[0]), SignInView(self.drivers[1]), SignInView(self.drivers[2])

        device_1_home = device_1.create_user()
        device_1_profile = device_1_home.profile_button.click()
        device_1_profile.privacy_and_security_button.click()
        device_1_profile.backup_recovery_phrase_button.click()
        device_1_profile.ok_continue_button.click()
        recovery_phrase = device_1_profile.get_recovery_phrase()
        device_1_profile.back_button.click()
        device_1_profile.get_back_to_home_view()
        device_3_home = device_3.create_user()
        device_3_chat_key, device_3_username = device_3_home.get_public_key_and_username(return_username=True)
        device_3.home_button.click()
        device_1_name, device_2_name, group_chat_name = 'creator', 'paired', 'some group chat'

        device_1.just_fyi('Add contact, start group chat')
        nickname = 'my_tester'
        device_1_home.add_contact(device_3_chat_key,nickname=nickname)
        device_1_home.back_button.click()
        device_1_chat = device_1_home.create_group_chat([device_3_username], group_chat_name)
        device_3_chat = device_3_home.get_chat(group_chat_name).click()
        device_3_chat.join_chat_button.click()

        device_2.just_fyi('Go to profile > Devices, set device name, discover device 2 to device 1')
        device_2_home = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        device_2_profile = device_2_home.profile_button.click()
        device_2_profile.discover_and_advertise_device(device_2_name)
        device_1.profile_button.click()
        device_1_profile.discover_and_advertise_device(device_1_name)
        device_1_profile.get_toggle_device_by_name(device_2_name).click()
        device_1_profile.sync_all_button.click()
        device_1_profile.sync_all_button.wait_for_visibility_of_element(15)
        device_1_profile.click_system_back_button(2)

        device_1.just_fyi('Send message to group chat and verify it on all devices')
        text_message = 'some text'
        device_1_profile.home_button.click(desired_view='chat')
        device_2_profile.home_button.click()
        device_1_chat.send_message(text_message)
        device_2_chat = device_2_home.get_chat(group_chat_name).click()
        for chat in device_1_chat, device_2_chat, device_3_chat:
            if not chat.chat_element_by_text(text_message).is_element_displayed():
                self.errors.append('Message was sent, but it is not shown')

        device_3.just_fyi('Send message to group chat as member and verify nickname on it')
        message_from_member = 'member1'
        device_3_chat.send_message(message_from_member)
        device_1_chat.chat_element_by_text(message_from_member).wait_for_visibility_of_element(20)
        for chat in device_1_chat, device_2_chat:
            if not chat.chat_element_by_text(message_from_member).username != '%s %s' % (nickname, device_3_username):
                self.errors.append('Nickname is not shown in group chat')

        device_1.just_fyi('Send image to group chat and verify it on all devices')
        device_1_chat.show_images_button.click()
        device_1_chat.allow_button.click()
        device_1_chat.first_image_from_gallery.click()
        device_1_chat.send_message_button.click()
        device_1_chat.chat_message_input.click()
        for chat in device_1_chat, device_2_chat, device_3_chat:
            if not chat.image_chat_item.is_element_displayed(30):
                self.errors.append('Image is not shown in chat after sending for %s' % chat.driver.number)

        device_1.just_fyi('Send audio message to group chat and verify it on all devices')
        device_1_chat.record_audio_message(message_length_in_seconds=3)
        device_1.send_message_button.click()
        device_1_chat.chat_message_input.click()
        for chat in device_1_chat, device_2_chat, device_3_chat:
            if not chat.play_pause_audio_message_button.is_element_displayed(30):
                self.errors.append('Audio message is not shown in chat after sending!')

        device_1.just_fyi('Send sticker to group chat and verify it on all devices')
        device_1_chat.profile_button.click()
        device_1_profile.switch_network()
        device_1_home.get_chat(group_chat_name).click()
        device_1_chat.show_stickers_button.click()
        device_1_chat.get_stickers.click()
        device_1_chat.install_sticker_pack_by_name('Status Cat')
        device_1_chat.back_button.click()
        sleep(2)
        device_1_chat.swipe_left()
        device_1_chat.sticker_icon.click()
        if not device_1_chat.sticker_message.is_element_displayed(30):
            self.errors.append('Sticker was not sent')

        self.errors.verify_no_errors()


class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(5721)
    @marks.medium
    def test_cant_add_more_twenty_participants_to_group_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        users = [transaction_senders['A'], transaction_senders['B'], transaction_senders['C'], transaction_senders['D'],
                 transaction_senders['E'], transaction_senders['F'], transaction_senders['G'], transaction_senders['H'],
                 transaction_senders['I'], transaction_senders['K'], transaction_senders['L'], transaction_senders['M'],
                 transaction_senders['N'], transaction_senders['O'], transaction_senders['P'], transaction_senders['Q'],
                 transaction_senders['R'], transaction_senders['S'], transaction_senders['T'], transaction_senders['U'],
                 ]
        usernames = []

        home.just_fyi('Add 20 users to contacts')
        for user in users:
            home.add_contact(user['public_key'])
            usernames.append(user['username'])
            home.get_back_to_home_view()

        home.just_fyi('Create group chat with max amount of users')
        chat = home.create_group_chat(usernames, 'some_group_chat')

        home.just_fyi('Verify that can not add more users via group info')
        chat.chat_options.click()
        group_info_view = chat.group_info.click()
        if group_info_view.add_members.is_element_displayed():
            self.errors.append('Add members button is displayed when max users are added in chat')
        if not group_info_view.element_by_text_part('20 members').is_element_displayed():
            self.errors.append('Amount of users is not shown on Group info screen')

        self.errors.verify_no_errors()
