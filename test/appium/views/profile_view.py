import time
from tests.base_test_case import AbstractTestCase
from views.base_element import BaseText, BaseButton, BaseEditBox, BaseElement
from views.base_view import BaseView


class PublicKeyText(BaseText):

    def __init__(self, driver):
        super(PublicKeyText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('address-text')

    @property
    def text(self):
        text = self.scroll_to_element().text
        self.driver.info('%s is %s' % (self.name, text))
        return text


class ProfileAddressText(BaseText):

    def __init__(self, driver):
        super(ProfileAddressText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-address')


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


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.widget.ImageView[@content-desc="chat-icon"]/../../android.widget.TextView')


class ShareMyContactKeyButton(BaseButton):

    def __init__(self, driver):
        super(ShareMyContactKeyButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-my-contact-code-button')


class EditButton(BaseButton):

    def __init__(self, driver):
        super(EditButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('edit-button')


class ProfilePictureElement(BaseElement):
    def __init__(self, driver):
        super(ProfilePictureElement, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-icon')


class EditPictureButton(BaseButton):

    def __init__(self, driver):
        super(EditPictureButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('edit-profile-photo-button')


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
        self.locator = self.Locator.accessibility_id('share-code-button')


class AdvancedButton(BaseButton):

    def __init__(self, driver):
        super(AdvancedButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Advanced')

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class BackupRecoveryPhraseButton(BaseButton):

    def __init__(self, driver):
        super(BackupRecoveryPhraseButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Backup your recovery phrase')


class OkContinueButton(BaseButton):

    def __init__(self, driver):
        super(OkContinueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK, CONTINUE']")


class RecoveryPhraseTable(BaseText):

    def __init__(self, driver):
        super(RecoveryPhraseTable, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.widget.FrameLayout/android.view.ViewGroup[3]/android.widget.TextView')


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
        self.locator = self.Locator.text_selector('OK, GOT IT')


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
        self.locator = self.Locator.xpath_selector(
            "//*[contains(@text,'Ropsten test network')]/following-sibling::android.widget.CheckBox[1]")


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


class RequestFeatureButton(BaseButton):

    def __init__(self, driver):
        super(RequestFeatureButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("request-feature-button")


class SubmitBugButton(BaseButton):

    def __init__(self, driver):
        super(SubmitBugButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("submit-bug-button")


class FaqButton(BaseButton):

    def __init__(self, driver):
        super(FaqButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("faq-button")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class BootnodesButton(BaseButton):

    def __init__(self, driver):
        super(BootnodesButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('bootnodes-settings-button')


class AddBootnodeButton(BaseButton):

    def __init__(self, driver):
        super(AddBootnodeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("(//*[@content-desc='icon'])[2]")


class BootnodeNameInput(BaseEditBox):

    def __init__(self, driver):
        super(BootnodeNameInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[@text='Specify a name']")


class BootnodeAddressInput(BaseEditBox):

    def __init__(self, driver):
        super(BootnodeAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[@text='Specify bootnode address']")


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
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[@text='Specify a mailserver address']")


class MailServerElement(BaseButton):

    def __init__(self, driver, server_name):
        super(MailServerElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='mailserver-item']//*[@text='%s']" % server_name)


class MailServerConnectButton(BaseButton):

    def __init__(self, driver):
        super(MailServerConnectButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('mailserver-connect-button')


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

        self.network_settings_button = NetworkSettingsButton(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.ropsten_chain_button = RopstenChainButton(self.driver)
        self.custom_network_url = CustomNetworkURL(self.driver)
        self.specify_name_input = SpecifyNameInput(self.driver)
        self.connect_button = NetworkSettingsButton.ConnectButton(self.driver)
        self.logout_button = LogoutButton(self.driver)
        self.logout_dialog = LogoutDialog(self.driver)
        self.confirm_logout_button = ConfirmLogoutButton(self.driver)

        self.main_currency_button = MainCurrencyButton(self.driver)

        self.username_text = UserNameText(self.driver)
        self.share_my_contact_key_button = ShareMyContactKeyButton(self.driver)
        self.edit_button = EditButton(self.driver)
        self.profile_picture = ProfilePictureElement(self.driver)
        self.edit_picture_button = EditPictureButton(self.driver)
        self.confirm_edit_button = ConfirmEditButton(self.driver)
        self.cross_icon = CrossIcon(self.driver)
        self.share_button = ShareButton(self.driver)
        self.advanced_button = AdvancedButton(self.driver)
        self.debug_mode_toggle = DebugModeToggle(self.driver)

        # Backup seed phrase
        self.backup_recovery_phrase_button = BackupRecoveryPhraseButton(self.driver)
        self.ok_continue_button = OkContinueButton(self.driver)
        self.recovery_phrase_table = RecoveryPhraseTable(self.driver)
        self.recovery_phrase_word_number = RecoveryPhraseWordNumberText(self.driver)
        self.recovery_phrase_word_input = RecoveryPhraseWordInput(self.driver)
        self.ok_got_it_button = OkGotItButton(self.driver)

        self.select_from_gallery_button = SelectFromGalleryButton(self.driver)
        self.capture_button = CaptureButton(self.driver)

        self.help_button = HelpButton(self.driver)
        self.request_feature_button = RequestFeatureButton(self.driver)
        self.submit_bug_button = SubmitBugButton(self.driver)
        self.faq_button = FaqButton(self.driver)

        # Bootnodes
        self.bootnodes_button = BootnodesButton(self.driver)
        self.bootnode_address_input = BootnodeAddressInput(self.driver)
        self.enable_bootnodes = EnableBootnodesToggle(self.driver)

        # Mailservers
        self.mail_server_button = MailServerButton(self.driver)
        self.mail_server_address_input = MailServerAddressInput(self.driver)
        self.mail_server_connect_button = MailServerConnectButton(self.driver)

    def switch_network(self, network):
        self.advanced_button.click()
        self.debug_mode_toggle.click()
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        network_button = NetworkSettingsButton.NetworkButton(self.driver, network)
        network_button.click()
        self.connect_button.click()
        from views.sign_in_view import SignInView
        return SignInView(self.driver)

    def add_custom_network(self):
        self.advanced_button.click()
        self.debug_mode_toggle.click()
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        self.plus_button.click_until_presence_of_element(self.ropsten_chain_button)
        self.ropsten_chain_button.click()
        self.custom_network_url.send_keys('https://ropsten.infura.io/iMko0kJNQUdhbCSaJcox')
        self.specify_name_input.send_keys('custom_ropsten')
        self.save_button.click()
        self.element_by_text_part('custom_ropsten').click_until_presence_of_element(self.connect_button)
        self.connect_button.click()
        return self.get_sign_in_view()

    def get_address(self):
        profile_view = self.profile_button.click()
        return profile_view.profile_address_text.text

    def get_recovery_phrase(self):
        text = [i.text for i in self.recovery_phrase_table.find_elements()]
        return dict(zip(map(int, text[::2]), text[1::2]))

    def backup_recovery_phrase(self):
        self.backup_recovery_phrase_button.click()
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
        self.profile_picture.template = file_name
        self.edit_button.click()
        self.swipe_down()
        self.edit_picture_button.click()
        self.select_from_gallery_button.click()
        if self.allow_button.is_element_displayed(sec=10):
            self.allow_button.click()
        picture = self.element_by_text(file_name)
        if not picture.is_element_displayed(2):
            for element_text in 'Images', 'DCIM':
                self.element_by_text(element_text).click()
        picture.click()
        self.confirm_edit_button.click()

    def logout(self):
        self.logout_button.click()
        return self.logout_dialog.logout_button.click()

    def set_currency(self, desired_currency='Euro (EUR)'):
        self.main_currency_button.click()
        desired_currency = self.element_by_text(desired_currency)
        desired_currency.scroll_to_element()
        desired_currency.click()

    def mail_server_by_name(self, server_name):
        return MailServerElement(self.driver, server_name)
