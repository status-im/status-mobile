import time
from tests.base_test_case import AbstractTestCase
from views.base_element import Text, Button, EditBox, SilentButton
from views.base_view import BaseView


class OptionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="(//android.view.ViewGroup[@content-desc='icon'])[2]")


class AddNewContactButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="add-new-contact-button")

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class LogoutButton(SilentButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="log-out-button")

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


class ENSusernames(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="ens-usernames")

    def navigate(self):
        from views.dapps_view import DappsView
        return DappsView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class AdvancedButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="advanced-button")

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class BackupRecoveryPhraseButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="back-up-recovery-phrase-button")

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


class RecoveryPhraseWordInput(EditBox):
    def __init__(self, driver):
        super(RecoveryPhraseWordInput, self).__init__(driver, xpath="//android.widget.EditText")


class HelpButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="help-button")

    def click(self):
        self.scroll_to_element().click()


class FaqButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="faq-button")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class MailServerElement(Button):
    def __init__(self, driver, server_name):
        super().__init__(driver, xpath="//*[@content-desc='mailserver-item']//*[@text='%s']" % server_name)
        self.server_name = server_name

    def click(self):
        size = self.driver.get_window_size()
        self.driver.swipe(500, size["height"] * 0.8, 500, size["height"] * 0.05)
        self.find_element().click()


class AboutButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="about-button")

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class SyncSettingsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="sync-settings-button")

    def click(self):
        self.scroll_to_element().click()


class DappPermissionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="dapps-permissions-button")

    def click(self):
        self.scroll_to_element().click()


class PrivacyPolicyButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="privacy-policy")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class TermsOfUseButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="terms-of-service")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class ProfilePictureElement(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="chat-icon")


