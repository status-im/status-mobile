import time

from tests import info
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
        info('%s is %s' % (self.name, text))
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
            self.locator = self.Locator.xpath_selector('//*[@text="' + network + '"]')

    class ConnectButton(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.ConnectButton, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('network-connect-button')


class LogoutButton(BaseButton):

    def __init__(self, driver):
        super(LogoutButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('log-out-button')


class ConfirmLogoutButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmLogoutButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='LOG OUT']")


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.widget.ImageView[@content-desc="chat-icon"]/../android.widget.TextView')


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


class ConfirmButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('done-button')


class CrossIcon(BaseButton):

    def __init__(self, driver):
        super(CrossIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('done-button')


class AdvancedButton(BaseButton):

    def __init__(self, driver):
        super(AdvancedButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Advanced"]')

    def click(self):
        self.scroll_to_element().click()
        info('Tap on %s' % self.name)
        return self.navigate()


class BackupSeedPhraseButton(BaseButton):

    def __init__(self, driver):
        super(BackupSeedPhraseButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Backup your Seed Phrase']")


class OkContinueButton(BaseButton):

    def __init__(self, driver):
        super(OkContinueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK, CONTINUE']")


class SeedPhraseTable(BaseText):

    def __init__(self, driver):
        super(SeedPhraseTable, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//android.widget.FrameLayout/android.view.ViewGroup[3]/android.widget.TextView')


class SeedPhraseWordNumberText(BaseText):

    def __init__(self, driver):
        super(SeedPhraseWordNumberText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[contains(@text,'#')]")

    @property
    def text(self):
        time.sleep(1)
        return self.find_element().text.split('#')[1]


class SeedPhraseWordInput(BaseEditBox):

    def __init__(self, driver):
        super(SeedPhraseWordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.EditText')


class OkGotItButton(BaseButton):

    def __init__(self, driver):
        super(OkGotItButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK, GOT IT']")


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
        self.locator = self.Locator.xpath_selector("//*[@text='Select from gallery']")


class ProfileView(BaseView):

    def __init__(self, driver):
        super(ProfileView, self).__init__(driver)
        self.driver = driver

        # old design
        self.options_button = OptionsButton(self.driver)
        self.username_input = OptionsButton.UsernameInput(self.driver)
        self.user_status_box = OptionsButton.UserStatusBox(self.driver)
        self.user_status_input = OptionsButton.UserStatusInput(self.driver)
        self.public_key_text = PublicKeyText(self.driver)
        self.profile_address_text = ProfileAddressText(self.driver)

        self.network_settings_button = NetworkSettingsButton(self.driver)
        self.connect_button = NetworkSettingsButton.ConnectButton(self.driver)
        self.logout_button = LogoutButton(self.driver)
        self.confirm_logout_button = ConfirmLogoutButton(self.driver)

        # new design

        self.username_text = UserNameText(self.driver)
        self.share_my_contact_key_button = ShareMyContactKeyButton(self.driver)
        self.edit_button = EditButton(self.driver)
        self.profile_picture = ProfilePictureElement(self.driver)
        self.edit_picture_button = EditPictureButton(self.driver)
        self.confirm_button = ConfirmButton(self.driver)
        self.cross_icon = CrossIcon(self.driver)
        self.advanced_button = AdvancedButton(self.driver)
        self.debug_mode_toggle = DebugModeToggle(self.driver)

        # Backup seed phrase
        self.backup_seed_phrase_button = BackupSeedPhraseButton(self.driver)
        self.ok_continue_button = OkContinueButton(self.driver)
        self.seed_phrase_table = SeedPhraseTable(self.driver)
        self.seed_phrase_word_number = SeedPhraseWordNumberText(self.driver)
        self.seed_phrase_word_input = SeedPhraseWordInput(self.driver)
        self.ok_got_it_button = OkGotItButton(self.driver)
        self.select_from_gallery_button = SelectFromGalleryButton(self.driver)

    def switch_network(self, network):
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        network_button = NetworkSettingsButton.NetworkButton(self.driver, network)
        network_button.click()
        self.connect_button.click()
        from views.sign_in_view import SignInView
        return SignInView(self.driver)

    def get_address(self):
        profile_view = self.profile_button.click()
        return profile_view.profile_address_text.text

    def get_seed_phrase(self):
        text = [i.text for i in self.seed_phrase_table.find_elements()]
        return dict(zip(text[::2], text[1::2]))

    def edit_profile_picture(self, file_name: str):
        if AbstractTestCase().environment == 'sauce':
            self.profile_picture.template = file_name
            self.edit_button.click()
            self.edit_picture_button.click()
            self.select_from_gallery_button.click()
            if self.allow_button.is_element_displayed(sec=10):
                self.allow_button.click()
            self.element_by_text(file_name).click()
            self.confirm_button.click()
        else:
            raise NotImplementedError('Test case is implemented to run on SauceLabs only')
