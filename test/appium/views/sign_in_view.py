import base64
import os

from selenium.common.exceptions import NoSuchElementException

from tests import common_password, appium_root_project_path
from tests.base_test_case import get_app_path
from views.base_element import Button, EditBox, Text
from views.base_view import BaseView


class MultiAccountButton(Button):
    class Username(Text):
        def __init__(self, driver, locator_value):
            super(MultiAccountButton.Username, self).__init__(driver,
                                                              xpath="%s//android.widget.TextView[@content-desc='username']" % locator_value)

    def __init__(self, driver, position=1):
        super(MultiAccountButton, self).__init__(driver,
                                                 xpath="//*[@content-desc='select-account-button-%s']" % position)
        self.username = self.Username(driver, self.locator)


class MultiAccountOnLoginButton(Button):
    def __init__(self, driver, position=1):
        super(MultiAccountOnLoginButton, self).__init__(driver,
                                                        xpath="(//*[@content-desc='chat-icon'])[%s]/.." % position)

    @property
    def account_logo(self):
        class AccountLogo(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="%s//*[@content-desc='chat-icon']" % parent_locator)

        return AccountLogo(self.driver, self.locator)


class MoveAndResetButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="move-and-reset-button")

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)


class BeginRecoveryButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="keycard-recovery-intro-button-text")

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class LogInButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="login-button")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class AccessKeyButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="access-existing-keys")

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class KeycardKeyStorageButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="select-storage-:advanced")

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class PrivacyPolicyLink(Button):
    def __init__(self, driver):
        super(PrivacyPolicyLink, self).__init__(driver, accessibility_id="privacy-policy-link")

    def click(self):
        self.driver.info('Click on link %s' % self.name)
        self.click_until_absense_of_element(TermsOfUseLink(self.driver))
        return self.navigate()

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class TermsOfUseLink(Button):
    def __init__(self, driver):
        super(TermsOfUseLink, self).__init__(driver, xpath="//*[contains(@text, 'Terms of Use')]")

    def click(self):
        counter = 0
        while PrivacyPolicyLink(self.driver).is_element_displayed(1) and counter <= 5:
            try:
                self.click_inside_element_by_coordinate(times_to_click=2)
                counter += 1
            except NoSuchElementException:
                return self.navigate()
        self.driver.info('Click on link %s' % self.name)
        return self.navigate()

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class UserProfileElement(Button):
    def __init__(self, driver, username):
        self.username = username
        super().__init__(driver,
                         xpath="//*[@text='%s']//ancestor::android.view.ViewGroup[@content-desc='profile-card']" % username)

    def open_user_options(self):
        Button(self.driver, xpath='%s//*[@content-desc="profile-card-options"]' % self.locator).click()


