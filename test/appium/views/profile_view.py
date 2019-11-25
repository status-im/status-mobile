import time
from tests.base_test_case import AbstractTestCase
from views.base_element import BaseText, BaseButton, BaseEditBox, BaseElement
from views.base_view import BaseView


class PublicKeyText(BaseText):

    def __init__(self, driver):
        super(PublicKeyText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-key')


class ProfileAddressText(BaseText):
    def __init__(self, driver):
        super(ProfileAddressText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-public-key')

    @property
    def text(self):
        text = self.scroll_to_element().text
        self.driver.info('%s is %s' % (self.name, text))
        return text


class OptionsButton(BaseButton):

    def __init__(self, driver):
        super(OptionsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '(//android.view.ViewGroup[@content-desc="icon"])[2]')

    class UserStatusBox(BaseButton):

        def __init__(self, driver):
            super(OptionsButton.UserStatusBox, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('(//android.widget.ScrollView)[2]//android.widget.TextView')

    class UsernameInput(BaseEditBox):

        def __init__(self, driver):
            super(OptionsButton.UsernameInput, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('username-input')

    class UserStatusInput(BaseEditBox):

        def __init__(self, driver):
            super(OptionsButton.UserStatusInput, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('(//android.widget.EditText)[2]')


class NetworkSettingsButton(BaseButton):

    def __init__(self, driver):
        super(NetworkSettingsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('network-button')

    class NetworkButton(BaseButton):
        def __init__(self, driver, network):
            super(NetworkSettingsButton.NetworkButton, self).__init__(driver)
            self.locator = self.Locator.text_selector(network)

    class ConnectButton(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.ConnectButton, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('network-connect-button')


class LogoutButton(BaseButton):

    def __init__(self, driver):
        super(LogoutButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('log-out-button')

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class LogoutDialog(BaseView):
    def __init__(self, driver):
        super(LogoutDialog, self).__init__(driver)
        self.logout_button = LogoutDialog.LogoutButton(driver)

    class LogoutButton(BaseButton):
        def __init__(self, driver):
            super(LogoutDialog.LogoutButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='LOG OUT' or @text='Log out']")

        def navigate(self):
            from views.sign_in_view import SignInView
            return SignInView(self.driver)


class ConfirmLogoutButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmLogoutButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('LOG OUT')


class DefaultUserNameText(BaseText):
    def __init__(self, driver):
        super(DefaultUserNameText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.view.ViewGroup[@content-desc="edit-profile-photo-button"]/../android.widget.TextView')

class ENSusernames(BaseButton):
    def __init__(self, driver):
        super(ENSusernames, self).__init__(driver)
        self.locator = self.Locator.text_selector('ENS usernames')

    def navigate(self):
        from views.dapps_view import DappsView
        return DappsView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class ShareMyProfileButton(BaseButton):

    def __init__(self, driver):
        super(ShareMyProfileButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[1]')


class ProfilePictureElement(BaseElement):
    def __init__(self, driver):
        super(ProfilePictureElement, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-icon')


class EditPictureButton(BaseButton):

    def __init__(self, driver):
        super(EditPictureButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.view.ViewGroup[@content-desc="edit-profile-photo-button"]')


class ConfirmEditButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmEditButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('done-button')


class CrossIcon(BaseButton):

    def __init__(self, driver):
        super(CrossIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('done-button')


class ShareButton(BaseButton):

    def __init__(self, driver):
        super(ShareButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-my-contact-code-button')


class AdvancedButton(BaseButton):

    def __init__(self, driver):
        super(AdvancedButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Advanced')

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class LogLevelSetting(BaseButton):

    def __init__(self, driver):
        super(LogLevelSetting, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="log-level-settings-button"]/android.widget.TextView[2]')

class BackupRecoveryPhraseButton(BaseButton):

    def __init__(self, driver):
        super(BackupRecoveryPhraseButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="back-up-recovery-phrase-button"]')

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class OkContinueButton(BaseButton):

    def __init__(self, driver):
        super(OkContinueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Ok, continue']")


class RecoveryPhraseTable(BaseText):

    def __init__(self, driver):
        super(RecoveryPhraseTable, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Your seed phrase']/following-sibling::android.view.ViewGroup[1]/android.widget.TextView")


class RecoveryPhraseWordNumberText(BaseText):

    def __init__(self, driver):
        super(RecoveryPhraseWordNumberText, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('#')

    @property
    def number(self):
        time.sleep(1)
        return int(self.find_element().text.split('#')[1])


class RecoveryPhraseWordInput(BaseEditBox):

    def __init__(self, driver):
        super(RecoveryPhraseWordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.EditText')


class OkGotItButton(BaseButton):

    def __init__(self, driver):
        super(OkGotItButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('OK, got it')


class DebugModeToggle(BaseButton):

    def __init__(self, driver):
        super(DebugModeToggle, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.Switch")

    def click(self):
        self.scroll_to_element()
        super(DebugModeToggle, self).click()


class SelectFromGalleryButton(BaseButton):

    def __init__(self, driver):
        super(SelectFromGalleryButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Select from gallery')


class CaptureButton(BaseButton):

    def __init__(self, driver):
        super(CaptureButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Capture')


class MainCurrencyButton(BaseButton):

    def __init__(self, driver):
        super(MainCurrencyButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("currency-button")


class PlusButton(BaseButton):

    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("(//android.view.ViewGroup[@content-desc='icon'])[2]")


class RopstenChainButton(BaseButton):

    def __init__(self, driver):
        super(RopstenChainButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[contains(@text,'Ropsten test network')]")


class SpecifyNameInput(BaseEditBox):

    def __init__(self, driver):
        super(SpecifyNameInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Name']/following-sibling::*[1]/android.widget.EditText")


class CustomNetworkURL(BaseEditBox):

    def __init__(self, driver):
        super(CustomNetworkURL, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='RPC URL']/following-sibling::*[1]/android.widget.EditText")


class HelpButton(BaseButton):

    def __init__(self, driver):
        super(HelpButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("help-button")

    def click(self):
        self.scroll_to_element().click()


class SubmitBugButton(BaseButton):

    def __init__(self, driver):
        super(SubmitBugButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("submit-bug-button")


class RequestFeatureButton(BaseButton):

    def __init__(self, driver):
        super(RequestFeatureButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("request-a-feature-button")


class FaqButton(BaseButton):

    def __init__(self, driver):
        super(FaqButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("faq-button")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class AppVersionText(BaseText):
    def __init__(self, driver):
        super(AppVersionText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='app-version']//android.widget.TextView[2]")


class NodeVersionText(BaseText):
    def __init__(self, driver):
        super(NodeVersionText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='node-version']//android.widget.TextView[2]")


class BootnodesButton(BaseButton):

    def __init__(self, driver):
        super(BootnodesButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('bootnodes-settings-button')


class AddBootnodeButton(BaseButton):

    def __init__(self, driver):
        super(AddBootnodeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("(//*[@content-desc='icon'])[2]")


class BootnodeAddressInput(BaseEditBox):

    def __init__(self, driver):
        super(BootnodeAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Bootnode address']/following-sibling::*[1]/android.widget.EditText")


class EnableBootnodesToggle(BaseEditBox):

    def __init__(self, driver):
        super(EnableBootnodesToggle, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.Switch')


class MailServerButton(BaseButton):

    def __init__(self, driver):
        super(MailServerButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('offline-messages-settings-button')


class MailServerAddressInput(BaseEditBox):

    def __init__(self, driver):
        super(MailServerAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Mailserver address']/following-sibling::*[1]/android.widget.EditText")


class MailServerAutoSelectionButton(BaseButton):
    def __init__(self, driver):
        super(MailServerAutoSelectionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("checkbox")


class MailServerElement(BaseButton):

    def __init__(self, driver, server_name):
        super(MailServerElement, self).__init__(driver)
        self.server_name = server_name
        self.locator = self.Locator.xpath_selector("//*[@content-desc='mailserver-item']//*[@text='%s']" % server_name)

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on "%s" mailserver value' % self.server_name)


class MailServerConnectButton(BaseButton):

    def __init__(self, driver):
        super(MailServerConnectButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('mailserver-connect-button')


class ActiveNetworkName(BaseText):

    def __init__(self, driver):
        super(ActiveNetworkName, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('with upstream RPC')


class AboutButton(BaseButton):
    def __init__(self, driver):
        super(AboutButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('about-button')

    def navigate(self):
        from views.about_view import AboutView
        return AboutView(self.driver)

    def click(self):
        self.scroll_to_element().click()
        return self.navigate()


class RemovePictureButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Remove current photo')


class DevicesButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="pairing-settings-button"]')


class DeviceNameInput(BaseEditBox):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.accessibility_id('device-name')


class ContinueButton(BaseButton):
    def __init__(self, driver):
        super(ContinueButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Continue')


class SyncSettingsButton(BaseButton):
    def __init__(self, driver):
        super(SyncSettingsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="sync-settings-button"]')


class GoToPairingSettingsButton(BaseButton):
    def __init__(self, driver):
        super(GoToPairingSettingsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('GO TO PAIRING SETTINGS')


class AdvertiseDeviceButton(BaseButton):
    def __init__(self, driver):
        super(AdvertiseDeviceButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('advertise-device')


class SyncedDeviceToggle(BaseButton):
    def __init__(self, driver, device_name):
        super(SyncedDeviceToggle, self).__init__(driver)
        self.device_name = device_name
        self.locator = self.Locator.xpath_selector(
            '//android.widget.TextView[contains(@text,"%s")]/../android.widget.Switch' % device_name)


class SyncAllButton(BaseButton):
    def __init__(self, driver):
        super(SyncAllButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Sync all devices')


class ContactsButton(BaseButton):
    def __init__(self, driver):
        super(ContactsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('contacts-button')


class BlockedUsersButton(BaseButton):
    def __init__(self, driver):
        super(BlockedUsersButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('blocked-users-list-button')


class DappPermissionsButton(BaseButton):
    def __init__(self, driver):
        super(DappPermissionsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('dapps-permissions-button')

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on %s' % self.name)


class RevokeAccessButton(BaseButton):
    def __init__(self, driver):
        super(RevokeAccessButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Revoke access')


class PrivacyAndSecurityButton(BaseButton):
    def __init__(self, driver):
        super(PrivacyAndSecurityButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('privacy-and-security-settings-button')


class ShowENSNameInChatsToggle(BaseButton):
    def __init__(self, driver):
        super(ShowENSNameInChatsToggle, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Show my ENS username in chats']/following-sibling::*[1][name()='android.widget.Switch'] ")

class UseMobileDataToggle(BaseButton):
    def __init__(self, driver):
        super(UseMobileDataToggle, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Use mobile data']/../*[name()='android.widget.Switch']")

class AskMeWhenOnMobileNetworkToggle(BaseButton):
    def __init__(self, driver):
        super(AskMeWhenOnMobileNetworkToggle, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Ask me when on mobile network']/../*[name()='android.widget.Switch']")

class ENSUsernameInChatSettings(BaseElement):
    def __init__(self, driver):
        super(ENSUsernameInChatSettings, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='chat-icon']/../android.widget.TextView[2]")



class ProfileView(BaseView):

    def __init__(self, driver):
        super(ProfileView, self).__init__(driver)
        self.driver = driver

        self.options_button = OptionsButton(self.driver)
        self.username_input = OptionsButton.UsernameInput(self.driver)
        self.user_status_box = OptionsButton.UserStatusBox(self.driver)
        self.user_status_input = OptionsButton.UserStatusInput(self.driver)
        self.public_key_text = PublicKeyText(self.driver)
        self.profile_address_text = ProfileAddressText(self.driver)
        self.about_button = AboutButton(self.driver)
        self.app_version_text = AppVersionText(self.driver)
        self.node_version_text = NodeVersionText(self.driver)

        self.network_settings_button = NetworkSettingsButton(self.driver)
        self.active_network_name = ActiveNetworkName(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.ropsten_chain_button = RopstenChainButton(self.driver)
        self.custom_network_url = CustomNetworkURL(self.driver)
        self.specify_name_input = SpecifyNameInput(self.driver)
        self.connect_button = NetworkSettingsButton.ConnectButton(self.driver)
        self.logout_button = LogoutButton(self.driver)
        self.logout_dialog = LogoutDialog(self.driver)
        self.confirm_logout_button = ConfirmLogoutButton(self.driver)

        self.main_currency_button = MainCurrencyButton(self.driver)

        self.default_username_text = DefaultUserNameText(self.driver)
        self.share_my_profile_button = ShareMyProfileButton(self.driver)
        self.profile_picture = ProfilePictureElement(self.driver)
        self.edit_picture_button = EditPictureButton(self.driver)
        self.remove_picture_button = RemovePictureButton(self.driver)
        self.confirm_edit_button = ConfirmEditButton(self.driver)
        self.cross_icon = CrossIcon(self.driver)
        self.share_button = ShareButton(self.driver)
        self.advanced_button = AdvancedButton(self.driver)
        self.log_level_setting = LogLevelSetting(self.driver)
        self.debug_mode_toggle = DebugModeToggle(self.driver)
        self.contacts_button = ContactsButton(self.driver)
        self.blocked_users_button = BlockedUsersButton(self.driver)
        self.dapp_permissions_button = DappPermissionsButton(self.driver)
        self.revoke_access_button = RevokeAccessButton(self.driver)
        self.privacy_and_security_button = PrivacyAndSecurityButton(self.driver)

        # Backup recovery phrase
        self.backup_recovery_phrase_button = BackupRecoveryPhraseButton(self.driver)
        self.ok_continue_button = OkContinueButton(self.driver)
        self.recovery_phrase_table = RecoveryPhraseTable(self.driver)
        self.recovery_phrase_word_number = RecoveryPhraseWordNumberText(self.driver)
        self.recovery_phrase_word_input = RecoveryPhraseWordInput(self.driver)
        self.ok_got_it_button = OkGotItButton(self.driver)

        self.select_from_gallery_button = SelectFromGalleryButton(self.driver)
        self.capture_button = CaptureButton(self.driver)

        self.help_button = HelpButton(self.driver)
        self.submit_bug_button = SubmitBugButton(self.driver)
        self.request_a_feature_button = RequestFeatureButton(self.driver)
        self.faq_button = FaqButton(self.driver)
        self.about_button = AboutButton(self.driver)
        self.sync_settings_button = SyncSettingsButton(self.driver)

        # Bootnodes
        self.bootnodes_button = BootnodesButton(self.driver)
        self.bootnode_address_input = BootnodeAddressInput(self.driver)
        self.enable_bootnodes = EnableBootnodesToggle(self.driver)

        # Mailservers
        self.mail_server_button = MailServerButton(self.driver)
        self.mail_server_address_input = MailServerAddressInput(self.driver)
        self.mail_server_connect_button = MailServerConnectButton(self.driver)
        self.mail_server_auto_selection_button = MailServerAutoSelectionButton(self.driver)

        # Pairing
        self.devices_button = DevicesButton(self.driver)
        self.device_name_input = DeviceNameInput(self.driver)
        self.continue_button = ContinueButton(self.driver)
        self.go_to_pairing_settings_button = GoToPairingSettingsButton(self.driver)
        self.advertise_device_button = AdvertiseDeviceButton(self.driver)
        self.sync_all_button = SyncAllButton(self.driver)

        # ENS
        self.show_ens_name_in_chats = ShowENSNameInChatsToggle(self.driver)
        self.username_in_ens_chat_settings_text = ENSUsernameInChatSettings(self.driver)
        self.ens_usernames_button = ENSusernames(self.driver)

        # Mobile Data
        self.use_mobile_data = UseMobileDataToggle(self.driver)
        self.ask_me_when_on_mobile_network = AskMeWhenOnMobileNetworkToggle(self.driver)

    def switch_network(self, network):
        self.advanced_button.click()
        self.debug_mode_toggle.click()
        self.network_settings_button.scroll_to_element(10, 'up')
        self.network_settings_button.click()
        network_button = NetworkSettingsButton.NetworkButton(self.driver, network)
        network_button.click()
        self.connect_button.click()
        self.confirm_button.click()
        from views.sign_in_view import SignInView
        signin_view = SignInView(self.driver)
        signin_view.sign_in()

    def switch_development_mode(self):
        self.advanced_button.click()
        self.debug_mode_toggle.click()

    def add_custom_network(self):
        self.advanced_button.click()
        self.debug_mode_toggle.click()
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        self.plus_button.click_until_presence_of_element(self.ropsten_chain_button)
        self.custom_network_url.send_keys('https://ropsten.infura.io/v3/f315575765b14720b32382a61a89341a')
        self.specify_name_input.send_keys('custom_ropsten')
        self.ropsten_chain_button.click()
        self.ropsten_chain_button.click()
        self.save_button.click()
        self.element_by_text_part('custom_ropsten').click_until_presence_of_element(self.connect_button)
        self.connect_button.click()
        self.confirm_button.click()
        return self.get_sign_in_view()

    def get_recovery_phrase(self):
        text = [i.text for i in self.recovery_phrase_table.find_elements()]
        return dict(zip(map(int, text[::2]), text[1::2]))

    def backup_recovery_phrase(self):
        self.ok_continue_button.click()
        recovery_phrase = self.get_recovery_phrase()
        self.next_button.click()
        word_number = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.set_value(recovery_phrase[word_number])
        self.next_button.click()
        word_number_1 = self.recovery_phrase_word_number.number
        self.recovery_phrase_word_input.set_value(recovery_phrase[word_number_1])
        self.done_button.click()
        self.yes_button.click()
        self.ok_got_it_button.click()
        return recovery_phrase

    def edit_profile_picture(self, file_name: str):
        if not AbstractTestCase().environment == 'sauce':
            raise NotImplementedError('Test case is implemented to run on SauceLabs only')
        self.profile_picture.click()
        self.profile_picture.template = file_name
        self.select_from_gallery_button.click()
        if self.allow_button.is_element_displayed(sec=5):
            self.allow_button.click()
        picture = self.element_by_text(file_name)
        if not picture.is_element_displayed(2):
            self.show_roots_button.click()
            for element_text in 'Images', 'DCIM':
                self.element_by_text(element_text).click()
        picture.click()

    def remove_profile_picture(self):
        if not AbstractTestCase().environment == 'sauce':
            raise NotImplementedError('Test case is implemented to run on SauceLabs only')
        self.profile_picture.click()
        self.remove_picture_button.click()

    def logout(self):
        self.logout_button.click()
        return self.logout_dialog.logout_button.click()

    def mail_server_by_name(self, server_name):
        return MailServerElement(self.driver, server_name)

    def get_toggle_device_by_name(self, device_name):
        return SyncedDeviceToggle(self.driver, device_name)

    def discover_and_advertise_device(self, device_name):
        self.profile_button.click()
        self.sync_settings_button.click()
        self.devices_button.scroll_to_element()
        self.devices_button.click()
        self.device_name_input.set_value(device_name)
        self.continue_button.click_until_presence_of_element(self.advertise_device_button, 2)
        self.advertise_device_button.click()

    def retry_to_connect_to_mailserver(self):
        i = 0
        while self.element_by_text_part("Error connecting").is_element_present(20) and i < 5:
            self.element_by_text('RETRY').click()
            i += 1
            self.just_fyi("retrying to connect: %s attempt" % i)
        if i == 5:
            self.driver.fail("Failed to connect after %s attempts" % i)

    def connect_existing_status_ens(self, name):
        self.just_fyi('switching to mainnet and add ENS')
        profile = self.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        self.profile_button.click()
        dapp_view = self.ens_usernames_button.click()
        dapp_view.element_by_text('Get started').click()
        dapp_view.ens_name.set_value(name)
        dapp_view.check_ens_name.click_until_presence_of_element(self.element_by_text('Ok, got it'))
        dapp_view.element_by_text('Ok, got it').click()
        return dapp_view


    @property
    def current_active_network(self):
        self.advanced_button.click()
        self.active_network_name.scroll_to_element(10, 'up')
        return self.active_network_name.text
