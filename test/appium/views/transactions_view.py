from selenium.common.exceptions import NoSuchElementException
from views.base_element import BaseElement, Button, Text
from views.base_view import BaseView


class OptionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="(//android.widget.ImageView[@content-desc='icon'])[2]")
    def click(self):
        self.click_until_presence_of_element(OptionsButton.CopyTransactionHashButton(self.driver))

    class CopyTransactionHashButton(Button):
        def __init__(self, driver):
            super().__init__(driver, translation_id="copy-transaction-hash")

    class OpenOnEtherscanButton(Button):
        def __init__(self, driver):
            super().__init__(driver, translation_id="open-on-etherscan")

class TransactionTable(BaseElement):
    def __init__(self, driver):
        super().__init__(driver, xpath="//android.widget.ScrollView")
        self.driver = driver

    class TransactionElement(Button):
        def __init__(self, driver):
            super().__init__(driver)

        @staticmethod
        def by_amount(driver, amount: str, asset):
            element = TransactionTable.TransactionElement(driver)
            element.locator = "(//android.widget.TextView[contains(@text,'%s %s')])" % (amount, asset)
            return element

        @staticmethod
        def by_index(driver, index: int):
            element = TransactionTable.TransactionElement(driver)
            element.locator = '(//android.view.ViewGroup[@content-desc="transaction-item"])[%d]' % (index + 1)
            return element

        class TransactionDetailsView(BaseView):
            def __init__(self, driver):
                super(TransactionTable.TransactionElement.TransactionDetailsView, self).__init__(driver)
                self.driver = driver
                self.locators = dict()
                self.options_button = OptionsButton(driver)
                self.copy_transaction_hash_button = OptionsButton.CopyTransactionHashButton(driver)
                self.open_transaction_on_etherscan_button = OptionsButton.OpenOnEtherscanButton(driver)

                self.locators['transaction_hash'] = "//android.widget.TextView[@text='Hash']/following-sibling::*[1]"
                self.locators['sender_address'] = "//*[@content-desc='sender-address-text']"
                self.locators['recipient_address'] = "//*[@content-desc='recipient-address-text'][last()]"

            class DetailsTextElement(Text):
                def __init__(self, driver, locator):
                    super(TransactionTable.TransactionElement.TransactionDetailsView.DetailsTextElement,
                          self).__init__(driver)
                    self.locator = locator

                def text(self):
                    text = self.find_element().text
                    self.driver.info('%s is %s' % (self.name, text))
                    return text

            def get_transaction_hash(self) -> str:
                return self.DetailsTextElement(driver=self.driver, locator=self.locators['transaction_hash']).text()

            def get_sender_address(self) -> str:
                return self.DetailsTextElement(driver=self.driver, locator=self.locators['sender_address']).text()

            def get_recipient_address(self) -> str:
                return self.DetailsTextElement(driver=self.driver, locator=self.locators['recipient_address']).text()

        def navigate(self):
            return self.TransactionDetailsView(self.driver)

    def transaction_by_index(self, index: int):
        self.driver.info('**Finding transaction by index %s**' % index)
        return self.TransactionElement.by_index(self.driver, index=index)


    def transaction_by_amount(self, amount: str, asset):
        self.driver.info('**Finding transaction by amount %s**' % amount)
        return self.TransactionElement.by_amount(self.driver, amount=amount.replace(',', '.'), asset=asset)

    def find_transaction(self, amount: str, asset='ETH') -> TransactionElement:
        element = self.transaction_by_amount(amount=amount, asset=asset)
        for i in range(9):
            try:
                element.find_element()
                return element
            except NoSuchElementException:
                from views.base_view import BaseView
                BaseView(self.driver).pull_to_refresh()
        self.driver.fail('Transaction %s %s was not found on Wallet/Transaction screen' %(amount, asset))

    def get_transactions_number(self):
        element = self.TransactionElement(self.driver)
        element.locator = '//android.view.ViewGroup[@content-desc="transaction-item"]'
        return len(element.wait_for_elements())

class TransactionsView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        self.transactions_table = TransactionTable(self.driver)
