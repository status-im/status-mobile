from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, ens_user
from views.sign_in_view import SignInView
from views.chat_view import ChatView
import random
import emoji
import pytest


@pytest.mark.xdist_group(name="group_chat_3")
class TestPublicChatMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(3)
        cls.message_before_adding = 'message before adding new user'
        cls.message_to_admin = 'Hey, admin!'

        cls.homes, cls.public_keys, cls.usernames, cls.chats = {}, {}, {}, {}
        for key in cls.drivers:
            sign_in = SignInView(cls.drivers[key])
            cls.homes[key] = sign_in.create_user(enable_notifications=True)
            cls.public_keys[key], cls.usernames[key] = sign_in.get_public_key_and_username(True)
            sign_in.home_button.click()
        cls.chat_name = cls.homes[0].get_random_chat_name()

        cls.homes[0].just_fyi('Admin adds future members to contacts')
        for i in range(1, 3):
            cls.homes[0].add_contact(cls.public_keys[i])
            cls.homes[0].home_button.double_click()

        cls.homes[0].just_fyi('Member adds admin to contacts to see PNs and put app in background')
        cls.homes[1].add_contact(cls.public_keys[0])
        cls.homes[1].home_button.double_click()

        cls.homes[0].just_fyi('Admin creates group chat')
        cls.chats[0] = cls.homes[0].create_group_chat([cls.usernames[1]], cls.chat_name)
        for i in range(1, 3):
            cls.chats[i] = ChatView(cls.drivers[i])

        cls.chats[0].send_message(cls.message_before_adding)

    @marks.testrail_id(3994)
    @marks.critical
    def test_group_pn_system_messages_when_invited(self):
        self.homes[1].just_fyi("Check system messages in PNs")
        self.homes[1].put_app_to_background()
        self.homes[1].open_notification_bar()
        pns = [self.chats[0].pn_invited_to_group_chat(self.usernames[0], self.chat_name),
               self.chats[0].pn_wants_you_to_join_to_group_chat(self.usernames[0], self.chat_name)]
        for pn in pns:
            if not self.homes[1].get_pn(pn):
                self.errors.append('%s is not shown after invite to group chat' % pn)
        if self.homes[1].get_pn(pns[0]):
            group_invite_pn = self.homes[1].get_pn(pns[0])
            group_invite_pn.click()
        else:
            self.homes[1].click_system_back_button(2)
            self.homes[1].get_chat(self.chat_name).click()

        self.homes[1].just_fyi("Check system messages in group chat for admin and member")
        create_system_message = self.chats[0].create_system_message(self.usernames[0], self.chat_name)
        invite_system_message = self.chats[0].invite_system_message(self.usernames[0], self.usernames[1])

        invited_to_join = self.chats[0].invited_to_join_system_message(self.usernames[0], self.chat_name)
        create_for_admin_system_message = self.chats[0].create_for_admin_system_message(self.chat_name)
        for message in [create_for_admin_system_message, create_system_message, invite_system_message]:
            if not self.chats[0].chat_element_by_text(message):
                self.errors.append('%s system message is not shown' % message)
        for message in [invited_to_join, create_system_message, invite_system_message]:
            if not self.chats[1].chat_element_by_text(message):
                self.errors.append('%s system message is not shown' % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(700731)
    @marks.critical
    def test_group_join_send_text_messages_pn(self):
        message_to_admin = self.message_to_admin
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[1].get_chat(self.chat_name).click()

        self.chats[1].just_fyi('Join to group chat')
        join_system_message = self.chats[1].join_system_message(self.usernames[1])
        self.chats[1].join_chat_button.click()
        if not self.chats[1].chat_element_by_text(join_system_message).is_element_displayed(30):
            self.drivers[1].fail('System message after joining group chat is not shown')
        self.chats[1].send_message(message_to_admin)

        self.chats[0].just_fyi('check that PN is received and after tap you are redirected to group chat')
        self.chats[0].open_notification_bar()
        pn = self.homes[0].get_pn(message_to_admin)
        if pn:
            pn.click()
        else:
            self.homes[0].click_system_back_button()
            self.homes[0].get_chat(self.chat_name).click()

        self.chats[1].just_fyi('Check message status and message delivery')
        message_status = self.chats[1].chat_element_by_text(message_to_admin).status
        if message_status != 'delivered':
            self.errors.append('Message status is not delivered, it is %s!' % message_status)
        for message in (join_system_message, message_to_admin):
            if not self.chats[0].chat_element_by_text(message).is_element_displayed(30):
                self.drivers[0].fail('Message %s was not received by admin' % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(700732)
    @marks.critical
    def test_group_add_new_member_activity_centre(self):
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[0].get_chat(self.chat_name).click()
        self.chats[0].add_members_to_group_chat([self.usernames[2]])

        self.chats[2].just_fyi("Check there will be no PN but unread in AC if got invite from non-contact")
        if not self.homes[2].notifications_unread_badge.is_element_displayed(60):
            self.drivers[2].fail("Group chat is not appeared in AC!")
        self.homes[2].open_notification_bar()
        if self.homes[2].element_by_text_part(self.usernames[0]).is_element_displayed():
            self.errors.append("PN about group chat invite is shown when invited by non-contact")
        self.homes[2].click_system_back_button()
        self.homes[2].get_chat(self.chat_name).click()
        self.chats[2].join_chat_button.click()
        for message in (self.message_to_admin, self.message_before_adding):
            if self.chats[2].chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is shown for new user' % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(3998)
    @marks.critical
    def test_group_offline_pn(self):
        [self.homes[i].home_button.double_click() for i in range(3)]
        chat_name = 'for_offline_pn'
        self.homes[0].create_group_chat([self.usernames[1], self.usernames[2]], chat_name)
        self.homes[0].home_button.double_click()
        for i in range(1, 3):
            self.homes[i].get_chat(chat_name).click()
            self.chats[i].join_chat_button.click()
        message_1, message_2 = 'message from old member', 'message from new member'

        self.homes[0].just_fyi("Put admin device to offline and send messages from members")
        self.homes[0].toggle_airplane_mode()
        self.chats[1].send_message(message_1)
        self.chats[2].send_message(message_2)

        self.homes[0].just_fyi("Put admin device to online and check that messages and PNs will be fetched")
        self.homes[0].toggle_airplane_mode()
        self.homes[0].connection_offline_icon.wait_for_invisibility_of_element(60)
        self.homes[0].open_notification_bar()
        for message in (message_1, message_2):
            if not self.homes[0].get_pn(message):
                self.errors.append('%s PN was not fetched from offline' % message)
        self.homes[0].click_system_back_button()
        unread_group = self.homes[0].get_chat(chat_name)
        if not unread_group.new_messages_counter.text == '2':
            self.errors.append('%s does not match unread messages' % unread_group.new_messages_counter.text)
        unread_group.click()

        self.homes[0].just_fyi("check that messages are shown for every member")
        for i in range(3):
            for message in (message_1, message_2):
                if not self.chats[i].chat_element_by_text(message).is_element_displayed():
                    self.errors.append('%s if not shown for device %s' % (message, str(i)))
        self.errors.verify_no_errors()

    @marks.testrail_id(5756)
    @marks.critical
    def test_group_decline_invite_chat_highligted(self):
        chat_name = 'for_invited'
        left_system_message = self.chats[0].leave_system_message(self.usernames[1])
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[0].create_group_chat([self.usernames[1]], chat_name)

        self.homes[1].just_fyi("Check that new group chat from contact is highlited")
        chat_2_element = self.homes[1].get_chat(chat_name)
        if chat_2_element.no_message_preview.is_element_differs_from_template('highligted_preview_group.png', 0):
            self.errors.append("Preview message is not hightligted or text is not shown! ")
        chat_2 = self.homes[1].get_chat(chat_name).click()
        chat_2.home_button.click()
        if not chat_2_element.no_message_preview.is_element_differs_from_template('highligted_preview_group.png', 0):
            self.errors.append("Preview message is still hightligted after opening! ")
        self.homes[1].get_chat(chat_name).click()
        chat_2.decline_invitation_button.click()
        if self.chats[0].chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('System message when user declined invite is shown')
        if self.homes[1].element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' is shown, but user declined invite" % chat_name)

        self.homes[0].just_fyi('Send message after invite is declined and check that it is not reappeared')
        message = 'sent after leaving'
        self.chats[0].send_message(message)
        if self.homes[1].element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' reappeared when new message is sent" % chat_name)
        self.errors.verify_no_errors()

    @marks.testrail_id(3997)
    @marks.critical
    def test_group_leave_relogin(self):
        self.drivers[2].quit()
        [self.homes[i].home_button.double_click() for i in range(2)]
        self.homes[0].home_button.double_click()
        self.homes[1].get_chat(self.chat_name).click()
        join_button = self.chats[1].join_chat_button
        if join_button.is_element_displayed():
            join_button.click()

        self.homes[0].just_fyi("Admin deleted chat via long press")
        self.homes[0].leave_chat_long_press(self.chat_name)

        self.homes[0].just_fyi("Member sends some message, admin relogins and check chat does not reappear")
        self.chats[1].send_message(self.message_to_admin)
        self.homes[0].relogin()
        if self.homes[0].get_chat_from_home_view(self.chat_name).is_element_displayed():
            self.drivers[0].fail('Deleted %s is present after relaunch app' % self.chat_name)


class TestGroupChatMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5694)
    @marks.medium
    def test_make_admin_member_of_group_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        public_key_1, username_1 = device_1.get_public_key_and_username(True)
        device_1.home_button.click()
        chat_name = home_1.get_random_chat_name()

        device_2.just_fyi('Create and join to group chat')
        device_2_key, device_2_username = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        home_1.add_contact(device_2_key)
        home_1.get_back_to_home_view()
        chat_1 = home_1.create_group_chat([device_2_username], chat_name)
        chat_2 = home_2.get_chat(chat_name).click()
        chat_2.join_chat_button.click()

        device_1.just_fyi('Check group info view and options of users')
        chat_1.chat_options.click()
        group_info_1 = chat_1.group_info.click()
        if not group_info_1.user_admin(username_1).is_element_displayed():
            self.errors.append("Admin user is not marked as admin")
        group_info_1.get_user_from_group_info(username_1).click()
        if chat_1.profile_block_contact.is_element_displayed():
            self.errors.append("Admin is redirected to own profile on tapping own username from group info")
        group_info_1.get_user_from_group_info(device_2_username).click()
        if not chat_1.profile_block_contact.is_element_displayed():
            self.errors.append("Admin is not redirected to user profile on tapping member username from group info")
        chat_1.close_button.click()

        device_1.just_fyi('Made admin another user and check system message')
        options = group_info_1.get_username_options(device_2_username).click()
        options.make_admin_button.click()
        admin_system_message = chat_1.has_been_made_admin_system_message(username_1, device_2_username)
        for chat in (chat_1, chat_2):
            if not chat.chat_element_by_text(admin_system_message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % admin_system_message)

        device_2.just_fyi('Check Admin in group info and that "add members" is available')
        chat_2.chat_options.click()
        group_info_1 = chat_2.group_info.click()
        for username in (username_1, device_2_username):
            if not group_info_1.user_admin(username).is_element_displayed():
                self.errors.append("Admin user is not marked as admin")
        if not group_info_1.add_members.is_element_displayed():
            self.errors.append("Add member button is not available for new admin")

        self.errors.verify_no_errors()

    @marks.testrail_id(6280)
    @marks.medium
    def test_rename_group_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        device_1_key, device_1_username = device_1.get_public_key_and_username(True)
        device_1.home_button.click()
        initial_chat_name = home_1.get_random_chat_name()

        device_2.just_fyi('Create and join group chat')
        public_key_2, username_2 = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        home_1.add_contact(public_key_2)
        home_1.get_back_to_home_view()
        device_1_chat = home_1.create_group_chat([username_2], initial_chat_name)
        device_2_chat = home_2.get_chat(initial_chat_name).click()
        device_2_chat.join_chat_button.click()

        device_2.just_fyi('Rename chat and check system messages')
        new_chat_name = device_1_chat.get_random_chat_name()
        device_1_chat.rename_chat_via_group_info(new_chat_name)
        for chat in (device_1_chat, device_2_chat):
            if not chat.element_by_text(
                    chat.create_system_message(device_1_username, initial_chat_name)).is_element_displayed():
                self.errors.append('Initial system message about creating chta was changed!')
            if not chat.element_by_text(
                    chat.changed_group_name_system_message(device_1_username, new_chat_name)).is_element_displayed():
                self.errors.append('Message about changing chat name is not shown')

        device_2.just_fyi('Check that you can see renamed chat')
        device_2_chat.back_button.click()
        home_2.get_chat(new_chat_name).wait_for_visibility_of_element(60)

        self.errors.verify_no_errors()

    @marks.testrail_id(6327)
    @marks.medium
    def test_nicknames_ens_group_chats(self):
        self.create_drivers(2)
        home_1 = SignInView(self.drivers[0]).create_user()
        home_2 = SignInView(self.drivers[1]).recover_access(ens_user['passphrase'])
        profile_1, profile_2 = [home.profile_button.click() for home in (home_1, home_2)]
        key_1, username_1 = profile_1.get_public_key_and_username(return_username=True)
        ens, full_ens, username_2 = ens_user['ens'], '@%s' % ens_user['ens'], ens_user['username']
        profile_2.connect_existing_ens(ens)
        [profile.home_button.click() for profile in (profile_1, profile_2)]

        home_1.just_fyi('Set nickname, using emojis, special chars and cyrrilic chars without adding to contact')
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        special_char, cyrrilic = '"£¢€¥~`•|√π¶∆×°™®©%$@', 'стат'
        nickname_to_set = emoji.emojize(emoji_message) + special_char + cyrrilic
        nickname_expected = emoji_unicode + special_char + cyrrilic
        chat_1 = home_1.add_contact(ens, add_in_contacts=False, nickname=nickname_to_set)
        if chat_1.user_name_text.text != nickname_expected:
            self.errors.append('Expected special char nickname %s does not match actual %s' % (nickname_expected, chat_1.user_name_text.text))

        home_1.just_fyi('Can remove nickname without adding to contact')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.profile_nickname_button.click()
        chat_1.nickname_input_field.clear()
        chat_1.element_by_text('Done').click()
        chat_1.close_button.click()
        if chat_1.user_name_text.text != full_ens:
            self.errors.append(
                'Nickname was not removed! real chat name is %s instead of %s' % (chat_1.user_name_text.text, full_ens))

        home_1.just_fyi('Adding ENS user to contacts and start group chat with him')
        group_name = 'ens_group'
        chat_1.add_to_contacts.click()
        chat_2 = home_2.add_contact(key_1)
        chat_2.send_message("first")
        chat_2.home_button.click()
        chat_1.home_button.click()
        chat_1 = home_1.create_group_chat([full_ens], group_name)
        chat_2 = home_2.get_chat(group_name).click()
        chat_2.join_chat_button.click()

        home_1.just_fyi('Check ENS and in group chat and suggestions list')
        chat_1.element_by_text_part(full_ens).wait_for_visibility_of_element(60)
        chat_1.select_mention_from_suggestion_list(ens, typed_search_pattern=ens[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        additional_text = 'and more'
        chat_1.send_as_keyevent(additional_text)
        chat_1.send_message_button.click()
        message_text = '%s %s' % (full_ens, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name is not resolved on sent message")
        chat_1 = home_1.get_chat_view()

        home_1.just_fyi('Set nickname via group info and check that can mention by nickname /username in group chat')
        nickname = 'funny_bunny'
        device_2_options = chat_1.get_user_options(full_ens)
        device_2_options.view_profile_button.click()
        chat_1.set_nickname(nickname, close_profile=False)
        if not chat_1.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname is not shown in profile view after setting from group info')
        chat_1.close_button.click()
        chat_1.element_by_text(nickname).scroll_to_element()
        chat_1.close_button.click()
        message_text = '%s %s' % (nickname, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name was not replaced with nickname on sent message")
        chat_1.chat_message_input.send_keys('@')
        if not chat_1.element_by_text('%s %s' % (nickname, full_ens)).is_element_displayed():
            self.errors.append("ENS name with nickname is not shown in mention input after set")
        if not chat_1.element_by_text(username_2).is_element_displayed():
            self.errors.append("3-random name is not shown in mention input after set from group info")
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' % (nickname, full_ens), typed_search_pattern=username_2[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS is not resolved in chat input after setting nickname in mention suggestions list (search by 3-random name)!')
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' % (nickname, full_ens), typed_search_pattern=nickname[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS is not resolved in chat input after setting nickname in mention suggestions list (search by nickname)!')
        chat_1.chat_message_input.clear()

        home_1.just_fyi('Can delete nickname via group info and recheck received messages')
        device_2_options = chat_1.get_user_options(full_ens)
        device_2_options.view_profile_button.click()
        chat_1.profile_nickname_button.click()
        chat_1.nickname_input_field.clear()
        chat_1.element_by_text('Done').click()
        chat_1.close_button.click()
        chat_1.close_button.click()
        message_text = '%s %s' % (full_ens, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name is not resolved on sent message after removing nickname")
        chat_1.chat_message_input.send_keys('@')
        if chat_1.element_by_text_part(nickname).is_element_displayed():
            self.errors.append("Nickname is shown in group chat after removing!")

        self.errors.verify_no_errors()

    @marks.testrail_id(5752)
    @marks.medium
    def test_block_and_unblock_user_from_group_chat_via_group_info(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        initial_chat_name = home_1.get_random_chat_name()

        device_2.just_fyi('Create and join group chat')
        public_key_2, username_2 = device_2.get_public_key_and_username(True)
        device_2.home_button.click()
        home_1.add_contact(public_key_2)
        home_1.get_back_to_home_view()
        chat_1 = home_1.create_group_chat([username_2], initial_chat_name)
        chat_2 = home_2.get_chat(initial_chat_name).click()
        chat_2.join_chat_button.click()

        device_2.just_fyi('Send message and block user via Group Info')
        message_before_block = 'message from device2'
        chat_2.send_message(message_before_block)
        options_2 = chat_1.get_user_options(username_2)
        options_2.view_profile_button.click()
        options_2.block_contact()
        home_1.close_button.click()
        if chat_1.chat_element_by_text(message_before_block).is_element_displayed(10):
            self.errors.append('User was blocked, but past message are shown')
        message_after_block = 'message from device2 after block'
        chat_2.send_message(message_after_block)
        if chat_1.chat_element_by_text(message_after_block).is_element_displayed(10):
            self.errors.append('User was blocked, but new messages still received')

        device_1.just_fyi('Unblock user via group info and check that new messages will arrive')
        options_2 = chat_1.get_user_options(username_2)
        options_2.view_profile_button.click()
        options_2.unblock_contact_button.click()
        [options_2.close_button.click() for _ in range(2)]
        message_after_unblock = 'message from device2 after unblock'
        chat_2.send_message(message_after_unblock)
        if not chat_1.chat_element_by_text(message_after_unblock).is_element_displayed(20):
            self.errors.append('User was unblocked, but new messages are not received')

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
        chat.get_back_to_home_view()
        home.get_chat('some_group_chat').click()
        chat.chat_options.click()
        group_info_view = chat.group_info.click()
        if group_info_view.add_members.is_element_displayed():
            self.errors.append('Add members button is displayed when max users are added in chat')
        if not group_info_view.element_by_text_part('20 members').is_element_displayed():
            self.errors.append('Amount of users is not shown on Group info screen')

        self.errors.verify_no_errors()
