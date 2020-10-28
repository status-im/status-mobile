import pytest
from tests import marks, test_dapp_url, test_dapp_name
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


class TestDApps(SingleDeviceTestCase):

    @marks.testrail_id(5353)
    @marks.critical
    def test_filters_from_daap(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        status_test_dapp.test_filters_button.click()
        for element in status_test_dapp.element_by_text('eth_uninstallFilter'), status_test_dapp.ok_button:
            if element.is_element_displayed(10):
                self.driver.fail("'Test filters' button produced an error")

    @marks.testrail_id(5397)
    @marks.high
    def test_request_public_key_status_test_daap(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(passphrase=user['passphrase'])
        status_test_dapp = home_view.open_status_test_dapp(allow_all=False)
        status_test_dapp.status_api_button.click_until_presence_of_element(status_test_dapp.request_contact_code_button)
        status_test_dapp.request_contact_code_button.click_until_presence_of_element(status_test_dapp.deny_button)
        status_test_dapp.deny_button.click()
        if status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            self.driver.fail('Public key is returned but access was not allowed')
        status_test_dapp.request_contact_code_button.click_until_presence_of_element(status_test_dapp.deny_button)
        status_test_dapp.allow_button.click()
        if not status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            self.driver.fail('Public key is not returned')

    @marks.testrail_id(6323)
    @marks.medium
    def test_resolve_ipns_name(self):
        user = basic_user
        ipns_url = 'uniswap.eth'
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(passphrase=user['passphrase'])
        profile_view = home_view.profile_button.click()
        profile_view.switch_network()
        self.driver.set_clipboard_text(ipns_url)
        dapp_view = home_view.dapp_tab_button.click()
        dapp_view.enter_url_editbox.click()
        dapp_view.paste_text()
        dapp_view.confirm()
        if not dapp_view.allow_button.is_element_displayed(30):
            self.driver.fail('No permission is asked for dapp, so IPNS name is not resolved')

    @marks.testrail_id(6232)
    @marks.medium
    def test_switching_accounts_in_dapp(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()

        wallet_view.just_fyi('create new account in multiaccount')
        wallet_view.set_up_wallet()
        status_account = 'Status account'
        account_name = 'Subaccount'
        wallet_view.add_account(account_name)
        address = wallet_view.get_wallet_address(account_name)

        sign_in_view.just_fyi('can see two accounts in DApps')
        dapp_view = sign_in_view.dapp_tab_button.click()
        dapp_view.select_account_button.click()
        for text in 'Select the account', status_account, account_name:
            if not dapp_view.element_by_text_part(text).is_element_displayed():
                self.driver.fail("No expected element %s is shown in menu" % text)

        sign_in_view.just_fyi('add permission to Status account')
        dapp_view.enter_url_editbox.click()
        status_test_dapp = home_view.open_status_test_dapp()

        sign_in_view.just_fyi('check that permissions from previous account was removed once you choose another')
        dapp_view.select_account_button.click()
        dapp_view.select_account_by_name(account_name).wait_for_element(30)
        dapp_view.select_account_by_name(account_name).click()
        profile_view = dapp_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.dapp_permissions_button.click()
        if profile_view.element_by_text(test_dapp_name).is_element_displayed():
            self.errors.append("Permissions for %s are not removed" % test_dapp_name)

        sign_in_view.just_fyi('check that can change account')
        profile_view.dapp_tab_button.click()
        if not status_test_dapp.element_by_text(account_name).is_element_displayed():
            self.errors.append("No expected account %s is shown in authorize web3 popup for wallet" % account_name)
        status_test_dapp.allow_button.click()
        dapp_view.profile_button.click()
        profile_view.element_by_text(test_dapp_name).click()
        for text in 'Chat key', account_name:
            if not dapp_view.element_by_text(text).is_element_displayed():
                self.errors.append("Access is not granted to %s" % text)

        sign_in_view.just_fyi('check correct account is shown for transaction if sending from DApp')
        profile_view.dapp_tab_button.click()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        address = send_transaction_view.get_formatted_recipient_address(address)
        if not send_transaction_view.element_by_text(address).is_element_displayed():
            self.errors.append("Wallet address %s in not shown in 'From' on Send Transaction screen" % address)

        sign_in_view.just_fyi('Relogin and check multiaccount loads fine')
        send_transaction_view.cancel_button.click()
        sign_in_view.profile_button.click()
        sign_in_view.get_back_to_home_view()
        sign_in_view.relogin()
        sign_in_view.wallet_button.click()
        if not wallet_view.element_by_text(account_name).is_element_displayed():
            self.errors.append("Subaccount is gone after relogin in Wallet!")
        sign_in_view.dapp_tab_button.click()
        if not dapp_view.element_by_text(account_name).is_element_displayed():
            self.errors.append("Subaccount is not selected after relogin in Dapps!")
        self.errors.verify_no_errors()

