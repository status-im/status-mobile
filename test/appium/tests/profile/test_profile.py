import math
from random import choice
from string import printable

import pytest
from currency_converter import CurrencyConverter
from selenium.common import NoSuchElementException, TimeoutException

from tests import marks, run_in_parallel
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_1")  # ToDo: find group
@marks.nightly
class TestProfileOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.create_user()
        self.profile = self.home.profile_button.click()
        self.username = self.profile.default_username_text.text

    @marks.testrail_id(741966)
    def test_profile_back_up_seed_phrase_validation(self):
        self.profile.backup_recovery_phrase_button.click()
        for checkbox in self.profile.checkbox_button.find_elements():
            checkbox.click()
        self.profile.button_one.click()
        recovery_phrase = self.profile.get_recovery_phrase()
        self.profile.button_one.click()
        word_number = self.profile.recovery_phrase_word_number.number
        self.profile.get_incorrect_word_button(recovery_phrase[word_number]).click()
        if not self.profile.element_by_translation_id('oops-wrong-word').is_element_displayed():
            self.errors.append(self.profile,
                               "Error message 'Oops! Wrong word' is not shown on the first seed phrase validation step")
        self.profile.get_incorrect_word_button(recovery_phrase[word_number]).click()
        if not self.profile.element_by_translation_id(
                'do-not-cheat').is_element_displayed() or not self.profile.element_by_translation_id(
            'do-not-cheat-description').is_element_displayed():
            self.errors.append(
                self.profile,
                "Expected messages are not shown for the second attempt with incorrect recovery phrase word")
        self.profile.button_one.click()
        if not self.profile.recovery_phrase_table.is_element_displayed():
            self.errors.append(self.profile, "Recovery phrase is not shown after failed attempts to check it")
            self.errors.verify_no_errors()  # the test fails if the recovery phrase is not shown on this step

        self.profile.button_one.click()
        self.profile.fill_recovery_phrase_checking_words(recovery_phrase)
        if not self.profile.element_by_translation_id('written-seed-ready').is_element_displayed():
            self.errors.append(self.profile, "Can't complete backup")

        self.profile.click_system_back_button(3)
        if not self.profile.backup_recovery_phrase_button.is_element_displayed():
            self.errors.append(self.profile, "Backup recovery phrase button is not shown after failed validation")
        self.profile.reopen_app(sign_in=True, user_name=self.username)
        self.home.profile_button.click()
        if not self.profile.backup_recovery_phrase_button.is_element_displayed():
            self.errors.append(self.profile, "Backup recovery phrase button is not shown after relogin")
        self.errors.verify_no_errors()

    @marks.testrail_id(741967)
    def test_profile_change_currency(self):
        self.sign_in.reopen_app(sign_in=False)
        self.sign_in.recover_access(passphrase=transaction_senders['ETH_2']['passphrase'], second_user=True)
        wallet = self.sign_in.wallet_tab.click()
        amount_text = float(wallet.total_balance_text.text[1:])
        expected_amount = CurrencyConverter().convert(amount_text, 'USD', 'EUR')
        wallet.profile_button.click()
        self.profile.change_currency('EUR')
        self.profile.click_system_back_button()
        balance_text = wallet.total_balance_text.text
        new_currency, new_amount = balance_text[0], float(balance_text[1:])
        if new_currency != '€':
            self.errors.append(wallet, "Currency in wallet is %s but should be €" % new_currency)
        if not math.isclose(new_amount, expected_amount, rel_tol=0.1):
            self.errors.append(wallet, "Amount in wallet is %s but expected to be %s" % (new_amount, expected_amount))
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="five_2")
@marks.nightly
class TestProfileMultipleDevices(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.homes = self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.username_1, self.username_2 = self.home_1.get_username(), self.home_2.get_username()
        self.public_key_2 = self.home_2.get_public_key()
        self.profile_1 = self.home_1.get_profile_view()
        self.profile_2 = self.home_2.get_profile_view()
        [home.navigate_back_to_home_view() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)

        self.home_1.get_chat(self.username_2).wait_for_visibility_of_element()
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.home_1.navigate_back_to_home_view()

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = "open community"
        self.channel_name = 'general'
        self.home_1.create_community(community_type="open")
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.invite_to_community(self.community_name, self.username_2)
        self.home_1.get_to_community_channel_from_home(self.community_name)
        self.community_2.join_community()
        self.community_2.get_channel(self.channel_name).click()
        self.message_community_2 = "message community from User 2"
        self.chat_2.send_message(self.message_community_2)
        self.message_community_1 = 'message in community from User 1'
        self.chat_1.send_message(self.message_community_1)
        self.chat_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()
        self.home_1.get_chat(self.username_2).click()
        self.message_1_1_from_1 = "message 1-1 chat from user 1"
        self.message_1_1_from_2 = "message 1-1 chat from user 2"
        self.chat_2.navigate_back_to_home_view()
        self.home_2.chats_tab.click()
        self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(self.message_1_1_from_2)
        self.chat_1.send_message(self.message_1_1_from_1)
        self.new_username_1 = 'user 1'

    @marks.smoke
    @marks.testrail_id(741968)
    def test_profile_change_username(self):
        self.home_1.navigate_back_to_home_view()
        self.home_1.just_fyi("User 1 updates username in the profile")
        self.home_1.profile_button.click()
        self.profile_1.edit_username(self.new_username_1)
        self.profile_1.click_system_back_button()
        if self.profile_1.default_username_text.text != self.new_username_1:
            pytest.fail("Device 1: Username is not updated")

        self.home_2.just_fyi("User 2 checks updated username in the community channel and mentions list")
        self.home_2.navigate_back_to_home_view()
        self.home_2.communities_tab.click()
        self.home_2.get_to_community_channel_from_home(self.community_name)
        self.chat_2.chat_element_by_text(self.message_community_1).wait_for_element(30)
        if self.chat_2.chat_element_by_text(self.message_community_1).username.text != self.new_username_1:
            self.errors.append(self.chat_2,
                               "Updated username '%s' is not shown in the community channel" % self.new_username_1)
        self.chat_2.chat_message_input.send_keys("@")
        self.chat_2.mentions_list.wait_for_element()
        if not self.chat_2.user_list_element_by_name(self.new_username_1).is_element_displayed():
            self.errors.append(self.chat_2, "Updated username is not shown in the mentions list")
        self.chat_2.navigate_back_to_home_view()
        self.home_2.chats_tab.click()

        self.home_2.just_fyi("User 2 checks updated username in the contacts list")
        self.home_2.contacts_tab.click()
        if not self.home_2.get_chat(self.new_username_1).is_element_displayed():
            self.errors.append(self.home_2, "Updated username is absent in the contacts list")

        self.home_2.just_fyi("User 2 checks updated username in the 1-1 chat and user's profile")
        self.home_2.recent_tab.click()
        if self.home_2.get_chat(self.new_username_1).is_element_displayed():
            self.home_2.get_chat(self.new_username_1).click()
            self.chat_2.chat_element_by_text(self.message_1_1_from_1).wait_for_element(30)
            if self.chat_2.chat_element_by_text(self.message_1_1_from_1).username.text != self.new_username_1:
                self.errors.append(self.chat_2,
                                   "Updated username '%s' is not shown in the 1-1 chat" % self.new_username_1)
            self.chat_2.chat_element_by_text(self.message_1_1_from_1).member_photo.click()
            if self.chat_2.get_profile_view().contact_name_text.text != self.new_username_1:
                self.errors.append(self.chat_2, "Updated username is not shown in the user's profile")
        else:
            self.errors.append(self.home_2, "Can't find chat with updated username")

        self.home_1.just_fyi("User 1 checks that updated username is shown in the login screen")
        try:
            self.home_1.reopen_app(sign_in=True, user_name=self.new_username_1)
        except NoSuchElementException:
            self.errors.append(self.home_1, "Updated username is absent on the login screen")

        self.errors.verify_no_errors()

    @marks.smoke
    @marks.testrail_id(741969)
    def test_profile_change_profile_photo(self):
        self.home_2.navigate_back_to_home_view()
        self.home_2.just_fyi("User 2 updates profile image")
        self.home_2.profile_button.click()
        self.profile_2.edit_profile_picture(image_index=0)
        if self.profile_2.user_avatar.is_element_differs_from_template("profile_image.png", diff=7):
            self.errors.append(self.profile_2, "Updated profile image is not shown for the profile owner")

        self.home_1.just_fyi("User 1 checks updated profile photo in the community channel")
        self.home_1.click_system_back_button_until_absence_of_element(self.profile_1.default_username_text)
        self.home_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        self.home_1.get_to_community_channel_from_home(self.community_name)
        self.chat_1.chat_element_by_text(self.message_community_2).wait_for_element(30)
        if self.chat_1.chat_element_by_text(self.message_community_2).member_photo.is_element_differs_from_template(
                "profile_image_in_1_1_chat.png", diff=7):
            self.errors.append(self.chat_1, "Updated image is not shown in the community channel")
        self.chat_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()

        self.home_1.just_fyi("User 1 checks updated profile photo in the contacts list")
        self.home_1.contacts_tab.click()
        chat_element = self.home_1.get_chat(self.username_2)
        if chat_element.is_element_displayed():
            if chat_element.chat_image.is_element_differs_from_template("profile_image_in_1_1_chat.png", diff=7):
                self.errors.append(self.home_1,
                                   "Chat image of user 2 doesn't match expected in the contacts list of user 1")
        else:
            self.errors.append(self.home_1, "User 2 is absent in the contacts list of user 1")

        self.home_1.just_fyi("User 1 checks updated profile image in the 1-1 chat and user's profile")
        self.home_1.recent_tab.click()
        if chat_element.is_element_displayed():
            chat_element.click()
            message_element = self.chat_1.chat_element_by_text(self.message_1_1_from_2)
            message_element.wait_for_element(30)
            if message_element.member_photo.is_element_differs_from_template("profile_image_in_1_1_chat.png", diff=7):
                self.errors.append(self.chat_1, "Updated profile image is not shown in the 1-1 chat")
            message_element.member_photo.click()
            if self.chat_1.get_profile_view().profile_picture.is_element_differs_from_template(
                    "profile_image_contacts_profile.png"):
                self.errors.append(self.chat_1, "Updated profile image is not shown in the user's profile")
            self.chat_1.click_system_back_button()
        else:
            self.errors.append(self.home_2, "Can't find chat with user 2")

        self.home_2.just_fyi("User 2 checks that updated profile image is shown in the login screen")
        self.home_2.reopen_app(sign_in=False)
        if self.device_2.get_user_profile_by_name(self.username_2).profile_image.is_element_differs_from_template(
                "profile_image_sign_in_view.png", diff=7):
            self.errors.append(self.device_2, "Updated profile image is not shown on the sign in view")
        self.device_2.sign_in(self.username_2)
        self.errors.verify_no_errors()

    @marks.testrail_id(741970)
    def test_profile_set_bio(self):
        self.home_2.just_fyi("Device 2 edits bio in profile")
        self.home_2.navigate_back_to_home_view()
        self.home_2.profile_button.click()
        self.profile_2.edit_profile_button.click()
        self.profile_2.edit_bio_button.click()
        new_bio = ''.join(choice(printable) for _ in range(240))
        self.profile_2.edit_profile_input.send_keys(new_bio + '1')
        if not self.profile_2.element_by_translation_id('bio-is-too-long').is_element_displayed():
            self.errors.append(self.profile_2, "Error message is not shown for 241 chars in the edit bio input field")
        self.profile_2.driver.press_keycode(67)  # deleting the last character
        self.profile_2.save_bio_button.click()
        self.profile_2.click_system_back_button()
        if self.profile_2.bio_text.text != new_bio:
            self.errors.append(self.profile_2, "Updated bio is not displayed in the user's profile")
        self.profile_2.click_system_back_button()

        self.home_1.just_fyi("User 1 checks updated bio in the user's 2 profile")
        self.home_1.click_system_back_button_until_absence_of_element(self.profile_1.default_username_text)
        self.home_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()
        self.home_1.contacts_tab.click()
        self.home_1.get_chat(self.username_2).find_element().click()
        if self.chat_1.contact_bio_text.text != new_bio:
            self.errors.append(self.profile_1, "Updated bio is not displayed in the contact's profile")
        self.errors.verify_no_errors()

    @marks.testrail_id(741971)
    def test_profile_change_accent_color(self):
        self.home_1.navigate_back_to_home_view()
        self.home_1.just_fyi("User 1 changes accent colour")
        self.home_1.profile_button.click()
        self.profile_1.change_accent_colour(colour_name='magenta')
        self.profile_1.click_system_back_button()
        red, green, blue = self.profile_1.user_avatar.get_element_rgb()
        if red < 70 or blue < 30:
            self.errors.append(
                self.profile_1,
                "Accent color is not properly applied in own profile, (red {}%, green {}%, blue {}%)".format(red,
                                                                                                             green,
                                                                                                             blue))
        self.home_2.just_fyi("User 2 checks updated accent color of user 1 in 1-1 chat and his profile")
        self.home_2.click_system_back_button_until_absence_of_element(self.profile_2.default_username_text)
        self.home_2.navigate_back_to_home_view()
        self.home_2.chats_tab.click()
        if self.home_2.get_chat(self.username_1).is_element_displayed():
            chat_element = self.home_2.get_chat(self.username_1)
        else:
            chat_element = self.home_2.get_chat(self.new_username_1)
        red, green, blue = chat_element.chat_image.get_element_rgb()
        if red < 90 or blue < 40:
            self.errors.append(
                self.profile_2,
                "Accent color is not applied for the chat preview image, (red {}%, green {}%, blue {}%)".format(
                    red,
                    green,
                    blue))
        chat_element.click()
        self.chat_2.chat_message_input.send_keys('test')
        red, green, blue = self.chat_2.send_message_button.get_element_rgb()
        if red < 90 or blue < 40:
            self.errors.append(
                self.profile_2,
                "Accent color is not applied for the send message button in chat, (red {}%, green {}%, blue {}%)".format(
                    red,
                    green,
                    blue))
        self.chat_2.options_button.click()
        self.chat_2.view_profile_button.click()
        red, green, blue = self.profile_2.profile_picture.get_element_rgb()
        if red < 90 or blue < 40:
            self.errors.append(
                self.profile_2,
                "Accent color is not applied for the profile picture, (red {}%, green {}%, blue {}%)".format(
                    red,
                    green,
                    blue))
        red, green, blue = self.chat_2.profile_send_message_button.get_element_rgb()
        if red < 90 or blue < 40:
            self.errors.append(
                self.profile_2,
                "Accent color is not applied for the send message button in profile, (red {}%, green {}%, blue {}%)".format(
                    red,
                    green,
                    blue))
        self.errors.verify_no_errors()

    @marks.testrail_id(741972)
    def test_profile_allow_new_contact_requests_toggle(self):
        self.device_1.just_fyi("Device 1 creates anew profile")
        self.device_1.reopen_app(sign_in=False)
        self.device_1.create_user(first_user=False)
        public_key = self.home_1.get_public_key()
        self.home_1.just_fyi("Device 1 disables new contact requests toggle in profile")
        self.home_1.profile_button.click()
        self.profile_1.turn_new_contact_requests_toggle('off')
        self.profile_1.click_system_back_button()

        self.home_2.just_fyi("Device 2 adds newly created user as a contact")
        self.home_2.navigate_back_to_home_view()
        self.home_2.chats_tab.click()
        self.home_2.add_contact(public_key=public_key, close_profile=False)

        self.home_1.just_fyi("Device 1 checks that no contact request is received")
        try:
            self.home_1.notifications_unread_badge.wait_for_visibility_of_element(30)
            self.errors.append(self.home_1, "Contact request received with turned off 'Allow contact requests' toggle")
        except TimeoutException:
            pass

        self.home_1.just_fyi("Device 1 enables new contact requests toggle in profile")
        self.home_1.profile_button.click()
        self.profile_1.turn_new_contact_requests_toggle('on')
        self.profile_1.click_system_back_button()

        self.home_2.just_fyi("Device 2 resends a contact request")
        self.chat_2.block_contact()
        self.chat_2.profile_unblock_button.click()
        self.chat_2.profile_send_contact_request_button.click()
        self.chat_2.contact_request_message_input.send_keys("hi")
        self.chat_2.confirm_send_contact_request_button.click()

        self.home_1.just_fyi("Device 1 checks that contact request is received")
        try:
            self.home_1.notifications_unread_badge.wait_for_visibility_of_element(60)
        except TimeoutException:
            self.errors.append(self.home_1,
                               "Contact request was not received with turned on 'Allow contact requests' toggle")

        self.errors.verify_no_errors()
