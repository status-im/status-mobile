from views.base_element import BaseElement, BaseButton, BaseEditBox, BaseText


class BackButton(BaseButton):

    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")


class BaseViewObject(object):

    def __init__(self, driver):
        self.driver = driver
        self.back_button = BackButton(self.driver)

    def confirm(self):
        self.driver.keyevent(66)

    def find_full_text(self, text):
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(60)

    def find_text_part(self, text):
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element.wait_for_element(60)

    def element_by_text(self, text, element_type='base'):

        element_types = {
            'base': BaseElement,
            'button': BaseButton,
            'edit_box': BaseEditBox,
            'text': BaseText
        }

        element = element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element

    def get_chats(self):
        from views.chats import ChatsViewObject
        return ChatsViewObject(self.driver)
