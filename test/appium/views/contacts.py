from views.base_element import BaseElement, BaseButton, BaseEditBox, BaseText
import logging
import time
import pytest


class AuctionHouseButton(BaseButton):

    def __init__(self, driver):
        super(AuctionHouseButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "(//android.widget.TextView[@text='Auction House'])[1]")

    def navigate(self):
        from views.web_views.auction_house import AuctionHouseWebView
        return AuctionHouseWebView(self.driver)


class ContactsViewObject(object):

    def __init__(self, driver):
        self.driver = driver

        self.auction_house_button = AuctionHouseButton(self.driver)
