import re

import pytest
from selenium.common import TimeoutException

import marks
from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from users import transaction_senders
from views.sign_in_view import SignInView
from views.web_views.external_browser_view import BridgeStatusNetworkView, UniswapView


@marks.nightly
@pytest.mark.xdist_group(name="two_1")
class TestWalletConnectBaseChecks(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sign_in_view.create_user()
        self.home_view = self.sign_in_view.get_home_view()
        self.username = self.home_view.get_username()
        self.wallet_view = self.sign_in_view.wallet_tab.click()
        self.status_dapp_url = 'https://bridge.status.network'
        self.status_dapp_name = 'Status Network Bridge'
        self.browser_view = BridgeStatusNetworkView(self.drivers[0])
        self.account_1_name = 'Account 1'

    @marks.testrail_id(742897)
    def test_wallet_connect_disconnect(self):
        self.wallet_view.just_fyi(
            "Open %s in an external browser and try connecting to Status" % self.status_dapp_url)
        self.browser_view.connect_status_wallet()
        for text in self.status_dapp_name, self.status_dapp_url:
            if not self.wallet_view.element_by_text(text).is_element_displayed():
                self.errors.append(self.wallet_view,
                                   "Text '%s' is not displayed when connecting to Status wallet" % text)
        expected_data = {'Account': 'Account 1', 'Networks': 'Mainnet, Status'}
        for key, value in expected_data.items():
            try:
                data = self.wallet_view.get_data_item_element_text(data_item_name=key)
                if data != value:
                    self.errors.append(
                        self.wallet_view,
                        "%s value '%s' doesn't match expected '%s' when connecting to Status" % (key, data, value))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s data is not shown when connecting to Status" % key)
        self.wallet_view.wallet_connect_button.click()
        self.wallet_view.just_fyi("Check that %s is added to connected dApps")
        self.wallet_view.get_account_element().click()
        self.wallet_view.connected_dapps_button.click()
        dapp_element = self.wallet_view.get_connected_dapp_element_by_name(dapp_name=self.status_dapp_name)
        if dapp_element.is_element_displayed():
            if dapp_element.url_text.text != self.status_dapp_url:
                self.errors.append(self.wallet_view,
                                   "DApp url %s is not shown for the connected dApp" % self.status_dapp_url)
        else:
            self.errors.append(self.wallet_view, "%s is not shown in connected dApps" % self.status_dapp_name)

        self.wallet_view.just_fyi("Check that dApp is connected in the browser")
        status_app_package = self.drivers[0].current_package
        self.browser_view.open_browser()
        if self.browser_view.connect_wallet_button.is_element_displayed():
            self.errors.append(self.wallet_view, "DApp is not connected in the browser")

        self.wallet_view.just_fyi("Check that dApp can be disconnected")
        self.drivers[0].activate_app(status_app_package)
        if dapp_element.is_element_displayed():
            dapp_element.disconnect()
            if not self.wallet_view.element_by_translation_id('no-dapps').is_element_displayed():
                self.errors.append(self.wallet_view, "DApp was not disconnected")
        self.errors.verify_no_errors()

    @marks.testrail_id(742898)
    def test_wallet_connect_decline_and_select_account(self):
        self.wallet_view.navigate_to_wallet_view()
        self.wallet_view.just_fyi("Add new wallet account")
        new_account_name = "New Account"
        self.wallet_view.add_regular_account(account_name=new_account_name)

        self.wallet_view.just_fyi("Decline connection to Status dApp")
        self.browser_view.open_browser()
        if self.browser_view.connect_wallet_button.is_element_displayed():
            refresh = False
        else:
            refresh = True
        self.browser_view.connect_status_wallet(refresh=refresh)
        self.wallet_view.wallet_decline_button.click()
        self.browser_view.open_browser()
        self.browser_view.element_by_text('Connection declined').wait_for_element()
        self.browser_view.element_by_text('Try again').click()

        self.wallet_view.just_fyi("Connect Status dApp with selecting newly created account")
        self.wallet_view.select_account_to_connect_dapp(account_name=new_account_name)
        self.wallet_view.wallet_connect_button.click()
        self.wallet_view.navigate_to_wallet_view()
        self.wallet_view.get_account_element(account_name=new_account_name).click()
        self.wallet_view.connected_dapps_button.click()
        dapp_element = self.wallet_view.get_connected_dapp_element_by_name(dapp_name=self.status_dapp_name)
        if dapp_element.is_element_displayed():
            if dapp_element.url_text.text != self.status_dapp_url:
                self.errors.append(self.wallet_view,
                                   "DApp url %s is not shown for the connected dApp" % self.status_dapp_url)
        else:
            self.errors.append(self.wallet_view, "%s is not shown in connected dApps" % self.status_dapp_name)
        self.errors.verify_no_errors()


@marks.nightly
@pytest.mark.xdist_group(name="two_1")
class TestWalletConnectDifferentNetworks(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sign_in_view.create_user()
        self.home_view = self.sign_in_view.get_home_view()
        self.username = self.home_view.get_username()
        self.wallet_view = self.sign_in_view.wallet_tab.click()
        self.status_dapp_url = 'https://bridge.status.network'
        self.status_dapp_name = 'Status Network Bridge'
        self.browser_view = BridgeStatusNetworkView(self.drivers[0])
        self.account_1_name = 'Account 1'
        self.profile_view = self.home_view.get_profile_view()
        self.status_app_package = self.drivers[0].current_package

    @marks.testrail_id(742899)
    def test_wallet_connect_testnet_dapp(self):
        self.home_view.navigate_back_to_home_view()
        if self.home_view.testnet_mode_enabled:
            self.home_view.just_fyi("Switch to mainnet")
            self.home_view.profile_button.click()
            self.profile_view.switch_network()
            self.home_view.navigate_back_to_home_view()
        self.home_view.just_fyi("Try connecting testnet dApp when being on mainnet")
        self.browser_view.connect_status_wallet()
        self.browser_view.open_browser()
        self.browser_view.element_by_text('Connection declined').wait_for_element()
        self.browser_view.connect_wallet_button.click()

        self.drivers[0].activate_app(self.status_app_package)
        self.home_view.just_fyi("Switch to testnet")
        self.home_view.profile_button.click()
        self.profile_view.switch_network()
        self.home_view.navigate_back_to_home_view()

        self.wallet_view.just_fyi("Connect to dApp on testnet")
        self.browser_view.connect_status_wallet(refresh=False)
        self.wallet_view.wallet_connect_button.click()

        self.wallet_view.just_fyi("Switch to mainnet and check that testnet dApp is not shown")
        self.wallet_view.profile_button.click()
        self.profile_view.switch_network()
        self.wallet_view.navigate_to_wallet_view()
        self.wallet_view.get_account_element(account_name=self.account_1_name).click()
        self.wallet_view.connected_dapps_button.click()
        if not self.wallet_view.element_by_translation_id('no-dapps').is_element_displayed():
            pytest.fail("%s dApp is shown on mainnet" % self.status_dapp_name)

    @marks.testrail_id(742900)
    def test_wallet_connect_mainnet_dapp(self):
        self.home_view.navigate_back_to_home_view()
        if self.home_view.testnet_mode_enabled:
            self.home_view.just_fyi("Switch to mainnet")
            self.home_view.profile_button.click()
            self.profile_view.switch_network()
            self.home_view.navigate_back_to_home_view()
        self.wallet_view.just_fyi("Connect dApp on mainnet")
        UniswapView(self.drivers[0]).connect_status_wallet()
        self.wallet_view.wallet_connect_button.click()
        self.wallet_view.get_account_element(account_name=self.account_1_name).click()
        self.wallet_view.connected_dapps_button.click_until_presence_of_element(self.wallet_view.add_dapp_button)
        dapp_name = 'Uniswap'
        dapp_url = 'https://app.uniswap.org'
        dapp_element = self.wallet_view.get_connected_dapp_element_by_name(dapp_name=dapp_name)
        if dapp_element.is_element_displayed():
            if dapp_element.url_text.text != dapp_url:
                self.errors.append(self.wallet_view,
                                   "DApp url %s is not shown for the connected dApp on mainnet" % dapp_url)
        else:
            self.errors.append(self.wallet_view, "%s is not shown in connected dApps on mainnet" % dapp_name)

        self.wallet_view.just_fyi("Switch to testnet and check that mainnet dApp is not shown")
        self.wallet_view.navigate_back_to_home_view()
        profile_view = self.wallet_view.profile_button.click()
        profile_view.switch_network()
        self.wallet_view.navigate_to_wallet_view()
        self.wallet_view.get_account_element(account_name=self.account_1_name).click()
        self.wallet_view.connected_dapps_button.click()
        if dapp_element.is_element_displayed():
            self.errors.append(self.wallet_view, "%s dApp is shown on testnet" % dapp_name)
        self.errors.verify_no_errors()


@marks.nightly
@pytest.mark.xdist_group(name="two_1")
class TestWalletConnectSignTransactions(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.user = transaction_senders['ETH_2']
        self.user['wallet_address'] = '0x' + self.user['address']
        self.sign_in_view.recover_access(passphrase=self.user['passphrase'])
        self.wallet_view = self.sign_in_view.wallet_tab.click()
        self.status_dapp_url = 'https://bridge.status.network/'
        self.browser_view = BridgeStatusNetworkView(self.drivers[0])

    @marks.testrail_id(742901)
    def test_wallet_connect_sign_transaction(self):
        self.wallet_view.just_fyi("Connect %s dApp" % self.status_dapp_url)
        self.browser_view.connect_status_wallet()
        self.wallet_view.wallet_connect_button.click()
        self.wallet_view.just_fyi("Make bridge transaction from the connected dApp")
        self.browser_view.open_browser()
        self.browser_view.amount_input.send_keys('0.000001')
        self.browser_view.bridge_button.scroll_and_click()
        data_to_check = {
            'Network': 'Sepolia',
            'Max fees': r"<?[$|€]\d+.\d+",
            'Est. time': r'>\d+ sec'
        }
        for key, expected_value in data_to_check.items():
            try:
                text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                if key == 'Max fees':
                    if not re.findall(expected_value, text):
                        self.errors.append(self.wallet_view,
                                           "Max fee is not a number - %s on the Review Transaction page" % text)
                elif key == 'Est. time':
                    if not re.findall(expected_value, text) or int(re.findall(r'\d+', text)[0]) > 60:
                        self.errors.append(
                            self.wallet_view, "Unexpected Est. time value - %s on the Review Transaction page" % text)
                else:
                    if text != expected_value:
                        self.errors.append(
                            self.wallet_view,
                            "%s text %s doesn't match expected %s on the Review Transaction page" % (
                                key, text, expected_value))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s is not shown on the Review Transaction page" % key)
        self.wallet_view.slide_and_confirm_with_password()
        self.browser_view.open_browser()
        if not self.browser_view.element_by_text('Transaction confirmed!').is_element_displayed(60):
            self.errors.append(self.wallet_view, "Transaction was not confirmed")
        self.errors.verify_no_errors()
