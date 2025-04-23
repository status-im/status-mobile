import re

import pytest
from _pytest.outcomes import Failed
from selenium.common import NoSuchElementException, TimeoutException

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests import marks
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="three_1")
@marks.nightly
@marks.smoke
class TestWalletOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_2']
        self.total_balance = {'Ether': 0.0362, 'Status': 15.0, 'Uniswap': 0.7, 'Dai Stablecoin': 0.0}
        self.mainnet_balance = {'Ether': 0.015,  'Status': 10.0, 'Uniswap': 0.2,
                                'Dai Stablecoin': 0.0}
        self.optimism_balance = {'Ether': 0.0011,  'Status': 5.0, 'Uniswap': 0, 'Dai Stablecoin': 0.0}
        self.arb_balance = {'Ether': 0.0051,'Status': 0.0, 'Uniswap': 0.5, 'Dai Stablecoin': 0.0}
        self.base_balance = {'Ether': 0.015, 'Status': 0.0, 'Uniswap': 0.0, 'Dai Stablecoin': 0.0}
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.sign_in_view.recover_access(passphrase=self.sender['passphrase'])

        self.home_view = self.sign_in_view.get_home_view()
        self.sender_username = self.home_view.get_username()
        self.profile_view = self.home_view.profile_button.click()
        self.profile_view.switch_network()
        self.sign_in_view.sign_in(user_name=self.sender_username)
        self.wallet_view = self.home_view.wallet_tab.click()
        self.account_name = 'Account 1'

    @marks.testrail_id(740490) #TODO: add USDc check back when token collision USDc and USDCN is resolved
    def test_wallet_balance_mainnet(self):
        self.wallet_view.just_fyi("Checking total balance")
        real_balance = {}
        for asset in self.total_balance:
            real_balance[asset] = self.wallet_view.get_asset(asset).get_amount()

        for asset in self.total_balance:
            if real_balance[asset] != self.total_balance[asset]:
                self.errors.append(self.wallet_view, "For the %s the wrong value %s is shown, expected %s in total" %
                                   (asset, real_balance[asset], self.total_balance[asset]))
        expected_balances = {
            'Ethereum': self.mainnet_balance,
            'Arbitrum': self.arb_balance,
            'Optimism': self.optimism_balance,
            'Base, NEW': self.base_balance
        }

        for network in expected_balances:
            self.wallet_view.just_fyi("Checking total balance on %s network" % network)
            self.wallet_view.set_network_in_wallet(network)
            real_balance = {}
            for asset in expected_balances[network]:
                real_balance[asset] = self.wallet_view.get_asset(asset).get_amount()
            for asset in expected_balances[network]:
                if real_balance[asset] != expected_balances[network][asset]:
                    self.errors.append(self.wallet_view, "For the %s the wrong value %s is shown, expected %s on %s" %
                                       (asset, real_balance[asset], expected_balances[network][asset], network))
            self.wallet_view.set_network_in_wallet(network)

        self.errors.verify_no_errors()

    @marks.testrail_id(741554)
    def test_wallet_send_flow_mainnet(self):
        self.wallet_view.get_account_element().click()
        self.wallet_view.send_button.click()
        self.wallet_view.address_text_input.send_keys(self.receiver['wallet_address'])
        self.wallet_view.continue_button.click()

        asset_data = {
            'Ether': {'networks': ['Ethereum', 'Arbitrum'], 'amount': '0.00001'},
            'Status': {'networks': ['Ethereum', 'Optimism'], 'amount': '1'}
        }
        for asset, data in asset_data.items():
            for network in data['networks']:
                self.wallet_view.just_fyi("Checking the send flow for %s on %s" % (asset, network))
                self.wallet_view.select_asset(asset)
                self.wallet_view.select_network(network)
                self.wallet_view.set_amount(data['amount'])
                try:
                    max_fees_text = self.wallet_view.get_data_item_element_text(data_item_name='Max fees')
                    if not re.findall(r"<?[$|€]\d+.\d+", max_fees_text):
                        self.errors.append(self.wallet_view,
                                           "%s on %s: max fee is not a number - %s" % (asset, network, max_fees_text))
                except TimeoutException:
                    self.errors.append(
                        self.wallet_view,
                        "%s on %s: max fees is not shown before Review Send button is clicked" % (asset, network))

                self.wallet_view.button_one.click_until_presence_of_element(self.wallet_view.slide_button_track)
                self.wallet_view.just_fyi("Checking Review Send page for %s on %s" % (asset, network))

                sender_short_address = self.sender['wallet_address'].replace(self.sender['wallet_address'][6:-3],
                                                                             '…').lower()
                receiver_short_address = self.receiver['wallet_address'].replace(self.receiver['wallet_address'][6:-3],
                                                                                 '…').lower()
                for text in [self.account_name, sender_short_address]:
                    if not self.wallet_view.from_data_container.get_child_element_by_text(text).is_element_displayed():
                        self.errors.append(
                            self.wallet_view,
                            "%s on %s: text %s is not shown in 'From' container on the Review Send page" % (
                                asset, network, text))
                if not self.wallet_view.to_data_container.get_child_element_by_text(
                        receiver_short_address).is_element_displayed():
                    self.errors.append(
                        self.wallet_view,
                        "%s on %s: text %s is not shown in 'To' container on the Review Send page" % (
                            asset, network, text))
                if not self.wallet_view.on_data_container.get_child_element_by_text(network).is_element_displayed():
                    self.errors.append(
                        self.wallet_view,
                        "%s on %s: network %s is not shown in 'On' container on the Review Send page" % (
                            asset, network, text))

                data_to_check = {
                    'Est. time': r'~\d+ sec',
                    'Max fees': r"<?[$|€]\d+.\d+",
                    'Recipient gets': "%s %s" % (data['amount'], 'ETH' if asset == 'Ether' else 'SNT')
                }
                for key, expected_value in data_to_check.items():
                    try:
                        text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                        if key == 'Max fees':
                            if not re.findall(expected_value, text):
                                self.errors.append(self.wallet_view,
                                                   "%s on %s: max fee is not a number - %s on the Review Send page" % (
                                                       asset, network, text))
                        elif key == 'Est. time':
                            if not re.findall(expected_value, text) or int(re.findall(r'\d+', text)[0]) > 60:
                                self.errors.append(
                                    self.wallet_view,
                                    "%s on %s: unexpected Est. time value - %s on the Review Send page" % (
                                        asset, network, text))
                        else:
                            if text != expected_value:
                                self.errors.append(
                                    self.wallet_view,
                                    "%s on %s: %s text %s doesn't match expected %s on the Review Send page" % (
                                        asset, network, key, text, expected_value))
                    except TimeoutException:
                        self.errors.append(self.wallet_view,
                                           "%s on %s: %s is not shown on the Review Send page" % (asset, network, key))

                self.wallet_view.slide_button_track.slide()
                if not self.wallet_view.password_input.is_element_displayed():
                    self.errors.append(self.wallet_view, "%s on %s: can't confirm transaction" % (asset, network))
                self.wallet_view.click_system_back_button_until_presence_of_element(
                    element=self.wallet_view.element_by_text('Select token'), attempts=4)
        self.errors.verify_no_errors()

    @marks.testrail_id(741555)
    def test_wallet_swap_flow_mainnet(self):
        self.sign_in_view.reopen_app(user_name=self.sender_username)
        self.home_view.wallet_tab.click()
        self.wallet_view.get_account_element().wait_for_rendering_ended_and_click()
        self.wallet_view.swap_button.wait_for_rendering_ended_and_click()
        for network in ['Ethereum', 'Optimism']:
            self.wallet_view.just_fyi("Checking the Swap flow for SNT on %s" % network)
            self.wallet_view.select_asset('Status')
            self.wallet_view.select_network(network)
            self.wallet_view.set_amount('1')
            data_to_check = {
                'Max fees': r"<?[$|€]\d+.\d+",
                'Max slippage': r"\d+.\d+[%]"
            }
            for key, expected_value in data_to_check.items():
                try:
                    text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                    if not re.findall(expected_value, text):
                        self.errors.append(self.wallet_view,
                                           "%s: %s is not a number - %s before pressing Review Swap button" % (
                                               network, key, text))
                except TimeoutException:
                    self.errors.append(self.wallet_view,
                                       "%s: %s is not shown before pressing Review Swap button" % (network, key))

            try:
                self.wallet_view.wait_for_swap_input_to_be_shown()
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s: ETH amount is not displayed")
                continue
            self.wallet_view.approve_swap_button.click()
            self.wallet_view.just_fyi("Checking 'Set Spending Cap' screen for %s" % network)
            if not self.wallet_view.spending_cap_approval_info_container.get_child_element_by_text(
                    '1 SNT').is_element_displayed():
                self.errors.append(self.wallet_view,
                                   "%s: Spending cap is not shown on the 'Set Spending Cap' screen" % network)
            for text in [self.account_name,
                         self.sender['wallet_address'].replace(self.sender['wallet_address'][5:-3], '...').lower()]:
                if not self.wallet_view.account_approval_info_container.get_child_element_by_text(
                        text).is_element_displayed():
                    self.errors.append(
                        self.wallet_view,
                        "%s: Text %s is not shown for the account on the 'Set Spending Cap' screen" % (network, text))
            if not self.wallet_view.token_approval_info_container.get_child_element_by_text(
                    'SNT').is_element_displayed():
                self.errors.append(self.wallet_view,
                                   "%s: Token is not shown on the 'Set Spending Cap' screen" % network)
            if not self.wallet_view.spender_contract_approval_info_container.is_element_displayed():
                self.errors.append(self.wallet_view,
                                   "%s: Spender contract info is missing on the 'Set Spending Cap' screen" % network)

            data_to_check = {
                'Network': network,
                'Max fees': r"<?[$|€]\d+.\d+",
            }
            for key, expected_value in data_to_check.items():
                try:
                    text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                    if key == 'Max fees':
                        if not re.findall(expected_value, text):
                            self.errors.append(self.wallet_view,
                                               "%s: max fee is not a number - %s on the 'Set Spending Cap' screen" % (
                                                   network, text))
                    else:
                        if text != expected_value:
                            self.errors.append(
                                self.wallet_view,
                                "%s: %s text %s doesn't match expected %s on the 'Set Spending Cap' screen" % (
                                    network, key, text, expected_value))
                except (TimeoutException, NoSuchElementException):
                    self.errors.append(self.wallet_view,
                                       "%s: %s is not shown on the 'Set Spending Cap' screen" % (network, key))
            try:
                self.wallet_view.slide_button_track.slide()
                if not self.wallet_view.password_input.is_element_displayed():
                    self.errors.append(self.wallet_view, "%s: can't sign swap" % network)
            except NoSuchElementException:
                self.errors.append(self.wallet_view, "%s: can't sign swap" % network)

            self.wallet_view.click_system_back_button_until_presence_of_element(
                element=self.wallet_view.element_by_text('Select asset to pay'), attempts=4)
        self.errors.verify_no_errors()

    @marks.testrail_id(741612)
    def test_wallet_bridge_flow_mainnet(self):
        self.sign_in_view.reopen_app(user_name=self.sender_username)
        self.home_view.wallet_tab.click()
        self.wallet_view.get_account_element().wait_for_rendering_ended_and_click()
        self.wallet_view.bridge_button.wait_for_rendering_ended_and_click()
        networks = {'Optimism': 'Arbitrum', 'Arbitrum': 'Base', 'Base': 'Optimism'}
        amount = '0.001'
        for network_from, network_to in networks.items():
            self.wallet_view.just_fyi("Checking bridge from %s to %s" % (network_from, network_to))
            self.wallet_view.select_asset('Ether')
            self.wallet_view.select_network(network_from)
            self.wallet_view.select_network(network_to)
            self.wallet_view.set_amount(amount)
            data_to_check = {
                'Max fees': r"<?[$|€]\d+.\d+",
                'Bridged to %s' % network_to: r"0.000\d+ ETH"
            }
            for key, expected_value in data_to_check.items():
                try:
                    text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                    if not re.findall(expected_value, text):
                        self.errors.append(self.wallet_view,
                                           "%s to %s: %s is not a number - %s before pressing Review Bridge button" % (
                                               network_from, network_to, key, text))
                except TimeoutException:
                    self.errors.append(self.wallet_view,
                                       "%s to %s: %s is not shown before pressing Review Bridge button" % (
                                           network_from, network_to, key))
            self.wallet_view.just_fyi("Checking routes from %s to %s" % (network_from, network_to))
            try:
                element = self.wallet_view.get_route_element('from')
                element.wait_for_element()
                shown_amount = element.amount_text
                if shown_amount != amount + ' ETH':
                    self.errors.append(self.wallet_view, "%s to %s: 'From' route amount %s doesn't match expected %s" %
                                       (network_from, network_to, shown_amount, amount + ' ETH'))
                shown_network = element.network_text
                if shown_network != network_from:
                    self.errors.append(self.wallet_view, "%s to %s: 'From' route network %s doesn't match expected %s" %
                                       (network_from, network_to, shown_network, network_from))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s to %s: 'From' route is not shown" % (network_from, network_to))

            try:
                element = self.wallet_view.get_route_element('to')
                element.wait_for_element()
                shown_amount = element.amount_text
                if not re.findall(r"0.000\d+ ETH", shown_amount):
                    self.errors.append(self.wallet_view, "%s to %s: 'To' route amount %s is not a number" %
                                       (network_from, network_to, shown_amount))
                shown_network = element.network_text
                if shown_network != network_to:
                    self.errors.append(self.wallet_view, "%s to %s: 'To' route network %s doesn't match expected %s" %
                                       (network_from, network_to, shown_network, network_to))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s to %s: 'To' route is not shown" % (network_from, network_to))

            self.wallet_view.button_one.click()
            self.wallet_view.just_fyi("Checking Bridge screen from %s to %s" % (network_from, network_to))
            containers = {'from': self.wallet_view.from_data_container, 'to': self.wallet_view.to_data_container}
            for name, container in containers.items():
                try:
                    container.wait_for_element()
                    for text in [self.account_name,
                                 self.sender['wallet_address'].replace(self.sender['wallet_address'][6:-3],
                                                                       '…').lower()]:
                        if not container.get_child_element_by_text(text).is_element_displayed():
                            self.errors.append(
                                self.wallet_view,
                                "%s to %s: Text %s is not shown in the '%s' data container on the Review Bridge screen"
                                % (network_from, network_to, text, name))
                except TimeoutException:
                    self.errors.append(self.wallet_view, "%s to %s: data '%s' is not shown in Review Bridge screen" %
                                       (network_from, network_to, name))
            if network_to == 'Arbitrum':
                network_to_short_name = 'Arb1.'
            elif network_to == 'Optimism':
                network_to_short_name = 'Oeth.'
            else:
                network_to_short_name = network_to
            data_to_check = {
                'Est. time': r'~\d+ sec',
                'Max fees': r"<?[$|€]\d+.\d+",
                'Bridged to %s' % network_to_short_name: r"0.000\d+ ETH"
            }
            for key, expected_value in data_to_check.items():
                try:
                    text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                    if key == 'Est. time':
                        exp_condition = re.findall(expected_value, text) and int(re.findall(r'\d+', text)[0]) < 60
                    else:
                        exp_condition = re.findall(expected_value, text)
                    if not exp_condition:
                        self.errors.append(self.wallet_view,
                                           "%s to %s: %s has incorrect value - %s on the Review Bridge screen" % (
                                               network_from, network_to, key, text))
                except TimeoutException:
                    self.errors.append(self.wallet_view,
                                       "%s to %s: %s is not shown on the Review Bridge screen" % (
                                           network_from, network_to, key))
            self.wallet_view.slide_button_track.slide()
            if not self.wallet_view.password_input.is_element_displayed():
                self.errors.append(self.wallet_view, "%s to %s: can't confirm bridge" % (network_from, network_to))
            self.wallet_view.click_system_back_button(times=5)
        self.errors.verify_no_errors()



