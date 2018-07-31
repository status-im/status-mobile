from views.base_element import BaseEditBox, BaseButton
from views.sign_in_view import SignInView


class PassphraseInput(BaseEditBox):

    def __init__(self, driver):
        super(PassphraseInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[contains(@text,'phrase')]")


class ConfirmRecoverAccess(BaseButton):

    def __init__(self, driver):
        super(ConfirmRecoverAccess, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='RECOVER ACCESS']")


class RecoverAccessView(SignInView):

    def __init__(self, driver):
        super(RecoverAccessView, self).__init__(driver)
        self.driver = driver

        self.passphrase_input = PassphraseInput(self.driver)
        self.confirm_recover_access = ConfirmRecoverAccess(self.driver)
