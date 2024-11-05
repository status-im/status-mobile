import time

import pytest

from tests import common_password
from views.base_element import Button, EditBox, Text, BaseElement
from views.base_view import BaseView
from views.home_view import HomeView
from views.sign_in_view import SignInView


class AssetElement(Button):

    def __init__(self, driver, asset_name):
        self.asset_name = asset_name
        self.locator = "//android.view.ViewGroup[@content-desc='container']/android.widget.TextView[@text='%s']" % \
                       self.asset_name
        super().__init__(driver=driver, xpath=self.locator)

    def get_amount(self):
        element = Text(self.driver, xpath=self.locator + "/../android.widget.TextView[3]")
        element.scroll_to_element()
        try:
            amount = element.text.split()[0]
            if '<' in amount:
                return 0
            else:
                return float(amount)
        except ValueError:
            pytest.fail("Cannot get %s amount" % self.asset_name)


class ActivityElement(BaseElement):
    def __init__(self, driver, index: int):
        self.locator = "(//*[@content-desc='wallet-activity'])[%s]" % index
        super().__init__(driver=driver, xpath=self.locator)

    @property
    def header(self):
        return Text(self.driver, prefix=self.locator, xpath="//*[@content-desc='transaction-header']").text

    @property
    def timestamp(self):
        return Text(self.driver, prefix=self.locator, xpath="//*[@content-desc='transaction-timestamp']").text

    @property
    def amount(self):
        return Text(self.driver, prefix=self.locator,
                    xpath="//*[@content-desc='context-tag'][1]/android.widget.TextView").text

    @property
    def from_text(self):
        return Text(self.driver, prefix=self.locator,
                    xpath="//*[@content-desc='context-tag'][2]/android.widget.TextView").text

    @property
    def to_text(self):
        return Text(self.driver, prefix=self.locator,
                    xpath="//*[@content-desc='context-tag'][3]/android.widget.TextView").text


