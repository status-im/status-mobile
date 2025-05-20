import time

import pytest
from selenium.common import NoSuchElementException, StaleElementReferenceException

from tests import common_password
from tests.base_test_case import AbstractTestCase
from views.base_element import Text, Button, EditBox, SilentButton, BaseElement
from views.base_view import BaseView


class OptionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="(//android.view.ViewGroup[@content-desc='icon'])[2]")


class LogoutButton(SilentButton):
    def __init__(self, driver):
        super().__init__(driver, translation_id="logout")

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class LogoutDialog(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        self.logout_button = LogoutDialog.LogoutButton(driver)

    class LogoutButton(SilentButton):
        def __init__(self, driver):
            super().__init__(driver, translation_id="logout", uppercase=True)

        def navigate(self):
            from views.sign_in_view import SignInView
            return SignInView(self.driver)


class AdvancedButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="icon, Advanced, label-component, icon")

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class RecoveryPhraseTable(Text):
    def __init__(self, driver):
        super().__init__(driver, translation_id="backup-recovery-phrase",
                         suffix="/following-sibling::android.view.ViewGroup[2]//android.widget.TextView")


class RecoveryPhraseWordNumberText(Text):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@content-desc='number-container']/android.widget.TextView")

    @property
    def number(self):
        time.sleep(1)
        return int(self.find_element().text)


class HelpButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="help-button")

    def click(self):
        self.scroll_to_element().click()


class SyncSettingsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="sync-settings-button")

    def click(self):
        self.scroll_to_element().click()