@pytest.mark.xdist_group(name="one_1")
@marks.nightly
@marks.smoke
class TestWalletOneDeviceTwo(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_5'], transaction_senders['ETH_2']

        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.sign_in_view.recover_access(passphrase=self.sender['passphrase'])

        self.home_view = self.sign_in_view.get_home_view()
        self.wallet_view = self.home_view.wallet_tab.click()
        self.account_name = 'Account 1'

    @marks.testrail_id(727231)
    def test_wallet_add_remove_regular_account(self):
        self.wallet_view.just_fyi("Adding new regular account")
        new_account_name = "New Account"
        self.wallet_view.add_regular_account(account_name=new_account_name)

        if self.wallet_view.account_name_text.text != new_account_name:
            pytest.fail("New account is not created")
        new_wallet_address = self.wallet_view.get_account_address()
        self.wallet_view.close_account_button.click_until_presence_of_element(self.home_view.show_qr_code_button)

        self.wallet_view.just_fyi("Checking that the new wallet is added to the Share QR Code menu")
        self.home_view.show_qr_code_button.click()
        self.home_view.share_wallet_tab_button.click()
        if self.home_view.account_name_text.text != self.account_name:
            self.errors.append(self.home_view, "Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append(self.home_view, "Can't swipe between accounts, newly added account is not shown")
        else:
            shown_address = self.home_view.copy_wallet_address()
            if set(shown_address.split(':')) != set(new_wallet_address.split(':')):
                self.errors.append(
                    self.home_view,
                    "Incorrect address '%s' is shown when swiping between accounts, expected one is '%s'" % (
                        shown_address, new_wallet_address))
        self.home_view.click_system_back_button()

        self.wallet_view.just_fyi("Removing newly added account")
        if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
            self.wallet_view.remove_account(account_name=new_account_name)
            if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
                self.errors.append(self.wallet_view, "Account was not removed from wallet")
        else:
            self.errors.append(self.wallet_view, "Newly added account is not shown in the accounts list")

        self.errors.verify_no_errors()

    @marks.testrail_id(727232)
    @marks.skip("The feature is disabled in https://github.com/status-im/status-mobile/pull/20955")
    @marks.xfail(reason="Missing networks in account address, https://github.com/status-im/status-mobile/issues/20166")
    def test_wallet_add_remove_watch_only_account(self):
        self.wallet_view.just_fyi("Adding new watch only account")
        new_account_name = "Account to watch"
        address_to_watch = "0x8d2413447ff297d30bdc475f6d5cb00254685aae"
        self.wallet_view.navigate_back_to_wallet_view()
        self.wallet_view.add_watch_only_account(address=address_to_watch, account_name=new_account_name)

        if self.wallet_view.account_name_text.text != new_account_name:
            pytest.fail("Account to watch was not added")
        self.wallet_view.close_account_button.click_until_presence_of_element(self.home_view.show_qr_code_button)

        self.wallet_view.just_fyi("Checking that the new wallet is added to the Share QR Code menu")
        self.home_view.show_qr_code_button.click()
        self.home_view.share_wallet_tab_button.click()
        if self.home_view.account_name_text.text != self.account_name:
            self.errors.append(self.home_view, "Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append(self.home_view, "Can't swipe between accounts, account to watch is not shown")
        else:
            shown_address = self.home_view.copy_wallet_address()
            if set(shown_address.split(':')) != {'eth', 'arb1', 'oeth', address_to_watch}:
                self.home_view.driver.fail(
                    "Incorrect address '%s' is shown when swiping between accounts, expected one is '%s'" % (
                        shown_address, address_to_watch))
        self.home_view.click_system_back_button()

        self.wallet_view.just_fyi("Removing account to watch")
        if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
            self.wallet_view.remove_account(account_name=new_account_name, watch_only=True)
            if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
                self.errors.append(self.wallet_view, "Account was not removed from wallet")
        else:
            self.errors.append(self.wallet_view, "Watch only account is not shown in the accounts list")

        self.errors.verify_no_errors()