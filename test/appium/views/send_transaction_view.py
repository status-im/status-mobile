from tests import common_password
from views.base_element import Text, SilentButton
from views.base_element import Button, EditBox
from views.base_view import BaseView


class AmountEditBox(EditBox, Button):
    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver, accessibility_id="amount-input")

    def send_keys(self, value):
        EditBox.send_keys(self, value)
        self.driver.press_keycode(66)


class ChooseRecipientButton(Button):
    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver, accessibility_id="choose-recipient-button")

    def click(self):
        self.click_until_presence_of_element(Button(self.driver, translation_id="my-accounts"))
        return self.navigate()


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
        super(ValidationErrorOnSendTransaction, self).__init__(driver,
                                                               xpath="//*[@text='%s']/../*[@content-desc='icon']" % field)


class NotEnoughEthForGas(Text):
    def __init__(self, driver):
        super().__init__(driver, translation_id="wallet-insufficient-gas")


class ValidationWarnings(object):
    def __init__(self, driver):
        self.not_enough_eth_for_gas = NotEnoughEthForGas(driver)


class SignWithKeycardButton(Button):
    def __init__(self, driver):
        super(SignWithKeycardButton, self).__init__(driver,
                                                    xpath="//*[contains(@text,'%s')]" % self.get_translation_by_key(
                                                        "sign-with"))

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        from views.keycard_view import KeycardView
        self.click_until_presence_of_element(KeycardView(self.driver).two_button)
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
        self.enter_recipient_address_text = Text(self.driver,
                                                 xpath="//*[@content-desc='choose-recipient-button']//android.widget.TextView")

        self.recent_recipients_button = Button(self.driver, translation_id="recent-recipients")
        self.amount_edit_box = AmountEditBox(self.driver)
        self.set_max_button = Button(self.driver, translation_id="set-max")
        self.validation_error_element = Text(self.driver,
                                             xpath="//*[@text='Network fee']/following-sibling::*[@content-desc='icon']")

        # Network fee elements
        self.network_fee_button = Button(self.driver, accessibility_id="custom-gas-fee")
        self.gas_limit_input = EditBox(self.driver, accessibility_id="gas-amount-limit")
        self.per_gas_tip_limit_input = EditBox(self.driver, accessibility_id="per-gas-tip-limit")
        self.per_gas_price_limit_input = EditBox(self.driver, accessibility_id="per-gas-price-limit")
        self.max_fee_text = Text(self.driver,
                                 xpath='//*[@text="Maximum fee:"]/following-sibling::android.widget.TextView[1]')
        self.save_fee_button = Button(self.driver, accessibility_id="save-fees")

        self.sign_transaction_button = Button(self.driver, accessibility_id="send-transaction-bottom-sheet")
        self.sign_with_keycard_button = SignWithKeycardButton(self.driver)
        self.sign_with_password = Button(self.driver, translation_id="sign-with-password")
        self.sign_button = Button(self.driver, translation_id="transactions-sign")
        self.sign_in_phrase_text = Text(self.driver, accessibility_id="signing-phrase-text")
        self.enter_password_input = EditBox(self.driver, accessibility_id="enter-password-input")
        self.got_it_button = Button(self.driver, accessibility_id="got-it-button")

        self.select_asset_button = Button(self.driver, accessibility_id="choose-asset-button")
        self.asset_text = Text(self.driver, xpath="//*[@content-desc='choose-asset-button']//android.widget.TextView")
        self.recipient_text = Text(self.driver,
                                   xpath="//*[@content-desc='choose-recipient-button']//android.widget.TextView")

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

        # Transaction management
        self.advanced_button = Button(self.driver, translation_id="advanced")
        self.nonce_input = EditBox(self.driver, accessibility_id="nonce")
        self.nonce_save_button = Button(self.driver, accessibility_id="save-nonce")

    def set_recipient_address(self, address):
        self.driver.info("Setting recipient address to '%s'" % address)
        self.chose_recipient_button.click()
        self.enter_recipient_address_input.send_keys(address)
        self.enter_recipient_address_input.click()
        self.done_button.click_until_absense_of_element(self.done_button)

    def sign_transaction(self, sender_password: str = common_password, keycard=False, error=False):
        self.driver.info("Signing transaction, (keycard: %s)" % str(keycard), device=False)
        if self.sign_in_phrase.is_element_displayed(30):
            self.set_up_wallet_when_sending_tx()
        if keycard:
            keycard_view = self.sign_with_keycard_button.click()
            keycard_view.enter_default_pin()
        else:
            self.sign_with_password.click_until_presence_of_element(self.enter_password_input)
            self.enter_password_input.send_keys(sender_password)
            self.sign_button.click_until_absense_of_element(self.sign_button)
        self.ok_button.wait_for_element(120)
        error_text = ''
        if error:
            error_text = Text(self.driver, id='android:id/message').text
        else:
            if self.element_by_text_part('Transaction failed').is_element_displayed():
                self.driver.fail('Transaction failed')
        self.driver.info("## Transaction is signed!", device=False)
        self.ok_button.click_until_absense_of_element(self.ok_button)
        return error_text

    @staticmethod
    def get_formatted_recipient_address(address):
        return address[:6] + '…' + address[-4:]

    def get_username_in_transaction_bottom_sheet_button(self, username_part):
        self.driver.info("Getting username by '%s' in transaction fee bottom sheet" % username_part)
        return SilentButton(self.driver,
                            xpath="//*[@content-desc='amount-input']/..//*[starts-with(@text,'%s')]" % username_part)

    def get_account_in_select_account_bottom_sheet_button(self, account_name):
        self.driver.info("Getting account by '%s' in transaction fee bottom sheet" % account_name)
        return SilentButton(self.driver, translation_id="select-account",
                            suffix="/..//*[starts-with(@text,'%s')]" % account_name)

    def get_validation_icon(self, field='Network fee'):
        return ValidationErrorOnSendTransaction(self.driver, field)

    def get_values_from_send_transaction_bottom_sheet(self):
        self.driver.info("Getting values from send transaction bottom sheet")
        data = {
            'amount': self.amount_edit_box.text,
            'asset': self.asset_text.text,
            'address': self.enter_recipient_address_text.text
        }
        return data

    def get_network_fee_from_bottom_sheet(self):
        self.driver.info("Getting network fee from send transaction bottom sheet")
        return Text(self.driver, xpath="//*[@content-desc='custom-gas-fee']/android.widget.TextView[1]").text[0:-9]

    def add_to_favorites(self, name):
        self.driver.info("Adding '%s' to favorite recipients" % name)
        self.recipient_add_to_favorites.click()
        self.new_favorite_name_input.send_keys(name)
        self.new_favorite_add_favorite.click()
