from views.base_element import BaseEditBox, BaseButton, BaseElement
from views.sign_in_view import SignInView


class PassphraseInput(BaseEditBox):

    def __init__(self, driver):
        super(PassphraseInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("enter-12-words")


class ConfirmRecoverAccess(BaseButton):

    def __init__(self, driver):
        super(ConfirmRecoverAccess, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='RECOVER ACCESS']")


class RequiredField(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector("Required field")


class InvalidRecoveryPhrase(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector("Recovery phrase is invalid")


class TooShortPassword(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector("Password is too short")


class MisspelledWords(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector("Some words might be misspelled")


class Warnings(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.required_field = RequiredField(driver)
        self.invalid_recovery_phrase = InvalidRecoveryPhrase(driver)
        self.too_short_password = TooShortPassword(driver)
        self.misspelled_words = MisspelledWords(driver)


class ConfirmPhraseButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.id("android:id/button1")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class CancelPhraseButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.id("android:id/button2")


class RecoverAccessView(SignInView):

    def __init__(self, driver):
        super(RecoverAccessView, self).__init__(driver)
        self.driver = driver

        self.passphrase_input = PassphraseInput(self.driver)
        self.confirm_recover_access = ConfirmRecoverAccess(self.driver)
        self.warnings = Warnings(self.driver)
        self.confirm_phrase_button = ConfirmPhraseButton(self.driver)
        self.cancel_button = CancelPhraseButton(self.driver)
