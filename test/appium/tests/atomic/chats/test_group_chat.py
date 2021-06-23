from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from tests.users import transaction_senders, ens_user
from views.sign_in_view import SignInView
from time import sleep
import random, emoji


class TestGroupChatMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3994)
    @marks.high
    def test_create_new_group_chat_messaging_pn_delivered(self):
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
                self.drivers[0].fail('System message after joining group chat is not shown')
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
        if device_1_chat.chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('System message after user left the group chat is shown if declined before accepting in Activity Centre')
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' is shown, but user has left" % chat_name)

        device_2.just_fyi('Send message after invite is declined and check that it is not reappeared')
        message = 'sent after leaving'
        device_1_chat.send_message(message)
        if device_2_home.element_by_text(chat_name).is_element_displayed():
            self.errors.append("Group chat '%s' reappeared when new message is sent" % chat_name)

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
        device_1_chat.close_button.click()

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

    @marks.testrail_id(6327)
    @marks.medium
    def test_nicknames_ens_group_chats(self):
        self.create_drivers(2)
        home_1 = SignInView(self.drivers[0]).create_user()
        home_2 = SignInView(self.drivers[1]).recover_access(ens_user['passphrase'])
        profile_1, profile_2 = [home.profile_button.click() for home in (home_1, home_2)]
        key_1, username_1 = profile_1.get_public_key_and_username(return_username=True)
        ens, full_ens, username_2 = ens_user['ens'], '@%s' % ens_user['ens'], ens_user['username']
        [profile.switch_network() for profile in (profile_1, profile_2)]

        home_2.just_fyi('Set ENS')
        profile_2 = home_2.profile_button.click()
        dapp_view = profile_2.ens_usernames_button.click()
        dapp_view.element_by_text('Get started').click()
        dapp_view.ens_name_input.set_value(ens)
        dapp_view.check_ens_name.click_until_presence_of_element(dapp_view.element_by_translation_id("ens-got-it"))
        dapp_view.element_by_translation_id("ens-got-it").click()
        home_2.home_button.click()

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
            self.errors.append('Nickname was not removed! real chat name is %s instead of %s' % (chat_1.user_name_text.text, full_ens))

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
        chat_1.set_nickname(nickname)
        if not chat_1.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname is not shown in profile view after setting from froup info')
        chat_1.close_button.click()
        if not chat_1.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname is not shown in group info view after setting from froup info')
        chat_1.close_button.click()
        message_text = '%s %s' % (nickname, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name was not replaced with nickname on sent message")
        chat_1.chat_message_input.send_keys('@')
        if not chat_1.element_by_text('%s %s' %(nickname, full_ens)).is_element_displayed():
            self.errors.append("ENS name with nickname is not shown in mention input after set")
        if not chat_1.element_by_text(username_2).is_element_displayed():
            self.errors.append("3-random name is not shown in mention input after set from group info")
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' %(nickname, full_ens), typed_search_pattern=username_2[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append('ENS is not resolved in chat input after setting nickname in mention suggestions list (search by 3-random name)!')
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' % (nickname, full_ens), typed_search_pattern=nickname[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append('ENS is not resolved in chat input after setting nickname in mention suggestions list (search by nickname)!')
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
        device_1_home.close_button.click()
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
        [device_2_options.close_button.click() for _ in range(2)]
        message_after_unblock = 'message from device2 after unblock'
        device_2_chat.send_message(message_after_unblock)
        if not device_1_chat.chat_element_by_text(message_after_unblock).is_element_displayed(20):
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
