from tests import marks, test_dapp_name
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


class TestDApps(SingleDeviceTestCase):

    @marks.testrail_id(6635)
    @marks.medium
    def test_webview_camera_permission(self):
        web_view_camera_url = 'https://simpledapp.status.im/webviewtest/webviewcamera.html'
        home = SignInView(self.driver).create_user()
        self.driver.set_clipboard_text(web_view_camera_url)
        dapp = home.dapp_tab_button.click()
        dapp.enter_url_editbox.click()
        dapp.paste_text()
        dapp.confirm()

        from views.web_views.base_web_view import BaseWebView
        camera_dapp = BaseWebView(self.driver)
        camera_dapp.just_fyi("Check camera request blocked (because it's not enabled in app yet)")
        camera_request_blocked = home.get_translation_by_key("page-camera-request-blocked")
        if not dapp.element_by_text_part(camera_request_blocked).is_element_displayed():
            self.driver.fail("There is no pop-up notifying that camera access need to be granted in app")
        camera_dapp.swipe_down()
        if not camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera permissions not allowed - acccess to camera granted")

        profile = home.profile_button.click()
        profile.privacy_and_security_button.click()

        camera_dapp.just_fyi("Enable camera requests in Dapps")
        camera_permission_requests = home.get_translation_by_key("webview-camera-permission-requests")
        if profile.element_by_text_part(camera_permission_requests).is_element_displayed():
            profile.element_by_text_part('Webview camera permission requests').click()
        home.dapp_tab_button.click(desired_element_text='webview')

        camera_dapp.just_fyi("Check DApp asks now to allow camera aceess but Deny in DApp")
        camera_dapp.browser_refresh_page_button.click()
        camera_dapp.deny_button.click()
        if not camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera access Denied to Dapp, - acccess to camera granted")

        camera_dapp.just_fyi("Check DApp asks now to allow camera aceess and Allow access to DApp")
        camera_dapp.browser_refresh_page_button.click()
        camera_dapp.allow_button.click()
        if camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera access Accepted to Dapp, - camera view is not shown")

        camera_dapp.just_fyi("Relogin and check camera access still needs to be allowed")
        home.profile_button.click()
        profile.relogin()
        home.dapp_tab_button.click()
        camera_dapp.open_tabs_button.click()
        dapp.element_by_text_part("https").click()
        if not camera_dapp.allow_button.is_element_displayed():
            self.driver.fail("No request to camera access after relogin")

    @marks.testrail_id(6323)
    @marks.medium
    @marks.flaky
    def test_resolve_ipns_name(self):
        user = basic_user
        ipns_url = 'uniswap.eth'
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(passphrase=user['passphrase'])
        profile_view = home_view.profile_button.click()
        profile_view.switch_network()
        self.driver.set_clipboard_text(ipns_url)
        dapp_view = home_view.dapp_tab_button.click()
        dapp_view.enter_url_editbox.click()
        dapp_view.paste_text()
        dapp_view.confirm()
        if not dapp_view.allow_button.is_element_displayed(30):
            self.driver.fail('No permission is asked for dapp, so IPNS name is not resolved')

    @marks.testrail_id(6232)
    @marks.medium
    def test_switching_accounts_in_dapp(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()

        wallet_view.just_fyi('create new account in multiaccount')
        wallet_view.set_up_wallet()
        status_account = home_view.status_account_name
        account_name = 'Subaccount'
        wallet_view.add_account(account_name)
        address = wallet_view.get_wallet_address(account_name)

        sign_in_view.just_fyi('can see two accounts in DApps')
        dapp_view = sign_in_view.dapp_tab_button.click()
        dapp_view.select_account_button.click()
        for text in 'Select the account', status_account, account_name:
            if not dapp_view.element_by_text_part(text).is_element_displayed():
                self.driver.fail("No expected element %s is shown in menu" % text)

        sign_in_view.just_fyi('add permission to Status account')
        dapp_view.enter_url_editbox.click()
        status_test_dapp = home_view.open_status_test_dapp()

        sign_in_view.just_fyi('check that permissions from previous account was removed once you choose another')
        dapp_view.select_account_button.click()
        dapp_view.select_account_by_name(account_name).wait_for_element(30)
        dapp_view.select_account_by_name(account_name).click()
        profile_view = dapp_view.profile_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.dapp_permissions_button.click()
        if profile_view.element_by_text(test_dapp_name).is_element_displayed():
            self.errors.append("Permissions for %s are not removed" % test_dapp_name)

        sign_in_view.just_fyi('check that can change account')
        profile_view.dapp_tab_button.click(desired_element_text='Allow')
        if not status_test_dapp.element_by_text_part(account_name).is_element_displayed():
            self.errors.append("No expected account %s is shown in authorize web3 popup for wallet" % account_name)
        status_test_dapp.allow_button.click()
        dapp_view.profile_button.click(desired_element_text='DApp permissions')
        profile_view.element_by_text(test_dapp_name).click()
        for text in 'Chat key', account_name:
            if not dapp_view.element_by_text_part(text).is_element_displayed():
                self.errors.append("Access is not granted to %s" % text)

        sign_in_view.just_fyi('check correct account is shown for transaction if sending from DApp')
        profile_view.dapp_tab_button.click(desired_element_text='Accounts')
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        address = send_transaction_view.get_formatted_recipient_address(address)
        if not send_transaction_view.element_by_text(address).is_element_displayed():
            self.errors.append("Wallet address %s in not shown in 'From' on Send Transaction screen" % address)

        sign_in_view.just_fyi('Relogin and check multiaccount loads fine')
        send_transaction_view.cancel_button.click()
        sign_in_view.profile_button.click()
        sign_in_view.relogin()
        sign_in_view.wallet_button.click()
        if not wallet_view.element_by_text(account_name).is_element_displayed():
            self.errors.append("Subaccount is gone after relogin in Wallet!")
        sign_in_view.profile_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.dapp_permissions_button.click()
        profile_view.element_by_text(test_dapp_name).click()
        if not profile_view.element_by_text(account_name).is_element_displayed():
            self.errors.append("Subaccount is not selected after relogin in Dapps!")
        self.errors.verify_no_errors()

