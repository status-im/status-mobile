import pytest
from tests import marks
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten
from views.sign_in_view import SignInView


# TODO: suspended according to #13257
@pytest.mark.xdist_group(name="pairing_2")
@marks.critical
@marks.skip
class TestPairingMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        from views.dbs.main_pairing.data import seed_phrase, password
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1 = self.device_1.import_db(seed_phrase=seed_phrase, import_db_folder_name='main_pairing',
                                              password=password)
        self.home_2 = self.device_2.recover_access(seed_phrase)

        self.home_1.just_fyi('Pair main and secondary devices')
        [self.profile_1, self.profile_2] = [home.profile_button.click() for home in (self.home_1, self.home_2)]
        name_1, name_2 = 'device_1', 'a_%s_2' % self.device_2.get_unique_amount()
        self.profile_2.discover_and_advertise_device(name_2)
        self.profile_1.sync_settings_button.scroll_and_click()
        self.profile_1.devices_button.scroll_to_element()
        self.profile_1.devices_button.click()
        self.home_1.element_by_text_part(name_2).scroll_and_click()
        self.profile_1.sync_all_button.click()
        self.profile_1.sync_all_button.wait_for_visibility_of_element(20)
        [profile.get_back_to_home_view() for profile in [self.profile_1, self.profile_2]]
        [home.home_button.click() for home in [self.home_1, self.home_2]]

    def test_pairing_initial_sync_chats(self):
        self.profile_2.just_fyi("Check chats and previews")
        from views.dbs.main_pairing.data import chats
        for chat in chats.keys():
            if chats[chat]['initial_sync']:
                if 'preview' in chats.keys():
                    actual_chat_preview = self.home_2.get_chat(chat).chat_preview.text
                    expected_chat_preview = chats[chat]['preview']
                    if actual_chat_preview != expected_chat_preview:
                        self.errors.append('Expected preview for %s is "%s", in fact "%s" after initial sync' %
                                           (chat, expected_chat_preview, actual_chat_preview))

        # TODO: blocked due to 13176
        # self.profile_2.just_fyi("Check unread indicator")
        # if self.home_2.home_button.counter.text != '2':
        #     self.errors.append('New messages counter is not shown on Home button')
        # for chat in chats.keys():
        #     if 'unread' in chats.keys():
        #         if self.home_2.get_chat(chat).new_messages_counter.text != chats[chat]['unread']:
        #             self.errors.append('No unread for %s after initial sync' % chat)
        self.errors.verify_no_errors()

    @marks.skip
    # TODO: blocked due to 13176
    def test_pairing_initial_sync_activity_centre(self):
        from views.dbs.main_pairing.data import activity_centre
        if self.home_2.notifications_unread_badge.is_element_displayed():
            self.home_2.notifications_unread_badge.click()
            for chat in activity_centre.keys():
                from views.home_view import ActivityCenterChatElement
                chat_in_ac = ActivityCenterChatElement(self.driver, chat_name=chat)
                if not chat_in_ac.is_element_displayed():
                    self.errors.append('No chat "%s" in activity centre' % chat)
                else:
                    if not chat_in_ac.chat_message_preview != activity_centre[chat]:
                        self.errors.append('No chat preview  for "%s" in activity centre, "%s" instead' %
                                           chat, chat_in_ac.chat_message_preview)
        else:
            self.home_2.driver.fail("No unread messages in Activity centre!")
        self.errors.verify_no_errors()

    def test_pairing_initial_sync_contacts_blocked_nickname(self):
        from views.dbs.main_pairing.data import contacts, blocked
        self.profile_2 = self.home_2.profile_button.click()
        self.profile_2.contacts_button.click()
        for contact in contacts:
            if not self.profile_2.element_by_text(contact).is_element_displayed():
                self.errors.append("%s contact is not synced after initial sync" % contact)
        self.profile_2.blocked_users_button.click()
        for blocked_user in blocked.keys():
            if not self.profile_2.element_by_text(blocked_user).is_element_displayed():
                self.errors.append("%s blocked user is not synced after initial sync" % blocked_user)
        self.profile_2.get_back_to_home_view()


