from views.base_element import BaseElement, BaseButton


class BackButton(BaseButton):

    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")


class ElementByName(BaseButton):
    def __init__(self, driver, user_name):
        super(ElementByName, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="' + user_name + '"]')


class BaseViewObject(object):

    def __init__(self, driver):
        self.driver = driver
        self.back_button = BackButton(self.driver)

    def confirm(self):
        self.driver.keyevent(66)

    def find_text(self, text):
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(120)

    def get_chats(self):
        from views.chats import ChatsViewObject
        return ChatsViewObject(self.driver)
