import datetime
import time

import pytest
from _pytest.outcomes import Failed
from selenium.common import TimeoutException, NoSuchElementException
from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from support.api.network_api import NetworkApi
from tests import marks, run_in_parallel
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_four_2")
@marks.nightly
@marks.secured
@marks.smoke
class TestWalletMultipleDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(2)
        self.sign_in_1, self.sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_2']
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.loop.run_until_complete(
            run_in_parallel(((self.sign_in_1.recover_access, {'passphrase': self.sender['passphrase']}),
                             (self.sign_in_2.recover_access, {'passphrase': self.receiver['passphrase']}))))
        self.home_1, self.home_2 = self.sign_in_1.get_home_view(), self.sign_in_2.get_home_view()
        self.sender_username, self.receiver_username = self.home_1.get_username(), self.home_2.get_username()
        self.wallet_1, self.wallet_2 = self.sign_in_1.get_wallet_view(), self.sign_in_2.get_wallet_view()
        self.wallet_1.wallet_tab.click()
        self.wallet_2.wallet_tab.click()

    def _get_balances_before_tx(self):
        sender_balance = self.network_api.get_balance(self.sender['wallet_address'])
        receiver_balance = self.network_api.get_balance(self.receiver['wallet_address'])
        self.wallet_1.just_fyi("Getting ETH amount in the wallet of the sender before transaction")
        self.wallet_1.get_account_element().click()
        eth_amount_sender = self.wallet_1.get_asset(asset_name='Ether').get_amount()
        self.wallet_2.just_fyi("Getting ETH amount in the wallet of the receiver before transaction")
        self.wallet_2.get_account_element().click()
        eth_amount_receiver = self.wallet_2.get_asset(asset_name='Ether').get_amount()
        return sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver

    def _check_balances_after_tx(self, amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                 eth_amount_receiver):
        try:
            self.network_api.wait_for_balance_to_be(address=self.sender['wallet_address'],
                                                    expected_balance=sender_balance - amount_to_send)
        except TimeoutException as e:
            self.errors.append("Sender " + e.msg)
        try:
            self.network_api.wait_for_balance_to_be(address=self.receiver['wallet_address'],
                                                    expected_balance=receiver_balance + amount_to_send)
        except TimeoutException as e:
            self.errors.append("Receiver " + e.msg)

        def wait_for_wallet_balance_to_update(wallet_view, user_name, initial_eth_amount):
            wallet_view.just_fyi("Getting ETH amount in the wallet of the %s after transaction" % user_name)
            if user_name == self.sender_username:
                exp_amount = round(initial_eth_amount - amount_to_send, 4)
            else:
                exp_amount = round(initial_eth_amount + amount_to_send, 4)

            # for _ in range(12):  # ToDo: 120 sec wait time, enable when autoupdate feature is ready
            new_eth_amount = round(wallet_view.get_asset(asset_name='Ether').get_amount(), 4)
            if user_name == self.sender_username and new_eth_amount <= exp_amount:
                return
            if user_name == self.receiver_username and new_eth_amount >= exp_amount:
                return
            self.errors.append(
                "Eth amount in the %s's wallet is %s but should be %s" % (user_name, new_eth_amount, exp_amount))

        # ToDo: disable relogin when autoupdate feature is ready
        self.home_1.just_fyi("Relogin for getting an updated balance")
        self.home_2.just_fyi("Relogin for getting an updated balance")
        for _ in range(6):  # just waiting 1 minute here to be sure that balances are updated
            self.wallet_1.wallet_tab.is_element_displayed()
            self.wallet_2.wallet_tab.is_element_displayed()
            time.sleep(10)
        self.loop.run_until_complete(
            run_in_parallel(((self.home_1.reopen_app, {'user_name': self.sender_username}),
                             (self.home_2.reopen_app, {'user_name': self.receiver_username}))))
        self.wallet_1.wallet_tab.wait_and_click()
        self.wallet_2.wallet_tab.wait_and_click()
        self.wallet_1.select_network(network_name='Arbitrum')
        self.wallet_2.select_network(network_name='Arbitrum')
        self.loop.run_until_complete(
            run_in_parallel(((wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_1,
                                                                  'user_name': self.sender_username,
                                                                  'initial_eth_amount': eth_amount_sender}),
                             (wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_2,
                                                                  'user_name': self.receiver_username,
                                                                  'initial_eth_amount': eth_amount_receiver}))))

    def _check_last_transaction_in_activity(self, wallet_view, device_time, amount_to_send, sender=True):
        wallet_view.get_account_element().click()
        wallet_view.activity_tab.click()
        wallet_view.just_fyi("Checking the transaction in the activity tab")
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_time = "Today %s" % current_time.strftime('%-I:%M %p')
        possible_times = [expected_time,
                          "Today %s" % (current_time + datetime.timedelta(minutes=1)).strftime('%-I:%M %p')]
        sender_address_short = self.sender['wallet_address'].replace(self.sender['wallet_address'][5:-3], '...').lower()
        receiver_address_short = self.receiver['wallet_address'].replace(self.receiver['wallet_address'][5:-3],
                                                                         '...').lower()
        activity_element = wallet_view.get_activity_element()
        try:
            if not all((activity_element.header == 'Send' if sender else 'Receive',
                        activity_element.timestamp in possible_times,
                        activity_element.amount == '%s ETH' % amount_to_send,
                        activity_element.from_text == sender_address_short,
                        activity_element.to_text == receiver_address_short)):
                self.errors.append(
                    "The last transaction is not listed in activity for the %s, expected timestamp is %s" %
                    ('sender' if sender else 'receiver', expected_time))
        except NoSuchElementException:
            self.errors.append("Can't find the last transaction for the %s" % ('sender' if sender else 'receiver'))
        finally:
            wallet_view.close_account_button.click_until_presence_of_element(wallet_view.show_qr_code_button)

    @marks.testrail_id(727229)
    def test_wallet_send_eth(self):
        self.wallet_1.select_network(network_name='Arbitrum')
        self.wallet_2.select_network(network_name='Arbitrum')
        sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()

        self.wallet_2.close_account_button.click()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending funds from wallet")
        amount_to_send = 0.0001
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.send_asset(address='arb1:' + self.receiver['wallet_address'],
                                 asset_name='Ether',
                                 amount=amount_to_send)
        self.network_api.wait_for_confirmation_of_transaction(address=self.sender['wallet_address'],
                                                              tx_time=device_time_before_sending)

        device_time_after_sending = self.wallet_1.driver.device_time

        self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                      eth_amount_receiver)

        # ToDo: enable when issues 20807 and 20808 are fixed
        # self.loop.run_until_complete(
        #     run_in_parallel(((self._check_last_transaction_in_activity, {'wallet_view': self.wallet_1,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send}),
        #                      (self._check_last_transaction_in_activity, {'wallet_view': self.wallet_2,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send,
        #                                                                  'sender': False}))))
        self.errors.verify_no_errors()

    @marks.testrail_id(727230)
    def test_wallet_send_asset_from_drawer(self):
        self.wallet_1.navigate_back_to_wallet_view()
        sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()
        self.wallet_2.close_account_button.click_if_shown()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending asset from drawer")
        amount_to_send = 0.0001
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.send_asset_from_drawer(address='arb1:' + self.receiver['wallet_address'],
                                             asset_name='Ether',
                                             amount=amount_to_send)
        self.network_api.wait_for_confirmation_of_transaction(address=self.sender['wallet_address'],
                                                              tx_time=device_time_before_sending)
        device_time_after_sending = self.wallet_1.driver.device_time

        self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                      eth_amount_receiver)

        # ToDo: enable when issues 20807 and 20808 are fixed
        # self.loop.run_until_complete(
        #     run_in_parallel(((self._check_last_transaction_in_activity, {'wallet_view': self.wallet_1,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send}),
        #                      (self._check_last_transaction_in_activity, {'wallet_view': self.wallet_2,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send,
        #                                                                  'sender': False}))))
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_one_2")
@marks.nightly
@marks.secured
@marks.smoke
class TestWalletOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_2']
        self.total_balance = {'Ether': 0.0052, 'USDCoin': 5.0, 'Status': 10.0, 'Uniswap': 0.627, 'Dai Stablecoin': 0.0}
        self.mainnet_balance = {'Ether': 0.005, 'USDCoin': 0.0, 'Status': 10.0, 'Uniswap': 0.127, 'Dai Stablecoin': 0.0}
        self.optimizm_balance = {'Ether': 0.0001, 'USDCoin': 5.0, 'Status': 0, 'Uniswap': 0, 'Dai Stablecoin': 0.0}
        self.arb_balance = {'Ether': 0.0001, 'USDCoin': 0.0, 'Status': 0.0, 'Uniswap': 0.5, 'Dai Stablecoin': 0.0}
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.sign_in_view.recover_access(passphrase=self.sender['passphrase'])

        self.home_view = self.sign_in_view.get_home_view()
        self.sender_username = self.home_view.get_username()
        self.wallet_view = self.home_view.wallet_tab.click()

    @marks.testrail_id(740490)
    def test_wallet_balance_mainnet(self):
        self.profile_view = self.home_view.profile_button.click()
        self.profile_view.switch_network()
        self.sign_in_view.sign_in(user_name=self.sender_username)
        self.sign_in_view.wallet_tab.click()

        self.wallet_view.just_fyi("Checking total balance")
        real_balance = {}
        for asset in self.total_balance:
            real_balance[asset] = self.wallet_view.get_asset(asset).get_amount()

        for asset in self.total_balance:
            if real_balance[asset] != self.total_balance[asset]:
                self.errors.append("For the %s the wrong value %s is shown, expected %s in total" %
                                   (asset, real_balance[asset], self.total_balance[asset]))
        expected_balances = {
            'Mainnet': self.mainnet_balance,
            'Arbitrum': self.arb_balance,
            'Optimism': self.optimizm_balance
        }

        for network in expected_balances:
            self.wallet_view.just_fyi("Checking total balance on %s network" % network)
            self.wallet_view.select_network(network)
            real_balance = {}
            for asset in expected_balances[network]:
                real_balance[asset] = self.wallet_view.get_asset(asset).get_amount()
            for asset in expected_balances[network]:
                if real_balance[asset] != expected_balances[network][asset]:
                    self.errors.append("For the %s the wrong value %s is shown, expected %s on %s" %
                                       (asset, real_balance[asset], expected_balances[network][asset], network))
            self.wallet_view.select_network(network)

        self.errors.verify_no_errors()

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
        if self.home_view.account_name_text.text != 'Account 1':
            self.errors.append("Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append("Can't swipe between accounts, newly added account is not shown")
        else:
            shown_address = self.home_view.copy_wallet_address()
            if set(shown_address.split(':')) != set(new_wallet_address.split(':')):
                self.errors.append(
                    "Incorrect address '%s' is shown when swiping between accounts, expected one is '%s'" % (
                        shown_address, new_wallet_address))
        self.home_view.click_system_back_button()

        self.wallet_view.just_fyi("Removing newly added account")
        if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
            self.wallet_view.remove_account(account_name=new_account_name)
            if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
                self.errors.append("Account was not removed from wallet")
        else:
            self.errors.append("Newly added account is not shown in the accounts list")

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
        if self.home_view.account_name_text.text != 'Account 1':
            self.errors.append("Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append("Can't swipe between accounts, account to watch is not shown")
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
                self.errors.append("Account was not removed from wallet")
        else:
            self.errors.append("Watch only account is not shown in the accounts list")

        self.errors.verify_no_errors()
