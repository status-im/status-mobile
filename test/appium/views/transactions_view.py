import time

import pytest
from selenium.common.exceptions import NoSuchElementException

from views.base_element import BaseElement, BaseButton, BaseText
from views.base_view import BaseView


class TransactionTable(BaseElement):
    def __init__(self, driver):
        super(TransactionTable, self).__init__(driver)
        self.driver = driver
        self.locator = self.Locator.xpath_selector("//android.support.v4.view.ViewPager")

    class TransactionElement(BaseButton):
        def __init__(self, driver, amount):
            super(TransactionTable.TransactionElement, self).__init__(driver)
            self.driver = driver
            self.locator = self.Locator.xpath_selector(
                "(//android.widget.TextView[contains(@text,'%s ETH')])" % amount)

        class TransactionDetailsView(BaseView):
            def __init__(self, driver):
                super(TransactionTable.TransactionElement.TransactionDetailsView, self).__init__(driver)
                self.driver = driver
                self.locators = dict(transaction_hash="//android.widget.TextView[@text='Hash']/following-sibling::*[1]")

            class DetailsTextElement(BaseText):
                def __init__(self, driver, locator):
                    super(TransactionTable.TransactionElement.TransactionDetailsView.DetailsTextElement,
                          self).__init__(driver)
                    self.locator = self.Locator.xpath_selector(locator)

            def get_transaction_hash(self) -> str:
                return self.DetailsTextElement(driver=self.driver, locator=self.locators['transaction_hash']).text

        def navigate(self):
            return self.TransactionDetailsView(self.driver)

    def get_transaction_element(self, amount: str):
        return self.TransactionElement(self.driver, amount=amount)

    def find_transaction(self, amount: str) -> TransactionElement:
        for i in range(9):
            try:
                element = self.get_transaction_element(amount=amount.replace(',', '.'))
                element.find_element()
                return element
            except NoSuchElementException:
                time.sleep(5)
                self.driver.swipe(500, 500, 500, 1000)
        pytest.fail('Transaction was not found on Wallet/Transaction screen')


class HistoryTab(BaseButton):
    def __init__(self, driver):
        super(HistoryTab, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='HISTORY']")


class UnsignedTab(BaseButton):
    def __init__(self, driver):
        super(UnsignedTab, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='UNSIGNED']")

    class SignButton(BaseButton):
        def __init__(self, driver):
            super(UnsignedTab.SignButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='SIGN']")


class TransactionsView(BaseView):
    def __init__(self, driver):
        super(TransactionsView, self).__init__(driver)
        self.driver = driver
        self.history_tab = HistoryTab(self.driver)
        self.unsigned_tab = UnsignedTab(self.driver)
        self.sign_button = UnsignedTab.SignButton(self.driver)
        self.transactions_table = TransactionTable(self.driver)
