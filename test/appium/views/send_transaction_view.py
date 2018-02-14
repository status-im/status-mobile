from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView
from selenium.common.exceptions import NoSuchElementException, TimeoutException


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient']/.."
                                                   "//android.widget.ImageView[@content-desc='chat-icon']")


class SignTransactionButton(BaseButton):
    def __init__(self, driver):
        super(SignTransactionButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SIGN TRANSACTION']")


class SignLaterButton(BaseButton):
    def __init__(self, driver):
        super(SignLaterButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SIGN LATER']")


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Amount']/..//android.widget.EditText")


class PasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Password']")


class EnterPasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterPasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class ConfirmButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='CONFIRM']")


class GotItButton(BaseButton):
    def __init__(self, driver):
        super(GotItButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='GOT IT']")


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Specify recipient...']")

    def click(self):
        desired_element = EnterContactCodeButton(self.driver)
        self.click_until_presence_of_element(desired_element=desired_element)


class EnterContactCodeButton(BaseButton):
    def __init__(self, driver):
        super(EnterContactCodeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Enter contact code']")


class EnterRecipientAddressInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterRecipientAddressInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Enter recipient address']")


class RecentRecipientsButton(BaseButton):
    def __init__(self, driver):
        super(RecentRecipientsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Recent recipients']")


class SelectAssetButton(BaseButton):
    def __init__(self, driver):
        super(SelectAssetButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[4]/..')


class STTButton(BaseButton):
    def __init__(self, driver):
        super(STTButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Status Test Token']")


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.enter_contact_code_button = EnterContactCodeButton(self.driver)
        self.enter_recipient_address_input = EnterRecipientAddressInput(self.driver)
        self.first_recipient_button = FirstRecipient(self.driver)
        self.recent_recipients_button = RecentRecipientsButton(self.driver)

        self.amount_edit_box = AmountEditBox(self.driver)
        self.sign_transaction_button = SignTransactionButton(self.driver)
        self.sign_later_button = SignLaterButton(self.driver)
        self.confirm_button = ConfirmButton(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.enter_password_input = EnterPasswordInput(self.driver)
        self.got_it_button = GotItButton(self.driver)

        self.select_asset_button = SelectAssetButton(self.driver)
        self.stt_button = STTButton(self.driver)

    def sign_transaction(self, sender_password):
        self.sign_transaction_button.click_until_presence_of_element(self.enter_password_input)
        self.enter_password_input.send_keys(sender_password)
        self.sign_transaction_button.click_until_presence_of_element(self.got_it_button)
        self.got_it_button.click()
