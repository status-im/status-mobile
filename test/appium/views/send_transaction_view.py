from tests import common_password
from views.base_element import Text, SilentButton
from views.base_element import Button, EditBox
from views.base_view import BaseView


class AmountEditBox(EditBox, Button):
    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver, accessibility_id="amount-input")

    def set_value(self, value):
        EditBox.set_value(self, value)
        self.driver.press_keycode(66)


class ChooseRecipientButton(Button):
    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver, accessibility_id="choose-recipient-button")

    def click(self):
        self.click_until_presence_of_element(Button(self.driver, translation_id="my-accounts"))
        return self.navigate()


class GasPriceInput(EditBox):
    def __init__(self, driver):
        super(GasPriceInput, self).__init__(driver, prefix="(", translation_id="gas-limit",
                                            suffix="/..//android.widget.EditText)[2]")

    @property
    def text(self):
        return self.find_element().text


class UpdateFeeButton(Button):
    def __init__(self, driver):
        super(UpdateFeeButton, self).__init__(driver, translation_id="update")

    def click(self):
        for _ in range(3):
            self.find_element().click()
            if not self.is_element_displayed():
                return self.navigate()


class ValidationErrorOnSendTransaction(Button):
    def __init__(self, driver, field):
        super(ValidationErrorOnSendTransaction, self).__init__(driver, xpath="//*[@text='%s']/../*[@content-desc='icon']" % field)


class NotEnoughEthForGas(Text):
    def __init__(self, driver):
        super().__init__(driver, translation_id="wallet-insufficient-gas")


class ValidationWarnings(object):
    def __init__(self, driver):
        self.not_enough_eth_for_gas = NotEnoughEthForGas(driver)


