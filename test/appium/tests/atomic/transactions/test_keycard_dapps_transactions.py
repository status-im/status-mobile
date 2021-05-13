from tests import marks, pin, puk, pair_code
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders
from views.sign_in_view import SignInView


@marks.transaction
class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(6249)
    @marks.critical
    def test_keycard_request_stt_from_daap(self):
        sender = transaction_senders['K']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        initial_amount_STT = wallet_view.get_asset_amount_by_name('STT')
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(keycard=True)
        status_test_dapp.wallet_button.click()

        send_transaction_view.just_fyi('Verify that wallet balance is updated')
        wallet_view.wait_balance_is_changed('STT', initial_amount_STT)

        send_transaction_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction_view.find_values_in_logcat(mnemonic=sender['passphrase'],
                                                                       pin=pin,
                                                                       puk=puk,
                                                                       password=pair_code)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(6251)
    @marks.critical
    def test_keycard_sign_message_and_transactions_from_daap(self):
        home = SignInView(self.driver).recover_access(passphrase=transaction_senders['Z']['passphrase'],
                                                      keycard=True)
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()

        wallet.just_fyi("Checking signing message")
        send_transaction = status_test_dapp.sign_message_button.click()
        if not send_transaction.element_by_text("Test message").is_element_displayed():
            self.errors.append("No message shown when signing!")
        keycard = send_transaction.sign_with_keycard_button.click()
        keycard.enter_default_pin()
        if not keycard.element_by_text_part('Signed message').is_element_displayed():
            self.errors.append('Message was not signed')
        keycard.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction.find_values_in_logcat(pin=pin, puk=puk, password=pair_code)
        if values_in_logcat:
            self.driver.fail("After signing message: %s" % values_in_logcat)

        wallet.just_fyi("Checking send 2 txs in batch")
        status_test_dapp.send_two_tx_in_batch_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction.sign_transaction(keycard=True)
        if not send_transaction.sign_with_keycard_button.is_element_displayed(10):
            self.driver.fail('Second send transaction screen did not appear!')
        send_transaction.sign_transaction(keycard=True)

        wallet.just_fyi("Checking send 2 txs one after another")
        status_test_dapp.send_two_tx_one_by_one_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_transaction.sign_transaction(keycard=True)
        if not send_transaction.sign_with_keycard_button.is_element_displayed(20):
            self.driver.fail('Second send transaction screen did not appear!')
        send_transaction.sign_transaction(keycard=True)

        self.errors.verify_no_errors()


    @marks.testrail_id(6310)
    @marks.medium
    def test_keycard_sign_typed_message_deploy_simple_contract(self):
        sender = transaction_senders['W']
        home = SignInView(self.driver).recover_access(sender['passphrase'], keycard=True)
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.sign_typed_message_button)

        wallet.just_fyi("Checking sign typed message")
        send_transaction = status_test_dapp.sign_typed_message_button.click()
        send_transaction.sign_with_keycard_button.click()
        keycard_view = send_transaction.sign_with_keycard_button.click()
        keycard_view.enter_default_pin()
        if not keycard_view.element_by_text_part('0xde3048417').is_element_displayed():
            self.errors.append('Typed message was not signed')

        wallet.just_fyi("Checking deploy simple contract")
        send_transaction_view = status_test_dapp.deploy_contract_button.click()
        send_transaction_view.sign_transaction(keycard=True, default_gas_price=False)
        if not status_test_dapp.element_by_text('Contract deployed at: ').is_element_displayed(180):
            self.errors.append('Contract was not created')
        for text in ['Call contract get function',
                     'Call contract set function', 'Call function 2 times in a row']:
            status_test_dapp.element_by_text(text).scroll_to_element()
        self.errors.verify_no_errors()



