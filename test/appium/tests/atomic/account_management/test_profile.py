import re

from tests import marks, bootnode_address, mailserver_address, test_dapp_url, test_dapp_name, mailserver_ams, \
    mailserver_gc, mailserver_hk, used_fleet, common_password
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten, user_mainnet
from views.sign_in_view import SignInView
from tests.users import chat_users
import pytest


@pytest.mark.xdist_group(name="ens_stickers_mention_2")
@marks.critical
class TestEnsStickersMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.sender, cls.reciever = transaction_senders['ETH_3'], ens_user
        cls.home_1 = cls.device_1.recover_access(passphrase=cls.sender['passphrase'])
        cls.home_2 = cls.device_2.recover_access(ens_user['passphrase'], enable_notifications=True)
        cls.ens = '@%s' % cls.reciever['ens']
        cls.pub_chat_name = cls.home_1.get_random_chat_name()
        cls.chat_1, cls.chat_2 = cls.home_1.join_public_chat(cls.pub_chat_name), cls.home_2.join_public_chat(cls.pub_chat_name)
        [home.home_button.double_click() for home in (cls.home_1, cls.home_2)]
        cls.profile_2 = cls.home_2.profile_button.click()
        cls.profile_2.connect_existing_ens(cls.reciever['ens'])
        cls.home_1.add_contact(cls.reciever['ens'])
        cls.home_2.home_button.click()
        cls.home_2.add_contact(cls.sender['public_key'])
        # To avoid activity centre for restored users
        [chat.send_message("hey!") for chat in (cls.chat_1, cls.chat_2)]
        [home.home_button.double_click() for home in (cls.home_1, cls.home_2)]

    @marks.testrail_id(702152)
    def test_ens_purchased_in_profile(self):
        self.home_2.profile_button.double_click()
        ens_name_after_adding = self.profile_2.default_username_text.text
        if ens_name_after_adding != '@%s' % ens_user['ens']:
            self.errors.append('ENS name is not shown as default in user profile after adding, "%s" instead' % ens_name_after_adding)

        self.home_2.just_fyi('check ENS name wallet address and public key')
        self.home_2.element_by_text(self.reciever['ens']).click()
        self.home_2.element_by_text(self.reciever['ens']).click()
        for text in (self.reciever['address'].lower(), self.reciever['public_key']):
            if not self.home_2.element_by_text_part(text).is_element_displayed(40):
                self.errors.append('%s text is not shown' % text)
        self.home_2.profile_button.double_click()

        self.home_2.just_fyi('check ENS name is shown on share my profile window')
        self.profile_2.share_my_profile_button.click()
        if self.profile_2.ens_name_in_share_chat_key_text.text != '%s' % ens_user['ens']:
            self.errors.append('No ENS name is shown on tapping on share icon in Profile')
        self.profile_2.close_share_popup()
        self.errors.verify_no_errors()

    @marks.transaction
    @marks.testrail_id(702153)
    def test_ens_command_send_eth_1_1_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        wallet_1 = self.home_1.wallet_button.click()
        wallet_1.wait_balance_is_changed()
        wallet_1.home_button.click()
        self.home_1.get_chat(self.ens).click()
        self.chat_1.commands_button.click()
        amount = self.chat_1.get_unique_amount()

        self.chat_1.just_fyi("Check sending assets to ENS name from sender side")
        send_message = self.chat_1.send_command.click()
        send_message.amount_edit_box.set_value(amount)
        send_message.confirm()
        send_message.next_button.click()
        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.drivers[0])
        send_transaction.ok_got_it_button.click()
        send_transaction.sign_transaction()
        chat_1_sender_message = self.chat_1.get_outgoing_transaction(transaction_value=amount)
        self.network_api.wait_for_confirmation_of_transaction(self.sender['address'], amount, confirmations=3)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)

        self.chat_2.just_fyi("Check that message is fetched for receiver")
        self.home_2.get_chat(self.sender['username']).click()
        chat_2_reciever_message = self.chat_2.get_incoming_transaction(transaction_value=amount)
        chat_2_reciever_message.transaction_status.wait_for_element_text(chat_2_reciever_message.confirmed)

    @marks.testrail_id(702155)
    def test_ens_mention_nickname_1_1_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi('Mention user by ENS in 1-1 chat')
        message, message_ens_owner = '%s hey!' % self.ens, '%s hey!' % self.reciever['ens']
        self.home_1.get_chat(self.ens).click()
        self.chat_1.send_message(message)

        self.home_1.just_fyi('Set nickname and mention user by nickname in 1-1 chat')
        russian_nickname = 'МОЙ дорогой ДРУх'
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.set_nickname(russian_nickname)
        self.chat_1.select_mention_from_suggestion_list(russian_nickname + ' ' + self.ens)

        self.chat_1.just_fyi('Check that nickname is shown in preview for 1-1 chat')
        updated_message = '%s hey!' % russian_nickname
        self.chat_1.home_button.double_click()
        if not self.chat_1.element_by_text(updated_message).is_element_displayed():
            self.errors.append('"%s" is not show in chat preview on home screen!' % message)
        self.home_1.get_chat(russian_nickname).click()

        self.chat_1.just_fyi('Check redirect to user profile on mention by nickname tap')
        self.chat_1.chat_element_by_text(updated_message).click()
        if not self.chat_1.profile_block_contact.is_element_displayed():
            self.errors.append(
                'No redirect to user profile after tapping on message with mention (nickname) in 1-1 chat')
        else:
            self.chat_1.profile_send_message.click()

        self.chat_2.just_fyi("Check message with mention for ENS owner")
        self.home_2.get_chat(self.sender['username']).click()
        if not self.chat_2.chat_element_by_text(message_ens_owner).is_element_displayed():
            self.errors.append('Expected %s message is not shown for ENS owner' % message_ens_owner)

        self.chat_1.just_fyi('Check if after deleting nickname ENS is shown again')
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.profile_nickname_button.click()
        self.chat_1.nickname_input_field.clear()
        self.chat_1.element_by_text('Done').click()
        self.chat_1.close_button.click()
        if self.chat_1.user_name_text.text != self.ens:
            self.errors.append("Username '%s' is not updated to ENS '%s' after deleting nickname" %
                               (self.chat_1.user_name_text.text, self.ens))

        self.errors.verify_no_errors()

    @marks.testrail_id(702156)
    def test_ens_mention_push_highlighted_public_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        ####
        self.home_2.put_app_to_background()
        self.home_2.open_notification_bar()

        self.home_1.just_fyi('check that can mention user with ENS name')
        self.home_1.get_chat(self.ens).click()
        self.chat_1.select_mention_from_suggestion_list(self.reciever['ens'])
        if self.chat_1.chat_message_input.text != self.ens + ' ':
            self.errors.append(
                'ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        self.chat_1.send_message_button.click()

        self.home_2.just_fyi('check that PN is received and after tap you are redirected to public chat, mention is highligted')
        # TODO: issue #11003
        pn = self.home_2.get_pn(self.reciever['username'])
        if pn:
            pn.click()
        else:
            self.home_2.click_system_back_button(2)
        if self.home_2.element_starts_with_text(self.reciever['ens']).is_element_differs_from_template('mentioned.png', 2):
            self.errors.append('Mention is not highlighted!')
        self.errors.verify_no_errors()

    @marks.testrail_id(702157)
    def test_sticker_1_1_public_chat(self):
        self.home_2.status_in_background_button.click_if_shown()
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        (profile_1, profile_2) = (home.profile_button.click() for home in (self.home_1, self.home_2))
        [profile.switch_network() for profile in (profile_1, profile_2)]
        # TODO: no check there is no stickers on ropsten due to transfer stickers to status-go

        self.home_2.just_fyi('Check that can use purchased stickerpack')
        self.home_2.get_chat('#%s' % self.pub_chat_name).click()
        self.chat_2.install_sticker_pack_by_name('Tozemoon')
        self.chat_2.sticker_icon.click()
        if not self.chat_2.chat_item.is_element_displayed():
            self.errors.append('Cannot use purchased stickers')

        self.home_1.just_fyi('Install free sticker pack and use it in 1-1 chat')
        self.home_1.get_chat(self.ens).click()
        self.chat_1.install_sticker_pack_by_name('Status Cat')
        self.chat_1.sticker_icon.click()
        if not self.chat_1.sticker_message.is_element_displayed():
            self.errors.append('Sticker was not sent')
        self.chat_1.swipe_right()
        if not self.chat_1.sticker_icon.is_element_displayed():
            self.errors.append('Sticker is not shown in recently used list')
        self.chat_1.get_back_to_home_view()

        self.home_1.just_fyi('Send stickers in public chat from Recent')
        self.home_1.join_public_chat(self.home_1.get_random_chat_name())
        self.chat_1.show_stickers_button.click()
        self.chat_1.sticker_icon.click()
        if not self.chat_1.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent from Recent')

        self.home_2.just_fyi('Check that can install stickers by tapping on sticker message')
        self.home_2.home_button.double_click()
        self.home_2.get_chat(self.sender['username']).click()
        self.chat_2.chat_item.click()
        if not self.chat_2.element_by_text_part('Status Cat').is_element_displayed():
            self.errors.append('Stickerpack is not available for installation after tapping on sticker message')
        self.chat_2.element_by_text_part('Free').click()
        if self.chat_2.element_by_text_part('Free').is_element_displayed():
            self.errors.append('Stickerpack was not installed')

        self.chat_2.just_fyi('Check that can navigate to another user profile via long tap on sticker message')
        self.chat_2.close_sticker_view_icon.click()
        self.chat_2.chat_item.long_press_element()
        self.chat_2.element_by_text('View Details').click()
        self.chat_2.profile_send_message.wait_and_click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702158)
    def test_start_new_chat_screen_validation(self):
        [home.get_back_to_home_view() for home in (self.home_1, self.home_2)]
        self.home_2.driver.quit()
        public_key = basic_user['public_key']
        self.home_1.plus_button.click()
        chat = self.home_1.start_new_chat_button.click()

        self.home_1.just_fyi("Validation: invalid public key and invalid ENS")
        for invalid_chat_key in (basic_user['public_key'][:-1], ens_user_ropsten['ens'][:-2]):
            chat.public_key_edit_box.clear()
            chat.public_key_edit_box.set_value(invalid_chat_key)
            chat.confirm()
            if not self.home_1.element_by_translation_id("profile-not-found").is_element_displayed():
                self.errors.append('Error is not shown for invalid public key')

        self.home_1.just_fyi("Check that valid ENS is resolved")
        chat.public_key_edit_box.clear()
        chat.public_key_edit_box.set_value(ens_user_ropsten['ens'])
        resolved_ens = '%s.stateofus.eth' % ens_user_ropsten['ens']
        if not chat.element_by_text(resolved_ens).is_element_displayed(10):
            self.errors.append('ENS name is not resolved after pasting chat key')
        self.home_1.close_button.click()

        self.home_1.just_fyi("Check that can paste public key from keyboard and start chat")
        self.home_1.get_chat('#%s' % self.pub_chat_name).click()
        chat.send_message(public_key)
        chat.copy_message_text(public_key)
        chat.back_button.click()
        self.home_1.plus_button.click()
        self.home_1.start_new_chat_button.click()
        chat.public_key_edit_box.paste_text_from_clipboard()
        if chat.public_key_edit_box.text != public_key:
            self.errors.append('Public key is not pasted from clipboard')
        if not chat.element_by_text(basic_user['username']).is_element_displayed():
            self.errors.append('3 random-name is not resolved after pasting chat key')

        self.home_1.just_fyi('My_profile button at Start new chat view opens own QR code with public key pop-up')
        self.home_1.my_profile_on_start_new_chat_button.click()
        account = self.home_1.get_profile_view()
        if not (account.public_key_text.is_element_displayed() and account.share_button.is_element_displayed()
                and account.qr_code_image.is_element_displayed()):
            self.errors.append('No self profile pop-up data displayed after My_profile button tap')
        self.errors.verify_no_errors()

