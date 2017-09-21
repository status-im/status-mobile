from views.base_view import BaseViewObject
from views.base_element import *


class PublicKeyText(BaseText):

    def __init__(self, driver):
        super(PublicKeyText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-public-key')


class ProfileAddressText(BaseText):

    def __init__(self, driver):
        super(ProfileAddressText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-address')


class ProfileViewObject(BaseViewObject):

    def __init__(self, driver):
        super(ProfileViewObject, self).__init__(driver)
        self.driver = driver

        self.public_key_text = PublicKeyText(self.driver)
        self.profile_address_text = ProfileAddressText(self.driver)
