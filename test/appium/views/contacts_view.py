from views.base_element import *
from views.base_view import BaseView


class AuctionHouseButton(BaseButton):

    def __init__(self, driver):
        super(AuctionHouseButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "(//android.widget.TextView[@text='Auction House'])[1]")

    def navigate(self):
        from views.web_views.auction_house import AuctionHouseWebView
        return AuctionHouseWebView(self.driver)


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")


class PublicKeyEditBox(BaseEditBox):
    def __init__(self, driver):
        super(PublicKeyEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class ConfirmPublicKeyButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmPublicKeyButton, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


class ContactsView(BaseView):

    def __init__(self, driver):
        super(ContactsView, self).__init__(driver)
        self.driver = driver

        self.plus_button = PlusButton(self.driver)
        self.public_key_edit_box = PublicKeyEditBox(self.driver)
        self.confirm_public_key_button = ConfirmPublicKeyButton(self.driver)

        self.auction_house_button = AuctionHouseButton(self.driver)
