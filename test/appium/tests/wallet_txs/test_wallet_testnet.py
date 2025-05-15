import pytest

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from support.api.network_api import NetworkApi
from tests import marks, run_in_parallel
from users import transaction_senders
from views.sign_in_view import SignInView
from support.api.network_api import SepoliaNetworkApi


@pytest.mark.xdist_group(name="four_2")
@marks.nightly
@marks.smoke
class TestWalletMultipleDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(2)
        self.sign_in_1, self.sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_3']
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.loop.run_until_complete(
            run_in_parallel(((self.sign_in_1.recover_access, {'passphrase': self.sender['passphrase']}),
                             (self.sign_in_2.recover_access, {'passphrase': self.receiver['passphrase']}))))
        self.home_1, self.home_2 = self.sign_in_1.get_home_view(), self.sign_in_2.get_home_view()
        self.sender_username, self.receiver_username = self.home_1.get_username(), self.home_2.get_username()
        self.wallets = (self.wallet_1, self.wallet_2) = self.sign_in_1.get_wallet_view(), self.sign_in_2.get_wallet_view()
        [wallet.wallet_tab.click() for wallet in self.wallets]

    @pytest.mark.parametrize(
        "network, amount",
        [
            pytest.param("Arbitrum Sepolia", 0.0001, marks=pytest.mark.testrail_id(742015)),
            pytest.param("Status Network Sepolia", 0.0002, marks=pytest.mark.testrail_id(727229)),
        ],
    )
    def test_send_eth(self, network, amount):
        asset, asset_ticker = 'Ether', 'ETH'
        [wallet.just_fyi("Test to send and verify %s ETH on %s" % (amount, network)) for wallet in self.wallets]
        eth_amount_receiver = self.wallet_2.get_balance()
        expected_amount_after_tx_receiver = self.wallet_1.round_amount_float(eth_amount_receiver + amount)

        self.wallet_1.just_fyi("Sending funds from wallet on %s" % network)
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.get_account_element().click()
        self.wallet_1.send_asset(address=self.receiver['wallet_address'],
                                 asset_name=asset,
                                 amount=self.wallet_1.round_amount_float(amount),
                                 network_name=network)

        self.wallet_1.just_fyi("Verify send tx in the list for sender")
        tx_errors = self.wallet_1.check_last_transaction_in_activity(device_time_before_sending, amount,
                                                                     send_to_account=self.receiver['wallet_address'],
                                                                     asset=asset_ticker,
                                                                     tx_type='Send',
                                                                     network=network)
        self.errors.append(self.wallet_1, tx_errors)

        self.wallet_2.just_fyi("Check that balance is updated for receiver")
        self.wallet_2.wait_for_wallet_balance_to_update(expected_amount_after_tx_receiver)

        self.errors.verify_no_errors()

    @pytest.mark.parametrize(
        "network, asset, asset_ticker, decimals, amount",
        [
            pytest.param("Sepolia", 'USD Coin', 'USDC', 2, 0.01,  marks=pytest.mark.testrail_id(742016)),
            pytest.param("Optimism Sepolia", 'USD Coin', 'USDC', 2, 0.01,  marks=pytest.mark.testrail_id(727230)),
        ],
    )
    def test_wallet_send_erc20_from_drawer(self, network, asset, asset_ticker, decimals, amount):
        [wallet.just_fyi("Test to send and verify %s %s on %s from drawer" % (amount, asset, network)) for wallet in self.wallets]

        self.wallet_1.navigate_back_to_wallet_view()
        initial_amount_sender, initial_amount_receiver  = self.wallet_1.get_balance(asset), self.wallet_2.get_balance(asset)
        expected_amount_after_tx_receiver = self.wallet_1.round_amount_float(initial_amount_receiver + amount, decimals)
        expected_amount_after_tx_sender = self.wallet_1.round_amount_float(initial_amount_sender - amount, decimals)

        self.wallet_1.just_fyi("Sending asset from drawer")
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.send_asset_from_drawer(address=self.receiver['wallet_address'],
                                             asset_name=asset,
                                             amount=self.wallet_1.round_amount_float(amount, decimals),
                                             network_name=network)

        self.wallet_1.just_fyi("Verify send tx in the list for sender")
        tx_errors = self.wallet_1.check_last_transaction_in_activity(device_time_before_sending, amount,
                                                                     send_to_account=self.receiver['wallet_address'],
                                                                     asset=asset_ticker,
                                                                     tx_type='Send',
                                                                     network=network)
        self.errors.append(self.wallet_1, tx_errors)

        self.wallet_2.just_fyi("Check that balance is updated for receiver")
        self.wallet_2.wait_for_wallet_balance_to_update(expected_amount_after_tx_receiver, asset, decimals)

        self.wallet_1.just_fyi("Check that balance is updated for sender")
        self.wallet_1.wait_for_wallet_balance_to_update(expected_amount_after_tx_sender, asset, decimals)

        self.errors.verify_no_errors()