class WalletView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        # Wallet view
        self.network_drop_down = Button(self.driver, accessibility_id='network-dropdown')
        self.collectibles_tab = Button(self.driver, accessibility_id='collectibles-tab')
        self.add_account_button = Button(self.driver, accessibility_id='add-account')

        # Account adding
        # ToDo: add unique accessibility ids for the next 2 elements:
        self.create_account_button = HomeView(self.driver).start_a_new_chat_bottom_sheet_button
        self.add_account_to_watch = HomeView(self.driver).add_a_contact_chat_bottom_sheet_button
        self.address_to_watch_input = EditBox(self.driver, accessibility_id='add-address-to-watch')
        self.account_has_activity_label = Text(self.driver, accessibility_id='account-has-activity')
        self.add_account_continue_button = Button(self.driver, accessibility_id='Continue')
        self.add_watched_address_button = Button(self.driver, accessibility_id='confirm-button-label')
        self.add_account_derivation_path_text = Text(
            self.driver, xpath="//*[contains(@content-desc,'icon, Derivation path')]/android.widget.TextView[2]")

        # Account view
        self.close_account_button = Button(self.driver, accessibility_id='top-bar')
        self.account_name_text = Text(
            self.driver, xpath="//*[@content-desc='account-avatar']/../following-sibling::android.widget.TextView[1]")
        self.account_emoji_button = Button(self.driver, accessibility_id='account-emoji')
        self.send_button = Button(self.driver, accessibility_id='send')
        self.send_from_drawer_button = Button(
            self.driver, xpath="//*[@content-desc='send']/*[@content-desc='left-icon-for-action']")
        self.copy_address_button = Button(self.driver, accessibility_id='copy-address')
        self.share_address_button = Button(self.driver, accessibility_id='share-account')
        self.remove_account_button = Button(self.driver, accessibility_id='remove-account')
        self.derivation_path_note_checkbox = Button(self.driver, accessibility_id='checkbox-off')
        self.account_about_derivation_path_text = Text(
            self.driver, xpath="//*[@content-desc='derivation-path-icon']/following-sibling::*[2]")

        self.activity_tab = Button(self.driver, accessibility_id='activity-tab')
        self.about_tab = Button(self.driver, accessibility_id='about')

        # Sending transaction
        self.address_text_input = EditBox(self.driver, accessibility_id='address-text-input')
        self.amount_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.from_network_text = Text(
            self.driver, xpath="(//*[@content-desc='loading']/following-sibling::android.widget.TextView)[1]")
        self.confirm_button = Button(self.driver, accessibility_id='button-one')
        self.done_button = Button(self.driver, accessibility_id='done')

        # Edit key pair
        self.edit_key_pair_button = Button(self.driver, accessibility_id="Edit")
        self.key_pairs_plus_button = Button(self.driver, accessibility_id="standard-title-action")
        self.generate_new_keypair_button = Button(self.driver, accessibility_id="generate-new-keypair")
        self.import_using_recovery_phrase_button = Button(self.driver, accessibility_id="import-using-phrase")
        self.key_pair_continue_button = Button(self.driver, accessibility_id="Continue")
        self.key_pair_name_input = EditBox(
            self.driver, xpath="//*[@text='Key pair name']/..//following-sibling::*/*[@content-desc='input']")

    def set_network_in_wallet(self, network_name: str):
        self.network_drop_down.click()
        Button(self.driver, accessibility_id="%s, label-component" % network_name.capitalize()).click()
        self.network_drop_down.click()

    def get_account_element(self, account_name: str = 'Account 1'):
        return Button(self.driver, xpath="//android.view.ViewGroup[contains(@content-desc,'%s')]" % account_name)

    def get_asset(self, asset_name: str):
        element = AssetElement(driver=self.driver, asset_name=asset_name)
        element.scroll_to_element(down_start_y=0.89, down_end_y=0.8)
        return element

    def select_asset(self, asset_name: str):
        Button(driver=self.driver,
               xpath="//*[@content-desc='token-network']/android.widget.TextView[@text='%s']" % asset_name).click()

    def select_network(self, network_name: str):
        Button(driver=self.driver,
               xpath="//*[@content-desc='network-list']/*[@text='%s']" % network_name).click()

    def slide_and_confirm_with_password(self):
        self.slide_button_track.slide()
        self.password_input.send_keys(common_password)
        self.login_button.click()

    def confirm_transaction(self):
        self.confirm_button.click()
        for _ in range(3):
            if self.slide_button_track.is_element_displayed():
                break
            time.sleep(1)
            self.confirm_button.click()
        self.slide_and_confirm_with_password()

    def set_amount(self, amount: float):
        for i in '{:f}'.format(amount).rstrip('0'):
            Button(self.driver, accessibility_id='keyboard-key-%s' % i).click()

    def send_asset(self, address: str, asset_name: str, amount: float, network_name: str):
        self.send_button.click()
        self.address_text_input.send_keys(address)
        self.continue_button.click()
        self.select_asset(asset_name)
        self.select_network(network_name)
        self.set_amount(amount)
        self.confirm_transaction()

    def send_asset_from_drawer(self, address: str, asset_name: str, amount: float, network_name: str):
        asset_element = self.get_asset(asset_name)
        asset_element.long_press_element()
        self.send_from_drawer_button.click()
        self.select_network(network_name)
        self.address_text_input.send_keys(address)
        self.continue_button.click()
        self.set_amount(amount)
        self.confirm_transaction()

    def add_regular_account(self, account_name: str):
        self.add_account_button.click()
        self.create_account_button.click()
        derivation_path = self.add_account_derivation_path_text.text
        SignInView(self.driver).profile_title_input.send_keys(account_name)
        self.slide_and_confirm_with_password()
        return derivation_path.replace(' ', '')

    def add_watch_only_account(self, address: str, account_name: str):
        self.add_account_button.click()
        self.add_account_to_watch.click()
        self.address_to_watch_input.send_keys(address)
        self.add_account_continue_button.click()
        SignInView(self.driver).profile_title_input.send_keys(account_name)
        self.add_watched_address_button.click()

    def remove_account(self, account_name: str, watch_only: bool = False):
        self.get_account_element(account_name=account_name).click()
        self.account_emoji_button.click()
        self.remove_account_button.click()
        if not watch_only:
            self.derivation_path_note_checkbox.click()
        self.confirm_button.click()

    def get_activity_element(self, index=1):
        return ActivityElement(self.driver, index=index)

    def add_key_pair_account(self, account_name, passphrase=None, key_pair_name=None):
        self.add_account_button.click()
        self.create_account_button.click()
        signin_view = SignInView(self.driver)
        signin_view.profile_title_input.send_keys(account_name)
        self.edit_key_pair_button.click()
        self.key_pairs_plus_button.click()
        if passphrase:
            self.import_using_recovery_phrase_button.click()
            signin_view.passphrase_edit_box.send_keys(passphrase)
            self.key_pair_continue_button.click()
            self.key_pair_name_input.send_keys(key_pair_name)
            self.key_pair_continue_button.click()
        else:
            self.generate_new_keypair_button.click()
            for checkbox in self.checkbox_button.find_elements():
                checkbox.click()
            self.element_by_translation_id("reveal-phrase").click()
            # ToDo: can't be done in current small size emulators, add when moved to LambdaTest
        self.slide_and_confirm_with_password()
        derivation_path = self.add_account_derivation_path_text.text
        return derivation_path.replace(' ', '')

    def get_account_address(self):
        self.account_emoji_button.click_until_presence_of_element(self.copy_address_button)
        self.share_address_button.click()
        wallet_address = self.sharing_text_native.text
        self.click_system_back_button()
        return wallet_address
