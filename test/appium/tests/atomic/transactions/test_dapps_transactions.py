from tests import marks, unique_password, common_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders
from views.sign_in_view import SignInView


@marks.transaction
class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(5309)
    @marks.critical
    def test_request_stt_from_daap(self):
        sender = transaction_senders['K']
        home = SignInView(self.driver).recover_access(sender['passphrase'], unique_password)
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction = status_test_dapp.request_stt_button.click()

        if not send_transaction.onboarding_message.is_element_displayed():
            self.driver.fail('It seems onboarding screen is not shown.')
        home.ok_got_it_button.click()
        home.cancel_button.click()
        wallet = home.wallet_button.click()
        initial_amount_STT = wallet.get_asset_amount_by_name('STT')
        wallet.dapp_tab_button.click(desired_element_text='Request STT')
        send_transaction = status_test_dapp.request_stt_button.click()
        send_transaction.sign_transaction(unique_password)
        status_test_dapp.wallet_button.click()

        send_transaction.just_fyi('Verify that wallet balance is updated')
        wallet.wait_balance_is_changed('STT', initial_amount_STT, scan_tokens=True)

        send_transaction.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5342)
    @marks.critical
    def test_sign_message_and_transactions_filters_from_daap(self):
        password = 'password_for_daap'
        home = SignInView(self.driver).recover_access(passphrase=transaction_senders['W']['passphrase'],
                                                           password=password)
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()

        wallet.just_fyi("Checking signing message")
        send_transaction = status_test_dapp.sign_message_button.click()
        if not send_transaction.element_by_text("Test message").is_element_displayed():
            self.errors.append("No message shown when signing!")
        send_transaction.enter_password_input.send_keys(password)
        send_transaction.sign_button.click()
        if not status_test_dapp.element_by_text_part('Signed message').is_element_displayed():
            self.errors.append('Message was not signed')
        send_transaction.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction.find_values_in_logcat(password=password)
        if values_in_logcat:
            self.errors.append("When signing message from dapp: %s" % values_in_logcat)

        wallet.just_fyi("Checking send 2 txs in batch")
        status_test_dapp.send_two_tx_in_batch_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction.sign_transaction(password)
        if not send_transaction.sign_with_password.is_element_displayed(10):
            self.driver.fail('Second send transaction screen did not appear!')
        send_transaction.sign_transaction(password)

        wallet.just_fyi("Checking send 2 txs one after another")
        status_test_dapp.send_two_tx_one_by_one_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_transaction.sign_transaction(password)
        if not send_transaction.sign_with_password.is_element_displayed(20):
            self.driver.fail('Second send transaction screen did not appear!')
        send_transaction.sign_transaction(password)

        wallet.just_fyi("Checking test filters")
        status_test_dapp.test_filters_button.scroll_and_click()
        for element in status_test_dapp.element_by_text('eth_uninstallFilter'), status_test_dapp.ok_button:
            if element.is_element_displayed(10):
                self.driver.fail("'Test filters' button produced an error")
        self.errors.verify_no_errors()


    @marks.testrail_id(5784)
    @marks.medium
    def test_sign_typed_message_deply_simple_contract_request_pub_key_from_dapp(self):
        user = transaction_senders['W']
        home = SignInView(self.driver).recover_access(passphrase=user['passphrase'])

        home.just_fyi("Checking requesting public key from dapp")
        status_test_dapp = home.open_status_test_dapp(allow_all=False)
        status_test_dapp.status_api_button.click_until_presence_of_element(status_test_dapp.request_contact_code_button)
        status_test_dapp.request_contact_code_button.click_until_presence_of_element(status_test_dapp.deny_button)
        status_test_dapp.deny_button.click()
        if status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            self.errors.append('Public key is returned but access was not allowed')
        status_test_dapp.request_contact_code_button.click_until_presence_of_element(status_test_dapp.deny_button)
        status_test_dapp.allow_button.click()
        if not status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            self.errors.append('Public key is not returned')
        status_test_dapp.dapp_tab_button.double_click()
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()

        home.just_fyi("Checking sign typed message")
        home.open_status_test_dapp(allow_all=True)
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.sign_typed_message_button)
        send_transaction = status_test_dapp.sign_typed_message_button.click()
        send_transaction.enter_password_input.send_keys(common_password)
        send_transaction.sign_button.click_until_absense_of_element(send_transaction.sign_button)
        if not status_test_dapp.element_by_text_part('0xde3048417e5881acc9ca8466ab0b3e2f9f96').is_element_displayed(30):
            self.errors.append("Hash of signed typed message is not shown!")

        home.just_fyi("Checking deploy simple contract")
        send_transaction = status_test_dapp.deploy_contract_button.click()
        send_transaction.sign_transaction(default_gas_price=True)
        if not status_test_dapp.element_by_text('Contract deployed at: ').is_element_displayed(180):
            self.errors.append('Contract was not created')
        for text in ['Call contract get function',
                    'Call contract set function', 'Call function 2 times in a row']:
            status_test_dapp.element_by_text(text).scroll_to_element()
        self.errors.verify_no_errors()
