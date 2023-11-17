import pytest
import random
import string

from tests.users import wallet_users, basic_user, ens_user
from tests import marks
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_3")
@marks.medium
class TestPairingSyncMediumMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.device_1, self.device_2, self.device_3 = SignInView(self.drivers[0]), SignInView(
            self.drivers[1]), SignInView(self.drivers[2])
        self.home_1 = self.device_1.create_user()

        self.public_key_1, self.username_1 = self.home_1.get_public_key()

        self.profile_1 = self.home_1.profile_button.click()
        self.profile_1.privacy_and_security_button.click()
        self.profile_1.backup_recovery_phrase_button.click()
        self.profile_1.ok_continue_button.click()
        self.recovery_phrase = self.profile_1.get_recovery_phrase()
        self.profile_1.close_button.click()
        self.profile_1.home_button.click()
        self.device_2.put_app_to_background_and_back()
        self.home_3 = self.device_3.create_user()
        self.public_key_3, self.username_3 = self.home_3.get_public_key()
        self.device_3.home_button.click()
        self.device_1.put_app_to_background_and_back()
        self.comm_before_sync_name, self.channel, self.message = 'b-%s' % self.home_1.get_random_chat_name(), 'some-rand-chann', 'comm_message'
        self.device_1_name, self.device_2_name, self.group_chat_name = 'creator', 'paired', 'some group chat'
        self.comm_after_sync_name = 'a-public-%s' % self.home_1.get_random_chat_name()
        self.channel_after_sync, self.message_after_sync = 'chann-after-sync', 'sent after sync'

        self.device_1.just_fyi('Create community, create group chat, edit user picture')
        self.comm_before_1 = self.home_1.create_community_e2e(self.comm_before_sync_name)
        self.channel_before_1 = self.comm_before_1.add_channel(self.channel)
        self.channel_before_1.send_message(self.message)
        self.home_1.home_button.double_click()
        self.device_3.put_app_to_background_and_back()
        self.device_2.put_app_to_background_and_back()

        self.device_1.just_fyi('Edit profile picture')
        self.home_1.profile_button.double_click()
        self.profile_1.edit_profile_picture('sauce_logo.png')

        self.profile_1.privacy_and_security_button.click()

        self.device_1.just_fyi('Set Accept new chats from contacts')
        self.profile_1.accept_new_chats_from.click()
        self.profile_1.accept_new_chats_from_contacts_only.click()
        self.profile_1.navigate_up_button.click()

        self.device_1.just_fyi('Set see profile pictures from to everyone')
        self.profile_1.show_profile_pictures_of.scroll_and_click()
        self.profile_1.element_by_translation_id('everyone').click()
        self.profile_1.navigate_up_button.click()

        self.device_1.just_fyi('Set show profile pictures to to none')
        self.profile_1.show_profile_pictures_to.scroll_and_click()
        self.profile_1.element_by_translation_id('none').click()
        self.profile_1.get_back_to_home_view()

        self.home_1.just_fyi('Add watch only wallet')
        self.wallet_1 = self.home_1.wallet_button.click()
        self.wallet_1.add_account_button.click()
        self.wallet_1.add_watch_only_address_button.click()
        self.wallet_1.enter_address_input.send_keys(basic_user['address'])
        self.account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet_1.account_name_input.send_keys(self.account_name)
        self.wallet_1.add_account_generate_account_button.click()

        self.wallet_1.just_fyi('Set account currency to AFN')
        currency_afg = 'Afghanistan Afghani (AFN)'
        self.wallet_1.set_currency(currency_afg)
        self.wallet_1.navigate_up_button.click()

        self.device_1.just_fyi('Add contact, start group chat')
        self.home_1.home_button.click()
        self.home_1.add_contact(self.public_key_3)

        self.home_3.handle_contact_request(self.username_1)
        self.home_3.home_button.double_click()

        self.device_2.put_app_to_background_and_back()
        self.home_1.get_back_to_home_view()
        self.chat_1 = self.home_1.create_group_chat([self.username_3], self.group_chat_name)
        self.chat_3 = self.home_3.get_chat(self.group_chat_name).click()
        self.chat_3.join_chat_button.click_if_shown()

        self.device_2.just_fyi("(secondary device): restore same multiaccount on another device")
        self.home_2 = self.device_2.recover_access(passphrase=' '.join(self.recovery_phrase.values()))
        self.profile_1, self.profile_2 = self.home_1.profile_button.click(), self.home_2.profile_button.click()
        self.device_1.put_app_to_background_and_back()

        self.device_2.just_fyi('Pair main and secondary devices')
        self.name_1, self.name_2 = 'device_%s' % self.device_1.driver.number, 'device_%s' % self.device_2.driver.number
        self.profile_2.discover_and_advertise_device(self.name_2)
        self.profile_1.discover_and_advertise_device(self.name_1)
        self.profile_1.get_toggle_device_by_name(self.name_2).wait_and_click()
        self.profile_2.get_toggle_device_by_name(self.name_1).wait_and_click()
        self.profile_1.sync_all_button.click()
        self.profile_1.sync_all_button.wait_for_visibility_of_element(20)
        [device.profile_button.double_click() for device in (self.profile_1, self.profile_2)]
        [device.home_button.double_click() for device in (self.profile_1, self.device_3)]
        self.device_3.put_app_to_background_and_back()

    @marks.testrail_id(702133)
    def test_pairing_sync_accept_new_chats_see_show_profile_picture_settings(self):
        self.home_2.just_fyi('Check accept new chat value was synced')
        self.profile_2 = self.home_2.profile_button.click()
        self.profile_2.privacy_and_security_button.click()
        self.profile_2.accept_new_chats_from.scroll_to_element()
        if not self.profile_2.accept_new_chats_from_contacts_only.is_element_displayed():
            self.errors.append('Accept new chats is not set to contacts value on synced device')

        self.profile_2.just_fyi('Check see/show pictures settings are set to everyone/none')
        self.profile_2.delete_my_profile_button.scroll_to_element()
        if not self.profile_2.element_by_translation_id('everyone').is_element_displayed():
            self.errors.append('See pictures from setting is not set to everyone on synced device')
        if not self.profile_2.element_by_translation_id('none').is_element_displayed():
            self.errors.append('Show picture to setting is not set to none on synced device')

        self.profile_2.just_fyi('Changing values of see/show pictures and accept new chats settings')
        self.device_2.just_fyi('Set Accept new chats from anyone')
        self.profile_2.accept_new_chats_from.scroll_to_element(direction='up')
        self.profile_2.accept_new_chats_from.click()
        self.profile_2.element_by_translation_id('anyone').click()
        self.profile_2.navigate_up_button.click()

        self.device_3.put_app_to_background_and_back()

        self.device_2.just_fyi('Set see profile pictures from to contacts')
        self.profile_2.show_profile_pictures_of.scroll_and_click()
        self.profile_2.element_by_translation_id('contacts').click()
        self.profile_2.navigate_up_button.click()

        self.device_2.just_fyi('Set show profile pictures to to contacts')
        self.profile_2.show_profile_pictures_to.scroll_and_click()
        self.profile_2.element_by_translation_id('contacts').click()
        self.profile_2.get_back_to_home_view()

        self.device_1.just_fyi('Check if see/show pictures and accept chats settings values changed to ones set on paired device 2')
        self.profile_1 = self.home_1.profile_button.click()
        self.profile_1.privacy_and_security_button.click()
        self.profile_1.accept_new_chats_from.scroll_to_element()
        if not self.profile_1.element_by_translation_id('anyone').is_element_displayed():
            self.errors.append('Accept new chats is not set to anyone value on paired device')

        self.profile_1.just_fyi('Check see/show pictures settings are set to contacts/contacts')
        self.profile_1.delete_my_profile_button.scroll_to_element()
        if not self.profile_1.element_by_translation_id('anyone').is_element_displayed():
            self.errors.append('See/show pictures from setting is not set to contacts on paired device')
        if self.profile_1.element_by_translation_id('none').is_element_displayed():
            self.errors.append('Show picture to setting is still set to none instead of contacts on paired device')
        if self.profile_1.element_by_translation_id('everyone').is_element_displayed():
            self.errors.append('See picture from setting is still set to everyone instead of contacts on paired device')

        [profile.get_back_to_home_view() for profile in (self.profile_1, self.profile_2)]
        self.errors.verify_no_errors()

        self.device_3.put_app_to_background_and_back()

    @marks.testrail_id(702266)
    def test_pairing_sync_currency(self):
        self.wallet_2 = self.home_2.wallet_button.click()
        self.wallet_2.just_fyi('Check currency is set to AFN on device 2 after sync')
        if not self.wallet_2.element_by_text_part('AFN').is_element_displayed():
            self.errors.append('Currency is not set to AFN on device 2 after sync')

        self.wallet_2.just_fyi('Change currency to AED on device 2')
        currency_aed = 'Emirati Dirham (AED)'
        self.wallet_2.set_currency(currency_aed)
        self.wallet_2.navigate_up_button.click()

        self.wallet_1 = self.home_1.wallet_button.click()
        self.wallet_1.just_fyi('Check currency set to AED on device 1 after changing value on paired device 2')
        if not self.wallet_1.element_by_text_part('AED').is_element_displayed():
            self.errors.append('Currency is not set to AED on device 1 after value was set on paired device 2')

        self.device_3.put_app_to_background_and_back()

        self.errors.verify_no_errors()

    @marks.testrail_id(702288)
    def test_pairing_sync_watch_only_account(self):
        self.wallet_2 = self.home_2.wallet_button.click()
        account_button = self.wallet_2.get_account_by_name(self.account_name)
        if not account_button.is_element_displayed():
            self.errors.append('Watch only account is displayed on device 2 after sync')

        self.wallet_2.just_fyi('Add another watch only wallet by paired device 2')
        if not self.wallet_2.add_account_button.is_element_displayed(3):
            self.wallet_2.accounts_status_account.swipe_left_on_element()
        self.wallet_2.add_account_button.click()
        self.wallet_2.add_watch_only_address_button.click()
        self.wallet_2.enter_address_input.send_keys(ens_user['address'])
        account_name_2 = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet_2.account_name_input.send_keys(account_name_2)
        self.wallet_2.add_account_generate_account_button.click()

        self.wallet_1.just_fyi('Check newly added watch only account appeared on paired device 1')
        self.wallet_1 = self.wallet_1.wallet_button.click()
        account_button_2 = self.wallet_1.get_account_by_name(account_name_2)
        if not account_button_2.is_element_displayed():
            self.wallet_1.accounts_status_account.swipe_left_on_element()
            if not account_button_2.is_element_displayed():
                self.errors.append('New watch only account has not appeared on paired device 1')

        self.errors.verify_no_errors()

    @marks.testrail_id(702269)
    @marks.xfail(reason="too long setup, can fail with Remote end closed connection")
    def test_pairing_sync_initial_community_send_message(self):
        [device.home_button.click() for device in (self.home_1, self.home_2)]
        self.device_2.just_fyi('check that created/joined community and profile details are updated')
        if not self.home_2.get_chat(self.comm_before_sync_name, community=True).is_element_displayed():
            self.errors.append('Community %s was not appeared after initial sync' % self.comm_before_sync_name)
        comm_before_2 = self.home_2.get_chat(self.comm_before_sync_name, community=True).click()
        channel_2 = comm_before_2.get_chat(self.channel).click()
        if not channel_2.chat_element_by_text(self.message).is_element_displayed(30):
            self.errors.append("Message sent to community channel before sync is not shown!")
        self.home_1.home_button.click()
        self.home_1.get_chat(self.comm_before_sync_name, community=True).click()
        channel_1 = self.comm_before_1.get_chat(self.channel).click()
        channel_1.send_message(self.message_after_sync)
        if not channel_2.chat_element_by_text(self.message_after_sync).is_element_displayed(30):
            self.errors.append("Message sent to community channel after sync is not shown!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702270)
    def test_pairing_sync_community_add_new_channel(self):
        self.device_1.just_fyi("Send message, add new channel and check it will be synced")
        self.device_3.put_app_to_background_and_back()
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        [home.get_chat(self.comm_before_sync_name, community=True).click() for home in (self.home_1, self.home_2)]
        self.comm_before_1.add_channel(self.channel_after_sync)
        if not self.home_2.get_chat(self.channel_after_sync).is_element_displayed(30):
            self.errors.append("New added channel after sync is not shown!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702271)
    def test_pairing_sync_community_leave(self):
        self.device_3.put_app_to_background_and_back()
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.device_1.just_fyi("Leave community and check it will be synced")
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.home_1.get_chat(self.comm_before_sync_name, community=True).click()
        self.comm_before_1.leave_community()
        if not self.home_2.element_by_text_part(self.comm_before_sync_name).is_element_disappeared(30):
            self.errors.append("Leaving community was not synced!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702272)
    def test_pairing_sync_community_add_new(self):
        self.device_3.put_app_to_background_and_back()
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.home_1.create_community_e2e(self.comm_after_sync_name)
        if not self.home_2.element_by_text(self.comm_after_sync_name).is_element_displayed(30):
            self.errors.append('Added community was not appeared after initial sync')
        self.errors.append("Leaving community was not synced!")

    @marks.testrail_id(702273)
    def test_pairing_sync_group_chat_send_different_messages(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        self.device_1.just_fyi('Send message to group chat and verify it on all devices')
        text_message = 'some text'
        [home.get_chat(self.group_chat_name).click() for home in (self.home_1, self.home_3)]
        self.chat_1.send_message(text_message)
        self.chat_2 = self.home_2.get_chat(self.group_chat_name).click()
        for chat in (self.chat_1, self.chat_2, self.chat_3):
            if not chat.chat_element_by_text(text_message).is_element_displayed():
                self.errors.append('Message was sent, but it is not shown')

        self.device_3.just_fyi('Send message to group chat as member')
        message_from_member = 'member1'
        self.chat_3.send_message(message_from_member)
        self.chat_1.chat_element_by_text(message_from_member).wait_for_visibility_of_element(20)
        for chat in (self.chat_1, self.chat_2):
            if not chat.chat_element_by_text(message_from_member).is_element_displayed():
                self.errors.append('No message from member')

        self.device_1.just_fyi('Send image to group chat and verify it on all devices')
        self.chat_1.show_images_button.click()
        self.chat_1.allow_button.click()
        self.chat_1.first_image_from_gallery.click()
        self.chat_1.send_message_button.click()
        self.chat_1.chat_message_input.click()
        for chat in (self.chat_1, self.chat_2, self.chat_3):
            if not chat.image_message_in_chat.is_element_displayed(60):
                self.errors.append('Image is not shown in chat after sending for %s' % chat.driver.number)

        self.device_1.just_fyi('Send audio message to group chat and verify it on all devices')
        self.chat_1.record_audio_message(message_length_in_seconds=3)
        self.device_1.send_message_button.click()
        self.chat_1.chat_message_input.click()
        for chat in (self.chat_1, self.chat_2, self.chat_3):
            if not chat.play_pause_audio_message_button.is_element_displayed(30):
                self.errors.append('Audio message is not shown in chat after sending!')
        self.errors.verify_no_errors()
