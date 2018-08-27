from views.web_views.base_web_view import BaseWebView, BaseButton


class AssetsButton(BaseButton):

    def __init__(self, driver):
        super(AssetsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Assets')

    class RequestETHButton(BaseButton):
        def __init__(self, driver):
            super(AssetsButton.RequestETHButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Request Ropsten ETH')

    class RequestSTTButton(BaseButton):
        def __init__(self, driver):
            super(AssetsButton.RequestSTTButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Request STT')

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

    class DeployContractButton(BaseButton):
        def __init__(self, driver):
            super(TransactionsButton.DeployContractButton, self).__init__(driver)
            self.locator = self.Locator.text_selector('Deploy simple contract')

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
        self.test_filters_button = TransactionsButton.TestFiltersButton(self.driver)

        self.status_api_button = StatusAPIButton(self.driver)
        self.request_contact_code_button = StatusAPIButton.RequestContactCodeButton(self.driver)