class ProfileView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)
        self.options_button = OptionsButton(self.driver)

        # Header
        self.default_username_text = Text(self.driver, accessibility_id="username")
        self.bio_text = Text(self.driver,
                             xpath="//*[@content-desc='username']/following-sibling::*/android.widget.TextView")
        self.contact_name_text = Text(self.driver, accessibility_id="contact-name")
        self.profile_picture = BaseElement(self.driver, accessibility_id="profile-picture")
        self.user_avatar = BaseElement(
            self.driver, xpath="//*[@content-desc='dropdown']/preceding-sibling::*/*[@content-desc='user-avatar']")
        self.online_indicator = Button(self.driver, accessibility_id="online-profile-photo-dot")
        self.crop_photo_button = Button(self.driver, accessibility_id="Crop")
        self.shutter_button = Button(self.driver, accessibility_id="Shutter")
        self.accept_photo_button = Button(self.driver, accessibility_id="Done")

        # Contacts
        self.contacts_button = Button(self.driver, accessibility_id="contacts-button")

        # Privacy and security
        self.privacy_and_security_button = Button(self.driver, accessibility_id="privacy-and-security-settings-button")
        self.reset_password_button = Button(self.driver, accessibility_id="reset-password")
        self.current_password_edit_box = EditBox(self.driver, accessibility_id="current-password")
        self.new_password_edit_box = EditBox(self.driver, accessibility_id="new-password")
        self.confirm_new_password_edit_box = EditBox(self.driver, accessibility_id="confirm-new-password")
        self.current_password_wrong_text = Text(self.driver, accessibility_id="current-password-error")

        # Appearance
        self.appearance_button = Button(self.driver, accessibility_id="appearance-settings-button")

        ## Backup recovery phrase
        self.backup_recovery_phrase_button = Button(
            self.driver, accessibility_id="icon, Backup recovery phrase, label-component, icon")

        self.recovery_phrase_table = RecoveryPhraseTable(self.driver)
        self.recovery_phrase_word_number = RecoveryPhraseWordNumberText(self.driver)

        # Notifications
        self.profile_notifications_button = Button(self.driver,
                                                   accessibility_id="icon, Notifications, label-component, icon")
        self.profile_notifications_toggle_button = Button(self.driver,
                                                          accessibility_id="Show notifications, label-component")
        self.push_notification_toggle = Button(
            self.driver, xpath="//*[@content-desc='notifications-button']//*[@content-desc='switch']")
        self.wallet_push_notifications = Button(self.driver, accessibility_id="notifications-button")

        # Sync settings
        self.sync_settings_button = SyncSettingsButton(self.driver)
        self.backup_settings_button = Button(self.driver, accessibility_id="backup-settings-button")
        self.perform_backup_button = Button(self.driver, translation_id="perform-backup")

        ## Device syncing
        self.syncing_button = Button(self.driver, accessibility_id="icon, Syncing, label-component, icon")
        self.paired_devices_button = Button(
            self.driver, xpath="//android.view.ViewGroup[contains(@content-desc,'icon, Paired devices,')]")
        self.sync_plus_button = Button(
            self.driver,
            xpath="//*[@text='Paired devices']/following-sibling::android.view.ViewGroup[@content-desc='icon']")
        self.sync_and_backup_button = Button(self.driver, xpath="//*[@text='Sync and backup']")
        self.wi_fi_only_button = Button(self.driver, accessibility_id='wifi-only-action')

        # Advanced
        self.advanced_button = AdvancedButton(self.driver)
        self.log_level_setting_button = Button(self.driver, accessibility_id="log-level-settings-button")
        self.fleet_setting_button = Button(self.driver, accessibility_id="fleet-settings-button")

        # Need help
        self.submit_bug_button = Button(self.driver, accessibility_id="submit-bug-button")
        self.bug_description_edit_box = EditBox(self.driver, accessibility_id="bug-report-description")
        self.bug_steps_edit_box = EditBox(self.driver, accessibility_id="bug-report-steps")
        self.bug_submit_button = Button(self.driver, accessibility_id="bug-report-submit")
        self.request_a_feature_button = Button(self.driver, accessibility_id="request-a-feature-button")

        # About
        self.app_version_text = Text(self.driver, xpath="//*[@content-desc='app-version']//android.widget.TextView[2]")
        self.node_version_text = Text(self.driver,
                                      xpath="//*[@content-desc='node-version']//android.widget.TextView[2]")

        # Logout
        self.logout_button = LogoutButton(self.driver)
        self.logout_dialog = LogoutDialog(self.driver)
        self.confirm_logout_button = Button(self.driver, translation_id="logout", uppercase=True)

        # New profile
        self.edit_profile_button = Button(self.driver, accessibility_id="icon, Edit Profile, label-component, icon")
        self.change_profile_photo_button = Button(
            self.driver,
            xpath="//*[@content-desc='user-avatar']/following-sibling::android.view.ViewGroup[@content-desc='icon']")
        self.take_photo_button = Button(self.driver, accessibility_id="take-photo-button")
        self.select_from_gallery_button = Button(self.driver, accessibility_id="select-from-gallery-button")
        self.profile_password_button = Button(self.driver, accessibility_id="icon, Password, label-component, icon")
        self.profile_messages_button = Button(self.driver, accessibility_id="icon, Messages, label-component, icon")
        self.profile_blocked_users_button = Button(self.driver, accessibility_id="Blocked users, label-component, icon")
        self.profile_wallet_button = Button(self.driver, accessibility_id="icon, Wallet, label-component, icon")
        self.profile_networks_button = Button(self.driver, accessibility_id='icon, Networks, label-component, icon')
        self.network_settings_button = Button(self.driver, accessibility_id="Network settings, label-component, icon")
        self.testnet_mode_toggle = Button(self.driver, accessibility_id="icon, Testnet mode, label-component")
        self.confirm_testnet_mode_change_button = Button(self.driver, accessibility_id="confirm-testnet-mode-change")
        self.key_pairs_and_accounts_button = Button(self.driver,
                                                    accessibility_id="Key pairs and accounts, label-component, icon")
        self.import_by_entering_recovery_phrase_button = Button(self.driver, accessibility_id="import-seed-phrase")

        # Edit profile
        self.edit_profile_name_button = Button(self.driver, accessibility_id='Name, label-component, icon')
        self.edit_profile_input = EditBox(self.driver, accessibility_id='input')
        self.save_name_button = Button(self.driver, accessibility_id='Save name')
        self.edit_bio_button = Button(self.driver, accessibility_id='Bio, label-component, icon')
        self.save_bio_button = Button(self.driver, accessibility_id='Save bio')
        self.edit_accent_colour = Button(self.driver, accessibility_id='Accent colour, label-component, icon')
        self.save_colour_button = Button(self.driver, accessibility_id='Save colour')

        # Language and currency
        self.language_and_currency_button = Button(
            self.driver, accessibility_id='icon, Language and currency, label-component, icon')
        self.change_currency_button = Button(
            self.driver,
            xpath="//*[@text='Currency']/following-sibling::*/*[contains(@content-desc,'label-component, icon')]")

    def navigate_back_to_main_profile_view(self, attempts=3):
        for _ in range(attempts):
            try:
                if self.view_id_tracker.text == 'settings':
                    return
            except NoSuchElementException:
                self.click_system_back_button()
            except StaleElementReferenceException:
                continue

    def switch_network(self):
        self.driver.info("Toggling test mode")
        self.profile_networks_button.scroll_and_click()
        self.testnet_mode_toggle.click()
        self.confirm_testnet_mode_change_button.click()

    def switch_push_notifications(self):
        self.driver.info("Enabling push notifications via Profile")
        self.profile_notifications_button.scroll_and_click()
        self.profile_notifications_toggle_button.click()
        self.allow_button.click_if_shown()

    def get_recovery_phrase(self):
        text = [i.text for i in self.recovery_phrase_table.find_elements()]
        return dict(zip(map(int, text[::2]), text[1::2]))

    def get_correct_word_button(self, word: str):
        return Button(self.driver, accessibility_id=word)

    def get_incorrect_word_button(self, word: str):
        try:
            button = Button(self.driver,
                            xpath="//*[@content-desc='%s']/following-sibling::android.view.ViewGroup" % word)
            button.find_element()
        except NoSuchElementException:
            button = Button(self.driver,
                            xpath="//*[@content-desc='%s']/preceding-sibling::*/*/android.widget.TextView" % word)
        return button

    def fill_recovery_phrase_checking_words(self, recovery_phrase: dict):
        for _ in range(4):
            word_number = self.recovery_phrase_word_number.number
            self.get_correct_word_button(recovery_phrase[word_number]).click()

    def backup_recovery_phrase(self):
        self.just_fyi("Back up recovery phrase")
        self.backup_recovery_phrase_button.click()
        for checkbox in self.checkbox_button.find_elements():
            checkbox.click()
        self.button_one.click()
        recovery_phrase = self.get_recovery_phrase()
        self.button_one.click()
        self.fill_recovery_phrase_checking_words(recovery_phrase)
        self.checkbox_button.click()
        self.button_one.click()
        return ' '.join(recovery_phrase.values())

    def edit_profile_picture(self, image_index: int, update_by="Gallery"):
        self.driver.info("## Setting custom profile image", device=False)
        if AbstractTestCase().environment != 'lt':
            raise NotImplementedError('Test case is implemented to run on LambdaTest only')
        self.edit_profile_button.click()
        self.change_profile_photo_button.click()
        if update_by == "Gallery":
            self.select_from_gallery_button.click()
            self.select_photo_from_gallery_by_index(image_index)
        else:
            ## take by Photo
            self.take_photo()
            self.click_system_back_button()
            self.profile_picture.click()
            self.take_photo()
            self.accept_photo_button.click()
        self.crop_photo_button.click()
        self.driver.info("## Custom profile image has been set", device=False)
        self.click_system_back_button()

    def edit_username(self, new_username: str):
        self.edit_profile_button.click()
        self.edit_profile_name_button.click()
        self.edit_profile_input.clear()
        self.edit_profile_input.send_keys(new_username)
        self.save_name_button.click()

    def take_photo(self):
        self.take_photo_button.click()
        if self.allow_button.is_element_displayed(sec=5):
            self.allow_button.click()
        if self.allow_all_the_time.is_element_displayed(sec=5):
            self.allow_all_the_time.click()
        if self.element_by_text("NEXT").is_element_displayed(sec=5):
            self.element_by_text("NEXT").click()
        self.shutter_button.click()

    def select_photo_from_gallery_by_index(self, image_index: int):
        self.allow_button.click_if_shown()
        self.allow_all_button.click_if_shown()
        image_element = Button(self.driver, class_name="androidx.cardview.widget.CardView")
        try:
            image_element.find_elements()[image_index].click()
        except IndexError:
            self.click_system_back_button(times=2)
            raise NoSuchElementException("Image with index %s was not found" % image_index) from None

    def change_accent_colour(self, colour_name: str):
        self.edit_profile_button.click()
        self.edit_accent_colour.click()
        Button(self.driver, accessibility_id=colour_name).click()
        self.save_colour_button.click()

    def logout(self):
        self.driver.info("Logging out")
        self.logout_button.click()
        self.logout_dialog.logout_button.click()
        self.logout_button.wait_for_invisibility_of_element(30)

    def get_sync_code(self):
        self.syncing_button.scroll_and_click()
        self.paired_devices_button.click()
        self.sync_plus_button.click()
        for checkbox in Button(
                self.driver,
                xpath="//*[@content-desc='checkbox-off'][@resource-id='checkbox-component']").find_elements():
            checkbox.click()
        self.continue_button.click()
        self.slide_button_track.swipe_right_on_element(width_percentage=1.3)
        password_input = self.password_input.find_element()
        password_input.send_keys(common_password)
        self.login_button.click()
        self.wait_for_staleness_of_element(password_input)
        return self.password_input.text

    def get_current_device_name(self):
        element = BaseElement(
            self.driver,
            xpath="//android.view.ViewGroup/android.view.ViewGroup[@content-desc='status-tag-positive']/..")
        return element.attribute_value('content-desc').split(',')[1].strip()

    def get_paired_device_by_name(self, device_name: str):

        class PairedDeviceElement(BaseElement):
            def __init__(self, driver, device_name):
                super().__init__(driver, xpath="//*[@content-desc='icon, %s, label-component']" % device_name)

            @property
            def get_pair_button(self):
                return Button(self.driver, xpath=self.locator + "//*[@content-desc='Pair']")

            @property
            def get_unpair_button(self):
                return Button(self.driver, xpath=self.locator + "//*[@content-desc='Unpair']")

        return PairedDeviceElement(self.driver, device_name)

    def get_key_pair_account_by_name(self, account_name: str):
        class KeyPairAccountElement(BaseElement):
            def __init__(self, driver, account_name):
                locator = "//*[@content-desc='account-avatar']/following-sibling::*[@text='%s']" % account_name
                super().__init__(driver, xpath=locator)
                self.address = Text(driver, xpath=locator + "/following-sibling::android.widget.TextView")

        return KeyPairAccountElement(self.driver, account_name)

    def get_missing_key_pair_by_name(self, key_pair_name: str):
        class MissingKeyPairElement(BaseElement):
            def __init__(self, driver, key_pair_name):
                locator = "//*[@content-desc='missing-keypair-item']//*[@text='%s']" % key_pair_name
                super().__init__(driver, xpath=locator)
                self.options_button = Button(driver, xpath=locator + "/../..//*[@content-desc='options-button']")

        return MissingKeyPairElement(self.driver, key_pair_name)

    def turn_new_contact_requests_toggle(self, state: str = 'on'):
        self.profile_messages_button.click()
        element = Button(
            self.driver,
            xpath="//*[@content-desc='Allow new contact requests, label-component']//*[@resource-id='toggle-component']")
        element.click()
        if element.attribute_value('content-desc') != 'toggle-%s' % state:
            pytest.fail("Allow new contact requests toggle was not turned %s" % state)
        self.click_system_back_button()

    def change_currency(self, new_currency: str):
        self.language_and_currency_button.scroll_and_click()
        self.change_currency_button.click()
        self.element_by_text_part(new_currency).click()
        time.sleep(1)
        self.navigate_back_to_main_profile_view(2)