class SignWithKeycardButton(Button):
    def __init__(self, driver):
        super(SignWithKeycardButton, self).__init__(driver, xpath="//*[contains(@text,'%s')]" % self.get_translation_by_key("sign-with"))

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        from views.keycard_view import KeycardView
        self.click_until_presence_of_element(KeycardView(self.driver).one_button)
        return self.navigate()


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.accounts_button = Button(self.driver, translation_id="my-accounts")
        self.enter_recipient_address_button = Button(self.driver, accessibility_id="choose-recipient-recipient-code")
        self.scan_qr_code_button = Button(self.driver, accessibility_id="scan-contact-code-button")
        self.enter_recipient_address_input = EditBox(self.driver, accessibility_id="recipient-address-input")
        self.first_recipient_button = Button(self.driver, accessibility_id="chat-icon")
        self.enter_recipient_address_text = Text(self.driver, xpath="//*[@content-desc='choose-recipient-button']//android.widget.TextView")

        self.recent_recipients_button = Button(self.driver, translation_id="recent-recipients")
        self.amount_edit_box = AmountEditBox(self.driver)
        self.set_max_button = Button(self.driver, translation_id="set-max")
        self.validation_error_element = Text(self.driver, xpath="//*[@content-desc='custom-gas-fee']/../android.view.ViewGroup//*[@content-desc='icon']")

        self.network_fee_button = Button(self.driver, accessibility_id="custom-gas-fee")
        self.transaction_fee_button = Button(self.driver, accessibility_id="transaction-fee-button")
        self.transaction_fee_total_value = Text(self.driver, translation_id="wallet-transaction-total-fee", suffix="//following::android.widget.TextView[1]")
        self.gas_limit_input = EditBox(self.driver, prefix="(", translation_id="gas-limit", suffix="/..//android.widget.EditText)[1]")
        self.gas_price_input = GasPriceInput(self.driver)
        self.total_fee_input = EditBox(self.driver, translation_id="wallet-transaction-total-fee", suffix="/following-sibling::android.widget.TextView")
        self.update_fee_button = UpdateFeeButton(self.driver)


        self.sign_transaction_button = Button(self.driver, accessibility_id="send-transaction-bottom-sheet")
        self.sign_with_keycard_button = SignWithKeycardButton(self.driver)
        self.sign_with_password = Button(self.driver, translation_id="sign-with-password")
        self.sign_button = Button(self.driver, translation_id="transactions-sign")
        self.sign_in_phrase_text = Text(self.driver, accessibility_id="signing-phrase-text")
        self.enter_password_input = EditBox(self.driver, accessibility_id="enter-password-input")
        self.got_it_button = Button(self.driver, accessibility_id="got-it-button")

        self.select_asset_button = Button(self.driver, accessibility_id="choose-asset-button")
        self.asset_text = Text(self.driver, xpath="//*[@content-desc='choose-asset-button']//android.widget.TextView")
        self.recipient_text = Text(self.driver, xpath="//*[@content-desc='choose-recipient-button']//android.widget.TextView")

        self.share_button = Button(self.driver, accessibility_id="share-address-button")

        self.onboarding_message = Text(self.driver, translation_id="this-is-you-signing")
        self.validation_warnings = ValidationWarnings(self.driver)
        self.eth_asset_in_select_asset_bottom_sheet_button = Button(self.driver, accessibility_id=":ETH-asset-value")

        # Elements for commands in 1-1 chat
        self.select_button = Button(self.driver, accessibility_id="select-account-bottom-sheet")
        self.request_transaction_button = Button(self.driver, accessibility_id="request-transaction-bottom-sheet")

        # Elements on set recipient screen
        self.recipient_add_to_favorites = Button(self.driver, accessibility_id="participant-add-to-favs")
        self.recipient_done = Button(self.driver, accessibility_id="participant-done")
        self.new_favorite_name_input = EditBox(self.driver, accessibility_id="fav-name")
        self.new_favorite_add_favorite = Button(self.driver, accessibility_id="add-fav")


    def set_recipient_address(self, address):
        self.driver.info("**Setting recipient address to %s**" % address)
        self.chose_recipient_button.click()
        self.enter_recipient_address_input.set_value(address)
        self.enter_recipient_address_input.click()
        self.done_button.click_until_absense_of_element(self.done_button)

    def sign_transaction(self, sender_password: str = common_password, keycard=False, default_gas_price=False):
        self.driver.info("**Signing transaction (keycard:%s, default_gas_price:%s)**" % (str(keycard), str(default_gas_price)))
        if not default_gas_price:
            self.network_fee_button.click()
            default_gas_price = self.gas_price_input.text
            self.gas_price_input.clear()
            self.gas_price_input.set_value(str(int(float(default_gas_price))+30))
            self.update_fee_button.click()
        if keycard:
            keycard_view = self.sign_with_keycard_button.click()
            keycard_view.enter_default_pin()
        else:
            self.sign_with_password.click_until_presence_of_element(self.enter_password_input)
            self.enter_password_input.send_keys(sender_password)
            self.sign_button.click_until_absense_of_element(self.sign_button)
        self.ok_button.wait_for_element(120)
        if self.element_by_text_part('Transaction failed').is_element_displayed():
            self.driver.fail('Transaction failed')
        self.driver.info("**Transaction is signed!**")
        self.ok_button.click()

    def get_transaction_fee_total(self):
        self.driver.info("**Getting transaction fee**")
        self.network_fee_button.click_until_presence_of_element(self.gas_limit_input)
        fee_value = self.transaction_fee_total_value.text.split()[0]
        self.update_fee_button.click()
        return fee_value

    @staticmethod
    def get_formatted_recipient_address(address):
        return address[:6] + '…' + address[-4:]

    def get_username_in_transaction_bottom_sheet_button(self, username_part):
        self.driver.info("**Getting username by '%s' in transaction fee bottom sheet**" % username_part)
        return SilentButton(self.driver, xpath="//*[@content-desc='amount-input']/..//*[starts-with(@text,'%s')]" % username_part)

    def get_account_in_select_account_bottom_sheet_button(self, account_name):
        self.driver.info("**Getting account by '%s' in transaction fee bottom sheet**" % account_name)
        return SilentButton(self.driver, translation_id="select-account", suffix="/..//*[starts-with(@text,'%s')]" % account_name)

    def get_validation_icon(self, field='Network fee'):
        return ValidationErrorOnSendTransaction(self.driver, field)

    def get_values_from_send_transaction_bottom_sheet(self, gas=False):
        self.driver.info("**Getting values from send transaction bottom sheet**")
        data = {
            'amount': self.amount_edit_box.text,
            'asset': self.asset_text.text,
            'address':  self.enter_recipient_address_text.text
        }
        if gas:
            self.sign_transaction_button.click_until_presence_of_element(self.sign_with_password)
            self.network_fee_button.click_until_presence_of_element(self.gas_limit_input)
            data['gas_limit'] = self.gas_limit_input.text
            data['gas_price'] = self.gas_price_input.text
            self.cancel_button.click()
        return data

    def add_to_favorites(self, name):
        self.driver.info("**Adding '%s' to favorite recipients**" % name)
        self.recipient_add_to_favorites.click()
        self.new_favorite_name_input.set_value(name)
        self.new_favorite_add_favorite.click()