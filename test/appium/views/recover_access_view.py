from views.base_element import BaseEditBox, BaseButton
from views.sign_in_view import SignInView


class RecoveryPhraseInput(BaseEditBox):

    def __init__(self, driver):
        super(RecoveryPhraseInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('recovery-phrase')


class AccessButton(BaseButton):

    def __init__(self, driver):
        super(AccessButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('access-button')


class RecoverAccessView(SignInView):

    def __init__(self, driver):
        super(RecoverAccessView, self).__init__(driver)
        self.driver = driver

        self.recovery_phrase_input = RecoveryPhraseInput(self.driver)
        self.access_button = AccessButton(self.driver)
