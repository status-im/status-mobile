from views.base_element import Button, EditBox
from views.base_view import BaseView


class AmountEditBox(EditBox, Button):
    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver, accessibility_id="amount-input")

    def send_keys(self, value):
        EditBox.send_keys(self, value)
        self.driver.press_keycode(66)


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        # Elements on set recipient screen
        self.recipient_add_to_favorites = Button(self.driver, accessibility_id="participant-add-to-favs")
        self.recipient_done = Button(self.driver, accessibility_id="participant-done")
        self.new_favorite_name_input = EditBox(self.driver, accessibility_id="fav-name")
        self.new_favorite_add_favorite = Button(self.driver, accessibility_id="add-fav")

        # Transaction management
        self.advanced_button = Button(self.driver, translation_id="advanced")

    def add_to_favorites(self, name):
        self.driver.info("Adding '%s' to favorite recipients" % name)
        self.recipient_add_to_favorites.click()
        self.new_favorite_name_input.send_keys(name)
        self.new_favorite_add_favorite.click()
