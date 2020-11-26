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
        self.locator = self.Locator.text_selector("Cancel")


class SignTransactionButton(BaseButton):
    def __init__(self, driver):
        super(SignTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-transaction-bottom-sheet')


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('amount-input')

    def set_value(self, value):
        BaseEditBox.set_value(self, value)
        self.driver.press_keycode(66)

class SetMaxButton(BaseButton):

    def __init__(self, driver):
        super(SetMaxButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Set max')


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

    def click(self):
        self.click_until_presence_of_element(AccountsButton(self.driver))
        return self.navigate()


class AccountsButton(BaseButton):
    def __init__(self, driver):
        super(AccountsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('My accounts')


class EnterRecipientAddressButton(BaseButton):
    def __init__(self, driver):
        super(EnterRecipientAddressButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('choose-recipient-recipient-code')


class ScanQRCodeButton(BaseButton):
    def __init__(self, driver):
        super(ScanQRCodeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('scan-contact-code-button')


class EnterRecipientAddressInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterRecipientAddressInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("recipient-address-input")


class EnterRecipientAddressInputText(BaseText):
    def __init__(self, driver):
        super(EnterRecipientAddressInputText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='choose-recipient-button']//android.widget.TextView")


class RecentRecipientsButton(BaseButton):
    def __init__(self, driver):
        super(RecentRecipientsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']")


class SelectAssetButton(BaseButton):
    def __init__(self, driver):
        super(SelectAssetButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('choose-asset-button')

class AssetText(BaseText):
    def __init__(self, driver):
        super(AssetText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="choose-asset-button"]//android.widget.TextView')

class RecipientText(BaseText):
    def __init__(self, driver):
        super(RecipientText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="choose-recipient-button"]//android.widget.TextView')


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
        self.locator = self.Locator.accessibility_id('custom-gas-fee')


class TransactionFeeButton(BaseButton):
    def __init__(self, driver):
        super(TransactionFeeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('transaction-fee-button')


class TransactionFeeTotalValue(BaseText):
    def __init__(self, driver):
        super(TransactionFeeTotalValue, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Total Fee']//following::android.widget.TextView[1]")


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

class ETHroAssetButtonInSelectAssetBottomSheet(BaseButton):
    def __init__(self, driver):
        super(ETHroAssetButtonInSelectAssetBottomSheet, self).__init__(driver)
        self.locator = self.Locator.accessibility_id(':ETH-asset-value')


class UpdateFeeButton(BaseButton):
    def __init__(self, driver):
        super(UpdateFeeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Update']")

    def click(self):
        for _ in range(3):
            self.driver.info('Tap on %s' % self.name)
            self.find_element().click()
            self.driver.info('Wait for no %s' % self.name)
            if not self.is_element_displayed():
                return self.navigate()


class ValidationErrorOnSendTransaction(BaseButton):
    def __init__(self, driver, field):
        super(ValidationErrorOnSendTransaction, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/../*[@content-desc='icon']" % field)


class ValidationIconOnSendTransaction(BaseButton):
    def __init__(self, driver):
        super(ValidationIconOnSendTransaction, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="custom-gas-fee"]/../android.view.ViewGroup//*[@content-desc="icon"]')


class ShareButton(BaseButton):
    def __init__(self, driver):
        super(ShareButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-address-button')

class RecipientAddToFavoritesButton(BaseButton):
    def __init__(self, driver):
        super(RecipientAddToFavoritesButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('participant-add-to-favs')

class RecipientDoneButton(BaseButton):
    def __init__(self, driver):
        super(RecipientDoneButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('participant-done')


class NewFavoriteNameInput(BaseEditBox):
    def __init__(self, driver):
        super(NewFavoriteNameInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('fav-name')


class NewFavoriteAddFavorite(BaseButton):
    def __init__(self, driver):
        super(NewFavoriteAddFavorite, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('add-fav')

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
        self.locator = self.Locator.text_selector('Sign with password')


class SignButton(BaseButton):
    def __init__(self, driver):
        super(SignButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Sign"]')


class SignWithKeycardButton(BaseButton):
    def __init__(self, driver):
        super(SignWithKeycardButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('Sign with')

    def navigate(self):
        from views.keycard_view import KeycardView
        return KeycardView(self.driver)

    def click(self):
        from views.keycard_view import TwoPinKeyboardButton
        self.click_until_presence_of_element(TwoPinKeyboardButton(self.driver))
        return self.navigate()

class SigningPhraseText(BaseText):
    def __init__(self, driver):
        super(SigningPhraseText, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('Signing phrase')


# Elements for commands in 1-1 chat
class UserNameInSendTransactionBottomSheet(BaseButton):
    def __init__(self, driver, username_part):
        super(UserNameInSendTransactionBottomSheet, self).__init__(driver)
        self.username = username_part
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='amount-input']/..//*[starts-with(@text,'%s')]" % self.username)

class AccountNameInSelectAccountBottomSheet(BaseButton):
    def __init__(self, driver, account_part):
        super(AccountNameInSelectAccountBottomSheet, self).__init__(driver)
        self.username = account_part
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Select account']/..//*[starts-with(@text,'%s')]" % self.username)


class SelectButton(BaseButton):
    def __init__(self, driver):
        super(SelectButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('select-account-bottom-sheet')

class RequestTransactionButtonBottomSheet(BaseButton):
    def __init__(self, driver):
        super(RequestTransactionButtonBottomSheet, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('request-transaction-bottom-sheet')

class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.accounts_button = AccountsButton(self.driver)
        self.enter_recipient_address_button = EnterRecipientAddressButton(self.driver)
        self.scan_qr_code_button = ScanQRCodeButton(self.driver)
        self.enter_recipient_address_input = EnterRecipientAddressInput(self.driver)
        self.first_recipient_button = FirstRecipient(self.driver)
        self.enter_recipient_address_text = EnterRecipientAddressInputText(self.driver)
        self.recent_recipients_button = RecentRecipientsButton(self.driver)
        self.amount_edit_box = AmountEditBox(self.driver)
        self.set_max_button = SetMaxButton(self.driver)
        self.validation_error_element = ValidationIconOnSendTransaction(self.driver)

        self.network_fee_button = NetworkFeeButton(self.driver)
        self.transaction_fee_button = TransactionFeeButton(self.driver)
        self.transaction_fee_total_value = TransactionFeeTotalValue(self.driver)
        self.gas_limit_input = GasLimitInput(self.driver)
        self.gas_price_input = GasPriceInput(self.driver)
        self.total_fee_input = TotalFeeInput(self.driver)
        self.update_fee_button = UpdateFeeButton(self.driver)

        self.cancel_button = CancelButton(self.driver)
        self.sign_transaction_button = SignTransactionButton(self.driver)
        self.sign_with_keycard_button = SignWithKeycardButton(self.driver)
        self.sign_with_password = SignWithPasswordButton(self.driver)
        self.sign_button = SignButton(self.driver)
        self.sign_in_phrase_text = SignInPhraseText(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.enter_password_input = EnterPasswordInput(self.driver)
        self.got_it_button = GotItButton(self.driver)

        self.select_asset_button = SelectAssetButton(self.driver)
        self.asset_text = AssetText(self.driver)
        self.recipient_text = RecipientText(self.driver)

        self.error_dialog = ErrorDialog(self.driver)

        self.share_button = ShareButton(self.driver)

        self.onboarding_message = OnboardingMessage(self.driver)
        self.validation_warnings = ValidationWarnings(self.driver)
        self.eth_asset_in_select_asset_bottom_sheet_button = ETHroAssetButtonInSelectAssetBottomSheet(self.driver)

        # Elements for commands in 1-1 chat
        self.select_button = SelectButton(self.driver)
        self.request_transaction_button = RequestTransactionButtonBottomSheet(self.driver)

        # Elements on set recipient screen
        self.recipient_add_to_favorites = RecipientAddToFavoritesButton(self.driver)
        self.recipient_done = RecipientDoneButton(self.driver)
        self.new_favorite_name_input = NewFavoriteNameInput(self.driver)
        self.new_favorite_add_favorite = NewFavoriteAddFavorite(self.driver)

    def complete_onboarding(self):
        if self.onboarding_message.is_element_displayed():
            from views.wallet_view import WalletView
            wallet_view = WalletView(self.driver)
            wallet_view.ok_got_it_button.click()

    def set_recipient_address(self, address):
        self.chose_recipient_button.click()
        self.enter_recipient_address_input.set_value(address)
        self.enter_recipient_address_input.click()
        self.done_button.click_until_absense_of_element(self.done_button)

    def sign_transaction(self, sender_password: str = common_password, keycard=False, default_gas_price=True):
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
        self.ok_button.click()

    def get_transaction_fee_total(self):
        self.network_fee_button.click_until_presence_of_element(self.gas_limit_input)
        fee_value = self.transaction_fee_total_value.text.split()[0]
        self.update_fee_button.click()
        return fee_value

    def get_formatted_recipient_address(self, address):
        return address[:6] + '…' + address[-4:]

    def get_username_in_transaction_bottom_sheet_button(self, username_part):
        return UserNameInSendTransactionBottomSheet(self.driver, username_part)

    def get_account_in_select_account_bottom_sheet_button(self, account_name):
        return AccountNameInSelectAccountBottomSheet(self.driver, account_name)

    def get_validation_icon(self, field='Network fee'):
        return ValidationErrorOnSendTransaction(self.driver, field)

    def get_values_from_send_transaction_bottom_sheet(self, gas=False):
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
        self.recipient_add_to_favorites.click()
        self.new_favorite_name_input.set_value(name)
        self.new_favorite_add_favorite.click()