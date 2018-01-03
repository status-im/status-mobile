from views.base_view import BaseView
import time
from views.base_element import *


class AllRecent(BaseButton):

    def __init__(self, driver):
        super(AllRecent, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Recent statuses']/..//*[@text='ALL']")


class AllPopular(BaseButton):

    def __init__(self, driver):
        super(AllPopular, self).__init__(driver)

        self.locator = self.Locator.xpath_selector("//*[@text='Popular #hashtags']/..//*[@text='ALL']")


class DiscoverView(BaseView):

    def __init__(self, driver):
        super(DiscoverView, self).__init__(driver)

        self.driver = driver
        self.all_popular = AllPopular(self.driver)
        self.all_recent = AllRecent(self.driver)
