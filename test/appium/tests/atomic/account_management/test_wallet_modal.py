import pytest

from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import wallet_users, basic_user
from views.sign_in_view import SignInView


@marks.wallet_modal
class TestWalletModal(SingleDeviceTestCase):

    @marks.testrail_id(5398)
    @marks.high
    def test_wallet_modal_public_chat(self):
        user = wallet_users['A']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        chat = home.join_public_chat(home.get_public_chat_name())
        chat.chat_options.click()
        wallet_modal = chat.wallet_modal_button.click()
        if wallet_modal.address_text.text != '0x' + user['address']:
            self.errors.append('Wallet address is not shown in wallet modal')
        modal_usd_value = wallet_modal.get_usd_total_value()
        if modal_usd_value > usd_value * 1.005 or modal_usd_value < usd_value * 0.995:
            self.errors.append('Total value in USD is not correct in wallet modal')
        if wallet_modal.get_eth_value() != eth_value:
            self.errors.append('ETH value is not correct in wallet modal')
        if wallet_modal.get_stt_value() != stt_value:
            self.errors.append('STT value is not correct in wallet modal')
        if not wallet_modal.transaction_history_button.is_element_displayed():
            self.errors.append('Transaction history button is not visible in wallet modal')
        self.verify_no_errors()

    @marks.testrail_id(5399)
    @marks.high
    def test_wallet_modal_dapp(self):
        user = wallet_users['B']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        webview = start_new_chat.open_button.click()
        webview.web_view_menu_button.click()
        wallet_modal = webview.wallet_modal_button.click()
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

    @marks.testrail_id(5692)
    @marks.high
    def test_wallet_modal_1_1_chat(self):
        user = wallet_users['C']
        recipient_address = basic_user['public_key']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        chat = home.add_contact(recipient_address)
        chat.chat_options.click()
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

    @marks.testrail_id(5693)
    @marks.high
    def test_wallet_modal_group_chat(self):
        user = wallet_users['D']
        recipient_address = basic_user['public_key']
        recipient_name = basic_user['username']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(user['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        usd_value = wallet.get_usd_total_value()
        eth_value = wallet.get_eth_value()
        stt_value = wallet.get_stt_value()
        home = wallet.home_button.click()
        private_chat = home.add_contact(recipient_address)
        home = private_chat.get_back_to_home_view()
        group_chat = home.create_group_chat([recipient_name], 'wallet-modal')
        group_chat.chat_options.click()
        wallet_modal = group_chat.wallet_modal_button.click()
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

    @marks.testrail_id(5400)
    @marks.high
    def test_wallet_modal_transaction_history(self):
        user = wallet_users['B']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        web_view = start_new_chat.open_button.click()
        web_view.web_view_menu_button.click()
        wallet_modal = web_view.wallet_modal_button.click()
        transaction_history = wallet_modal.transaction_history_button.click()
        transaction_history.transactions_table.wait_for_visibility_of_element()
        if transaction_history.transactions_table.get_transactions_number() < 1:
            pytest.fail('Transactions history is not shown')

    @marks.testrail_id(5480)
    @marks.low
    def test_close_and_open_wallet_modal(self):
        user = wallet_users['B']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        web_view = start_new_chat.open_button.click()
        web_view.web_view_menu_button.click()
        wallet_modal = web_view.wallet_modal_button.click()
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
        chat.chat_options.click()
        chat.wallet_modal_button.click()
        if not wallet_modal.usd_total_value.is_element_displayed():
            pytest.fail("Wallet modal was not opened")

    @marks.testrail_id(5484)
    @marks.low
    def test_close_wallet_modal_via_closs_icon(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        web_view = start_new_chat.open_button.click()
        web_view.web_view_menu_button.click()
        wallet_modal = web_view.wallet_modal_button.click()
        wallet_modal.cross_icon.click()
        if not web_view.browser_previous_page_button.is_element_displayed():
            pytest.fail('Modal wallet was not closed')

    @marks.testrail_id(5483)
    @marks.low
    def test_close_wallet_modal_via_device_back_button(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        web_view = start_new_chat.open_button.click()
        web_view.web_view_menu_button.click()
        wallet_modal = web_view.wallet_modal_button.click()
        transaction_history = wallet_modal.transaction_history_button.click()
        transaction_history.click_system_back_button()
        if not web_view.web_view_menu_button.is_element_displayed():
            pytest.fail('Transaction history screen from modal wallet was not closed')
        else:
            web_view.web_view_menu_button.click()
        web_view.wallet_modal_button.click()
        wallet_modal.click_system_back_button()
        if not web_view.web_view_menu_button.is_element_displayed():
            pytest.fail('Modal wallet was not closed')

    @marks.testrail_id(5482)
    @marks.low
    def test_switch_between_main_and_history_in_wallet_modal(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.element_by_text('Airswap').click()
        web_view = start_new_chat.open_button.click()
        web_view.web_view_menu_button.click()
        wallet_modal = web_view.wallet_modal_button.click()
        transaction_history = wallet_modal.transaction_history_button.click()
        if not transaction_history.wallet_modal_switch_button.is_element_displayed():
            pytest.fail('No back to wallet button in modal transaction history')
        else:
            transaction_history.wallet_modal_switch_button.click()
        if not wallet_modal.transaction_history_button.is_element_displayed():
            pytest.fail('Failed to switch back to wallet modal main screen')
