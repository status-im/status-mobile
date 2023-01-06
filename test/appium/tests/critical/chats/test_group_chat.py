import pytest

from tests import marks, run_in_parallel
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.chat_view import ChatView
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_3")
@marks.critical
class TestGroupChatMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.message_before_adding = 'message before adding new user'
        self.message_to_admin = 'Hey, admin!'

        self.homes, self.public_keys, self.usernames, self.chats = {}, {}, {}, {}
        for key in self.drivers:
            sign_in = SignInView(self.drivers[key])
            self.homes[key] = sign_in.create_user(enable_notifications=True)
            SignInView(self.drivers[2]).put_app_to_background_and_back()
            self.public_keys[key], self.usernames[key] = sign_in.get_public_key_and_username(True)
            sign_in.home_button.click()
            SignInView(self.drivers[0]).put_app_to_background_and_back()
        self.chat_name = self.homes[0].get_random_chat_name()

        self.homes[0].just_fyi('Admin adds future members to contacts')
        for i in range(1, 3):
            self.homes[0].add_contact(self.public_keys[i])
            self.homes[0].home_button.double_click()

        self.homes[0].just_fyi('Members add admin to contacts to see PNs and put app in background')
        for i in range(1, 3):
            self.homes[i].handle_contact_request(self.usernames[0])
            self.homes[i].home_button.double_click()

        self.homes[0].just_fyi('Admin creates group chat')
        self.chats[0] = self.homes[0].create_group_chat([self.usernames[1]], self.chat_name)
        for i in range(1, 3):
            self.chats[i] = ChatView(self.drivers[i])

        self.chats[0].send_message(self.message_before_adding)

    @marks.testrail_id(3994)
    def test_group_chat_push_system_messages_when_invited(self):
        self.homes[1].just_fyi("Check system messages in PNs")
        self.homes[2].put_app_to_background_and_back()
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
        has_added_system_message = self.chats[0].has_added_system_message(self.usernames[0], self.usernames[1])

        create_for_admin_system_message = 'You created the group %s' % self.chat_name
        joined_message = "You've joined %s from invitation by %s" % (self.chat_name, self.usernames[0])

        for message in [create_for_admin_system_message, create_system_message, has_added_system_message]:
            if not self.chats[0].element_by_text(message).is_element_displayed():
                self.errors.append('%s system message is not shown' % message)

        for message in [joined_message, create_system_message, has_added_system_message]:
            if not self.chats[1].element_by_text(message).is_element_displayed():
                self.errors.append('%s system message is not shown' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(700732)
    def test_group_chat_add_new_member(self):
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[0].get_chat(self.chat_name).click()
        self.chats[0].add_members_to_group_chat([self.usernames[2]])

        self.chats[2].just_fyi("Check there will be PN and no unread in AC for a new member")
        if self.homes[2].notifications_unread_badge.is_element_displayed(60):
            self.drivers[2].fail("Group chat appeared in AC!")
        self.homes[2].open_notification_bar()
        if not self.homes[2].element_by_text_part(self.usernames[0]).is_element_displayed():
            self.errors.append("PN about group chat invite is not shown when invited by mutual contact")

        self.homes[2].click_system_back_button()

        self.homes[2].just_fyi("Check new group appeared in chat list for a new member")
        if not self.homes[2].get_chat(self.chat_name).is_element_displayed(60):
            self.drivers[2].fail("New group chat hasn't appeared in chat list")

        self.homes[2].get_chat(self.chat_name).click()

        for message in (self.message_to_admin, self.message_before_adding):
            if self.chats[2].chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is shown for new user' % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(5756)
    def test_group_chat_highligted(self):
        chat_name = 'for_invited'
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

    @marks.testrail_id(3997)
    def test_group_chat_leave_relogin(self):
        left_system_message = self.chats[1].leave_system_message(self.usernames[0])
        self.drivers[2].quit()
        [self.homes[i].home_button.double_click() for i in range(2)]
        self.homes[0].home_button.double_click()
        self.homes[1].get_chat(self.chat_name).click()

        self.homes[0].just_fyi("Admin deleted chat via long press")
        self.homes[0].leave_chat_long_press(self.chat_name)

        self.homes[1].just_fyi('Check that leave system message is presented after user left the group chat')
        if not self.chats[1].chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('System message when user leaves the chat is not shown')

        self.homes[0].just_fyi("Member sends some message, admin relogins and check chat does not reappear")
        self.chats[1].send_message(self.message_to_admin)
        self.homes[0].relogin()
        if self.homes[0].get_chat_from_home_view(self.chat_name).is_element_displayed():
            self.drivers[0].fail('Deleted %s is present after relaunch app' % self.chat_name)


@pytest.mark.xdist_group(name="one_3")
@marks.new_ui_critical
class TestGroupChatMultipleDeviceMergedNewUI(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.message_before_adding = 'message before adding new user'
        self.message_to_admin = 'Hey, admin!'
        self.public_keys, self.usernames, self.chats = {}, {}, {}
        sign_in_views = [SignInView(self.drivers[key]) for key in self.drivers]
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (sign_in_views[0].create_user,),
                    (sign_in_views[1].create_user,),
                    (sign_in_views[2].create_user,)
                )
            )
        )
        self.homes = [sign_in.get_home_view() for sign_in in sign_in_views]
        users = self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.homes[0].get_public_key_and_username, True),
                    (self.homes[1].get_public_key_and_username, True),
                    (self.homes[2].get_public_key_and_username, True)
                )
            )
        )
        self.profiles = [home.get_profile_view() for home in self.homes]
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.profiles[0].switch_push_notifications,),
                    (self.profiles[1].switch_push_notifications,),
                    (self.profiles[2].switch_push_notifications,)
                )
            )
        )
        self.homes[0].just_fyi('Admin adds future members to contacts')

        for i in range(3):
            self.public_keys[i], self.usernames[i] = users[i]

        for i in range(1, 3):
            self.homes[0].browser_tab.click()
            self.profiles[0].add_contact_via_contacts_list(self.public_keys[i])

        for i in range(3):
            self.homes[i].chats_tab.click()

        self.homes[0].just_fyi('Members add admin to contacts to see PNs and put app in background')
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.homes[1].handle_contact_request, self.usernames[0]),
                    (self.homes[2].handle_contact_request, self.usernames[0])
                )
            )
        )
        for i in range(1, 3):
            self.homes[i].click_system_back_button_until_element_is_shown()

        self.homes[0].just_fyi('Admin creates group chat')
        self.chat_name = self.homes[0].get_random_chat_name()
        self.homes[0].communities_tab.click()
        self.chats[0] = self.homes[0].create_group_chat(user_names_to_add=[self.usernames[1]],
                                                        group_chat_name=self.chat_name,
                                                        new_ui=True)
        for i in range(1, 3):
            self.chats[i] = ChatView(self.drivers[i])

        self.chats[0].send_message(self.message_before_adding)

    @marks.testrail_id(702807)
    def test_group_chat_join_send_text_messages_push(self):
        message_to_admin = self.message_to_admin
        [self.homes[i].click_system_back_button_until_element_is_shown() for i in range(3)]
        self.homes[1].get_chat(self.chat_name).click()

        self.chats[1].send_message(message_to_admin)

        self.chats[0].just_fyi('Check that PN is received and after tap you are redirected to group chat')
        self.chats[0].open_notification_bar()
        pn = self.homes[0].get_pn(message_to_admin)
        if pn:
            pn.click()
        else:
            self.homes[0].click_system_back_button()
            self.homes[0].get_chat(self.chat_name).click()

        self.chats[1].just_fyi('Check message status and message delivery')
        # Not available yet
        # message_status = self.chats[1].chat_element_by_text(message_to_admin).status
        # if message_status != 'delivered':
        #     self.errors.append('Message status is not delivered, it is %s!' % message_status)
        if not self.chats[0].chat_element_by_text(message_to_admin).is_element_displayed(30):
            self.errors.append('Message %s was not received by admin' % message_to_admin)
        self.errors.verify_no_errors()

    @marks.testrail_id(702808)
    @marks.xfail(reason="mysterious issue when PNs are not fetched from offline,can not reproduce on real devices; needs investigation")
    def test_group_chat_offline_pn(self):
        [self.homes[i].click_system_back_button_until_element_is_shown() for i in range(3)]
        chat_name = 'for_offline_pn'
        self.homes[0].create_group_chat(user_names_to_add=[self.usernames[1], self.usernames[2]],
                                        group_chat_name=chat_name,
                                        new_ui=True)
        self.homes[0].click_system_back_button_until_element_is_shown()
        for i in range(1, 3):
            self.homes[i].get_chat(chat_name).click()

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
            if not self.homes[0].element_by_text(message).is_element_displayed(30):
                self.errors.append('%s PN was not fetched from offline' % message)
        self.homes[0].click_system_back_button()
        self.homes[0].chats_tab.click()
        self.homes[0].get_chat(chat_name).click()

        self.homes[0].just_fyi("check that messages are shown for every member")
        for i in range(3):
            for message in (message_1, message_2):
                if not self.chats[i].chat_element_by_text(message).is_element_displayed():
                    self.errors.append('%s if not shown for device %s' % (message, str(i)))
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="two_2")
@marks.new_ui_critical
class TestGroupChatMediumMultipleDeviceNewUI(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.home_1.browser_tab.click()  # temp, until profile is on browser tab
        self.profile_1 = self.home_1.get_profile_view()
        self.default_username_1 = self.profile_1.default_username_text.text
        self.public_key_2, self.default_username_2 = self.home_2.get_public_key_and_username(return_username=True)
        self.profile_1.add_contact_via_contacts_list(self.public_key_2)
        self.home_2.click_system_back_button_until_element_is_shown()
        self.home_1.click_system_back_button_until_element_is_shown()
        self.home_1.chats_tab.click()
        self.home_2.chats_tab.click()
        self.home_2.handle_contact_request(self.default_username_1)
        self.home_2.click_system_back_button_until_element_is_shown()
        # workaround for group chat new UI
        self.home_1.communities_tab.click()
        self.group_chat_name = "Group Chat"
        self.group_chat_1 = self.home_1.create_group_chat(user_names_to_add=[self.default_username_2],
                                                          group_chat_name=self.group_chat_name,
                                                          new_ui=True)
        self.group_chat_2 = self.home_2.get_chat(self.group_chat_name).click()
        self.group_chat_2.join_chat_button.click_if_shown()
        self.message_1, self.message_2, self.message_3, self.message_4 = \
            "Message 1", "Message 2", "Message 3", "Message 4"

    @marks.testrail_id(702732)
    @marks.xfail(reason="blocked by #14637")
    def test_group_chat_pin_messages(self):
        self.home_1.just_fyi("Enter group chat and pin message there. It's pinned for both members.")

        self.group_chat_1.send_message(self.message_1)
        self.group_chat_1.pin_message(self.message_1, "pin-to-chat")
        if not (self.group_chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed(30) and
                self.group_chat_2.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed(30)):
            self.errors.append("Message 1 is not pinned in group chat!")

        self.home_1.just_fyi("Check that non admin user can not unpin messages")
        self.group_chat_2.chat_element_by_text(self.message_1).long_press_element()
        if self.group_chat_2.element_by_translation_id("unpin-from-chat").is_element_displayed():
            self.errors.append("Unpin option is available for non-admin user")
        self.group_chat_2.click_system_back_button()

        # not implemented yet :

        # self.home_1.just_fyi("Grant another user with admin rights and check he can unpin message now")
        # self.group_chat_1.chat_options.click()
        # group_info = self.group_chat_1.group_info.click()
        # options = group_info.get_username_options(self.default_username_2).click()
        # options.make_admin_button.click()
        # self.group_chat_2.click_system_back_button()
        # self.group_chat_2.pin_message(self.message_1, action="unpin")
        # if (self.group_chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed() and
        #         self.group_chat_2.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed()):
        #     self.errors.append("Message failed to be unpinned by user who granted admin permissions!")

        self.home_1.just_fyi("Send, pin messages and check they are pinned")
        for message in self.message_2, self.message_3:
            # here group_chat_1 should be changed to group_chat_2 after enabling the previous block
            self.group_chat_1.send_message(message)
            self.group_chat_1.pin_message(message, 'pin-to-chat')
            if not (self.group_chat_1.chat_element_by_text(message).pinned_by_label.is_element_displayed(30) and
                    self.group_chat_2.chat_element_by_text(message).pinned_by_label.is_element_displayed(30)):
                self.errors.append("%s is not pinned in group chat!" % message)

        self.home_1.just_fyi("Check that a user can not pin more than 3 messages")
        self.group_chat_1.send_message(self.message_4)
        self.group_chat_1.pin_message(self.message_4, 'pin-to-chat')
        if self.group_chat_1.pin_limit_popover.is_element_displayed(30):
            self.group_chat_1.view_pinned_messages_button.click_until_presence_of_element(
                self.group_chat_1.pinned_messages_list)
            self.group_chat_1.pinned_messages_list.message_element_by_text(
                self.message_2).click_inside_element_by_coordinate()
            self.group_chat_1.element_by_translation_id('unpin-from-chat').double_click()
            self.group_chat_1.chat_element_by_text(self.message_4).click()
            self.group_chat_1.pin_message(self.message_4, 'pin-to-chat')
            if not (self.group_chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30) and
                    self.group_chat_2.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30)):
                self.errors.append("Message 4 is not pinned in group chat after unpinning previous one")
        else:
            self.errors.append("Can pin more than 3 messages in group chat")

        self.home_1.just_fyi("Check pinned messages count and content")
        for chat_number, group_chat in enumerate([self.group_chat_1, self.group_chat_2]):
            count = group_chat.pinned_messages_count.text
            if count != '3':
                self.errors.append(
                    "Pinned messages count %s doesn't match expected 3 for user %s" % (count, chat_number + 1))
            group_chat.pinned_messages_count.click()
            for message in self.message_1, self.message_3, self.message_4:
                pinned_by = group_chat.pinned_messages_list.get_message_pinned_by_text(message)
                if pinned_by.is_element_displayed():
                    text = pinned_by.text.strip()
                    expected_text = "You" if chat_number == 0 else self.default_username_1
                    if text != expected_text:
                        self.errors.append(
                            "Pinned by '%s' doesn't match expected '%s' for user %s" % (
                                text, expected_text, chat_number + 1)
                        )
                else:
                    self.errors.append(
                        "Message '%s' is missed on Pinned messages list for user %s" % (message, chat_number + 1)
                    )

        self.errors.verify_no_errors()
