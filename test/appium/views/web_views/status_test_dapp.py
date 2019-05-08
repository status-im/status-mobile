from views.web_views.base_web_view import BaseWebView, BaseButton


class AssetsButton(BaseButton):

    def __init__(self, driver):
        super(AssetsButton, self).__init__(driver)
        self.locator = self.Locator.webview_selector('Assets')

    class RequestETHButton(BaseButton):
        def __init__(self, driver):
            super(AssetsButton.RequestETHButton, self).__init__(driver)
            self.locator = self.Locator.webview_selector('Request Ropsten ETH')

    class RequestSTTButton(BaseButton):
        def __init__(self, driver):
            super(AssetsButton.RequestSTTButton, self).__init__(driver)
            self.locator = self.Locator.webview_selector('Request STT')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)


class TransactionsButton(BaseButton):

    def __init__(self, driver):
        super(TransactionsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Transactions')

    class SignMessageButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.SignMessageButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Sign message')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class SignTypedMessageButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.SignTypedMessageButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Sign Typed Message')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class DeployContractButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.DeployContractButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Deploy simple contract')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class SendTwoTxOneByOneButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.SendTwoTxOneByOneButton, self).__init__(driver)
            self.locator = self.Locator.webview_selector('Send two Txs, one after another, 0.00001 and 0.00002 ETH')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

        def click(self):
            self.driver.swipe(500, 1000, 500, 800)
            self.wait_for_visibility_of_element().click()
            return self.navigate()

    class SendTwoTxInBatchButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.SendTwoTxInBatchButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Send two Txs in batch, 0.00001 and 0.00002 ETH')

        def navigate(self):
            from views.send_transaction_view import SendTransactionView
            return SendTransactionView(self.driver)

    class TestFiltersButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.TestFiltersButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Test filters')


class StatusAPIButton(BaseButton):

    def __init__(self, driver):
        super(StatusAPIButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Status API')

    class RequestContactCodeButton(BaseButton):
        def __init__(self, driver):
            super(StatusAPIButton.RequestContactCodeButton, self).__init__(driver)
            self.locator = self.Locator.text_part_selector('Request contact code')

    def click(self):
        self.wait_for_visibility_of_element().click()


class SendOneTransactionInBatchButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Send one Tx in batch')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class StatusTestDAppView(BaseWebView):

    def __init__(self, driver):
        super(StatusTestDAppView, self).__init__(driver)
        self.driver = driver

        self.assets_button = AssetsButton(self.driver)
        self.request_eth_button = AssetsButton.RequestETHButton(self.driver)
        self.request_stt_button = AssetsButton.RequestSTTButton(self.driver)

        self.transactions_button = TransactionsButton(self.driver)
        self.sign_message_button = TransactionsButton.SignMessageButton(self.driver)
        self.deploy_contract_button = TransactionsButton.DeployContractButton(self.driver)
        self.send_one_tx_in_batch_button = SendOneTransactionInBatchButton(self.driver)
        self.send_two_tx_one_by_one_button = TransactionsButton.SendTwoTxOneByOneButton(self.driver)
        self.send_two_tx_in_batch_button = TransactionsButton.SendTwoTxInBatchButton(self.driver)
        self.test_filters_button = TransactionsButton.TestFiltersButton(self.driver)
        self.sign_typed_message_button = TransactionsButton.SignTypedMessageButton(self.driver)

        self.status_api_button = StatusAPIButton(self.driver)
        self.request_contact_code_button = StatusAPIButton.RequestContactCodeButton(self.driver)

    def wait_for_d_aap_to_load(self, wait_time=10):
        self.assets_button.wait_for_visibility_of_element(seconds=wait_time)

    def faucet_asset(self, asset='eth'):
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
