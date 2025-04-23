import pytest

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.wallet_view import WalletView
from tests import marks
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="three_1")
@marks.nightly
class TestWalletOneDeviceThree(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_7'], transaction_senders['ETH_2']
        self.sign_in.recover_access(passphrase=self.sender['passphrase'])

        self.home_view = self.sign_in.get_home_view()
        self.sender_username = self.home_view.get_username()
        self.profile = self.home_view.profile_button.click()
        self.profile.switch_network()
        self.home_view.navigate_back_to_home_view()
        self.wallet = self.home_view.wallet_tab.click()
        self.account_name = 'Account 1'

    @marks.testrail_id(742063)
    def test_wallet_swap_dai_snt_real_tx(self):
        self.wallet = WalletView(self.drivers[0])
        network_name = 'Arbitrum'
        amount, asset_name_from, from_ticker = 0.01, 'Dai Stablecoin', 'DAI'
        asset_name_to, to_ticker, decimals_to = 'Status', 'SNT', 18

        fiat_initial_amount_from = self.wallet.get_balance(asset_name_from, fiat=True)
        fiat_initial_amount_to = self.wallet.get_balance(asset_name_to, fiat=True)
        fiat_expected_amount_from = self.wallet.round_amount_float(fiat_initial_amount_from - amount, 2)
        fiat_expected_amount_to = self.wallet.round_amount_float(fiat_initial_amount_to + amount, 2)

        self.home_view.wallet_tab.click()
        self.wallet.get_account_element().wait_for_rendering_ended_and_click()
        self.swap_data = self.wallet.swap_asset_from_drawer(asset_name=asset_name_from,
                                                            amount=amount,
                                                            network_name=network_name,
                                                            decimals_to=decimals_to,
                                                            asset_to=asset_name_to)
        for key, value in self.swap_data.items():
            self.wallet.just_fyi(f"Param tx {key} from review page: {value}")

        self.wallet.just_fyi("Check swapping toast")
        swapping_toast = f"Swapping {amount} {from_ticker} to "

        if not self.wallet.element_by_text_part(swapping_toast).is_element_displayed(10):
            self.errors.append(self.wallet, f"Can't find {swapping_toast} toast")

        self.wallet.just_fyi("Verify swap tx in the list")
        amount_in_tx = str(self.wallet.round_amount_float(self.swap_data['receive_amount'], 6))
        tx_errors = self.wallet.check_last_transaction_in_activity(self.wallet.driver.device_time,
                                                                   amount,
                                                                   tx_type='Swap',
                                                                   asset=from_ticker,
                                                                   from_account_name=self.account_name,
                                                                   swap_amount_to=amount_in_tx,
                                                                   swap_asset_to=to_ticker,
                                                                   network=network_name)
        self.errors.append(self.wallet, tx_errors)

        self.wallet.just_fyi("Check that balance is updated for receiver")
        self.wallet.wait_for_wallet_balance_to_update(fiat_expected_amount_from, asset_name_from, fiat=True)
        self.wallet.wait_for_wallet_balance_to_update(fiat_expected_amount_to, asset_name_to, fiat=True)

        self.errors.verify_no_errors()
