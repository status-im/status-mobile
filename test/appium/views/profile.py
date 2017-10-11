from views.base_view import BaseViewObject
from views.base_element import *


class PublicKeyText(BaseText):

    def __init__(self, driver):
        super(PublicKeyText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-public-key')

    @property
    def text(self):
        text = self.scroll_to_element().text
        logging.info('%s is %s' % (self.name, text))
        return text


class ProfileAddressText(BaseText):

    def __init__(self, driver):
        super(ProfileAddressText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-address')


class NetworkSettingsButton(BaseButton):

    def __init__(self, driver):
        super(NetworkSettingsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Network settings"]')

    class Ropsten(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.Ropsten, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Ropsten"]')

    class RopstenWithUpstreamRPC(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.RopstenWithUpstreamRPC, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Ropsten with upstream RPC"]')

    class Rinkeby(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.Rinkeby, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Rinkeby"]')

    class RinkebyWithUpstreamRPC(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.RinkebyWithUpstreamRPC, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Rinkeby with upstream RPC"]')

    class Mainnet(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.Mainnet, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Mainnet"]')

    class MainnetWithUpstreamRPC(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.MainnetWithUpstreamRPC, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="Mainnet with upstream RPC"]')

    class ConnectButton(BaseButton):

        def __init__(self, driver):
            super(NetworkSettingsButton.ConnectButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//*[@text="CONNECT"]')



class ProfileViewObject(BaseViewObject):

    def __init__(self, driver):
        super(ProfileViewObject, self).__init__(driver)
        self.driver = driver

        self.public_key_text = PublicKeyText(self.driver)
        self.profile_address_text = ProfileAddressText(self.driver)

        self.network_settings_button = NetworkSettingsButton(self.driver)

        self.ropsten = NetworkSettingsButton.Ropsten(self.driver)
        self.ropsten_upstream_rpc = NetworkSettingsButton.RopstenWithUpstreamRPC(self.driver)
        self.rinkeby = NetworkSettingsButton.Rinkeby(self.driver)
        self.rinkeby_upstream_rpc_ = NetworkSettingsButton.RinkebyWithUpstreamRPC(self.driver)
        self.mainnet = NetworkSettingsButton.Mainnet(self.driver)
        self.mainnet_upstream_rpc = NetworkSettingsButton.MainnetWithUpstreamRPC(self.driver)
        self.connect_button = NetworkSettingsButton.ConnectButton(self.driver)

    def switch_network(self, network):
        self.network_settings_button.scroll_to_element()
        self.network_settings_button.click()
        networks = {'Ropsten': self.ropsten,
                    'Ropsten with upstream RPC': self.ropsten_upstream_rpc,
                    'Rinkeby': self.rinkeby,
                    'Rinkeby with upstream RPC': self.ropsten_upstream_rpc,
                    'Mainnet': self.mainnet,
                    'Mainnet with upstream RPC': self.mainnet_upstream_rpc}
        networks[network].click()
        self.connect_button.click()
        from views.login import LoginView
        return LoginView(self.driver)