class KeycardButton(Button):

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class ProfileView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)
        self.options_button = OptionsButton(self.driver)

        # Header
        self.public_key_text = Text(self.driver, accessibility_id="chat-key")
        self.default_username_text = Text(self.driver, accessibility_id="default-username")
        self.share_my_profile_button = Button(self.driver, accessibility_id="share-header-button")
        self.profile_picture = ProfilePictureElement(self.driver)
        self.online_indicator = Button(self.driver, accessibility_id="online-profile-photo-dot")
        self.edit_picture_button = Button(self.driver, accessibility_id="edit-profile-photo-button")
        self.confirm_edit_button = Button(self.driver, accessibility_id="done-button")
        self.select_from_gallery_button = Button(self.driver, translation_id="profile-pic-pick")
        self.capture_button = Button(self.driver, translation_id="image-source-make-photo")
        self.take_photo_button = Button(self.driver, accessibility_id="take-photo")
        self.crop_photo_button = Button(self.driver, accessibility_id="Crop")
        self.decline_photo_crop = Button(self.driver, accessibility_id="Navigate up")
        self.shutter_button = Button(self.driver, accessibility_id="Shutter")
        self.accept_photo_button = Button(self.driver, accessibility_id="Done")

        # ENS
        self.username_in_ens_chat_settings_text = EditBox(self.driver,
                                                          xpath="//*[@content-desc='chat-icon']/../../android.widget.TextView[2]")
        self.ens_usernames_button = ENSusernames(self.driver)
        self.ens_name_in_share_chat_key_text = Text(self.driver, accessibility_id="ens-username")

        # Contacts
        self.contacts_button = Button(self.driver, accessibility_id="contacts-button")
        self.blocked_users_button = Button(self.driver, accessibility_id="blocked-users-list-button")
        self.add_new_contact_button = AddNewContactButton(self.driver)
        self.invite_friends_in_contact_button = Button(self.driver, accessibility_id="invite-friends-button")

        # Privacy and security
        self.privacy_and_security_button = Button(self.driver, accessibility_id="privacy-and-security-settings-button")
        self.accept_new_chats_from = Button(self.driver, accessibility_id="accept-new-chats-from")
        self.accept_new_chats_from_contacts_only = Button(self.driver, translation_id="contacts")
        self.reset_password_button = Button(self.driver, accessibility_id="reset-password")
        self.current_password_edit_box = EditBox(self.driver, accessibility_id="current-password")
        self.new_password_edit_box = EditBox(self.driver, accessibility_id="new-password")
        self.confirm_new_password_edit_box = EditBox(self.driver, accessibility_id="confirm-new-password")
        self.current_password_wrong_text = Text(self.driver, accessibility_id="current-password-error")

        # Appearance
        self.appearance_button = Button(self.driver, accessibility_id="appearance-settings-button")
        self.show_profile_pictures_of = Button(self.driver, accessibility_id="show-profile-pictures")
        self.show_profile_pictures_to = Button(self.driver, accessibility_id="show-profile-pictures-to")
        ## Backup recovery phrase
        self.backup_recovery_phrase_button = BackupRecoveryPhraseButton(self.driver)
        self.recovery_phrase_table = RecoveryPhraseTable(self.driver)
        self.recovery_phrase_word_number = RecoveryPhraseWordNumberText(self.driver)
        self.recovery_phrase_word_input = RecoveryPhraseWordInput(self.driver)
        ## Dapps permissions
        self.dapp_permissions_button = DappPermissionsButton(self.driver)
        self.revoke_access_button = Button(self.driver, translation_id="revoke-access")
        ## Delete my profile
        self.delete_my_profile_button = Button(self.driver, translation_id="delete-my-profile")
        self.delete_my_profile_password_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.delete_profile_button = Button(self.driver, accessibility_id="delete-profile-confirm")

        # Notifications
        self.profile_notifications_button = Button(self.driver, accessibility_id="notifications-settings-button")
        self.profile_notifications_toggle_button = Button(self.driver, accessibility_id="local-notifications-settings-button")
        self.push_notification_toggle = Button(self.driver,
                                               xpath="//*[@content-desc='notifications-button']//*[@content-desc='switch']")
        self.wallet_push_notifications = Button(self.driver, accessibility_id="notifications-button")

        # Sync settings
        self.sync_settings_button = SyncSettingsButton(self.driver)
        ## Mobile Data
        self.use_mobile_data = Button(self.driver, translation_id="mobile-network-use-mobile",
                                      suffix="/following-sibling::android.widget.Switch[1]")
        self.ask_me_when_on_mobile_network = Button(self.driver, translation_id="mobile-network-ask-me",
                                                    suffix="/following-sibling::android.widget.Switch[1]")
        ## Backup settings
        self.backup_settings_button = Button(self.driver, accessibility_id="backup-settings-button")
        ## Perform backup
        self.perform_backup_button = Button(self.driver, translation_id="perform-backup")

        ## Sync history data
        self.sync_history_for_button = Button(self.driver, accessibility_id="default-sync-period-button")
        ## History nodes
        self.mail_server_button = Button(self.driver, accessibility_id="offline-messages-settings-button")
        self.mail_server_address_input = EditBox(self.driver, translation_id="mailserver-address",
                                                 suffix="/following-sibling::*[1]/android.widget.EditText")
        self.mail_server_connect_button = Button(self.driver, accessibility_id="mailserver-connect-button")
        self.mail_server_auto_selection_button = Button(self.driver, translation_id="mailserver-automatic",
                                                        suffix="/following-sibling::*[1]")
        self.use_history_node_button = Button(self.driver, translation_id="offline-messaging-use-history-nodes",
                                              suffix="/following-sibling::*[1]")
        self.mail_server_delete_button = Button(self.driver, accessibility_id="mailserver-delete-button")
        self.mail_server_confirm_delete_button = Button(self.driver,
                                                        xpath='//*[@text="%s"]' % self.get_translation_by_key(
                                                            "delete-mailserver").upper())
        ## Device syncing
        self.devices_button = Button(self.driver, accessibility_id="pairing-settings-button")
        self.device_name_input = EditBox(self.driver, accessibility_id="device-name")
        self.go_to_pairing_settings_button = Button(self.driver, translation_id="pairing-go-to-installation",
                                                    uppercase=True)
        self.advertise_device_button = Button(self.driver, accessibility_id="advertise-device")
        self.sync_all_button = Button(self.driver, translation_id="sync-all-devices")

        # Keycard
        self.keycard_button = Button(self.driver, accessibility_id="keycard-button")
        self.change_pin_button = KeycardButton(self.driver, translation_id="change-pin")
        self.change_puk_button = KeycardButton(self.driver, translation_id="change-puk")
        self.change_pairing_code_button = KeycardButton(self.driver, translation_id="change-pairing")
        self.create_keycard_backup_button = KeycardButton(self.driver, translation_id="keycard-backup")

        # Advanced
        self.advanced_button = AdvancedButton(self.driver)
        self.mutual_contact_request_switcher = Button(self.driver, accessibility_id="mutual-contact-requests-switch")
        ## Network
        self.network_settings_button = Button(self.driver, accessibility_id="network-button")
        self.active_network_name = Text(self.driver,
                                        xpath="//android.widget.TextView[contains(@text,'with upstream RPC')]")
        self.plus_button = Button(self.driver, xpath="(//android.widget.ImageView[@content-desc='icon'])[2]")
        self.custom_chain_button = Button(self.driver, translation_id="custom")
        self.custom_network_url_input = EditBox(self.driver, translation_id="rpc-url",
                                                suffix="/following-sibling::*[1]/android.widget.EditText")
        self.custom_network_symbol_input = EditBox(self.driver, translation_id="specify-symbol")
        self.specify_name_input = EditBox(self.driver, translation_id="name",
                                          suffix="/following-sibling::*[1]/android.widget.EditText")
        self.specify_network_id_input = EditBox(self.driver, translation_id="specify-network-id")
        self.connect_button = Button(self.driver, accessibility_id="network-connect-button")
        ## Toggles
        self.transaction_management_enabled_toggle = Button(self.driver,
                                                            accessibility_id="transactions-management-enabled")
        self.webview_debug_toggle = Button(self.driver, accessibility_id="webview-debug-switch")
        self.waku_bloom_toggle = Button(self.driver, accessibility_id="waku-bloom-filter-mode-settings-switch")
        ## Log level
        self.log_level_setting_button = Button(self.driver, accessibility_id="log-level-settings-button")
        ## Fleet
        self.fleet_setting_button = Button(self.driver, accessibility_id="fleet-settings-button")
        ## Bootnodes
        self.bootnodes_button = Button(self.driver, accessibility_id="bootnodes-settings-button")
        self.bootnode_address_input = EditBox(self.driver, accessibility_id="bootnode-address")
        self.enable_bootnodes = Button(self.driver, xpath="//android.widget.Switch")
        self.add_bootnode_button = Button(self.driver, accessibility_id="add-bootnode")

        # Need help
        self.help_button = HelpButton(self.driver)
        self.submit_bug_button = Button(self.driver, accessibility_id="submit-bug-button")
        self.bug_description_edit_box = EditBox(self.driver, accessibility_id="bug-report-description")
        self.bug_steps_edit_box = EditBox(self.driver, accessibility_id="bug-report-steps")
        self.bug_submit_button = Button(self.driver, accessibility_id="bug-report-submit")
        self.request_a_feature_button = Button(self.driver, accessibility_id="request-a-feature-button")
        self.faq_button = FaqButton(self.driver)

        # About
        self.about_button = AboutButton(self.driver)
        self.privacy_policy_button = PrivacyPolicyButton(self.driver)
        self.terms_of_use_button = TermsOfUseButton(self.driver)
        self.app_version_text = Text(self.driver, xpath="//*[@content-desc='app-version']//android.widget.TextView[2]")
        self.node_version_text = Text(self.driver,
                                      xpath="//*[@content-desc='node-version']//android.widget.TextView[2]")

        # Logout
        self.logout_button = LogoutButton(self.driver)
        self.logout_dialog = LogoutDialog(self.driver)
        self.confirm_logout_button = Button(self.driver, translation_id="logout", uppercase=True)

    def switch_network(self, network='Mainnet with upstream RPC'):
        self.driver.info("## Switch network to '%s'" % network, device=False)
        self.advanced_button.click()
        self.network_settings_button.click()
        network_button = Button(self.driver, xpath="//*[@text='%s']" % network)
        network_button.scroll_and_click()
        self.connect_button.click_until_presence_of_element(self.confirm_button)
        self.confirm_button.click_until_absense_of_element(self.confirm_button)
        from views.sign_in_view import SignInView
        SignInView(self.driver).sign_in()
        self.driver.info("## Network is switched successfully!", device=False)

    def open_contact_from_profile(self, username):
        self.driver.info("Opening profile of '%s' via Contacts" % username)
        self.contacts_button.wait_and_click(30)
        self.element_by_text(username).click()
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def add_contact_via_contacts_list(self, public_key):
        self.driver.info("Adding user to Contacts via Profile > Contacts")
        self.contacts_button.wait_and_click(30)
        self.add_new_contact_button.wait_and_click()
        chat = self.get_chat_view()
        chat.public_key_edit_box.click()
        chat.public_key_edit_box.send_keys(public_key)
        chat.view_profile_new_contact_button.click_until_presence_of_element(chat.profile_add_to_contacts_button)
        chat.profile_add_to_contacts_button.click()
        self.navigate_back_to_home_view()

    def switch_push_notifications(self):
        self.driver.info("Enabling push notifications via Profile")
        self.profile_notifications_button.scroll_and_click()
        self.profile_notifications_toggle_button.click()
        self.navigate_back_to_home_view()

    def add_custom_network(self, rpc_url: str, name: str, symbol: str, netwrok_id:str):
        self.driver.info("## Add custom network", device=False)
        self.advanced_button.click()
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        self.plus_button.click_until_presence_of_element(self.custom_chain_button)
        self.custom_network_url_input.send_keys(rpc_url)
        self.specify_name_input.send_keys(name)
        self.custom_network_symbol_input.send_keys(symbol)
        self.custom_chain_button.scroll_and_click()
        self.specify_network_id_input.scroll_to_element()
        self.specify_network_id_input.send_keys(netwrok_id)
        self.save_button.click()
        self.element_by_text_part(name).scroll_to_element()
        self.element_by_text_part(name).click_until_presence_of_element(self.connect_button)
        self.connect_button.click()
        self.confirm_button.click()
        self.driver.info("## Custom network is added succesfully!", device=False)
        return self.get_sign_in_view()

    def get_recovery_phrase(self):
        text = [i.text for i in self.recovery_phrase_table.find_elements()]
        return dict(zip(map(int, text[::2]), text[1::2]))

    def backup_recovery_phrase(self):
        self.driver.info("## Back up seed phrase", device=False)
        self.ok_continue_button.click()
        recovery_phrase = self.get_recovery_phrase()
        self.next_button.click()
        word_number = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.send_keys(recovery_phrase[word_number])
        self.next_button.click()
        word_number_1 = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.send_keys(recovery_phrase[word_number_1])
        self.done_button.click()
        self.yes_button.click()
        self.ok_got_it_button.click()
        self.driver.info("## Seed phrase is backed up!", device=False)
        return recovery_phrase

    def edit_profile_picture(self, file_name: str, update_by="Gallery"):
        self.driver.info("## Setting custom profile image", device=False)
        if not AbstractTestCase().environment == 'sauce':
            raise NotImplementedError('Test case is implemented to run on SauceLabs only')
        self.profile_picture.click()
        self.profile_picture.template = file_name
        if update_by == "Gallery":
            self.select_from_gallery_button.click()
            self.select_photo_from_gallery(file_name)
        else:
            ## take by Photo
            self.take_photo()
            self.click_system_back_button()
            self.profile_picture.click()
            self.take_photo()
            self.accept_photo_button.click()
        self.crop_photo_button.click()
        self.driver.info("## Custom profile image has been set", device=False)

    def take_photo(self):
        self.take_photo_button.click()
        if self.allow_button.is_element_displayed(sec=5):
            self.allow_button.click()
        if self.allow_all_the_time.is_element_displayed(sec=5):
            self.allow_all_the_time.click()
        if self.element_by_text("NEXT").is_element_displayed(sec=5):
            self.element_by_text("NEXT").click()
        self.shutter_button.click()

    def select_photo_from_gallery(self, file_name: str):
        if self.allow_button.is_element_displayed(sec=5):
            self.allow_button.click()
        image_full_content = self.get_image_in_storage_by_name(file_name)
        if not image_full_content.is_element_displayed(2):
            self.show_roots_button.click()
            for element_text in 'Images', 'DCIM':
                self.element_by_text(element_text).click()
        image_full_content.click()

    def logout(self):
        self.driver.info("Logging out")
        self.logout_button.click()
        self.logout_dialog.logout_button.click()
        self.logout_button.wait_for_invisibility_of_element(30)

    def mail_server_by_name(self, server_name):
        return MailServerElement(self.driver, server_name)

    def get_image_in_storage_by_name(self, image_name=str()):
        return SilentButton(self.driver, xpath="//*[contains(@content-desc,'%s')]" % image_name)

    def get_toggle_device_by_name(self, device_name):
        self.driver.info("Selecting device '%s' for sync" % device_name)
        return SilentButton(self.driver,
                            xpath="//android.widget.TextView[contains(@text,'%s')]/..//android.widget.CheckBox" % device_name)

    def discover_and_advertise_device(self, device_name):
        self.driver.info("Discovering and advertising '%s'" % device_name)
        self.sync_settings_button.click()
        self.devices_button.scroll_to_element()
        self.devices_button.click()
        self.device_name_input.send_keys(device_name)
        self.continue_button.click_until_presence_of_element(self.advertise_device_button, 2)
        self.advertise_device_button.click()

    def retry_to_connect_to_mailserver(self):
        self.driver.info("Retrying to connect to mailserver 5 times")
        i = 0
        while self.element_by_translation_id("mailserver-error-title").is_element_displayed(20) and i < 5:
            self.element_by_translation_id("mailserver-retry", uppercase=True).click()
            i += 1
            self.just_fyi("retrying to connect: %s attempt" % i)
            time.sleep(10)
        if i == 5:
            self.driver.fail("Failed to connect after %s attempts" % i)

    def connect_existing_ens(self, name, is_stateofus=False):
        self.driver.info("## Connect existing ENS: %s" % name, device=False)
        dapp_view = self.ens_usernames_button.click()
        dapp_view.element_by_translation_id("get-started").click()
        if not is_stateofus:
            dapp_view.element_by_translation_id("ens-want-custom-domain").click()
        dapp_view.ens_name_input.send_keys(name)
        expected_text = dapp_view.get_translation_by_key("ens-username-connected")
        if not dapp_view.element_by_text_part(expected_text).is_element_displayed():
            dapp_view.click_system_back_button()
            dapp_view.element_by_text_part(expected_text).wait_for_element(30)
        dapp_view.check_ens_name.click_until_presence_of_element(dapp_view.element_by_translation_id("ens-got-it"))
        dapp_view.element_by_translation_id("ens-got-it").click()
        self.driver.info("## ENS name is connected successfully!", device=False)
        return dapp_view

    @staticmethod
    def return_mailserver_name(mailserver_name, fleet):
        return mailserver_name + '.' + fleet

    @property
    def current_active_network(self):
        self.advanced_button.click()
        self.active_network_name.scroll_to_element(10, 'up')
        return self.active_network_name.text
