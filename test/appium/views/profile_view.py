import time

from selenium.common import NoSuchElementException

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
        super().__init__(driver, translation_id="your-recovery-phrase",
                         suffix="/following-sibling::android.view.ViewGroup[1]/android.widget.TextView")


class RecoveryPhraseWordNumberText(Text):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[contains(@text,'#')]")

    @property
    def number(self):
        time.sleep(1)
        return int(self.find_element().text.split('#')[1])


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


class ProfilePictureElement(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="chat-icon")


class ProfileView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)
        self.options_button = OptionsButton(self.driver)

        # Header
        self.default_username_text = Text(self.driver, accessibility_id="username")
        self.contact_name_text = Text(self.driver, accessibility_id="contact-name")
        self.profile_picture = ProfilePictureElement(self.driver)
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
        self.recovery_phrase_next_button = Button(self.driver, accessibility_id="Next, icon")
        self.recovery_phrase_word_input = EditBox(self.driver, xpath="//android.widget.EditText")


        # Notifications
        self.profile_notifications_button = Button(self.driver,
                                                   accessibility_id="icon, Notifications, label-component, icon")
        self.profile_notifications_toggle_button = Button(self.driver,
                                                          accessibility_id="local-notifications-settings-button")
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
        self.network_settings_button = Button(self.driver, accessibility_id="Network settings, label-component, icon")
        self.profile_legacy_button = Button(self.driver,
                                            accessibility_id="icon, Legacy settings, label-component, icon")
        self.testnet_mode_toggle = Button(self.driver, accessibility_id="icon, Testnet mode, label-component")
        self.confirm_testnet_mode_change_button = Button(self.driver, accessibility_id="confirm-testnet-mode-change")
        self.key_pairs_and_accounts_button = Button(self.driver,
                                                    accessibility_id="Key pairs and accounts, label-component, icon")
        self.options_button = Button(self.driver, accessibility_id="options-button")
        self.import_by_entering_recovery_phrase_button = Button(self.driver, accessibility_id="import-seed-phrase")

    def switch_network(self):
        self.driver.info("Toggling test mode")
        self.profile_wallet_button.click()
        self.network_settings_button.click()
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

    def backup_recovery_phrase(self):
        self.just_fyi("Back up recovery phrase")
        self.backup_recovery_phrase_button.click()
        self.ok_continue_button.click()
        recovery_phrase = self.get_recovery_phrase()
        self.recovery_phrase_next_button.click()
        word_number = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.send_keys(recovery_phrase[word_number])
        self.recovery_phrase_next_button.click()
        word_number_1 = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.send_keys(recovery_phrase[word_number_1])
        self.done_button.click()
        self.yes_button.click()
        self.ok_got_it_button.click()
        return ' '.join(recovery_phrase.values())

    def edit_profile_picture(self, image_index: int, update_by="Gallery"):
        self.driver.info("## Setting custom profile image", device=False)
        if not AbstractTestCase().environment == 'lt':
            raise NotImplementedError('Test case is implemented to run on SauceLabs only')
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
        return BaseElement(self.driver,
                           xpath="//*[@content-desc='missing-keypair-item']//*[@text='%s']" % key_pair_name)
