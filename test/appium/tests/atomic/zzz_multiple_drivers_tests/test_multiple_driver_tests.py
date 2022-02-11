import pytest
from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten
from views.sign_in_view import SignInView
from views.chat_view import ChatView


# TODO: moved here until resolve of 13048
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

    @marks.testrail_id(5762)
    @marks.high
    def test_pair_devices_sync_one_to_one_contacts_nicknames_public_chat(self):
        self.create_drivers(3)
        device_1, device_2, device_3 = SignInView(self.drivers[0]), SignInView(self.drivers[1]), SignInView(
            self.drivers[2])

        no_contact_nickname = 'no_contact_nickname'
        name_1, name_2 = 'device_%s' % device_1.driver.number, 'device_%s' % device_2.driver.number
        message_before_sync, message_after_sync = 'sent before sync', 'sent after sync'
        message_blocked_before, message_blocked_after = 'I am blocked user', 'Not blocked anymore'
        public_chat_before_sync, public_chat_after_sync = device_1.get_random_chat_name(), 'after-pairing'

        device_3.just_fyi("Block contact: create user for blocking from main device")
        home_3 = device_3.create_user()
        public_chat_3 = home_3.join_public_chat(public_chat_before_sync)
        public_chat_3.send_message(message_blocked_before)
        self.drivers[2].quit()

        device_1.just_fyi("(main device): create main user")
        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.backup_recovery_phrase_button.click()
        profile_1.ok_continue_button.click()
        recovery_phrase = profile_1.get_recovery_phrase()
        profile_1.close_button.click()
        profile_1.home_button.click()

        device_1.just_fyi('Add contact, 1-1 chat (main device): 3-random, contact with ENS, start 1-1')
        chat_1 = home_1.add_contact(basic_user['public_key'])
        chat_1.send_message(message_before_sync)
        chat_1.home_button.click()
        chat_1 = home_1.add_contact(ens_user['ens'])
        chat_1.home_button.click()

        device_1.just_fyi('Chats, contacts (main device): join public chat, block user, set nickname')
        public_chat_1 = home_1.join_public_chat(public_chat_before_sync)
        public_chat_1.open_user_profile_from_public_chat(message_blocked_before)
        public_chat_1.set_nickname(no_contact_nickname, close_profile=False)
        public_chat_1.block_contact()

        device_2.just_fyi("(secondary device): restore same multiaccount on another device")
        home_2 = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()

        device_2.just_fyi('Nicknames (main device): set nickname for contact')
        profile_1.open_contact_from_profile(basic_user['username'])
        nickname = 'my_basic_user'
        chat_1.set_nickname(nickname)
        device_1.back_button.click()

        device_2.just_fyi('Pair main and secondary devices')
        profile_2.discover_and_advertise_device(name_2)
        profile_1.discover_and_advertise_device(name_1)
        profile_1.get_toggle_device_by_name(name_2).wait_and_click()
        profile_1.sync_all_button.click()
        profile_1.sync_all_button.wait_for_visibility_of_element(15)
        [device.profile_button.click() for device in (profile_1, profile_2)]

        device_2.just_fyi('Contacts (secondary device): check contacts + blocked users after initial sync')
        profile_2.contacts_button.scroll_to_element(9, 'up')
        profile_2.contacts_button.click()
        if not profile_2.blocked_users_button.is_element_displayed(30):
            self.errors.append('Blocked users are not synced after initial sync')
        for name in (basic_user['username'], nickname, '@%s' % ens_user['ens']):
            if not profile_2.element_by_text(name).is_element_displayed():
                self.errors.append('"%s" is not found in Contacts after initial sync' % name)
        profile_2.blocked_users_button.click()
        if not profile_2.element_by_text(no_contact_nickname).is_element_displayed():
            self.errors.append(
                "'%s' nickname without addeing to contacts is not synced after initial sync" % no_contact_nickname)
        profile_2.profile_button.double_click()

        device_1.just_fyi("Contacts(main device): unblock user, send message from unblocked user")
        profile_1.profile_button.click()
        profile_1.contacts_button.scroll_to_element(direction='up')
        profile_1.contacts_button.click()
        profile_1.blocked_users_button.click()
        profile_1.element_by_text(no_contact_nickname).click()
        public_chat_1.unblock_contact_button.click()
        profile_1.close_button.click()

        device_1.just_fyi('Chats, contacts, nickname (main device): send message to 1-1, add new contact')
        profile_1.home_button.click(desired_view='chat')
        public_chat_1.back_button.click()
        home_1.get_chat(nickname).click()
        chat_1.send_message(message_after_sync)
        chat_1.back_button.click()
        new_contact, new_contact_ens = transaction_senders['A'], ens_user_ropsten
        home_1.add_contact(new_contact['public_key'])
        home_1.home_button.click()
        home_1.add_contact(new_contact_ens['ens'])

        device_2.just_fyi('Contacts (secondary device): check unblocked user, new contact')
        profile_2.contacts_button.click()
        profile_2.blocked_users_button.wait_for_invisibility_of_element(60)
        for name in (new_contact['username'], '@%s' % new_contact_ens['ens']):
            if not profile_2.element_by_text(name).is_element_displayed(60):
                self.errors.append('"%s" is not found in Contacts after adding when devices are paired' % name)

        device_1.just_fyi('Contacts (main device): set nickname, (secondary device): check that synced')
        home_1.profile_button.click()
        profile_1.contacts_button.scroll_to_element(9, 'up')
        profile_1.open_contact_from_profile(transaction_senders['A']['username'])
        nickname_after_sync = 'my_transaction sender'
        chat_1.set_nickname(nickname_after_sync)
        device_1.home_button.double_click()
        if not profile_2.element_by_text(nickname_after_sync).is_element_displayed(60):
            self.errors.append(
                '"%s" is not updated in Contacts after setting nickname when devices are paired' % nickname_after_sync)

        device_2.just_fyi("Chats(secondary device): check public chats")
        profile_2.home_button.click()
        if not home_2.element_by_text_part(public_chat_before_sync).is_element_displayed():
            self.errors.append(
                '"%s" is not found in Home after initial sync when devices are paired' % public_chat_before_sync)
        public_chat_2 = home_2.get_chat('#%s' % public_chat_before_sync).click()
        if public_chat_2.chat_element_by_text(message_blocked_before).is_element_displayed(30):
            self.errors.append('Message %s from previously blocked user is fetched' % message_blocked_before)

        home_2.just_fyi("Chats (secondary device): check messages in 1-1")
        public_chat_2.home_button.click()
        chat = home_2.get_chat(nickname).click()
        if chat.chat_element_by_text(message_before_sync).is_element_displayed():
            self.errors.append('"%s" message sent before pairing is synced' % message_before_sync)
        if not chat.chat_element_by_text(message_after_sync).is_element_displayed(60):
            self.errors.append('"%s" message in 1-1 is not synced' % message_after_sync)

        device_1.just_fyi('Chats (main device):add new public chat, (secondary device): check that synced')
        home_1.join_public_chat(public_chat_after_sync)
        home_2 = chat.get_back_to_home_view()
        if not home_2.element_by_text_part(public_chat_after_sync).is_element_displayed(20):
            self.errors.append(
                '"%s" public chat is not synced after adding when devices are paired' % public_chat_after_sync)

        home_1.just_fyi('Contacts (main device): remove and block contact')
        home_1.profile_button.click()
        profile_1.contacts_button.scroll_to_element(9, 'up')
        profile_1.open_contact_from_profile(nickname)
        chat_1.block_contact()
        profile_1.element_by_text(nickname_after_sync).click()
        chat_1.remove_from_contacts.click()

        home_2.just_fyi('Contacts (secondary device): check removed and blocked contact')
        home_2.element_by_text_part(nickname).wait_for_invisibility_of_element(60)
        home_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(nickname_after_sync).wait_for_invisibility_of_element(60)

        device_1.just_fyi('Chats (main device):delete added public chat, (secondary device): check that synced')
        for profile in (profile_1, profile_2):
            profile.get_back_to_home_view()
            profile.home_button.click()
        home_1.delete_chat_long_press('#%s' % public_chat_after_sync)
        home_2.element_by_text('#%s' % public_chat_after_sync).wait_for_invisibility_of_element(60)

        self.errors.verify_no_errors()

    @marks.testrail_id(6324)
    @marks.medium
    def test_invite_to_group_chat_handling(self):
        self.create_drivers(3)
        sign_ins, homes, public_keys, usernames, chats = {}, {}, {}, {}, {}
        for key in self.drivers:
            sign_ins[key] = SignInView(self.drivers[key])
            homes[key] = sign_ins[key].create_user()
            public_keys[key], usernames[key] = sign_ins[key].get_public_key_and_username(True)
            sign_ins[key].home_button.click()
        [driver.close_app() for driver in (self.drivers[1], self.drivers[2])]
        chat_name = homes[0].get_random_chat_name()

        homes[0].just_fyi('Create group chats without members')
        chats[0] = homes[0].create_group_chat([], chat_name)
        link = chats[0].get_group_invite_via_group_info()
        chats[0].get_back_to_home_view()
        chats[0].just_fyi('Member_1, member_2: both users send requests to join group chat')
        [sign_in.open_weblink_and_login(link) for sign_in in (sign_ins[1], sign_ins[2])]
        introduction_messages = ['message for retrying']
        for i in range(1, 3):
            homes[i].element_by_text_part(chat_name).click()
            chats[i] = ChatView(self.drivers[i])
            introduction_messages.append('Please add me, member_%s to your gorgeous group chat' % str(i))
            chats[i].request_membership_for_group_chat(introduction_messages[i])

        chats[0].just_fyi('Admin: accept request for Member_1 and decline for Member_2')
        homes[0].get_chat(chat_name).click()
        chats[0].group_membership_request_button.wait_and_click()
        chats[0].element_by_text(usernames[1]).click()
        if not chats[0].element_by_text_part(introduction_messages[1]).is_element_displayed():
            self.errors.append('Introduction message is not shown!')
        chats[0].accept_group_invitation_button.wait_and_click()
        chats[0].accept_membership_for_group_chat_via_chat_view(usernames[2], accept=False)
        chats[0].click_system_back_button()

        chats[2].just_fyi('Member_2: retry request')
        chats[2].retry_group_invite_button.wait_and_click()
        chats[2].request_membership_for_group_chat(introduction_messages[0])

        chats[2].just_fyi('Admin: decline request for Member_2')
        chats[0].group_membership_request_button.wait_and_click()
        chats[0].element_by_text(usernames[2]).click()
        if not chats[0].element_by_text_part(introduction_messages[0]).is_element_displayed():
            self.errors.append('Introduction message that was set after retrying attempt is not shown for admin!')
        chats[0].decline_group_invitation_button.wait_and_click()
        chats[0].click_system_back_button()

        chats[2].just_fyi('Member_2: remove chat')
        chats[2].remove_group_invite_button.wait_and_click()

        chats[2].just_fyi('Double check after relogin')
        if chats[0].group_membership_request_button.is_element_displayed():
            self.errors.append('Group membership request is still shown when there are no pending requests anymore')
        [homes[i].relogin() for i in range(0, 3)]
        if homes[2].element_by_text_part(chat_name).is_element_displayed():
            self.errors.append('Group chat was not removed when removing after declining group invite')
        [home.get_chat(chat_name).click() for home in (homes[0], homes[1])]
        if chats[0].group_membership_request_button.is_element_displayed():
            self.errors.append(
                'Group membership request is shown after relogin when there are no pending requests anymore')
        join_system_message = chats[0].join_system_message(usernames[1])
        for chat in (chats[1], chats[0]):
            if not chat.chat_element_by_text(join_system_message).is_element_displayed():
                self.errors.append('%s is not shown after joining to group chat via invite' % join_system_message)
        self.errors.verify_no_errors()

    @marks.testrail_id(4001)
    @marks.medium
    def test_remove_member_from_group_chat(self):
        self.create_drivers(3)
        homes, public_keys, usernames, chats = {}, {}, {}, {}
        for key in self.drivers:
            sign_in_view = SignInView(self.drivers[key])
            homes[key] = sign_in_view.create_user()
            public_keys[key], usernames[key] = sign_in_view.get_public_key_and_username(True)
            sign_in_view.home_button.click()
        chat_name = homes[0].get_random_chat_name()
        for i in range(1, 3):
            homes[0].add_contact(public_keys[i])
            homes[0].get_back_to_home_view()
        chats[0] = homes[0].create_group_chat([usernames[1],
                                               usernames[2]], chat_name)
        chats[0].just_fyi('Member_1, member_2: both users join to group chat')
        for i in range(1, 3):
            chats[i] = homes[i].get_chat(chat_name).click()
            chats[i].join_chat_button.click()
        chats[0].just_fyi("Admin: get options for device 2 in group chat and remove him")
        options = chats[0].get_user_options(usernames[1])
        options.remove_user_button.click()
        left_message = chats[0].leave_system_message(usernames[1])
        for key in chats:
            if not chats[key].chat_element_by_text(left_message).is_element_displayed():
                self.errors.append("Message with text '%s' was not received" % left_message)

        chats[0].just_fyi("Check that input field is not available after removing")
        if chats[1].chat_message_input.is_element_displayed():
            self.errors.append("Message input is still available for removed user")
        chats[0].just_fyi("Send message and check that it is available only for remaining users")
        message = 'after removing member'
        chats[0].send_message(message)
        for chat in (chats[0], chats[2]):
            if not chat.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append("Message '%s' was not received after removing member" % message)
        if chats[1].chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message '%s' was received by removed member" % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(6317)
    @marks.medium
    def test_pair_devices_group_chat_different_messages_nicknames(self):
        self.create_drivers(3)
        device_1, device_2, device_3 = SignInView(self.drivers[0]), SignInView(self.drivers[1]), SignInView(
            self.drivers[2])
        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.backup_recovery_phrase_button.click()
        profile_1.ok_continue_button.click()
        recovery_phrase = profile_1.get_recovery_phrase()
        profile_1.close_button.click()
        profile_1.home_button.click()
        home_3 = device_3.create_user()
        public_key_3, username_3 = home_3.get_public_key_and_username(return_username=True)
        device_3.home_button.click()
        device_1_name, device_2_name, group_chat_name = 'creator', 'paired', 'some group chat'
        home_2 = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))

        device_1.just_fyi('Add contact, start group chat')
        nickname = 'my_tester'
        home_1.add_contact(public_key_3, nickname=nickname)
        home_1.get_back_to_home_view()
        chat_1 = home_1.create_group_chat([username_3], group_chat_name)
        chat_3 = home_3.get_chat(group_chat_name).click()
        chat_3.join_chat_button.click()

        device_2.just_fyi('Go to profile > Devices, set device name, discover device 2 to device 1')
        profile_2 = home_2.profile_button.click()
        profile_2.discover_and_advertise_device(device_2_name)
        device_1.profile_button.click()
        profile_1.discover_and_advertise_device(device_1_name)
        profile_1.get_toggle_device_by_name(device_2_name).click()
        profile_1.sync_all_button.click()
        profile_1.sync_all_button.wait_for_visibility_of_element(15)
        profile_1.click_system_back_button(2)

        device_1.just_fyi('Send message to group chat and verify it on all devices')
        text_message = 'some text'
        profile_1.home_button.click(desired_view='chat')
        profile_2.home_button.click()
        chat_1.send_message(text_message)
        chat_2 = home_2.get_chat(group_chat_name).click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.chat_element_by_text(text_message).is_element_displayed():
                self.errors.append('Message was sent, but it is not shown')

        device_3.just_fyi('Send message to group chat as member and verify nickname on it')
        message_from_member = 'member1'
        chat_3.send_message(message_from_member)
        chat_1.chat_element_by_text(message_from_member).wait_for_visibility_of_element(20)
        for chat in chat_1, chat_2:
            if not chat.chat_element_by_text(message_from_member).username != '%s %s' % (nickname, username_3):
                self.errors.append('Nickname is not shown in group chat')

        device_1.just_fyi('Send image to group chat and verify it on all devices')
        chat_1.show_images_button.click()
        chat_1.allow_button.click()
        chat_1.first_image_from_gallery.click()
        chat_1.send_message_button.click()
        chat_1.chat_message_input.click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.image_message_in_chat.is_element_displayed(60):
                self.errors.append('Image is not shown in chat after sending for %s' % chat.driver.number)

        device_1.just_fyi('Send audio message to group chat and verify it on all devices')
        chat_1.record_audio_message(message_length_in_seconds=3)
        device_1.send_message_button.click()
        chat_1.chat_message_input.click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.play_pause_audio_message_button.is_element_displayed(30):
                self.errors.append('Audio message is not shown in chat after sending!')

        device_1.just_fyi('Send sticker to group chat and verify it on all devices')
        chat_1.profile_button.click()
        profile_1.switch_network()
        home_1.get_chat(group_chat_name).click()
        chat_1.install_sticker_pack_by_name()
        chat_1.sticker_icon.click()
        if not chat_1.sticker_message.is_element_displayed(30):
            self.errors.append('Sticker was not sent')
        self.errors.verify_no_errors()
