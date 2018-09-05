from views.base_element import BaseButton, BaseText
from views.base_view import BaseView


class PrivacyPolicyButton(BaseButton):
    def __init__(self, driver):
        super(PrivacyPolicyButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//*[@content-desc="privacy-policy"]')

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class VersionInfo(BaseText):
    def __init__(self, driver):
        super(VersionInfo, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//*[@content-desc="version"]//android.widget.TextView')


class AboutView(BaseView):
    def __init__(self, driver):
        super(AboutView, self).__init__(driver)

        self.privacy_policy_button = PrivacyPolicyButton(self.driver)
        self.version_info = VersionInfo(self.driver)
