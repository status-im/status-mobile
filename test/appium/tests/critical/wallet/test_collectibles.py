import re
import time

import pytest
from selenium.common import TimeoutException

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests import marks
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_one_2")
@marks.nightly
@marks.secured
@marks.smoke
class TestWalletCollectibles(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_2']
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.sign_in_view.recover_access(passphrase=self.sender['passphrase'])

        self.home_view = self.sign_in_view.get_home_view()
        self.sender_username = self.home_view.get_username()
        self.profile_view = self.home_view.profile_button.click()
        self.profile_view.switch_network()
        self.sign_in_view.sign_in(user_name=self.sender_username)
        self.home_view.wallet_tab.click()
        self.wallet_view = self.home_view.get_wallet_view()
        self.account_name = 'Account 1'
        self.sender_short_address = self.sender['wallet_address'].replace(self.sender['wallet_address'][6:-3],
                                                                          '…').lower()
        self.receiver_short_address = self.receiver['wallet_address'].replace(self.receiver['wallet_address'][6:-3],
                                                                              '…').lower()
        self.network_name = 'Base'

    @marks.testrail_id(741839)
    def test_wallet_collectibles_balance(self):
        self.wallet_view.collectibles_tab.click()
        for _ in range(4):
            self.wallet_view.pull_to_refresh()
            if not self.wallet_view.element_by_text('No collectibles').is_element_displayed():
                return
            time.sleep(5)

        self.wallet_view.set_network_in_wallet(self.network_name + ', NEW')
        collectibles = {
            "BVL": {"quantity": 2,
                    "info": {"Account": "Account 1",
                             "Network": "Base",
                             "category": "Football Player",
                             "rank": "Star",
                             "type": "Modric"}},
            "Glitch Punks": {"quantity": 1,
                             "info": {"Account": "Account 1",
                                      "Network": "Base",
                                      "Race": "Skull Blue",
                                      "Mouth": "Lipstick Green",
                                      "Eyes": "Femme Shade Eyes Variant 3",
                                      "Face": "Pipe",
                                      "Ear Accessory": "Silver Stud Cross Combo",
                                      "Nose": "Bot Nose 3",
                                      "Eye Accessory": "Nouns",
                                      "Head": "Double Spike"}}
        }
        for collectible_name, data in collectibles.items():
            self.wallet_view.just_fyi("Check %s collectible info and image" % collectible_name)
            try:
                element = self.wallet_view.get_collectible_element(collectible_name)
                element.wait_for_element()
            except TimeoutException:
                self.errors.append(self.wallet_view, "Collectible '%s' is not displayed" % collectible_name)
                continue
            if element.image_element.is_element_differs_from_template(
                    '%s_collectible_image_template.png' % collectible_name):
                self.errors.append(self.wallet_view, "%s image doesn't match expected template" % collectible_name)
            if element.quantity != data['quantity']:
                self.errors.append(self.wallet_view, "%s quantity %s doesn't match expected %s" % (
                    collectible_name, element.quantity, data['quantity']))
            self.wallet_view.just_fyi("Check %s collectible expanded info" % collectible_name)
            element.click()
            if self.wallet_view.expanded_collectible_image.is_element_differs_from_template(
                    '%s_expanded_collectible_image_template.png' % collectible_name):
                self.errors.append(self.wallet_view,
                                   "%s expanded image doesn't match expected template" % collectible_name)
            self.wallet_view.driver.swipe(500, 2000, 500, 300)
            for item, expected_text in data['info'].items():
                try:
                    text = self.wallet_view.get_data_item_element_text(item)
                    if text != expected_text:
                        self.errors.append(self.wallet_view, "%s: shown %s text '%s' doesn't match expected '%s'" % (
                            collectible_name, item, text, expected_text))
                except TimeoutException:
                    self.errors.append(self.wallet_view, "%s: %s data item is not shown" % (collectible_name, item))
            self.wallet_view.click_system_back_button()
        self.errors.verify_no_errors()

    @marks.testrail_id(741840)
    def test_wallet_send_collectible(self):
        self.wallet_view.get_account_element().click()
        self.wallet_view.send_button.click()
        self.wallet_view.address_text_input.send_keys(self.receiver['wallet_address'])
        self.wallet_view.continue_button.click()
        self.wallet_view.collectibles_tab_on_select_token_view.click()
        self.wallet_view.get_collectible_element('BVL').wait_and_click(20)
        self.wallet_view.confirm_button.click()
        data_to_find_on_review = {
            self.account_name: ("From", self.wallet_view.from_data_container),
            self.sender_short_address: ("From", self.wallet_view.from_data_container),
            self.network_name: ("On", self.wallet_view.on_data_container),
            self.receiver_short_address: ("To", self.wallet_view.to_data_container)
        }

        for text, (label, container) in data_to_find_on_review.items():
            if not container.get_child_element_by_text(text).is_element_displayed():
                self.errors.append(
                    self.wallet_view,
                    "Text %s is not shown inside '%s' container on the Review Send page" % (text, label)
                )
        data_to_check = {
            'Est. time': r'~\d+ sec',
            'Max fees': r"[$]\d+.\d+",
            'Recipient gets': '1 '
        }
        for key, expected_value in data_to_check.items():
            try:
                text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                if key == 'Max fees':
                    if not re.findall(expected_value, text):
                        self.errors.append(self.wallet_view,
                                           "Max fee is not a number - %s on the Review Send page" % text)
                elif key == 'Est. time':
                    if not re.findall(expected_value, text) or int(re.findall(r'\d+', text)[0]) > 60:
                        self.errors.append(
                            self.wallet_view,
                            "Unexpected value '%s' is shown for est. time on the Review Send page" % text)
                else:
                    if text != expected_value:
                        self.errors.append(
                            self.wallet_view,
                            "%s text %s doesn't match expected %s on the Review Send page" % (
                                key, text, expected_value))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s is not shown on the Review Send page" % key)

        self.wallet_view.slide_button_track.slide()
        if not self.wallet_view.password_input.is_element_displayed():
            self.errors.append(self.wallet_view, "Can't confirm transaction")

        self.wallet_view.click_system_back_button(times=6)
        self.errors.verify_no_errors()

    @marks.testrail_id(741841)
    def test_wallet_collectible_send_from_expanded_info_view(self):
        self.wallet_view.collectibles_tab.click()
        self.wallet_view.get_collectible_element('Glitch Punks').wait_for_element().click()
        self.wallet_view.send_from_collectible_info_button.click()
        self.wallet_view.address_text_input.send_keys(self.receiver['wallet_address'])
        self.wallet_view.continue_button.click()
        data_to_find_on_review = {
            self.account_name: ("From", self.wallet_view.from_data_container),
            self.sender_short_address: ("From", self.wallet_view.from_data_container),
            self.network_name: ("On", self.wallet_view.on_data_container),
            self.receiver_short_address: ("To", self.wallet_view.to_data_container)
        }

        for text, (label, container) in data_to_find_on_review.items():
            if not container.get_child_element_by_text(text).is_element_displayed():
                self.errors.append(
                    self.wallet_view,
                    "Text %s is not shown inside '%s' container on the Review Send page" % (text, label)
                )
        data_to_check = {
            'Est. time': r'~\d+ sec',
            'Max fees': r"[$]\d+.\d+",
            'Recipient gets': '1 '
        }
        for key, expected_value in data_to_check.items():
            try:
                text = self.wallet_view.get_data_item_element_text(data_item_name=key)
                if key == 'Max fees':
                    if not re.findall(expected_value, text):
                        self.errors.append(self.wallet_view,
                                           "Max fee is not a number - %s on the Review Send page" % text)
                elif key == 'Est. time':
                    if not re.findall(expected_value, text) or int(re.findall(r'\d+', text)[0]) > 60:
                        self.errors.append(
                            self.wallet_view,
                            "Unexpected value '%s' is shown for est. time on the Review Send page" % text)
                else:
                    if text != expected_value:
                        self.errors.append(
                            self.wallet_view,
                            "'%s' text '%s' doesn't match expected '%s' on the Review Send page" % (
                                key, text, expected_value))
            except TimeoutException:
                self.errors.append(self.wallet_view, "%s is not shown on the Review Send page" % key)

        self.wallet_view.slide_button_track.slide()
        if not self.wallet_view.password_input.is_element_displayed():
            self.errors.append(self.wallet_view, "Can't confirm transaction")

        self.errors.verify_no_errors()
