import pytest

from tests import common_password
from views.base_element import Button, EditBox, Text
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
        element.scroll_to_element(down_start_y=0.89, down_end_y=0.8)
        try:
            amount = element.text.split()[0]
            if '<' in amount:
                return 0
            else:
                return float(amount)
        except ValueError:
            pytest.fail("Cannot get %s amount" % self.asset_name)


class WalletView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        # Wallet view
        self.network_drop_down = Button(self.driver, accessibility_id='network-dropdown')
        self.collectibles_tab = Button(self.driver, accessibility_id='Collectibles')
        self.add_account_button = Button(self.driver, accessibility_id='add-account')

        # Account adding
        # ToDo: add unique accessibility ids for the next 2 elements:
        self.create_account_button = HomeView(self.driver).start_a_new_chat_bottom_sheet_button
        self.add_account_to_watch = HomeView(self.driver).add_a_contact_chat_bottom_sheet_button
        self.address_to_watch_input = EditBox(self.driver, accessibility_id='add-address-to-watch')
        self.account_has_activity_label = Text(self.driver, accessibility_id='account-has-activity')
        self.add_account_continue_button = Button(self.driver, accessibility_id='Continue')
        self.add_watched_address_button = Button(self.driver, accessibility_id='confirm-button-label')

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

        # Sending transaction
        self.address_text_input = EditBox(self.driver, accessibility_id='address-text-input')
        self.continue_button = Button(self.driver, accessibility_id='continue-button')
        self.amount_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.confirm_button = Button(self.driver, accessibility_id='button-one')
        self.done_button = Button(self.driver, accessibility_id='done')

    def get_account_element(self, account_name: str = 'Account 1'):
        return Button(self.driver, xpath="//android.view.ViewGroup[contains(@content-desc,'%s')]" % account_name)

    def get_asset(self, asset_name: str):
        element = AssetElement(driver=self.driver, asset_name=asset_name)
        element.scroll_to_element(down_start_y=0.89, down_end_y=0.8)
        return element

    def select_asset(self, asset_name: str):
        return Button(driver=self.driver,
                      xpath="//*[@content-desc='token-network']/android.widget.TextView[@text='%s']" % asset_name)

    def slide_and_confirm_with_password(self):
        self.slide_button_track.slide()
        self.password_input.send_keys(common_password)
        self.login_button.click()

    def confirm_transaction(self):
        self.confirm_button.click_until_presence_of_element(self.slide_button_track)
        self.slide_and_confirm_with_password()
        self.done_button.click()

    def set_amount(self, amount: float):
        for i in '{:f}'.format(amount).rstrip('0'):
            Button(self.driver, accessibility_id='keyboard-key-%s' % i).click()

    def send_asset(self, address: str, asset_name: str, amount: float):
        self.send_button.click()
        self.address_text_input.send_keys(address)
        self.continue_button.click_until_presence_of_element(self.collectibles_tab)
        self.select_asset(asset_name).click()
        self.set_amount(amount)
        self.confirm_transaction()

    def send_asset_from_drawer(self, address: str, asset_name: str, amount: float):
        asset_element = self.get_asset(asset_name)
        asset_element.long_press_element()
        self.send_from_drawer_button.click()
        self.address_text_input.send_keys(address)
        self.continue_button.click_until_presence_of_element(self.confirm_button)
        self.set_amount(amount)
        self.confirm_transaction()

    def add_regular_account(self, account_name: str):
        self.add_account_button.click()
        self.create_account_button.click()
        SignInView(self.driver).profile_title_input.send_keys(account_name)
        self.slide_and_confirm_with_password()

    def add_watch_only_account(self, address: str, account_name: str):
        self.add_account_button.click()
        self.add_account_to_watch.click()
        self.address_to_watch_input.send_keys(address)
        self.account_has_activity_label.wait_for_visibility_of_element()
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