# TODO: suspended according to #13257
@pytest.mark.xdist_group(name="pairing_2")
@marks.critical
@marks.skip
class TestPairingMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        from views.dbs.main_pairing.data import seed_phrase, password
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.home_1 = cls.device_1.import_db(seed_phrase=seed_phrase, import_db_folder_name='main_pairing', password=password)
        cls.home_2 = cls.device_2.recover_access(seed_phrase)

        cls.home_1.just_fyi('Pair main and secondary devices')
        [cls.profile_1, cls.profile_2] = [home.profile_button.click() for home in (cls.home_1, cls.home_2)]
        name_1, name_2 = 'device_1', 'a_%s_2' % cls.device_2.get_unique_amount()
        cls.profile_2.discover_and_advertise_device(name_2)
        cls.profile_1.sync_settings_button.scroll_and_click()
        cls.profile_1.devices_button.scroll_to_element()
        cls.profile_1.devices_button.click()
        cls.home_1.element_by_text_part(name_2).scroll_and_click()
        cls.profile_1.sync_all_button.click()
        cls.profile_1.sync_all_button.wait_for_visibility_of_element(20)
        [profile.get_back_to_home_view() for profile in [cls.profile_1, cls.profile_2]]
        [home.home_button.click() for home in [cls.home_1, cls.home_2]]

    def test_pairing_initial_sync_chats(self):
        self.profile_2.just_fyi("Check chats and previews")
        from views.dbs.main_pairing.data import chats
        for chat in chats.keys():
            if chats[chat]['initial_sync']:
                if 'preview' in chats.keys():
                    actual_chat_preview = self.home_2.get_chat(chat).chat_preview.text
                    expected_chat_preview = chats[chat]['preview']
                    if actual_chat_preview != expected_chat_preview:
                        self.errors.append('Expected preview for %s is "%s", in fact "%s" after initial sync' % (
                        chat, expected_chat_preview, actual_chat_preview))

        # TODO: blocked due to 13176
        # self.profile_2.just_fyi("Check unread indicator")
        # if self.home_2.home_button.counter.text != '2':
        #     self.errors.append('New messages counter is not shown on Home button')
        # for chat in chats.keys():
        #     if 'unread' in chats.keys():
        #         if self.home_2.get_chat(chat).new_messages_counter.text != chats[chat]['unread']:
        #             self.errors.append('No unread for %s after initial sync' % chat)
        self.errors.verify_no_errors()

    @marks.skip
    # TODO: blocked due to 13176
    def test_pairing_initial_sync_activity_centre(self):
        from views.dbs.main_pairing.data import activity_centre
        if self.home_2.notifications_unread_badge.is_element_displayed():
            self.home_2.notifications_unread_badge.click()
            for chat in activity_centre.keys():
                from views.home_view import ActivityCenterChatElement
                chat_in_ac = ActivityCenterChatElement(self.driver, chat_name=chat)
                if not chat_in_ac.is_element_displayed():
                    self.errors.append('No chat "%s" in activity centre' % chat)
                else:
                    if not chat_in_ac.chat_message_preview != activity_centre[chat]:
                        self.errors.append('No chat preview  for "%s" in activity centre, "%s" instead' % chat, chat_in_ac.chat_message_preview)
        else:
            self.home_2.driver.fail("No unread messages in Activity centre!")
        self.errors.verify_no_errors()

    def test_pairing_initial_sync_contacts_blocked_nickname(self):
        from views.dbs.main_pairing.data import contacts, blocked
        self.profile_2 = self.home_2.profile_button.click()
        self.profile_2.contacts_button.click()
        for contact in contacts:
            if not self.profile_2.element_by_text(contact).is_element_displayed():
                self.errors.append("%s contact is not synced after initial sync" % contact)
        self.profile_2.blocked_users_button.click()
        for blocked_user in blocked.keys():
            if not self.profile_2.element_by_text(blocked_user).is_element_displayed():
                self.errors.append("%s blocked user is not synced after initial sync" % blocked_user)
        self.profile_2.get_back_to_home_view()


