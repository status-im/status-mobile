from views.web_views.base_web_view import *


class ToggleNavigationButton(BaseButton):

    def __init__(self, driver):
        super(ToggleNavigationButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('Toggle navigation ')

    class NewAuctionButton(BaseButton):
        def __init__(self, driver):
            super(ToggleNavigationButton.NewAuctionButton, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('New Auction')


class ReserveAssetName(BaseElement):

    class NameToReserveInput(BaseEditBox, BaseButton):

        def __init__(self, driver):
            super(ReserveAssetName.NameToReserveInput, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                '(//android.widget.EditText[@content-desc="eg MyFamousWallet.eth"])[1]')

    class RegisterNameButton(BaseButton):

        def __init__(self, driver):
            super(ReserveAssetName.RegisterNameButton, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('Register Name')


class AuctionHouseWebView(BaseWebView):

    def __init__(self, driver):
        super(AuctionHouseWebView, self).__init__(driver)
        self.driver = driver
        self.wait_for_page_loaded()

        self.toggle_navigation_button = ToggleNavigationButton(self.driver)
        self.new_auction_button = ToggleNavigationButton.NewAuctionButton(self.driver)

        self.name_to_reserve_input = ReserveAssetName.NameToReserveInput(self.driver)
        self.register_name_button = ReserveAssetName.RegisterNameButton(self.driver)
