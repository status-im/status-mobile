from tests import common_password
from views.base_element import BaseText, BaseElement
from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView, OkButton


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-icon')


class CancelButton(BaseButton):
    def __init__(self, driver):
        super(CancelButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Cancel']")


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


class AccountsButton(BaseButton):
    def __init__(self, driver):
        super(AccountsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Accounts')


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


class NetworkFeeButton(BaseButton):
    def __init__(self, driver):
        super(NetworkFeeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Network fee"]')


class TransactionFeeButton(BaseButton):
    def __init__(self, driver):
        super(TransactionFeeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('transaction-fee-button')


class TransactionFeeTotalValue(BaseText):
    def __init__(self, driver):
        super(TransactionFeeTotalValue, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Network fee']"
                                                   "/following-sibling::android.widget.TextView")


class GasLimitInput(BaseEditBox):
    def __init__(self, driver):
        super(GasLimitInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("(//*[@text='Gas limit']/..//android.widget.EditText)[1]")


class GasPriceInput(BaseEditBox):
    def __init__(self, driver):
        super(GasPriceInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("(//*[@text='Gas limit']/..//android.widget.EditText)[2]")

    @property
    def text(self):
        return self.find_element().text


class TotalFeeInput(BaseText):
    def __init__(self, driver):
        super(TotalFeeInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Total Fee']/following-sibling::android.widget.TextView")


class UpdateFeeButton(BaseButton):
    def __init__(self, driver):
        super(UpdateFeeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Update']")


class ShareButton(BaseButton):

    def __init__(self, driver):
        super(ShareButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-address-button')


class OnboardingMessage(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('This is your signing phrase')


class NotEnoughEthForGas(BaseText):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Not enough ETH for gas')


class ValidationWarnings(object):
    def __init__(self, driver):
        self.not_enough_eth_for_gas = NotEnoughEthForGas(driver)


class SignWithPasswordButton(BaseButton):

    def __init__(self, driver):
        super(SignWithPasswordButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Sign with password"]')


class SignButton(BaseButton):

    def __init__(self, driver):
        super(SignButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Sign"]')


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.accounts_button = AccountsButton(self.driver)
        self.enter_recipient_address_button = EnterRecipientAddressButton(self.driver)
        self.scan_qr_code_button = ScanQRCodeButton(self.driver)
        self.enter_recipient_address_input = EnterRecipientAddressInput(self.driver)
        self.first_recipient_button = FirstRecipient(self.driver)
        self.recent_recipients_button = RecentRecipientsButton(self.driver)
        self.amount_edit_box = AmountEditBox(self.driver)

        self.network_fee_button = NetworkFeeButton(self.driver)
        self.transaction_fee_button = TransactionFeeButton(self.driver)
        self.transaction_fee_total_value = TransactionFeeTotalValue(self.driver)
        self.gas_limit_input = GasLimitInput(self.driver)
        self.gas_price_input = GasPriceInput(self.driver)
        self.total_fee_input = TotalFeeInput(self.driver)
        self.update_fee_button = UpdateFeeButton(self.driver)

        self.cancel_button = CancelButton(self.driver)
        self.sign_transaction_button = SignTransactionButton(self.driver)
        self.sign_with_password = SignWithPasswordButton(self.driver)
        self.sign_button = SignButton(self.driver)
        self.sign_in_phrase_text = SignInPhraseText(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.enter_password_input = EnterPasswordInput(self.driver)
        self.got_it_button = GotItButton(self.driver)

        self.select_asset_button = SelectAssetButton(self.driver)

        self.error_dialog = ErrorDialog(self.driver)

        self.share_button = ShareButton(self.driver)

        self.onboarding_message = OnboardingMessage(self.driver)
        self.validation_warnings = ValidationWarnings(self.driver)

    def complete_onboarding(self):
        if self.onboarding_message.is_element_displayed():
            from views.wallet_view import WalletView
            wallet_view = WalletView(self.driver)
            wallet_view.ok_got_it_button.click()

    def sign_transaction(self, sender_password: str = common_password):
        self.sign_with_password.click_until_presence_of_element(self.enter_password_input)
        self.enter_password_input.send_keys(sender_password)
        self.sign_button.click_until_presence_of_element(self.ok_button)
        self.ok_button.click()

    def get_transaction_fee_total(self):
        return self.transaction_fee_total_value.text.split()[0]

