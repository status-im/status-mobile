from views.web_views.base_web_view import BaseWebView, BaseButton


class AssetsButton(BaseButton):

    def __init__(self, driver):
        super(AssetsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Assets')

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


class StatusTestDAppView(BaseWebView):

    def __init__(self, driver):
        super(StatusTestDAppView, self).__init__(driver)
        self.driver = driver

        self.assets_button = AssetsButton(self.driver)
        self.request_stt_button = AssetsButton.RequestSTTButton(self.driver)

        self.transactions_button = TransactionsButton(self.driver)
        self.sign_message_button = TransactionsButton.SignMessageButton(self.driver)