class SignInView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)
        self.driver = driver

        # intro screen
        self.sign_in_intro_button = Button(self.driver, accessibility_id="already-use-status-button")
        self.i_m_new_in_status_button = Button(self.driver, accessibility_id="new-to-status-button")

        self.migration_password_input = EditBox(self.driver, accessibility_id="enter-password-input")
        self.login_button = LogInButton(self.driver)
        self.access_key_button = AccessKeyButton(self.driver)
        self.generate_key_button = Button(self.driver, accessibility_id="generate-old-key")
        self.your_keys_more_icon = Button(self.driver, xpath="//androidx.appcompat.widget.LinearLayoutCompat")
        self.generate_new_key_button = Button(self.driver, accessibility_id="generate-a-new-key")
        self.create_password_input = EditBox(self.driver,
                                             xpath="(//android.widget.EditText[@content-desc='password-input'])[1]")
        self.confirm_your_password_input = EditBox(self.driver,
                                                   xpath="(//android.widget.EditText[@content-desc='password-input'])[2]")
        self.privacy_policy_link = PrivacyPolicyLink(self.driver)
        self.terms_of_use_link = TermsOfUseLink(self.driver)
        self.keycard_storage_button = KeycardKeyStorageButton(self.driver)
        self.first_username_on_choose_chat_name = Text(self.driver,
                                                       xpath="//*[@content-desc='select-account-button-0']//android.widget.TextView[1]")
        self.get_keycard_banner = Button(self.driver, translation_id="get-a-keycard")
        self.accept_tos_checkbox = self.checkbox_button

        # keycard recovery
        self.recover_with_keycard_button = Button(self.driver, accessibility_id="recover-with-keycard-button")
        self.begin_recovery_button = BeginRecoveryButton(self.driver)
        self.pair_to_this_device_button = Button(self.driver, translation_id="pair-card")

        # restore from seed phrase
        self.seedphrase_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.enter_seed_phrase_button = Button(self.driver, accessibility_id="enter-seed-phrase-button")
        self.reencrypt_your_key_button = Button(self.driver, accessibility_id="onboarding-next-button")

        # migrate multiaccount
        self.options_button = Button(self.driver, xpath="//androidx.appcompat.widget.LinearLayoutCompat")
        self.manage_keys_and_storage_button = Button(self.driver, accessibility_id="manage-keys-and-storage-button")
        self.multi_account_on_login_button = MultiAccountOnLoginButton(self.driver)
        self.move_keystore_file_option = Button(self.driver, accessibility_id="move-keystore-file")
        self.reset_database_checkbox = Button(self.driver, translation_id="reset-database")
        self.move_and_reset_button = MoveAndResetButton(self.driver)
        self.choose_storage_button = Button(self.driver, translation_id="choose-storage")
        self.enter_seed_phrase_next_button = Button(self.driver, translation_id="enter-seed-phrase")
        self.keycard_required_option = Button(self.driver, translation_id="empty-keycard-required")

        # errors
        self.custom_seed_phrase_label = Text(self.driver, translation_id="custom-seed-phrase")
        self.continue_custom_seed_phrase_button = Button(self.driver, accessibility_id="continue-custom-seed-phrase")
        self.cancel_custom_seed_phrase_button = Button(self.driver, accessibility_id="cancel-custom-seed-phrase")

        # New onboarding
        self.generate_keys_button = Button(self.driver, translation_id="create-multiaccount")
        self.profile_your_name_edit_box = EditBox(self.driver, accessibility_id="profile-title-input")
        self.profile_continue_button = Button(self.driver, accessibility_id="submit-create-profile-button")
        self.profile_password_edit_box = EditBox(self.driver, translation_id="password-creation-placeholder-1")
        self.profile_repeat_password_edit_box = EditBox(self.driver, translation_id="password-creation-placeholder-2")
        self.profile_confirm_password_button = Button(self.driver, translation_id="password-creation-confirm")
        self.enable_biometric_maybe_later_button = Button(self.driver, translation_id="maybe-later")
        self.identifiers_button = Button(self.driver, accessibility_id="skip-identifiers")
        self.enable_notifications_button = Button(self.driver, accessibility_id="enable-notifications-button")
        self.maybe_later_button = Button(self.driver, accessibility_id="enable-notifications-later-button")
        self.start_button = Button(self.driver, accessibility_id="welcome-button")
        self.use_recovery_phrase_button = Button(self.driver, translation_id="use-recovery-phrase")
        self.passphrase_edit_box = EditBox(self.driver, accessibility_id="passphrase-input")
        self.show_profiles_button = Button(self.driver, accessibility_id="show-profiles")
        self.plus_profiles_button = Button(self.driver, accessibility_id="show-new-account-options")
        self.create_new_profile_button = Button(self.driver, accessibility_id="create-new-profile")
        self.remove_profile_button = Button(self.driver, accessibility_id="remove-profile")

    def set_password(self, password: str):
        self.profile_password_edit_box.send_keys(password)
        self.profile_repeat_password_edit_box.click()
        self.profile_repeat_password_edit_box.send_keys(password)
        self.checkbox_button.scroll_to_element()
        self.checkbox_button.click()
        self.profile_confirm_password_button.click()

    def set_profile(self, username: str, set_image=False):
        self.profile_your_name_edit_box.send_keys(username)
        self.profile_continue_button.click_until_presence_of_element(self.profile_password_edit_box)
        if set_image:
            pass

    def create_user(self, password=common_password, keycard=False, enable_notifications=False, second_user=False,
                    username="test user"):
        self.driver.info("## Creating new multiaccount (password:'%s', keycard:'%s', enable_notification: '%s')" %
                         (password, str(keycard), str(enable_notifications)), device=False)
        if second_user:
            self.show_profiles_button.wait_and_click(20)
            self.plus_profiles_button.click()
            self.create_new_profile_button.click()
            self.generate_keys_button.click_until_presence_of_element(self.profile_your_name_edit_box)
        else:
            self.i_m_new_in_status_button.click_until_presence_of_element(self.generate_keys_button)
            self.generate_keys_button.click_until_presence_of_element(self.profile_your_name_edit_box)
        self.set_profile(username)
        self.set_password(password)
        if self.enable_biometric_maybe_later_button.is_element_displayed(10):
            self.enable_biometric_maybe_later_button.click()
        # self.next_button.click_until_absense_of_element(self.element_by_translation_id("intro-wizard-title2"))
        # if keycard:
        #     keycard_flow = self.keycard_storage_button.click()
        #     keycard_flow.confirm_pin_and_proceed()
        #     keycard_flow.backup_seed_phrase()
        # else:
        #     self.next_button.click()
        #     self.create_password_input.send_keys(password)
        #     self.confirm_your_password_input.send_keys(password)
        #     self.next_button.click()
        self.identifiers_button.wait_and_click(30)
        if enable_notifications:
            self.enable_notifications_button.click_until_presence_of_element(self.start_button)
        else:
            self.maybe_later_button.click_until_presence_of_element(self.start_button)
        self.start_button.click()
        self.chats_tab.wait_for_visibility_of_element(30)
        self.driver.info("## New multiaccount is created successfully!", device=False)
        return self.get_home_view()

    def recover_access(self, passphrase: str, password: str = common_password, keycard=False,
                       enable_notifications=False, second_user=False, username='Restore user', set_image=False):
        self.driver.info("## Recover access(password:%s, keycard:%s)" % (password, str(keycard)), device=False)

        if not second_user:
            self.i_m_new_in_status_button.click_until_presence_of_element(self.generate_keys_button)
        else:
            self.show_profiles_button.wait_and_click(20)
            self.plus_profiles_button.click()
            self.create_new_profile_button.click()
        self.use_recovery_phrase_button.click()
        self.passphrase_edit_box.send_keys(passphrase)
        self.continue_button.click_until_presence_of_element(self.profile_your_name_edit_box)
        self.set_profile(username, set_image)
        self.set_password(password)
        if self.enable_biometric_maybe_later_button.is_element_displayed(10):
            self.enable_biometric_maybe_later_button.click()
        self.identifiers_button.wait_and_click(30)
        if enable_notifications:
            self.enable_notifications_button.click_until_presence_of_element(self.start_button)
        else:
            self.maybe_later_button.click_until_presence_of_element(self.start_button)
        self.start_button.click()
        self.chats_tab.wait_for_visibility_of_element(30)
        self.driver.info("## Multiaccount is recovered successfully!", device=False)
        return self.get_home_view()

    def sign_in(self, password=common_password, keycard=False, position=1):
        self.driver.info("## Sign in (password:%s, keycard:%s)" % (password, str(keycard)), device=False)

        if keycard:
            from views.keycard_view import KeycardView
            keycard_view = KeycardView(self.driver)
            keycard_view.one_button.wait_for_visibility_of_element(10)
            keycard_view.enter_default_pin()
            if keycard_view.connect_selected_card_button.is_element_displayed():
                keycard_view.connect_selected_card_button.click()
        else:
            self.password_input.send_keys(password)
            self.login_button.click()
        self.driver.info("## Signed in successfully!", device=False)
        return self.get_home_view()

    def get_multiaccount_by_position(self, position: int, element_class=MultiAccountOnLoginButton):
        account_button = element_class(self.driver, position)
        if account_button.is_element_displayed():
            return account_button
        else:
            raise NoSuchElementException(
                'Device %s: Unable to find multiaccount by position %s' % (self.driver.number, position)) from None

    def open_weblink_and_login(self, url_weblink):
        self.driver.info("Open weblink '%s'" % url_weblink)
        self.open_universal_web_link(url_weblink)
        self.sign_in()

    def import_db(self, seed_phrase, import_db_folder_name, password=common_password):
        self.driver.info('## Importing database', device=False)
        import_file_name = 'export.db'
        home = self.recover_access(passphrase=seed_phrase, password=password)
        profile = home.profile_button.click()
        full_path_to_file = os.path.join(appium_root_project_path, 'views/dbs/%s/%s' %
                                         (import_db_folder_name, import_file_name))
        profile.logout()
        self.multi_account_on_login_button.wait_for_visibility_of_element(30)
        self.get_multiaccount_by_position(1).click()
        self.password_input.send_keys(password)
        self.driver.push_file(source_path=full_path_to_file,
                              destination_path='%s%s' % (get_app_path(), import_file_name))
        self.options_button.click()
        self.element_by_text('Import unencrypted').click()
        self.element_by_text('Import unencrypted').wait_for_invisibility_of_element(40)
        self.sign_in_button.click()
        self.home_button.wait_for_element(40)
        self.driver.info('## Importing database is finished!', device=False)
        return self.get_home_view()

    def export_db(self, seed_phrase, file_to_export='export.db', password=common_password):
        self.driver.info('## Export database', device=False)
        home = self.recover_access(passphrase=seed_phrase, password=password)
        profile = home.profile_button.click()
        full_path_to_file = os.path.join(appium_root_project_path, 'views/dbs/%s' % file_to_export)
        profile.logout()
        self.multi_account_on_login_button.wait_for_visibility_of_element(30)
        self.get_multiaccount_by_position(1).click()
        self.password_input.send_keys(common_password)
        self.options_button.click()
        self.element_by_text('Export unencrypted').wait_and_click(40)
        self.element_by_text('Export unencrypted').wait_for_invisibility_of_element(40)
        file_base_64 = self.driver.pull_file('%s/export.db' % get_app_path())
        try:
            with open(full_path_to_file, "wb") as f:
                f.write(base64.b64decode(file_base_64))
        except Exception as e:
            print(str(e))
        self.driver.info('## Exporting database is finished!', device=False)

    def get_user(self, username):
        self.driver.info("Getting username card by '%s'" % username)
        expected_element = UserProfileElement(self.driver, username)
        return expected_element if expected_element.is_element_displayed(10) else self.driver.fail("User is not found!")