@pytest.mark.xdist_group(name="two_1")
@marks.nightly
@marks.smoke
class TestWalletCustomParamOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop  = create_shared_drivers(1)
        self.driver = self.drivers[0]
        self.sign_in = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_ADI_STT_2'], transaction_senders['ETH_3']
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.home = self.sign_in.recover_access(self.sender['passphrase'])
        self.wallet = self.sign_in.get_wallet_view()
    
    @marks.testrail_id(742910) 
    def test_send_snt_custom_tx_params(self):
        wallet = self.wallet
        wallet.navigate_back_to_wallet_view()
        network = 'Sepolia'
        address, asset_name, amount, asset_ticker = self.receiver['wallet_address'], 'Status Test Token', '1', 'STT'
        max_base_fee, max_prio, max_gas_amount = '10', '1', '60000'

        wallet.just_fyi("Set amount and address")
        wallet.set_amount_and_address(address, asset_name, amount)
        wallet.button_one.click_until_presence_of_element(wallet.advanced_tx_button)

        wallet.just_fyi("Set custom tx params to make pending tx")
        wallet.advanced_tx_button.wait_and_click()
        wallet.custom_tx_params_button.wait_and_click()
        wallet.custom_max_base_fee_button.click()
        wallet.clear_value_and_set_with_custom_keyboard(max_base_fee)
        wallet.custom_max_prio_fee_button.wait_and_click()
        wallet.clear_value_and_set_with_custom_keyboard(max_prio)
        wallet.custom_max_gas_amount_button.wait_and_click()
        wallet.clear_value_and_set_with_custom_keyboard(max_gas_amount)
        wallet.button_one.click()

        wallet.just_fyi("Check that custom tx params are preserved")
        wallet.advanced_tx_button.wait_and_click()
        wallet.custom_tx_params_button.wait_and_click()
        for value in (max_base_fee, max_prio, max_gas_amount):
            if not wallet.get_custom_tx_element(value).is_element_displayed():
                self.errors.append(wallet, "Value %s is not preserved in custom tx settings drawer!" % value)
        wallet.button_one.click()

        wallet.just_fyi("Sign first tx (for dropping) and copy tx hash")
        wallet.slide_and_confirm_with_password()

        wallet.just_fyi("Verify send tx in the list for sender")
        device_time_before_sending = wallet.driver.device_time
        tx_errors = wallet.check_last_transaction_in_activity(device_time_before_sending, amount,
                                                                   send_to_account=self.receiver['wallet_address'],
                                                                   asset=asset_ticker,
                                                                   tx_type='Send',
                                                                   network=network, 
                                                                   navigate_to_main_screen=False)
        self.errors.append(wallet, tx_errors)
        dropped_tx_hash = wallet.copy_tx_hash()

        wallet.just_fyi("Send second tx (for replacing) with custom nonce")
        wallet.navigate_back_to_wallet_view()
        wallet.set_amount_and_address(address, asset_name, amount)
        wallet.button_one.click_until_presence_of_element(wallet.advanced_tx_button)
        wallet.advanced_tx_button.wait_and_click()
        wallet.custom_tx_params_button.wait_and_click()
        wallet.custom_nonce_button.wait_and_click()
        current_nonce = int(wallet.amount_input.text)
        wallet.clear_nonce_field()
        wallet.set_amount(str(current_nonce - 1))
        wallet.button_one.click()
        wallet.button_one.click()
        wallet.slide_and_confirm_with_password()
        replacemant_tx_hash = wallet.copy_tx_hash()
        
        if replacemant_tx_hash == dropped_tx_hash:
            wallet.just_fyi("Scenario1, nonce is too low: initial tx failed, check the status")
            if SepoliaNetworkApi().is_tx_successful(dropped_tx_hash) is not False:
                self.errors.append(wallet,
                                   "Initial tx %s is not failed, so custom nonce is not set" % dropped_tx_hash)

        else:
            wallet.just_fyi("Scenario2, Nonce is replaced: check that initial tx was dropped as custom nonce has been set")
            if SepoliaNetworkApi().is_tx_successful(dropped_tx_hash) is None:
                self.errors.append(wallet, "Dropped tx %s receipt is available, so custom nonce is not set" % dropped_tx_hash)

        self.errors.verify_no_errors()


