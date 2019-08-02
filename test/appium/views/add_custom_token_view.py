from views.base_element import BaseEditBox
from views.base_view import BaseView


class ContractAddressInput(BaseEditBox):
    def __init__(self, driver):
        super(ContractAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Contract address']/following-sibling::*[2]/android.widget.EditText")


class NameInput(BaseEditBox):
    def __init__(self, driver):
        super(NameInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Name']/following-sibling::*[1]/android.widget.EditText")


class SymbolInput(BaseEditBox):
    def __init__(self, driver):
        super(SymbolInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Symbol']/following-sibling::*[2]/android.widget.EditText")


class DecimalsInput(BaseEditBox):
    def __init__(self, driver):
        super(DecimalsInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Decimals']/following-sibling::*[2]/android.widget.EditText")


class AddCustomTokenView(BaseView):
    def __init__(self, driver):
        super(AddCustomTokenView, self).__init__(driver)
        self.driver = driver
        self.contract_address_input = ContractAddressInput(self.driver)
        self.name_input = NameInput(self.driver)
        self.symbol_input = SymbolInput(self.driver)
        self.decimals_input = DecimalsInput(self.driver)
