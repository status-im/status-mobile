from views.web_views.base_web_view import *


class ToggleNavigationButton(BaseButton):

    def __init__(self, driver):
        super(ToggleNavigationButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Toggle navigation ']")

    class NewAuctionButton(BaseButton):
        def __init__(self, driver):
            super(ToggleNavigationButton.NewAuctionButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='New Auction']")


class ReserveAssetName(BaseElement):

    class NameToReserveInput(BaseEditBox, BaseButton):

        def __init__(self, driver):
            super(ReserveAssetName.NameToReserveInput, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                '(//*[@text="Name To Reserve:"])[2]')

    class RegisterNameButton(BaseButton):

        def __init__(self, driver):
            super(ReserveAssetName.RegisterNameButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Register Name"]')


class AssetContract(BaseElement):

    def __init__(self, driver):
        super(AssetContract, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Asset contract"]')


class AuctionHouseWebView(BaseWebView):

    def __init__(self, driver):
        super(AuctionHouseWebView, self).__init__(driver)
        self.driver = driver

        self.toggle_navigation_button = ToggleNavigationButton(self.driver)
        self.new_auction_button = ToggleNavigationButton.NewAuctionButton(self.driver)

        self.name_to_reserve_input = ReserveAssetName.NameToReserveInput(self.driver)
        self.register_name_button = ReserveAssetName.RegisterNameButton(self.driver)
        self.asset_contract = AssetContract(self.driver)
