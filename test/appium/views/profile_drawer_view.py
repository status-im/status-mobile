from views.base_view import BaseView
from views.base_element import *


class ProfileIcon(BaseButton):
    def __init__(self, driver):
        super(ProfileIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('drawer-profile-icon')

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)


class SwitchUsersButton(BaseButton):
    def __init__(self, driver):
        super(SwitchUsersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='SWITCH USERS']")

    def click(self):
        self.find_element().click()
        info('Tap on %s' % self.name)
        return self.navigate()

    def navigate(self):
        from views.sign_in_view import SignInView
        return SignInView(self.driver)


class ProfileDrawer(BaseView):
    def __init__(self, driver):
        super(ProfileDrawer, self).__init__(driver)
        self.driver = driver

        self.profile_icon = ProfileIcon(self.driver)
        self.switch_users_button = SwitchUsersButton(self.driver)
