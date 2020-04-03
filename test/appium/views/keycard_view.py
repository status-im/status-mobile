from views.base_element import BaseButton, BaseText, BaseElement, BaseEditBox
from views.base_view import BaseView


class BeginSetupButton(BaseButton):
    def __init__(self, driver):
        super(BeginSetupButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Begin setup")


class OnePinKeyboardButton(BaseButton):
    def __init__(self, driver):
        super(OnePinKeyboardButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("1")


class TwoPinKeyboardButton(BaseButton):
    def __init__(self, driver):
        super(TwoPinKeyboardButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("2")

class ConnectCardButton(BaseButton):
    def __init__(self, driver):
        super(ConnectCardButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("connect-card")


class DisconnectCardButton(BaseButton):
    def __init__(self, driver):
        super(DisconnectCardButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("disconnect-card")


class ResetCardButton(BaseButton):
    def __init__(self, driver):
        super(ResetCardButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("keycard-reset-state")


class RecoveryWordText(BaseText):
    def __init__(self, driver, word_id):
        super(RecoveryWordText, self).__init__(driver)
        self.word_id = word_id
        self.locator = self.Locator.accessibility_id("word%s" % word_id)

class WordNumberText(BaseText):
    def __init__(self, driver):
        super(WordNumberText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("word-number")

class ConfirmSeedPhraseInput(BaseEditBox):
    def __init__(self, driver):
        super(ConfirmSeedPhraseInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("enter-word")


class KeycardView(BaseView):
    def __init__(self, driver):
        super(KeycardView, self).__init__(driver)
        self.begin_setup_button = BeginSetupButton(self.driver)
        self.connect_card_button = ConnectCardButton(self.driver)
        self.disconnect_card_button = DisconnectCardButton(self.driver)
        self.reset_card_state_button = ResetCardButton(self.driver)

        #keyboard
        self.one_button = OnePinKeyboardButton(self.driver)
        self.two_button = TwoPinKeyboardButton(self.driver)

        #backup seed phrase
        self.confirm_seed_phrase_edit_box = ConfirmSeedPhraseInput(self.driver)

    def enter_default_pin(self):
        for _ in range(3):
            self.one_button.click()
            self.two_button.click()

    def get_recovery_word(self, word_id):
        word_element = RecoveryWordText(self.driver, word_id)
        return word_element.text

    def get_required_word_number(self):
        description = WordNumberText(self.driver)
        full_text = description.text
        if ("11" in full_text) or ("12" in full_text):
            word_number = full_text[-2:]
        else:
            word_number = full_text[-1]
        return word_number

    def backup_seed_phrase(self):
        recovery_phrase = dict()
        for i in range(0,12):
            word = self.get_recovery_word(i)
            recovery_phrase[str(i+1)] = word
        self.confirm_button.click()
        self.yes_button.click()
        for _ in range(2):
            number = self.get_required_word_number()
            self.confirm_seed_phrase_edit_box.set_value(recovery_phrase[number])
            self.next_button.click()
