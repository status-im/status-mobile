from views.web_views.base_web_view import BaseWebView, Button
import time

class RequestSTTButton(Button):
    def __init__(self, driver):
        super(RequestSTTButton, self).__init__(driver, webview="Request STT")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class TransactionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@text='Transactions']")

    class SignMessageButton(Button):
        def __init__(self, driver):
            super().__init__(driver, xpath="//*[@text='Sign message']")

        def click(self):
            from views.base_element import Text
            self.click_until_presence_of_element(Text(self.driver, translation_id="signing-phrase"))
            return self.navigate()

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class SignTypedMessageButton(Button):
        def __init__(self, driver):
            super().__init__(driver, xpath="//*[@text='Sign Typed Message']")

        def click(self):
            from views.base_element import Text
            self.click_until_presence_of_element(Text(self.driver, translation_id="signing-phrase"))
            return self.navigate()

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class DeployContractButton(Button):
        def __init__(self, driver):
            super().__init__(driver, xpath="//*[@text='Deploy simple contract']")

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class SendTwoTxOneByOneButton(Button):
        def __init__(self, driver):
            super().__init__(driver, webview="Send two Txs, one after another, 0.00001 and 0.00002 ETH")

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

        def click(self):
            self.swipe_to_web_element()
            time.sleep(2)
            self.wait_for_visibility_of_element().click()
            return self.navigate()

    class SendTwoTxInBatchButton(Button):
        def __init__(self, driver):
            super().__init__(driver, xpath="//*[@text='Send two Txs in batch, 0.00001 and 0.00002 ETH']")

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class TestFiltersButton(Button):
        def __init__(self, driver):
            super().__init__(driver, xpath="//*[@text='Test filters']")


class StatusAPIButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@text='Status API']")

    def click(self):
        self.wait_for_visibility_of_element().click()


class SendOneTransactionInBatchButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@text='Send one Tx in batch']")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class StatusTestDAppView(BaseWebView):

    def __init__(self, driver):
        super(StatusTestDAppView, self).__init__(driver)
        self.driver = driver

        self.assets_button = Button(self.driver, webview="Assets")
        self.request_eth_button = Button(self.driver, webview="Request Ropsten ETH")
        self.request_stt_button = RequestSTTButton(self.driver)

        self.transactions_button = TransactionsButton(self.driver)
        self.sign_message_button = TransactionsButton.SignMessageButton(self.driver)
        self.deploy_contract_button = TransactionsButton.DeployContractButton(self.driver)
        self.send_one_tx_in_batch_button = SendOneTransactionInBatchButton(self.driver)
        self.send_two_tx_one_by_one_button = TransactionsButton.SendTwoTxOneByOneButton(self.driver)
        self.send_two_tx_in_batch_button = TransactionsButton.SendTwoTxInBatchButton(self.driver)
        self.test_filters_button = TransactionsButton.TestFiltersButton(self.driver)
        self.sign_typed_message_button = TransactionsButton.SignTypedMessageButton(self.driver)

        self.status_api_button = StatusAPIButton(self.driver)
        self.request_contact_code_button = Button(self.driver, xpath="//*[@text='Request contact code (public key)']")

    def wait_for_d_aap_to_load(self, wait_time=10):
        self.driver.info("**Wait %ss for assets in simpledapp**" % wait_time)
        self.assets_button.wait_for_visibility_of_element(seconds=wait_time)

    def faucet_asset(self, asset='eth'):
        self.driver.info("**Faucet %s in dapp**" % asset)
        self.wait_for_d_aap_to_load()
        self.assets_button.click()
        if asset == 'eth':
            self.request_eth_button.click()
            self.element_by_text('Faucet request recieved').wait_for_visibility_of_element()
            self.ok_button.click()
            self.element_by_text('Faucet request recieved').wait_for_invisibility_of_element()
        elif asset == 'stt':
            send_transaction_view = self.request_stt_button.click()
            send_transaction_view.sign_transaction()
