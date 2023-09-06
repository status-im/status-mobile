from views.base_element import Button, Text, EditBox, SilentButton, CheckBox
from views.base_view import BaseView


class KeycardView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)
        self.begin_setup_button = Button(self.driver, translation_id="begin-set-up")
        self.connect_card_button = Button(self.driver, accessibility_id="connect-card")
        self.disconnect_card_button = Button(self.driver, accessibility_id="disconnect-card")
        self.reset_card_state_button = Button(self.driver, accessibility_id="keycard-reset-state")
        self.connect_selected_card_button = Button(self.driver, accessibility_id="connect-selected-card")
        self.pair_code_text = Text(self.driver, accessibility_id="pair-code")
        self.pair_code_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.pair_to_this_device_button = Button(self.driver, translation_id="pair-card")
        self.connect_pairing_card_button = Button(self.driver, accessibility_id="connect-pairing-card")
        self.return_card_to_factory_settings_checkbox = CheckBox(self.driver, accessibility_id=":checkbox-off")

        # Keyboard
        self.zero_button = SilentButton(self.driver, accessibility_id="numpad-button-0")
        self.one_button = SilentButton(self.driver, accessibility_id="numpad-button-1")
        self.two_button = SilentButton(self.driver, accessibility_id="numpad-button-2")

        # Backup seed phrase
        self.confirm_seed_phrase_edit_box = EditBox(self.driver, accessibility_id="enter-word")

    def enter_default_pin(self):
        self.driver.info("Enter default pin 111111")
        [self.one_button.click() for _ in range(6)]

    def enter_default_puk(self):
        self.driver.info("Enter default pin 1111 1111 1111")
        [self.one_button.click() for _ in range(12)]

    def enter_another_pin(self):
        self.driver.info("Enter not-default pin 222222")
        for _ in range(6):
            self.two_button.click()

    def get_recovery_word(self, word_id):
        word_element = SilentButton(self.driver, accessibility_id="word%s" % word_id)
        word_element.wait_for_visibility_of_element()
        return word_element.text

    def get_required_word_number(self):
        description = SilentButton(self.driver, accessibility_id="word-number")
        full_text = description.text
        word_number = ''.join(i for i in full_text if i.isdigit())
        return word_number

    def get_seed_phrase(self):
        recovery_phrase = dict()
        for i in range(0, 12):
            word = self.get_recovery_word(i)
            recovery_phrase[str(i + 1)] = word
        return recovery_phrase

    def backup_seed_phrase(self):
        self.driver.info("Backing up seed phrase for keycard")
        recovery_phrase = self.get_seed_phrase()
        self.confirm_button.click()
        self.yes_button.click()
        for _ in range(2):
            number = self.get_required_word_number()
            self.confirm_seed_phrase_edit_box.send_keys(recovery_phrase[number])
            self.next_button.click()
        return ' '.join(recovery_phrase.values())

    def confirm_pin_and_proceed(self):
        self.next_button.click()
        self.begin_setup_button.click()
        self.connect_card_button.wait_and_click()
        self.enter_default_pin()
        self.enter_default_pin()
