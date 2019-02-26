from tests import common_password
from views.base_element import BaseText, BaseElement
from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView, OkButton, ProgressBar


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-icon')


class CancelButton(BaseButton):
    def __init__(self, driver):
        super(CancelButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('cancel-button')


class SignTransactionButton(BaseButton):
    def __init__(self, driver):
        super(SignTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('sign-transaction-button')


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('amount-input')


class SignInPhraseText(BaseText):
    def __init__(self, driver):
        super(SignInPhraseText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('signing-phrase-text')


class PasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Password']")


class EnterPasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterPasswordInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('enter-password-input')


class GotItButton(BaseButton):
    def __init__(self, driver):
        super(GotItButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('got-it-button')


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('choose-recipient-button')


class EnterRecipientAddressButton(BaseButton):
    def __init__(self, driver):
        super(EnterRecipientAddressButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Enter recipient address']")


class ScanQRCodeButton(BaseButton):
    def __init__(self, driver):
        super(ScanQRCodeButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Scan QR code')


class EnterRecipientAddressInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterRecipientAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Enter recipient address']")


class RecentRecipientsButton(BaseButton):
    def __init__(self, driver):
        super(RecentRecipientsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']")


class SelectAssetButton(BaseButton):
    def __init__(self, driver):
        super(SelectAssetButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('choose-asset-button')


class ErrorDialog(BaseView):
    def __init__(self, driver):
        super(ErrorDialog, self).__init__(driver)
        self.ok_button = OkButton(driver)

    def wait_for_error_message(self, error_message, wait_time=30):
        element = self.element_by_text_part(error_message)
        return element.wait_for_element(wait_time)


class AdvancedButton(BaseButton):
    def __init__(self, driver):
        super(AdvancedButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('advanced-button')


class TransactionFeeButton(BaseButton):
    def __init__(self, driver):
        super(TransactionFeeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('transaction-fee-button')


class TransactionFeeTotalValue(BaseButton):
    def __init__(self, driver):
        super(TransactionFeeTotalValue, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='transaction-fee-button']"
                                                   "/android.widget.TextView[1]")


class GasLimitInput(BaseEditBox):
    def __init__(self, driver):
        super(GasLimitInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('gas-limit-input')


class GasPriceInput(BaseEditBox):
    def __init__(self, driver):
        super(GasPriceInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('gas-price-input')


class TotalFeeInput(BaseEditBox):
    def __init__(self, driver):
        super(TotalFeeInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='total-fee-input']/android.widget.TextView")


class ShareButton(BaseButton):

    def __init__(self, driver):
        super(ShareButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-button')


class OnboardingMessage(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Set up your wallet')


class NotEnoughEthForGas(BaseText):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Not enough ETH for gas')


class ValidationWarnings(object):
    def __init__(self, driver):
        self.not_enough_eth_for_gas = NotEnoughEthForGas(driver)


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.enter_recipient_address_button = EnterRecipientAddressButton(self.driver)
        self.scan_qr_code_button = ScanQRCodeButton(self.driver)
        self.enter_recipient_address_input = EnterRecipientAddressInput(self.driver)
        self.first_recipient_button = FirstRecipient(self.driver)
        self.recent_recipients_button = RecentRecipientsButton(self.driver)
        self.amount_edit_box = AmountEditBox(self.driver)

        self.advanced_button = AdvancedButton(self.driver)
        self.transaction_fee_button = TransactionFeeButton(self.driver)
        self.transaction_fee_total_value = TransactionFeeTotalValue(self.driver)
        self.gas_limit_input = GasLimitInput(self.driver)
        self.gas_price_input = GasPriceInput(self.driver)
        self.total_fee_input = TotalFeeInput(self.driver)

        self.cancel_button = CancelButton(self.driver)
        self.sign_transaction_button = SignTransactionButton(self.driver)
        self.sign_in_phrase_text = SignInPhraseText(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.enter_password_input = EnterPasswordInput(self.driver)
        self.got_it_button = GotItButton(self.driver)

        self.select_asset_button = SelectAssetButton(self.driver)

        self.error_dialog = ErrorDialog(self.driver)

        self.share_button = ShareButton(self.driver)
        self.progress_bar = ProgressBar(self.driver)

        self.onboarding_message = OnboardingMessage(self.driver)
        self.validation_warnings = ValidationWarnings(self.driver)

    def complete_onboarding(self):
        if self.onboarding_message.is_element_displayed():
            from views.wallet_view import WalletView
            wallet_view = WalletView(self.driver)
            wallet_view.done_button.click()
            self.yes_button.click()

    def sign_transaction(self, sender_password: str = common_password):
        self.sign_transaction_button.click_until_presence_of_element(self.enter_password_input)
        self.enter_password_input.send_keys(sender_password)
        self.sign_transaction_button.click_until_presence_of_element(self.got_it_button)
        self.progress_bar.wait_for_invisibility_of_element(20)
        self.got_it_button.click()

    def get_transaction_fee_total(self):
        return self.transaction_fee_total_value.text.split()[0]

