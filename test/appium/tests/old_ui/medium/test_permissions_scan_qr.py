import pytest

from tests import marks, test_dapp_url
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from tests.users import transaction_senders, basic_user, ens_user_message_sender, ens_user
from views.chat_view import ChatView
from views.profile_view import ProfileView
from views.send_transaction_view import SendTransactionView
from views.sign_in_view import SignInView
from views.web_views.base_web_view import BaseWebView


@pytest.mark.xdist_group(name="one_2")
@marks.medium
class TestPermissionsScanQrOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.recover_access(transaction_senders['C']['passphrase'])
        self.public_key = self.home.get_public_key()
        self.home.home_button.click()

    @marks.testrail_id(702289)
    def test_permissions_deny_access_camera_and_gallery(self):
        general_camera_error = self.home.element_by_translation_id("camera-access-error")

        self.home.just_fyi("Denying access to camera in universal qr code scanner")
        self.home.plus_button.click()
        self.home.universal_qr_scanner_button.click()
        self.home.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        self.home.ok_button.click()
        self.home.get_back_to_home_view()

        self.home.just_fyi("Denying access to camera in scan chat key view")
        self.home.plus_button.click()
        chat = self.home.start_new_chat_button.click()
        chat.scan_contact_code_button.click()
        chat.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        chat.ok_button.click()
        self.home.get_back_to_home_view()

        self.home.just_fyi("Denying access to gallery at attempt to send image")
        self.home.add_contact(basic_user['public_key'])
        chat.show_images_button.click()
        chat.deny_button.click()
        chat.element_by_translation_id("external-storage-denied").wait_for_visibility_of_element(3)
        chat.ok_button.click()

        self.home.just_fyi("Denying access to audio at attempt to record audio")
        chat.audio_message_button.click()
        chat.deny_button.click()
        chat.element_by_translation_id("audio-recorder-permissions-error").wait_for_visibility_of_element(3)
        chat.ok_button.click()
        self.home.get_back_to_home_view()

        self.home.just_fyi("Denying access to camera in wallet view")
        wallet = self.home.wallet_button.click()
        wallet.scan_qr_button.click()
        wallet.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        wallet.ok_button.click()

        self.home.just_fyi("Denying access to camera in send transaction > scan address view")
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.chose_recipient_button.scroll_and_click()
        send_transaction.scan_qr_code_button.click()
        send_transaction.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        send_transaction.ok_button.click()
        wallet.close_button.click()
        wallet.close_send_transaction_view_button.click()

        self.home.just_fyi("Allow access to camera in universal qr code scanner and check it in other views")
        wallet.home_button.click()
        self.home.plus_button.click()
        self.home.universal_qr_scanner_button.click()
        self.home.allow_button.click()
        if not self.home.element_by_text('Scan QR code').is_element_displayed():
            self.errors.append('Scan QR code is not opened after denying and allowing permission to the camera')
        self.home.cancel_button.click()
        wallet = self.home.wallet_button.click()
        wallet.scan_qr_button.click()
        if not self.home.element_by_text('Scan QR code').is_element_displayed():
            self.errors.append(
                'Scan QR code is not opened after allowing permission to the camera from univesal QR code'
                ' scanner view')
        wallet.cancel_button.click()
        wallet.home_button.click()
        self.home.get_chat(basic_user['username']).click()
        chat.show_images_button.click()
        chat.allow_button.click()
        if not chat.first_image_from_gallery.is_element_displayed():
            self.errors.append('Image previews are not shown after denying and allowing access to gallery')
        self.home.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702290)
    def test_permissions_webview_camera(self):
        web_view_camera_url = 'https://simpledapp.status.im/webviewtest/webviewcamera.html'
        self.drivers[0].set_clipboard_text(web_view_camera_url)
        dapp = self.home.dapp_tab_button.click()
        dapp.enter_url_editbox.click()
        dapp.paste_text()
        dapp.confirm()

        # from views.web_views.base_web_view import BaseWebView
        camera_dapp = BaseWebView(self.drivers[0])
        camera_dapp.just_fyi("Check camera request blocked (because it's not enabled in app yet)")
        camera_request_blocked = self.home.get_translation_by_key("page-camera-request-blocked")
        if not dapp.element_by_text_part(camera_request_blocked).is_element_displayed():
            self.driver.fail("There is no pop-up notifying that camera access need to be granted in app")
        camera_dapp.swipe_down()
        if not camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera permissions not allowed - acccess to camera granted")

        profile = self.home.profile_button.click()
        profile.privacy_and_security_button.click()

        camera_dapp.just_fyi("Enable camera requests in Dapps")
        profile.element_by_translation_id("webview-camera-permission-requests").scroll_and_click()
        self.home.dapp_tab_button.click(desired_element_text='webview')

        camera_dapp.just_fyi("Check DApp asks now to allow camera access but Deny in DApp")
        camera_dapp.browser_refresh_page_button.click()
        camera_dapp.deny_button.click()
        if not camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera access Denied to Dapp, - access to camera granted")

        camera_dapp.just_fyi("Check DApp asks now to allow camera access and Allow access to DApp")
        camera_dapp.browser_refresh_page_button.click()
        camera_dapp.allow_button.click()
        if camera_dapp.camera_image_in_dapp.is_element_image_similar_to_template('blank_camera_image.png'):
            self.driver.fail("Even camera access Accepted to Dapp, - camera view is not shown")

        camera_dapp.just_fyi("Relogin and check camera access still needs to be allowed")
        self.home.profile_button.click()
        profile.reopen_app()
        self.home.dapp_tab_button.click()
        camera_dapp.open_tabs_button.click()
        dapp.element_by_text_part("https").click()
        if not camera_dapp.allow_button.is_element_displayed():
            self.driver.fail("No request to camera access after relogin")
        camera_dapp.allow_button.click()
        self.home.home_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(702292)
    def test_scan_qr_with_scan_contact_code_via_start_chat(self):

        url_data = {
            'ens_with_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s.stateofus.eth' % ens_user_message_sender['ens'],
                'username': '@%s' % ens_user_message_sender['ens']
            },
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user_message_sender['ens'],
                'username': '@%s' % ens_user_message_sender['ens']
            },
            'ens_another_domain_deep_link': {
                'url': 'status-im://u/%s' % ens_user['ens'],
                'username': '@%s' % ens_user['ens']
            },
            'own_profile_key_deep_link': {
                'url': 'https://join.status.im/u/%s' % self.public_key,
                'error': "That's you"
            },
            'other_user_profile_key_deep_link': {
                'url': 'https://join.status.im/u/%s' % transaction_senders['M']['public_key'],
                'username': transaction_senders['M']['username']
            },
            'other_user_profile_key_deep_link_invalid': {
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Please enter or scan a valid chat key'
            },
            'own_profile_key': {
                'url': self.public_key,
                'error': "That's you"
            },
            # 'ens_without_stateofus_domain': {
            #     'url': ens_user['ens'],
            #     'username': ens_user['username']
            # },
            'other_user_profile_key': {
                'url': transaction_senders['M']['public_key'],
                'username': transaction_senders['M']['username']
            },
            'other_user_profile_key_invalid': {
                'url': '%s123' % ens_user['public_key'],
                'error': 'Please enter or scan a valid chat key'
            },
        }

        for key in url_data:
            self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
            contacts = self.home.start_new_chat_button.click()
            self.home.just_fyi('Checking scanning qr for "%s" case' % key)
            contacts.scan_contact_code_button.click()
            contacts.allow_button.click_if_shown(3)
            contacts.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            chat = ChatView(self.drivers[0])
            if url_data[key].get('error'):
                if not chat.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat.ok_button.click()
            if url_data[key].get('username'):
                if not chat.chat_message_input.is_element_displayed():
                    self.errors.append(
                        'In "%s" case chat input is not found after scanning, so no redirect to 1-1' % key)
                if not chat.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In "%s" case "%s" not found after scanning' % (key, url_data[key]['username']))
                chat.get_back_to_home_view()
        self.errors.verify_no_errors()

    @marks.testrail_id(702293)
    def test_scan_qr_different_links_with_universal_qr_scanner(self):
        wallet = self.home.wallet_button.click()
        wallet.home_button.click()
        send_transaction = SendTransactionView(self.drivers[0])

        url_data = {
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user_message_sender['ens'],
                'username': '@%s' % ens_user_message_sender['ens']
            },

            'other_user_profile_key_deep_link': {
                'url': 'status-im://u/%s' % basic_user['public_key'],
                'username': basic_user['username']
            },
            'other_user_profile_key_deep_link_invalid': {
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Unable to read this code'
            },
            'own_profile_key': {
                'url': self.public_key,
            },
            'other_user_profile_key': {
                'url': transaction_senders['A']['public_key'],
                'username': transaction_senders['A']['username']
            },
            'wallet_validation_wrong_address_transaction': {
                'url': 'ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e@5/transfer?address=blablabla&uint256=1e10',
                'error': 'Invalid address',
            },
            'wallet_eip_ens_for_receiver': {
                'url': 'ethereum:0x3d6afaa395c31fcd391fe3d562e75fe9e8ec7e6a@5/transfer?address=%s.stateofus.eth&uint256=1e-1' % ens_user_message_sender['ens'],
                'data': {
                    'asset': 'STT',
                    'amount': '0.1',
                    'address': '0x75fF…4184',
                },
            },
            'wallet_eip_payment_link': {
                'url': 'ethereum:pay-0x3d6afaa395c31fcd391fe3d562e75fe9e8ec7e6a@5/transfer?address=0x3d597789ea16054a084ac84ce87f50df9198f415&uint256=1e1',
                'data': {
                    'amount': '10',
                    'asset': 'STT',
                    'address': '0x3D59…F415',
                },
            },
            'dapp_deep_link': {
                'url': 'https://join.status.im/b/%s' % test_dapp_url,
            },
            'dapp_deep_link_https': {
                'url': 'https://join.status.im/b/%s' % test_dapp_url,
            },
            'public_chat_deep_link': {
                'url': 'https://join.status.im/baga-ma-2020',
                'chat_name': 'baga-ma-2020'
            },
        }

        for key in url_data:
            self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
            self.home.just_fyi('Checking %s case' % key)
            if self.home.universal_qr_scanner_button.is_element_displayed():
                self.home.universal_qr_scanner_button.click()
            if self.home.allow_button.is_element_displayed():
                self.home.allow_button.click()
            self.home.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            from views.chat_view import ChatView
            chat = ChatView(self.drivers[0])
            if key == 'own_profile_key':
                profile = ProfileView(self.drivers[0])
                if not profile.default_username_text.is_element_displayed():
                    self.errors.append('In %s case was not redirected to own profile' % key)
                self.home.home_button.double_click()
            if url_data[key].get('error'):
                if not chat.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat.ok_button.click()
            if url_data[key].get('username'):
                if not chat.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In %s case username not shown' % key)
            if 'wallet' in key:
                if url_data[key].get('data'):
                    actual_data = send_transaction.get_values_from_send_transaction_bottom_sheet()
                    difference_in_data = url_data[key]['data'].items() - actual_data.items()
                    if difference_in_data:
                        self.errors.append(
                            'In %s case returned value does not match expected in %s' % (key, repr(difference_in_data)))
                    wallet.close_send_transaction_view_button.click()
                wallet.home_button.click()
            if 'dapp' in key:
                self.home.open_in_status_button.click()
                if not chat.allow_button.is_element_displayed():
                    self.errors.append('No allow button is shown in case of navigating to Status dapp!')
                chat.dapp_tab_button.click()
                chat.home_button.click()
            if 'public' in key:
                if not chat.chat_message_input.is_element_displayed():
                    self.errors.append('No message input is shown in case of navigating to public chat via deep link!')
                if not chat.element_by_text_part(url_data[key]['chat_name']).is_element_displayed():
                    self.errors.append('Chat name is not shown in case of navigating to public chat via deep link!')
            chat.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702291)
    def test_scan_qr_eip_681_links_via_wallet(self):

        wallet = self.home.wallet_button.click()
        wallet.wait_balance_is_changed()
        send_transaction_view = SendTransactionView(self.drivers[0])

        self.home.just_fyi("Setting up wallet")
        wallet.accounts_status_account.click_until_presence_of_element(wallet.send_transaction_button)
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.set_recipient_address('0x%s' % basic_user['address'])
        send_transaction.amount_edit_box.send_keys("0")
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        wallet.set_up_wallet_when_sending_tx()
        wallet.cancel_button.click()
        wallet.close_button.click()

        url_data = {
            'ens_for_receiver': {
                'url': 'ethereum:0x3D6AFAA395C31FCd391fE3D562E75fe9E8ec7E6a@5/transfer?address=%s.stateofus.eth&uint256=1e-1' % ens_user_message_sender['ens'],
                'data': {
                    'asset': 'STT',
                    'amount': '0.1',
                    'address': '0x75fF…4184',
                },
            },
            # 'gas_settings': {
            #     'url': 'ethereum:0x3d597789ea16054a084ac84ce87f50df9198f415@3?value=1e16&gasPrice=1000000000&gasLimit=100000',
            #     'data': {
            #         'amount': '0.01',
            #         'asset': 'ETHro',
            #         'address': '0x3D59…F415',
            #         'gas_limit': '100000',
            #         'gas_price': '1',
            #     },
            # },
            'payment_link': {
                'url': 'ethereum:pay-0x3D6AFAA395C31FCd391fE3D562E75fe9E8ec7E6a@5/transfer?address=0x3d597789ea16054a084ac84ce87f50df9198f415&uint256=1e1',
                'data': {
                    'amount': '10',
                    'asset': 'STT',
                    'address': '0x3D59…F415',
                },
            },
            'validation_amount_too_presize': {
                'url': 'ethereum:0x3D6AFAA395C31FCd391fE3D562E75fe9E8ec7E6a@5/transfer?address=0x101848D5C5bBca18E6b4431eEdF6B95E9ADF82FA&uint256=1e-19',
                'data': {
                    'amount': '1e-19',
                    'asset': 'STT',
                    'address': '0x1018…82FA',

                },
                'send_transaction_validation_error': 'Amount is too precise',
            },
            'validation_amount_too_big': {
                'url': 'ethereum:0x101848D5C5bBca18E6b4431eEdF6B95E9ADF82FA@5?value=1e25',
                'data': {
                    'amount': '10000000',
                    'asset': 'ETHgo',
                    'address': '0x1018…82FA',

                },
                'send_transaction_validation_error': 'Insufficient funds',
            },
            'validation_wrong_chain_id': {
                'url': 'ethereum:0x101848D5C5bBca18E6b4431eEdF6B95E9ADF82FA?value=1e17',
                'error': 'Network does not match',
                'data': {
                    'amount': '0.1',
                    'asset': 'ETHgo',
                    'address': '0x1018…82FA',
                },
            },
            'validation_wrong_address': {
                'url': 'ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e@5/transfer?address=blablabla&uint256=1e10',
                'error': 'Invalid address',
            },
        }

        for key in url_data:
            wallet.just_fyi('Checking %s case' % key)
            wallet.scan_qr_button.click()
            if wallet.allow_button.is_element_displayed():
                wallet.allow_button.click()
            wallet.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            if url_data[key].get('error'):
                if not wallet.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                wallet.ok_button.click()
            if url_data[key].get('data'):
                actual_data = send_transaction_view.get_values_from_send_transaction_bottom_sheet()
                difference_in_data = url_data[key]['data'].items() - actual_data.items()
                if difference_in_data:
                    self.errors.append(
                        'In %s case returned value does not match expected in %s' % (key, repr(difference_in_data)))
                if url_data[key].get('send_transaction_validation_error'):
                    error = url_data[key]['send_transaction_validation_error']
                    if not wallet.element_by_text_part(error).is_element_displayed():
                        self.errors.append(
                            'Expected error %s is not shown' % error)
                if wallet.close_send_transaction_view_button.is_element_displayed():
                    wallet.close_send_transaction_view_button.wait_and_click()
                else:
                    wallet.cancel_button.wait_and_click()

        self.errors.verify_no_errors()
