from views.base_element import EditBox
from views.base_view import BaseView


class AddCustomTokenView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        self.contract_address_input = EditBox(self.driver, translation_id="contract-address",
                                              suffix="/following-sibling::*[2]/android.widget.EditText")
        self.name_input = EditBox(self.driver, translation_id="name",
                                  suffix="/following-sibling::*[1]/android.widget.EditText")
        self.symbol_input = EditBox(self.driver, translation_id="symbol",
                                    suffix="/following-sibling::*[2]/android.widget.EditText")
        self.decimals_input = EditBox(self.driver, translation_id="decimals",
                                      suffix="/following-sibling::*[2]/android.widget.EditText")
