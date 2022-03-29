from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, ens_user, chat_users
from views.sign_in_view import SignInView
import random
import emoji


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


class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(5721)
    @marks.medium
    def test_cant_add_more_twenty_participants_to_group_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        users = [chat_users['A'],
                 chat_users['B'],
                 transaction_senders['ETH_8'],
                 transaction_senders['ETH_1'],
                 transaction_senders['ETH_2'],
                 transaction_senders['ETH_7'],
                 transaction_senders['ETH_STT_3'],
                 transaction_senders['ETH_STT_ADI_1'],
                 transaction_senders['C'],
                 transaction_senders['F'],
                 transaction_senders['G'],
                 transaction_senders['H'],
                 transaction_senders['I'],
                 transaction_senders['M'],
                 transaction_senders['N'],
                 transaction_senders['Q'],
                 transaction_senders['R'],
                 transaction_senders['S'],
                 transaction_senders['T'],
                 transaction_senders['U']]
        usernames = []

        home.just_fyi('Add 20 users to contacts')
        profile = home.profile_button.click()
        profile.contacts_button.click()
        chat = home.get_chat_view()
        for user in users:
            profile.add_new_contact_button.click()
            chat.public_key_edit_box.click()
            chat.public_key_edit_box.set_value(user['public_key'])
            chat.confirm_until_presence_of_element(profile.add_new_contact_button)
            usernames.append(user['username'])

        home.just_fyi('Create group chat with max amount of users')
        profile.home_button.click()
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