@pytest.mark.xdist_group(name="five_2")
@marks.critical
class TestPairingSyncMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.no_contact_nickname = 'no_contact_nickname'
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1 = self.device_1.create_user()

        self.name_1, self.name_2 = 'device_%s' % self.drivers[0].number, 'device_%s' % self.drivers[1].number
        self.message_before_sync, self.message_after_sync = 'sent before sync', 'sent after sync'
        self.contact_before_sync = basic_user
        self.public_chat_before_sync, self.public_chat_after_sync = self.home_1.get_random_chat_name(), 'after-pairing'

        self.home_1.just_fyi("(main device): get recovery phrase")
        self.profile_1 = self.home_1.profile_button.click()
        self.profile_1.privacy_and_security_button.click()
        self.profile_1.backup_recovery_phrase_button.click()
        self.profile_1.ok_continue_button.click()
        self.recovery_phrase = self.profile_1.get_recovery_phrase()
        self.profile_1.close_button.click()
        self.profile_1.home_button.click()
        self.profile_1.get_recovery_phrase()
        self.device_2.put_app_to_background_and_back()

        self.home_1.just_fyi('Add contact, 1-1 chat (main device): 3-random, contact with ENS, start 1-1')
        self.chat_1 = self.home_1.add_contact(self.contact_before_sync['public_key'])
        self.chat_1.send_message(self.message_before_sync)
        self.chat_1.home_button.click()
        self.home_1.add_contact(ens_user['ens'])
        self.chat_1.home_button.click()

        self.home_1.just_fyi('Chats, contacts (main device): join public chat, block user, set nickname')
        public_chat_1 = self.home_1.join_public_chat(self.public_chat_before_sync)
        public_chat_1.home_button.click()
        self.home_1.add_contact(transaction_senders['A']['public_key'], add_in_contacts=False,
                                nickname=self.no_contact_nickname)
        self.chat_1.open_user_profile_from_1_1_chat()
        self.chat_1.block_contact()

        self.device_2.just_fyi("(secondary device): restore same multiaccount on another device")
        self.home_2 = self.device_2.recover_access(passphrase=' '.join(self.recovery_phrase.values()))
        self.profile_1, self.profile_2 = self.home_1.profile_button.click(), self.home_2.profile_button.click()

        self.device_2.just_fyi('Nicknames (main device): set nickname for contact')
        self.profile_1.profile_button.click()
        self.profile_1.open_contact_from_profile(self.contact_before_sync['username'])
        self.nickname = 'my_basic_user'
        self.chat_1.set_nickname(self.nickname)
        self.device_1.get_back_to_home_view()

        self.device_2.just_fyi('Pair main and secondary devices')
        self.profile_2.discover_and_advertise_device(self.name_2)
        self.profile_1.discover_and_advertise_device(self.name_1)
        self.profile_1.get_toggle_device_by_name(self.name_2).wait_and_click()
        self.profile_1.sync_all_button.click()
        self.profile_1.sync_all_button.wait_for_visibility_of_element(20)
        [device.profile_button.double_click() for device in (self.profile_1, self.profile_2)]

    @marks.testrail_id(702194)
    def test_pairing_sync_initial_contacts_blocked_users(self):
        self.profile_2.contacts_button.scroll_to_element(9, 'up')
        self.profile_2.contacts_button.click()
        if not self.profile_2.blocked_users_button.is_element_displayed(30):
            self.errors.append('Blocked users are not synced after initial sync')
        for name in (basic_user['username'], self.nickname, '@%s' % ens_user['ens']):
            if not self.profile_2.element_by_text(name).is_element_displayed():
                self.errors.append('"%s" is not found in Contacts on initial sync' % name)
        self.profile_2.blocked_users_button.click()
        if not self.profile_2.element_by_text(self.no_contact_nickname).is_element_displayed():
            self.errors.append(
                "'%s' nickname without adding to contacts is not synced on initial sync" % self.no_contact_nickname)
        self.errors.verify_no_errors()

    @marks.testrail_id(702195)
    def test_pairing_sync_initial_public_chats(self):
        [device.home_button.double_click() for device in (self.profile_1, self.profile_2)]
        if not self.home_2.element_by_text_part(self.public_chat_before_sync).is_element_displayed():
            self.errors.append(
                "'%s' public chat is not appeared on secondary device on initial sync" % self.public_chat_before_sync)
        self.errors.verify_no_errors()

    @marks.testrail_id(702196)
    def test_pairing_sync_contacts_block_unblock(self):
        [device.profile_button.double_click() for device in (self.profile_1, self.profile_2)]
        new_user_for_block = transaction_senders['C']

        self.profile_1.just_fyi("Contacts(main device): block and unblock user")
        self.profile_1.profile_button.click()
        self.profile_1.contacts_button.scroll_to_element(direction='up')
        self.profile_1.contacts_button.click()
        self.profile_1.blocked_users_button.click()
        self.profile_1.element_by_text(self.no_contact_nickname).click()
        self.chat_1.unblock_contact_button.click()
        self.profile_1.close_button.click()
        self.home_1.home_button.click()
        self.home_1.add_contact(new_user_for_block['public_key'], add_in_contacts=False)
        self.chat_1.open_user_profile_from_1_1_chat()
        self.chat_1.block_contact()

        self.device_2.just_fyi('Contacts (secondary device): check unblocked and blocked user')
        self.profile_2.contacts_button.click()
        self.profile_2.blocked_users_button.click()
        if self.profile_2.element_by_text(self.no_contact_nickname).is_element_displayed():
            self.errors.append("'%s' unblocked user is not synced!" % self.no_contact_nickname)
        if not self.profile_2.element_by_text(new_user_for_block['username']).is_element_displayed():
            self.errors.append("'%s' blocked user is not synced!" % new_user_for_block['username'])
        self.errors.verify_no_errors()

    @marks.testrail_id(702197)
    def test_pairing_sync_contacts_add_remove_set_nickname_ens(self):
        [device.home_button.double_click() for device in (self.profile_1, self.profile_2)]
        new_contact, new_nickname = transaction_senders['F'], "completely_new_nick"
        self.home_1.add_contact(ens_user_ropsten['ens'])
        self.home_1.home_button.click()
        self.home_1.add_contact(new_contact['public_key'])

        self.device_2.just_fyi('Contacts (secondary device):check new contact')
        self.profile_2.profile_button.double_click()
        self.profile_2.contacts_button.scroll_to_element(direction='up')
        self.profile_2.contacts_button.click()
        for contact in (new_contact['username'], '@%s' % ens_user_ropsten['ens']):
            if not self.profile_2.element_by_text(contact).is_element_displayed():
                self.errors.append("'%s' new contact is not synced!" % contact)

        self.device_1.just_fyi('(Main device): set nickname, (secondary device): check nickname')
        self.chat_1.open_user_profile_from_1_1_chat()
        self.chat_1.set_nickname(new_nickname)
        if not self.profile_2.element_by_text(new_nickname).is_element_displayed():
            self.errors.append("'%s' new nickname is not synced!" % new_nickname)

        self.device_1.just_fyi('(Main device): remove contact, (secondary device): check removed contact')
        self.chat_1.open_user_profile_from_1_1_chat()
        self.chat_1.remove_from_contacts.click()
        self.chat_1.close_button.click()
        self.profile_2.element_by_text(new_nickname).is_element_disappeared(40)
        self.errors.verify_no_errors()

    @marks.testrail_id(702198)
    def test_pairing_sync_1_1_chat_message(self):
        [device.home_button.double_click() for device in (self.profile_1, self.profile_2)]
        self.home_1.get_chat(self.nickname).click()
        self.chat_1.send_message(self.message_after_sync)

        self.home_2.just_fyi("Chats (secondary device): check messages in 1-1")
        chat = self.home_2.get_chat(self.nickname).click()
        if chat.chat_element_by_text(self.message_before_sync).is_element_displayed():
            self.errors.append('"%s" message sent before pairing is synced' % self.message_before_sync)
        if not chat.chat_element_by_text(self.message_after_sync).is_element_displayed(60):
            self.errors.append('"%s" message in 1-1 is not synced' % self.message_after_sync)
        self.errors.verify_no_errors()

    @marks.testrail_id(702199)
    def test_pairing_sync_public_chat_add_remove(self):
        [device.home_button.double_click() for device in (self.profile_1, self.profile_2)]
        self.home_1.join_public_chat(self.public_chat_after_sync)
        if not self.home_2.element_by_text("#%s" % self.public_chat_after_sync).is_element_displayed(10):
            self.errors.append('Public chat "%s" is not synced' % self.public_chat_after_sync)
        self.home_1.home_button.click()
        self.home_1.delete_chat_long_press('#%s' % self.public_chat_after_sync)
        if not self.home_2.element_by_text('#%s' % self.public_chat_after_sync).is_element_disappeared(60):
            self.errors.append('Remove of "%s" public chat is not synced!' % self.public_chat_after_sync)
        self.errors.verify_no_errors()
