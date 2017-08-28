from views.base_view import BaseViewObject
from views.base_element import *


class PublicKeyText(BaseText):

    def __init__(self, driver):
        super(PublicKeyText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView")

    def get_key(self):
        texts = self.driver.find_elements(self.locator.by, self.locator.value)
        for i in texts:
            if i.text.startswith('0x04'):
                return i.text
        pytest.fail("Public key wasn't found!")


class ProfileViewObject(BaseViewObject):

    def __init__(self, driver):
        super(ProfileViewObject, self).__init__(driver)
        self.driver = driver

        self.public_key_text = PublicKeyText(self.driver)