class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(6318)
    @marks.medium
    def test_can_delete_several_multiaccounts(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        delete_alert_warning = sign_in.get_translation_by_key("delete-profile-warning")
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.back_button.click()
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.confirm_your_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.lets_go_button)
        sign_in.lets_go_button.click()

        sign_in.just_fyi('Delete 2nd multiaccount')
        public_key, username = sign_in.get_public_key_and_username(return_username=True)
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        for text in (username, delete_alert_warning):
            if not profile.element_by_text(text).is_element_displayed():
                self.errors.append('Required %s is not shown when deleting multiaccount' % text)
        profile.delete_profile_button.click()
        if profile.element_by_translation_id("profile-deleted-title").is_element_displayed():
            self.driver.fail('Profile is deleted without confirmation with password')
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click_until_presence_of_element(
            profile.element_by_translation_id("profile-deleted-title"))
        profile.ok_button.click()

        sign_in.just_fyi('Delete last multiaccount')
        sign_in.sign_in()
        sign_in.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click()
        profile.ok_button.click()
        if not sign_in.get_started_button.is_element_displayed(20):
            self.errors.append('No redirected to carousel view after deleting last multiaccount')
        self.errors.verify_no_errors()

    @marks.testrail_id(695890)
    @marks.medium
    def test_can_use_another_fleets_and_networks_advanced_set_nonce(self):
        user = user_mainnet
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])

        home.just_fyi("Check that can enable all toggles and still login successfully")
        profile = home.profile_button.click()
        profile.advanced_button.click()
        profile.transaction_management_enabled_toggle.click()
        profile.webview_debug_toggle.click()
        profile.waku_bloom_toggle.scroll_and_click()
        sign_in.sign_in()

        home.just_fyi("Check tx management")
        wallet = home.wallet_button.click()
        send_tx = wallet.send_transaction_from_main_screen.click()
        from views.send_transaction_view import SendTransactionView
        send_tx = SendTransactionView(self.driver)
        send_tx.amount_edit_box.set_value('0')
        send_tx.set_recipient_address(transaction_senders['ETH_7']['address'])
        send_tx.next_button.click()
        send_tx.set_up_wallet_when_sending_tx()
        send_tx.advanced_button.click()
        send_tx.nonce_input.set_value('4')
        send_tx.nonce_save_button.click()
        error_text = send_tx.sign_transaction(error=True)
        if error_text != 'nonce too low':
            self.errors.append("%s is not expected error when signing tx with custom nonce" % error_text)

        home.just_fyi("Check balance on mainnet")
        profile = home.profile_button.click()
        profile.switch_network()
        wallet = home.wallet_button.click()
        wallet.scan_tokens()
        [wallet.wait_balance_is_equal_expected_amount(asset, value) for asset, value in user['mainnet'].items()]

        home.just_fyi("Check balance on xDai and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('xDai Chain')
        home.wallet_button.click()
        wallet.element_by_text(user['xdai']).wait_for_element(30)

        home.just_fyi("Check balance on BSC and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('BSC Network')
        home.wallet_button.click()
        wallet.element_by_text(user['bsc']).wait_for_element(30)

        self.errors.verify_no_errors()

    @marks.testrail_id(5502)
    @marks.critical
    def test_can_add_existing_ens_on_mainnet(self):
        home = SignInView(self.driver).recover_access(ens_user['passphrase'])
        profile = home.profile_button.click()

        profile.just_fyi('check if your name can be added via "ENS usernames" in Profile')
        profile.switch_network()
        home.profile_button.click()
        profile.connect_existing_ens(ens_user['ens'])

        profile.just_fyi('check that after adding username is shown in "ENS usernames" and profile')
        if not profile.element_by_text(ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own "ENS usernames" after adding')
        profile.back_button.click()
        if not profile.element_by_text('@%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        if not profile.element_by_text('%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        profile.share_my_profile_button.click()
        if profile.ens_name_in_share_chat_key_text.text != '%s' % ens_user['ens']:
            self.errors.append('No ENS name is shown on tapping on share icon in Profile')
        profile.close_share_popup()

        self.errors.verify_no_errors()
    @marks.testrail_id(5453)
    @marks.medium
    def test_privacy_policy_terms_of_use_node_version_need_help_in_profile(self):
        signin = SignInView(self.driver)
        no_link_found_error_msg = 'Could not find privacy policy link at'
        no_link_open_error_msg = 'Could not open our privacy policy from'
        no_link_tos_error_msg = 'Could not open Terms of Use from'

        signin.just_fyi("Checking privacy policy and TOS links")
        if not signin.privacy_policy_link.is_element_present():
            self.errors.append('%s Sign in view!' % no_link_found_error_msg)
        if not signin.terms_of_use_link.is_element_displayed():
            self.driver.fail("No Terms of Use link on Sign in view!")

        home = signin.create_user()
        profile = home.profile_button.click()
        profile.about_button.click()
        profile.privacy_policy_button.click()
        from views.web_views.base_web_view import BaseWebView
        web_page = BaseWebView(self.driver)
        if not web_page.policy_summary.is_element_displayed():
            self.errors.append('%s Profile about view!' % no_link_open_error_msg)
        web_page.click_system_back_button()

        profile.terms_of_use_button.click()
        web_page.wait_for_d_aap_to_load()
        web_page.swipe_by_custom_coordinates(0.5, 0.8, 0.5, 0.4)
        if not web_page.terms_of_use_summary.is_element_displayed(30):
            self.errors.append('%s Profile about view!' % no_link_tos_error_msg)
        web_page.click_system_back_button()

        signin.just_fyi("Checking that version match expected format and can be copied")
        app_version = profile.app_version_text.text
        node_version = profile.node_version_text.text
        if not re.search(r'\d[.]\d{1,2}[.]\d{1,2}\s[(]\d*[)]', app_version):
            self.errors.append("App version %s didn't match expected format" % app_version)
        if not re.search(r'StatusIM/v.*/android-\d{3}/go\d[.]\d+', node_version):
            self.errors.append("Node version %s didn't match expected format" % node_version)
        profile.app_version_text.click()
        profile.back_button.click()
        profile.home_button.click()
        chat = home.join_public_chat(home.get_random_chat_name())
        message_input = chat.chat_message_input
        message_input.paste_text_from_clipboard()
        if message_input.text != app_version:
            self.errors.append('Version number was not copied to clipboard')

        signin.just_fyi("Checking Need help section")
        home.profile_button.double_click()
        profile.help_button.click()
        web_page = profile.faq_button.click()
        web_page.open_in_webview()
        web_page.wait_for_d_aap_to_load()
        if not profile.element_by_text_part("F.A.Q").is_element_displayed(30):
            self.errors.append("FAQ is not shown")
        profile.click_system_back_button()
        profile.submit_bug_button.click()

        signin.just_fyi("Checking bug submitting form")
        profile.bug_description_edit_box.set_value('1234')
        profile.bug_submit_button.click()
        if not profile.element_by_translation_id("bug-report-too-short-description").is_element_displayed():
            self.errors.append("Can submit big with too short description!")
        profile.bug_description_edit_box.clear()
        [field.set_value("Something wrong happened!!") for field in
         (profile.bug_description_edit_box, profile.bug_steps_edit_box)]
        profile.bug_submit_button.click()
        if not profile.element_by_text_part("Welcome to Gmail").is_element_displayed(30):
            self.errors.append("Mail client is not opened when submitting bug")
        profile.click_system_back_button(2)

        signin.just_fyi("Checking request feature")
        profile.request_a_feature_button.click()
        if not profile.element_by_text("#support").is_element_displayed(30):
            self.errors.append("Support channel is not suggested for requesting a feature")
        self.errors.verify_no_errors()

    @marks.testrail_id(5766)
    @marks.medium
    @marks.flaky
    def test_use_pinned_mailserver(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()

        profile.just_fyi('pin history node')
        profile.sync_settings_button.click()
        node_gc, node_ams, node_hk = [profile.return_mailserver_name(history_node_name, used_fleet) for
                                      history_node_name in (mailserver_gc, mailserver_ams, mailserver_hk)]
        h_node = node_ams
        profile.mail_server_button.click()
        profile.mail_server_auto_selection_button.click()
        profile.mail_server_by_name(h_node).click()
        profile.confirm_button.click()
        if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
            h_node = node_hk
            profile.element_by_translation_id("mailserver-pick-another", uppercase=True).click()
            profile.mail_server_by_name(h_node).click()
            profile.confirm_button.click()
            if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
                self.driver.fail("Couldn't connect to any history node")

        profile.just_fyi('check that history node is pinned')
        profile.close_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)
        profile.home_button.click()

        profile.just_fyi('Relogin and check that settings are preserved')
        home.relogin()
        home.profile_button.click()
        profile.sync_settings_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)

        self.errors.verify_no_errors()

    @marks.testrail_id(6219)
    @marks.medium
    def test_set_primary_ens_custom_domain(self):
        home = SignInView(self.driver).recover_access(ens_user['passphrase'])
        ens_second, ens_main = ens_user['ens_another'], ens_user['ens']

        home.just_fyi('add 2 ENS names in Profile')
        profile = home.profile_button.click()
        dapp = profile.connect_existing_ens(ens_main)
        profile.element_by_translation_id("ens-add-username").wait_and_click()
        profile.element_by_translation_id("ens-want-custom-domain").wait_and_click()
        dapp.ens_name_input.set_value(ens_second)
        dapp.check_ens_name.click_until_presence_of_element(dapp.element_by_translation_id("ens-got-it"))
        dapp.element_by_translation_id("ens-got-it").wait_and_click()

        home.just_fyi('check that by default %s ENS is set' % ens_main)
        dapp.element_by_translation_id("ens-primary-username").click()
        message_to_check = 'Your messages are displayed to others with'
        if not dapp.element_by_text('%s\n@%s' % (message_to_check, ens_main)).is_element_displayed():
            self.errors.append('%s ENS username is not set as primary by default' % ens_main)

        home.just_fyi('check view in chat settings ENS from other domain: %s after set new primary ENS' % ens_second)
        dapp.set_primary_ens_username(ens_second).click()
        if profile.username_in_ens_chat_settings_text.text != '@' + ens_second:
            self.errors.append('ENS username %s is not shown in ENS username Chat Settings after enabling' % ens_second)
        self.errors.verify_no_errors()


class TestProfileMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(6636)
    @marks.medium
    def test_show_profile_picture_of_setting_online_indicator(self):
        ####TODO: add check by make photo in this test
        # profile_1.just_fyi("Set user Profile image by taking Photo")
        # home_1.profile_button.click()
        # profile_1.edit_profile_picture(file_name='sauce_logo.png', update_by='Make Photo')
        # home_1.home_button.click(desired_view='chat')
        # public_chat_1.chat_message_input.send_keys(message)
        # public_chat_1.send_message_button.click()
        #
        # if public_chat_2.chat_element_by_text(message).member_photo.is_element_image_similar_to_template(
        #         'sauce_logo.png'):
        #     self.drivers[0].fail('Profile picture was not updated in chat after making photo')
        ####
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user(enable_notifications=True)
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        public_key_1, default_username_1 = profile_1.get_public_key_and_username(return_username=True)
        public_key_2, default_username_2 = profile_2.get_public_key_and_username(return_username=True)
        logo_online, logo_default, logo_chats, logo_group = 'logo_new.png', 'sauce_logo.png', 'logo_chats_view.png', 'group_logo.png'

        [profile.home_button.click() for profile in (profile_1, profile_2)]
        home_1.add_contact(public_key_2)
        home_1.profile_button.click()

        profile_1.just_fyi("Set user Profile image from Gallery")
        profile_1.edit_profile_picture(file_name=logo_default)
        home_1.profile_button.click()
        profile_1.swipe_down()

        profile_1.just_fyi('set status in profile')
        device_1_status = 'My new update!'
        timeline = profile_1.status_button.click()
        timeline.set_new_status(device_1_status)
        if not timeline.timeline_own_account_photo.is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in timeline')

        profile_1.just_fyi('Check profile image it is not in mentions because user not in contacts yet')
        one_to_one_chat_2 = home_2.add_contact(public_key_1, add_in_contacts=False)
        one_to_one_chat_2.chat_message_input.set_value('@' + default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if one_to_one_chat_2.user_profile_image_in_mentions_list(
                default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture is updated in 1-1 chat mentions list of contact not in Contacts list')

        profile_1.just_fyi('Check profile image is in mentions because now user was added in contacts')
        one_to_one_chat_2.add_to_contacts.click()
        one_to_one_chat_2.send_message("hey")
        one_to_one_chat_2.chat_message_input.set_value('@' + default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if not one_to_one_chat_2.user_profile_image_in_mentions_list(
                default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in 1-1 chat mentions list')
        home_1.reopen_app()
        one_to_one_chat_2.get_back_to_home_view()

        profile_1.just_fyi('Check profile image is updated in Group chat view')
        profile_2 = one_to_one_chat_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()
        profile_2.online_indicator.wait_for_visibility_of_element(180)
        if not profile_2.profile_picture.is_element_image_similar_to_template('new_profile_online.png'):
            self.errors.append('Profile picture was not updated on user Profile view')
        profile_2.close_button.click()
        home_2.home_button.double_click()
        if not home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_chats):
            self.errors.append('User profile picture was not updated on Chats view')

        profile_1.just_fyi('Check profile image updated in user profile view in Group chat views')
        group_chat_name, group_chat_message = 'new_group_chat', 'Trololo'
        group_chat_2 = home_2.create_group_chat(user_names_to_add=[default_username_1])

        group_chat_2.send_message('Message', wait_chat_input_sec=10)
        group_chat_1 = home_1.get_chat(group_chat_name).click()
        group_chat_1.join_chat_button.click()
        group_chat_1.send_message(group_chat_message)
        if not group_chat_2.chat_element_by_text(group_chat_message).member_photo.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture was not updated in message Group chat view')
        home_2.put_app_to_background()

        profile_1.just_fyi('Check profile image updated in group chat invite')
        home_1.get_back_to_home_view()
        new_group_chat = 'new_gr'
        home_2.click_system_back_button()
        home_2.open_notification_bar()
        home_1.create_group_chat(user_names_to_add=[default_username_2], group_chat_name=new_group_chat)

        invite = group_chat_2.pn_invited_to_group_chat(default_username_1, new_group_chat)
        pn = home_2.get_pn(invite)
        if pn:
            if not pn.group_chat_icon.is_element_image_similar_to_template(logo_group):
                self.errors.append("Group chat invite is not updated with custom logo!")
            pn.click()
        else:
            home_2.click_system_back_button(2)

        profile_1.just_fyi('Check profile image updated in on login view')
        home_1.profile_button.click()
        profile_1.logout()
        sign_in_1 = home_1.get_sign_in_view()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on Multiaccounts list select login view')
        sign_in_1.element_by_text(default_username_1).click()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on account login view')
        sign_in_1.password_input.set_value(common_password)
        sign_in_1.sign_in_button.click()

        profile_1.just_fyi('Remove user from contact and check there is no profile image displayed')
        group_chat_2.profile_button.double_click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()
        one_to_one_chat_2.remove_from_contacts.click()
        # Send message to User 2 so update of profile image picked up
        group_chat_1 = home_1.get_chat('new_group_chat').click()
        group_chat_1.send_message(group_chat_message)
        one_to_one_chat_2.close_button.click()
        one_to_one_chat_2.home_button.double_click()
        if home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture is not default to default after user removed from Contacts')

        profile_2.just_fyi('Enable to see profile image from "Everyone" setting')
        home_2.profile_button.double_click()
        profile_2.privacy_and_security_button.click()
        profile_2.show_profile_pictures_of.scroll_and_click()
        profile_2.element_by_translation_id("everyone").click()
        group_chat_1.send_message(group_chat_message)
        profile_2.home_button.click(desired_view='home')
        if not home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_chats):
            self.errors.append('User profile picture is not returned to default after user removed from Contacts')
        self.errors.verify_no_errors()

    @marks.testrail_id(5432)
    @marks.medium
    def test_custom_bootnodes(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        public_key = home_2.get_public_key_and_username()

        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        username_1, username_2 = profile_1.default_username_text.text, profile_2.default_username_text.text

        profile_1.just_fyi('Add custom bootnode, enable bootnodes and check validation')
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.add_bootnode_button.click()
        profile_1.specify_name_input.set_value('test')
        # TODO: blocked as validation is missing for bootnodes (rechecked 23.11.21, valid)
        # profile_1.bootnode_address_input.set_value('invalid_bootnode_address')
        # if not profile_1.element_by_text_part('Invalid format').is_element_displayed():
        #      self.errors.append('Validation message about invalid format of bootnode is not shown')
        # profile_1.save_button.click()
        # if profile_1.add_bootnode_button.is_element_displayed():
        #      self.errors.append('User was navigated to another screen when tapped on disabled "Save" button')
        # profile_1.bootnode_address_input.clear()
        profile_1.bootnode_address_input.set_value(bootnode_address)
        profile_1.save_button.click()
        profile_1.enable_bootnodes.click()
        profile_1.home_button.click()

        profile_1.just_fyi('Add contact and send first message')
        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        profile_2.home_button.click()
        chat_2 = home_2.get_chat(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        chat_2.add_to_contacts.click()

        profile_1.just_fyi('Disable custom bootnodes')
        chat_1.profile_button.click()
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.enable_bootnodes.click()
        profile_1.home_button.click()

        profile_1.just_fyi('Send message and check that it is received after disabling bootnodes')
        home_1.get_chat(username_2).click()
        message_1 = 'new message'
        chat_1.chat_message_input.send_keys(message_1)
        chat_1.send_message_button.click()
        for chat in chat_1, chat_2:
            if not chat.chat_element_by_text(message_1).is_element_displayed():
                self.errors.append('Message was not received after enabling bootnodes!')
        self.errors.verify_no_errors()

    @marks.testrail_id(5436)
    @marks.medium
    @marks.flaky
    def test_add_switch_delete_custom_mailserver(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        public_key = home_2.get_public_key_and_username()
        home_2.home_button.click()

        profile_1 = home_1.profile_button.click()
        username_1 = profile_1.default_username_text.text

        profile_1.just_fyi('disable autoselection')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        mailserver = profile_1.return_mailserver_name(mailserver_hk, used_fleet)
        profile_1.mail_server_auto_selection_button.click()
        profile_1.mail_server_by_name(mailserver).click()
        profile_1.confirm_button.click()

        profile_1.just_fyi('add custom mailserver (check address/name validation) and connect to it')
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.save_button.click()
        if profile_1.element_by_text(mailserver).is_element_displayed():
            self.errors.append('Could add custom mailserver with empty address and name')
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value(mailserver_address[:-3])
        profile_1.save_button.click()
        if not profile_1.element_by_text_part("Invalid format").is_element_displayed():
            self.errors.append('could add custom mailserver with invalid address')
        profile_1.mail_server_address_input.clear()
        profile_1.mail_server_address_input.set_value(mailserver_address)
        profile_1.save_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()
        if profile_1.element_by_text_part("Error connecting").is_element_displayed(40):
            profile_1.retry_to_connect_to_mailserver()
        profile_1.get_back_to_home_view()
        profile_1.home_button.click()

        profile_1.just_fyi('start chat with user2 and check that all messages are delivered')
        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2 = home_2.get_chat(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        message_1 = 'new message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element()

        profile_1.just_fyi('delete custom mailserver')
        chat_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.element_by_text(mailserver).scroll_to_element()
        profile_1.element_by_text(mailserver).click()
        profile_1.confirm_button.click()
        profile_1.element_by_text(server_name).scroll_to_element()
        profile_1.element_by_text(server_name).click()
        profile_1.mail_server_delete_button.scroll_to_element()
        profile_1.mail_server_delete_button.click()
        profile_1.mail_server_confirm_delete_button.click()
        if profile_1.element_by_text(server_name).is_element_displayed():
            self.errors.append('Deleted custom mailserver is shown')
        profile_1.get_back_to_home_view()
        profile_1.relogin()
        chat_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        if profile_1.element_by_text(server_name).is_element_displayed():
            self.errors.append('Deleted custom mailserver is shown after relogin')

        self.errors.verify_no_errors()

    @marks.testrail_id(5767)
    @marks.medium
    def test_can_not_connect_to_mailserver(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        profile_1 = home_1.profile_button.click()

        profile_1.just_fyi('add non-working mailserver and connect to it')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.mail_server_auto_selection_button.click()
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value('%s%s' % (mailserver_address[:-3], '553'))
        profile_1.save_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.wait_and_click()
        profile_1.confirm_button.wait_and_click()

        profile_1.just_fyi('check that popup "Error connecting" will not reappear if tap on "Cancel"')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        profile_1.cancel_button.click()

        home_2.just_fyi('send several messages to public channel')
        public_chat_name = home_2.get_random_chat_name()
        message = 'test_message'
        public_chat_2 = home_2.join_public_chat(public_chat_name)
        public_chat_2.chat_message_input.send_keys(message)
        public_chat_2.send_message_button.click()
        public_chat_2.back_button.click()

        profile_1.just_fyi('join same public chat and try to reconnect via "Tap to reconnect" and check "Connecting"')
        profile_1.home_button.click()
        public_chat_1 = home_1.join_public_chat(public_chat_name)
        public_chat_1.reopen_app()

        profile_1.just_fyi('check that still connected to custom mailserver after relogin')
        home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        if not profile_1.element_by_text(server_name).is_element_displayed():
            self.drivers[0].fail("Not connected to custom mailserver after re-login")

        profile_1.just_fyi('check that can RETRY to connect')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        public_chat_1.element_by_translation_id('mailserver-retry', uppercase=True).wait_and_click(60)

        profile_1.just_fyi('check that can pick another mailserver and receive messages')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        profile_1.element_by_translation_id('mailserver-pick-another', uppercase=True).wait_and_click(120)
        mailserver = profile_1.return_mailserver_name(mailserver_ams, used_fleet)
        profile_1.element_by_text(mailserver).click()
        profile_1.confirm_button.click()
        profile_1.home_button.click()
        home_1.get_chat('#%s' % public_chat_name).click()
        if not public_chat_1.chat_element_by_text(message).is_element_displayed(60):
            self.errors.append("Chat history wasn't fetched")

        self.errors.verify_no_errors()

    @marks.testrail_id(6332)
    @marks.medium
    def test_disable_use_history_node(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        profile_1 = home_1.profile_button.click()

        home_2.just_fyi('send several messages to public channel')
        public_chat_name = home_2.get_random_chat_name()
        message, message_no_history = 'test_message', 'history node is disabled'
        public_chat_2 = home_2.join_public_chat(public_chat_name)
        public_chat_2.send_message(message)

        profile_1.just_fyi(
            'disable use_history_node and check that no history is fetched but you can still send messages')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.use_history_node_button.click()
        profile_1.home_button.click()
        public_chat_1 = home_1.join_public_chat(public_chat_name)
        if public_chat_1.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append('Chat history was fetched when use_history_node is disabled')
        public_chat_1.send_message(message_no_history)
        if not public_chat_2.chat_element_by_text(message_no_history).is_element_displayed(30):
            self.errors.append('Message sent when use_history_node is disabled was not received')
        public_chat_1.profile_button.click()
        profile_1.relogin()
        home_1.get_chat('#%s' % public_chat_name).click()
        if public_chat_1.chat_element_by_text(message).is_element_displayed(30):
            self.drivers[0].fail('History was fetched after relogin when use_history_node is disabled')

        profile_1.just_fyi('enable use_history_node and check that history is fetched')
        home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.use_history_node_button.click()
        profile_1.home_button.click(desired_view='chat')
        if not public_chat_1.chat_element_by_text(message).is_element_displayed(60):
            self.errors.append('History was not fetched after enabling use_history_node')
        self.errors.verify_no_errors()

    @marks.testrail_id(695856)
    @marks.medium
    def test_pair_devices_sync_photo_community_group_chats(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.backup_recovery_phrase_button.click()
        recovery_phrase = profile_1.backup_recovery_phrase()
        profile_1.home_button.double_click()
        name_1, name_2 = 'device_%s' % device_1.driver.number, 'device_%s' % device_2.driver.number
        comm_before_sync_name, channel, message = 'b-%s' % home_1.get_random_chat_name(), 'some-rand-chann', 'comm_message'
        comm_joined_name = 'Status'
        comm_after_sync_name = 'a-public-%s' % home_1.get_random_chat_name()
        group_chat_name = 'group-%s' % home_1.get_random_chat_name()
        channel_after_sync, message_after_sync = 'chann-after-sync', 'sent after sync'

        device_1.just_fyi('join Status community, create community, create group chat, edit user picture')
        # Follow Status community
        # TODO: no predefined community to follow now
        # home_1.element_by_text(comm_joined_name).scroll_and_click()
        # from views.chat_view import CommunityView
        # comm_to_join_1 = CommunityView(self.drivers[0])
        # comm_to_join_1.follow_button.wait_and_click()
        # comm_to_join_1.home_button.double_click()
        # Create community as admin, add channel, send message
        comm_before_1 = home_1.create_community(comm_before_sync_name)
        channel_before_1 = comm_before_1.add_channel(channel)
        channel_before_1.send_message(message)
        home_1.home_button.double_click()
        # Starting group chat
        one_to_one_1 = home_1.add_contact(basic_user['public_key'])
        one_to_one_1.home_button.click()
        group_chat_1 = home_1.create_group_chat([basic_user['username']], group_chat_name)
        group_chat_1.home_button.click()
        # Editing profile picture
        home_1.profile_button.double_click()
        profile_1.edit_profile_picture('sauce_logo.png')

        device_2.just_fyi('go to profile > Devices, set device name, discover device 2 to device 1')
        home_2 = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        profile_2 = home_2.profile_button.click()

        device_2.just_fyi('Pair main and secondary devices')
        profile_2.discover_and_advertise_device(name_2)
        profile_1.discover_and_advertise_device(name_1)
        profile_1.get_toggle_device_by_name(name_2).wait_and_click()
        profile_1.sync_all_button.click()
        profile_1.sync_all_button.wait_for_visibility_of_element(15)
        [device.profile_button.double_click() for device in (profile_1, profile_2)]

        device_2.just_fyi('check that created/joined community and profile details are updated')
        home_2 = profile_2.home_button.click()
        # TODO: no predefined community to follow
        # for community in (comm_before_sync_name, comm_joined_name):
        if not home_2.get_chat(comm_before_sync_name, community=True).is_element_displayed():
            self.errors.append('Community %s was not appeared after initial sync' % comm_before_sync_name)
        comm_before_2 = home_2.get_chat(comm_before_sync_name, community=True).click()
        channel_2 = comm_before_2.get_chat(channel).click()
        if not channel_2.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append("Message sent to community channel before sync is not shown!")

        device_1.just_fyi("Send message, add new channel and check it will be synced")
        home_1.home_button.click()
        home_1.get_chat(comm_before_sync_name, community=True).click()
        channel_1 = comm_before_1.get_chat(channel).click()
        channel_1.send_message(message_after_sync)
        if not channel_2.chat_element_by_text(message_after_sync).is_element_displayed(30):
            self.errors.append("Message sent to community channel after sync is not shown!")
        [channel.back_button.click() for channel in (channel_1, channel_2)]
        [home.get_chat(comm_before_sync_name, community=True).click() for home in (home_1, home_2)]
        comm_before_1.add_channel(channel_after_sync)
        if not comm_before_2.get_chat(channel_after_sync).is_element_displayed(30):
            self.errors.append("New added channel after sync is not shown!")

        device_1.just_fyi("Leave community and check it will be synced")
        [home.home_button.double_click() for home in (home_1, home_2)]
        home_1.get_chat(comm_before_sync_name, community=True).click()
        comm_before_1.leave_community()
        if not home_2.element_by_text_part(comm_before_sync_name).is_element_disappeared(30):
            self.errors.append("Leaving community was not synced!")

        device_1.just_fyi("Adding new community and check it will be synced")
        home_1.create_community(comm_after_sync_name)
        if not home_2.element_by_text(comm_after_sync_name).is_element_displayed(30):
            self.errors.append('Added community was not appeared after initial sync')

        # TODO: skip until #11558 (rechecked 23.11.21, valid)
        # home_2.profile_button.click()
        # if not profile_2.profile_picture.is_element_image_equals_template('sauce_logo_profile.png'):
        #     self.errors.append('Profile picture was not updated after initial sync')
        # profile_2.home_button.click()
        #
        device_1.just_fyi('send message to group chat, check that message in group chat is shown')
        home_1 = profile_1.home_button.click()
        home_1.get_chat(group_chat_name).click()
        group_chat_1.send_message(message_after_sync)
        group_chat_1.back_button.click()
        group_chat_2 = home_2.get_chat(group_chat_name).click()
        if not group_chat_2.chat_element_by_text(message_after_sync).is_element_displayed():
            self.errors.append('"%s" message in group chat is not synced' % message_after_sync)

        self.errors.verify_no_errors()
