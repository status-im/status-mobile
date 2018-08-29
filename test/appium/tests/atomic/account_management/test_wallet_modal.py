import pytest

from tests import marks, transaction_users
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.wallet_modal
class TestWalletModal(SingleDeviceTestCase):

    @marks.testrail_id(3794)
    def test_wallet_modal_public_chat(self):
        user = transaction_users['A_USER']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'], user['password'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        chat = home.join_public_chat(home.get_public_chat_name())
        wallet_modal = chat.wallet_modal_button.click()
        if wallet_modal.address_text.text != '0x' + user['address']:
            self.errors.append('Wallet address is not shown in wallet modal')
        modal_usd_value = wallet_modal.get_usd_total_value()
        if modal_usd_value > usd_value * 1.001 or modal_usd_value < usd_value * 0.999:
            self.errors.append('Total value in USD is not correct in wallet modal')
        if wallet_modal.get_eth_value() != eth_value:
            self.errors.append('ETH value is not correct in wallet modal')
        if wallet_modal.get_stt_value() != stt_value:
            self.errors.append('STT value is not correct in wallet modal')
        if not wallet_modal.transaction_history_button.is_element_displayed():
            self.errors.append('Transaction history button is not visible in wallet modal')
        self.verify_no_errors()

    @marks.testrail_id(3795)
    def test_wallet_modal_dapp(self):
        user = transaction_users['B_USER']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'], user['password'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        start_new_chat.open_button.click()
        wallet_modal = start_new_chat.wallet_modal_button.click()
        if wallet_modal.address_text.text != '0x' + user['address']:
            self.errors.append('Wallet address is not shown in wallet modal')
        modal_usd_value = wallet_modal.get_usd_total_value()
        if modal_usd_value > usd_value * 1.001 or modal_usd_value < usd_value * 0.999:
            self.errors.append('Total value in USD is not correct in wallet modal')
        if wallet_modal.get_eth_value() != eth_value:
            self.errors.append('ETH value is not correct in wallet modal')
        if wallet_modal.get_stt_value() != stt_value:
            self.errors.append('STT value is not correct in wallet modal')
        if not wallet_modal.transaction_history_button.is_element_displayed():
            self.errors.append('Transaction history button is not visible in wallet modal')
        self.verify_no_errors()

    @marks.testrail_id(3797)
    def test_wallet_modal_transaction_history(self):
        user = transaction_users['B_USER']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'], user['password'])
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        start_new_chat.open_button.click()
        wallet_modal = start_new_chat.wallet_modal_button.click()
        transaction_history = wallet_modal.transaction_history_button.click()
        transaction_history.transactions_table.wait_for_visibility_of_element()
        if transaction_history.transactions_table.get_transactions_number() < 1:
            pytest.fail('Transactions history is not shown')

    @marks.testrail_id(3800)
    def test_close_and_open_wallet_modal(self):
        user = transaction_users['B_USER']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'], user['password'])
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        start_new_chat.open_button.click()
        web_view = start_new_chat.get_base_web_view()
        wallet_modal = start_new_chat.wallet_modal_button.click()
        if not wallet_modal.usd_total_value.is_element_displayed():
            pytest.fail("Wallet modal doesn't contain balance")
        transaction_history = wallet_modal.transaction_history_button.click()
        if transaction_history.transactions_table.get_transactions_number() < 1:
            pytest.fail('Transactions history is not shown')
        transaction_history.cross_icon.click()
        if not web_view.browser_previous_page_button.is_element_displayed():
            pytest.fail('Modal wallet was not closed')
        web_view.cross_icon.click()
        chat = home.join_public_chat(home.get_public_chat_name())
        chat.wallet_modal_button.click()
        if not wallet_modal.usd_total_value.is_element_displayed():
            pytest.fail("Wallet modal was not opened")
