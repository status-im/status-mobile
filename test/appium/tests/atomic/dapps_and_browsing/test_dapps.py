import pytest
from tests import marks, test_dapp_url, test_dapp_name
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


@pytest.mark.all
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

    @marks.testrail_id(6234)
    @marks.high
    def test_always_allow_web3_permissions(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=user['passphrase'])
        dapp_view = sign_in_view.dapp_tab_button.click()

        dapp_view.just_fyi('check that web3 permissions window is shown')
        if not dapp_view.element_by_text_part('ÐApps can access my wallet').is_element_displayed():
            self.errors.append('Permissions window is not shown!')

        dapp_view.just_fyi('check that can enable "Always allow" and Dapp will not ask for permissions')
        dapp_view.always_allow_radio_button.click()
        dapp_view.close_web3_permissions_window_button.click()
        dapp_view.open_url(test_dapp_url)
        status_test_dapp = dapp_view.get_status_test_dapp_view()
        if status_test_dapp.allow_button.is_element_displayed():
            self.driver.append('DApp is asking permissions (Always allow is enabled)')

        dapp_view.just_fyi('check that after relogin window is not reappearing and DApps are still not asking for permissions')
        sign_in_view.relogin()
        sign_in_view.dapp_tab_button.click()
        dapp_view.open_url(test_dapp_url)
        if status_test_dapp.allow_button.is_element_displayed():
            self.driver.append('DApp is asking permissions after relogin (Always allow is enabled)')
        self.errors.verify_no_errors()


    @marks.testrail_id(6232)
    @marks.medium
    def test_switching_accounts_in_dapp(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()

        wallet_view.just_fyi('create new account in multiaccount')
        wallet_view.set_up_wallet()
        status_account = 'Status account'
        account_name = 'subaccount'
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
        profile_view.privacy_and_security_button.click()
        profile_view.dapp_permissions_button.click()
        if profile_view.element_by_text(test_dapp_name).is_element_displayed():
            self.errors.append("Permissions for %s are not removed" % test_dapp_name)

        sign_in_view.just_fyi('check that can change account')
        profile_view.dapp_tab_button.click()
        if not status_test_dapp.element_by_text(account_name).is_element_displayed():
            self.errors.append("No expected account %s is shown in authorize web3 popup for wallet" % account_name)
        status_test_dapp.allow_button.click()
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

        self.errors.verify_no_errors()

    @marks.testrail_id(5654)
    @marks.low
    def test_can_proceed_dapp_usage_after_transacting_it(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(passphrase=user['passphrase'])
        chat = home_view.join_public_chat(home_view.get_public_chat_name())
        chat.back_button.click()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.ok_got_it_button.click()
        send_transaction_view.sign_transaction()
        if not status_test_dapp.assets_button.is_element_displayed():
            self.driver.fail('Oops! Cannot proceed to use Status Test Dapp.')
