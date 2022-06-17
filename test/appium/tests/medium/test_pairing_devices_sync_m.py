import pytest

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

        self.profile_1 = self.home_1.profile_button.click()
        self.profile_1.privacy_and_security_button.click()
        self.profile_1.backup_recovery_phrase_button.click()
        self.profile_1.ok_continue_button.click()
        self.recovery_phrase = self.profile_1.get_recovery_phrase()
        self.profile_1.close_button.click()
        self.profile_1.home_button.click()
        self.device_2.put_app_to_background_and_back()
        self.home_3 = self.device_3.create_user()
        self.public_key_3, self.username_3 = self.home_3.get_public_key_and_username(return_username=True)
        self.device_3.home_button.click()
        self.device_1.put_app_to_background_and_back()
        self.comm_before_sync_name, self.channel, self.message = 'b-%s' % self.home_1.get_random_chat_name(), 'some-rand-chann', 'comm_message'
        self.device_1_name, self.device_2_name, self.group_chat_name = 'creator', 'paired', 'some group chat'
        self.comm_after_sync_name = 'a-public-%s' % self.home_1.get_random_chat_name()
        self.channel_after_sync, self.message_after_sync = 'chann-after-sync', 'sent after sync'

        self.device_1.just_fyi('Create community, create group chat, edit user picture')
        self.comm_before_1 = self.home_1.create_community(self.comm_before_sync_name)
        self.channel_before_1 = self.comm_before_1.add_channel(self.channel)
        self.channel_before_1.send_message(self.message)
        self.home_1.home_button.double_click()
        self.device_3.put_app_to_background_and_back()
        self.device_2.put_app_to_background_and_back()

        self.device_1.just_fyi('Edit profile picture')
        self.home_1.profile_button.double_click()
        self.profile_1.edit_profile_picture('sauce_logo.png')

        self.device_1.just_fyi('Add contact, start group chat')
        self.home_1.home_button.click()
        self.home_1.add_contact(self.public_key_3)
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
        self.profile_1.sync_all_button.click()
        self.profile_1.sync_all_button.wait_for_visibility_of_element(20)
        [device.profile_button.double_click() for device in (self.profile_1, self.profile_2)]
        [device.home_button.double_click() for device in (self.profile_1, self.profile_2, self.device_3)]

    @marks.testrail_id(702269)
    @marks.xfail(reason="too long setup, can fail with Remote end closed connection")
    def test_pairing_sync_initial_community_send_message(self):
        # Pricture sync is not implemented yet
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
        self.home_1.create_community(self.comm_after_sync_name)
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
