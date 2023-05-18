from tests import marks, pytest_config_global, test_dapp_name, staging_fleet, mailserver_hk, mailserver_ams, \
    mailserver_gc
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests.users import upgrade_users, transaction_recipients, basic_user, ens_user, transaction_senders
from views.sign_in_view import SignInView
import views.dbs.chats.data as chat_data
import views.dbs.dapps.data as dapp_data
import views.dbs.pairing.data as sync_data
import views.dbs.group.data as group


@marks.upgrade
class TestUpgradeApplication(SingleDeviceTestCase):

    @marks.testrail_id(6284)
    @marks.flaky
    def test_unread_previews_public_chat_version_upgrade(self):
        sign_in = SignInView(self.driver)
        unread_one_to_one_name, unread_public_name = 'All Whopping Dassierat', '#before-upgrade'
        chats = chat_data.chats
        seed = upgrade_users['chats']['passphrase']
        home = sign_in.import_db(seed_phrase=seed, import_db_folder_name='chats')
        home.just_fyi("Grab profile version")

        profile = home.profile_button.click()
        profile.about_button.click()
        old_version = profile.app_version_text.text

        profile.upgrade_app()
        home = sign_in.sign_in()
        home.profile_button.click()
        profile.about_button.click()
        new_version = profile.app_version_text.text
        if 'release' in pytest_config_global['apk_upgrade']:
            if new_version == old_version:
                self.errors.append('Upgraded app version is %s vs base version is %s ' % (new_version, old_version))

        home.home_button.click()

        home.just_fyi("Check chat previews")
        for chat in chats.keys():
            if 'preview' in chats.keys():
                actual_chat_preview = home.get_chat(chat).chat_preview.text
                expected_chat_preview = chats[chat]['preview']
                if actual_chat_preview != expected_chat_preview:
                    self.errors.append('Expected preview for %s is "%s", in fact "%s"' % (chat, expected_chat_preview, actual_chat_preview))

        home.just_fyi("Check unread indicator")
        if home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')
        unread_one_to_one, unread_public = home.get_chat(unread_one_to_one_name), home.get_chat(unread_public_name)
        if unread_one_to_one.new_messages_counter.text != chats[unread_one_to_one_name]['unread']:
            self.errors.append('New messages counter is not shown on chat element')
        if not unread_public.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is not shown in public chat')

        home.just_fyi("Check images / add to contacts")
        not_contact = unread_one_to_one_name
        not_contact_chat = home.get_chat(not_contact).click()
        if not not_contact_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts is not shown in 1-1 chat')
        images = not_contact_chat.image_message_in_chat.find_elements()
        if len(images) != 2:
            self.errors.append('%s images are shown instead of 2' % str(len(images)))
        for message in chats[not_contact]['messages']:
            if not not_contact_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('"%s" is not shown after upgrade' % message)
        home.home_button.double_click()
        if unread_one_to_one.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element after opening chat')

        home.just_fyi("Checking previews in public chat")
        pub_chat_data = chats[unread_public_name]
        home.element_by_text(unread_public_name).click()
        public_chat = home.get_chat_view()
        public_chat.scroll_to_start_of_history()
        for key in pub_chat_data['preview_messages']:
            home.just_fyi("Checking %s preview case in public chat" % key)
            data = pub_chat_data['preview_messages'][key]
            if not public_chat.element_by_text_part(data['txt']).is_element_displayed():
                public_chat.element_by_text_part(data['txt']).scroll_to_element()
            message = public_chat.get_preview_message_by_text(data['txt'])
            if not message.preview_image:
                self.errors.append('Preview message is not shown for %s' % key)
            if 'title' in data:
                if message.preview_title.text != data['title']:
                    self.errors.append(
                        "Title '%s' does not match expected '%s'" % (message.preview_title.text, data['title']))
                if message.preview_subtitle.text != data['subtitle']:
                    self.errors.append("Subtitle '%s' does not match expected '%s'" % (message.preview_subtitle.text, data['subtitle']))
        home.home_button.click()

        home.just_fyi("Checking markdown messages")
        markdown_name = '#before-upgrade-3'
        pub_chat_data = chats[markdown_name]
        public_chat = home.get_chat(markdown_name).click()
        messages = pub_chat_data['markdown_text_messages']
        public_chat.element_starts_with_text(messages[0]).scroll_to_element(10, 'up')
        public_chat.element_starts_with_text('quoted').scroll_to_element()
        for i in range(len(messages)):
            if not public_chat.chat_element_by_text(messages[i]).is_element_displayed():
                self.errors.append("Markdown message '%s' does not match expected in %s" % (messages[i], markdown_name))
        public_chat.home_button.click()

        home.just_fyi("Checking reactions, sticker, tag messages")
        public_chat = home.get_chat(markdown_name).click()
        tag_message = public_chat.chat_element_by_text(pub_chat_data['tag'])
        if tag_message.emojis_below_message(emoji='love', own=True) != 1:
            self.errors.append("Reactions are not displayed below tag message!")
        if not public_chat.sticker_message.is_element_displayed():
            self.errors.append("No sticker message is shown!")
        public_chat.element_starts_with_text(pub_chat_data['tag']).click()
        if not public_chat.user_name_text.text == pub_chat_data['tag']:
            self.errors.append('Could not redirect a user to a public chat tapping the tag message after upgrade')
        home.home_button.click()

        home.just_fyi("Checking reply to long messages and mentions")
        mention = '#before-upgrade-2'
        public_chat = home.get_chat(mention).click()
        public_chat.scroll_to_start_of_history()
        pub_chat_data = chats[mention]
        public_chat.element_starts_with_text(pub_chat_data['reply']).scroll_to_element()
        public_replied_message = public_chat.chat_element_by_text(pub_chat_data['reply'])
        if pub_chat_data['long'] not in public_replied_message.replied_message_text:
            self.errors.append("Reply is not present in message received in public chat %s after upgrade" % mention)
        public_chat.element_starts_with_text(pub_chat_data['mention']).scroll_to_element()
        if not public_chat.chat_element_by_text(pub_chat_data['mention']).is_element_displayed():
            self.errors.append("Mention is not present in %s after upgrade" % mention)
        home.home_button.click()

        home.just_fyi("Checking collapsable messages")
        long_name = '#before-upgrade-4'
        public_chat = home.get_chat(long_name).click()
        pub_chat_data = chats[long_name]
        if not public_chat.chat_element_by_text(pub_chat_data['long']).uncollapse:
            self.errors.append("No uncollapse icon on long message is shown in %s!" % long_name)

        self.errors.verify_no_errors()

    @marks.testrail_id(695804)
    @marks.flaky
    def test_dapps_browser_several_accounts_upgrade(self):
        sign_in = SignInView(self.driver)
        favourites = dapp_data.dapps['favourites']
        seed = transaction_recipients['K']['passphrase']
        home = sign_in.import_db(seed_phrase=seed, import_db_folder_name='dapps')
        home.upgrade_app()
        sign_in.sign_in()
        dapps = home.dapp_tab_button.click()

        sign_in.just_fyi('Check Dapps favourites')
        for key in favourites:
            if not dapps.element_by_text(key).is_element_displayed():
                self.errors.append('Name of bookmark "%s" is not shown in favourites!' % key)
            if not dapps.element_by_text(favourites[key]).is_element_displayed():
                self.errors.append('"%s" of bookmark is not shown in favourites!' % favourites[key])

        sign_in.just_fyi('Check dapps are still in history')
        browsing = sign_in.get_base_web_view()
        browsing.open_tabs_button.click()
        visited = dapp_data.dapps['history']['visited']
        for key in visited:
            if not dapps.element_by_text(key).is_element_displayed():
                self.errors.append('Name of tab "%s" is not shown in browser history!' % key)
            if not dapps.element_by_text(visited[key]).is_element_displayed():
                self.errors.append('"%s" of tab is not shown in browser history!' % visited[key])
        if dapps.element_by_text(dapp_data.dapps['history']['deleted']).is_element_displayed():
            self.errors.append('Closed tab is shown in browser!')

        sign_in.just_fyi('Check browser history is kept')
        github = dapp_data.dapps['browsed_page']
        dapps.element_by_text(github['name']).click()
        browsing.wait_for_d_aap_to_load()
        browsing.browser_previous_page_button.click()
        browsing.wait_for_d_aap_to_load()
        if not dapps.element_by_text(github['previous_text']).is_element_displayed():
            self.errors.append('Previous page is not opened!')

        sign_in.just_fyi('Check permissions for dapp')
        profile = dapps.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        if profile.element_by_text_part(dapp_data.dapps['permissions']['deleted']).is_element_displayed():
            self.errors.append('Deleted permissions reappear after upgrade!')
        profile.element_by_text(test_dapp_name).click()
        permissions = dapp_data.dapps['permissions']['added'][test_dapp_name]
        for text in permissions:
            if not profile.element_by_text(text).is_element_displayed():
                self.errors.append('%s is deleted after upgrade from %s permissions' % (text, test_dapp_name))

        sign_in.just_fyi('Check that balance is preserved')
        accounts = dapp_data.wallets
        wallet = profile.wallet_button.click()
        for asset in ('ETH', 'YEENUS', 'STT'):
            wallet.wait_balance_is_changed(asset=asset)

        sign_in.just_fyi('Check accounts inside multiaccount')
        if not wallet.element_by_text(accounts['generated']['address']).is_element_displayed():
            self.errors.append('Address of generated account is not shown')
        generated = wallet.get_account_by_name(accounts['generated']['name'])
        if not generated.color_matches('multi_account_color.png'):
            self.errors.append('Colour of generated account does not match expected after upgrade')

        wallet.get_account_by_name(accounts['default']['name']).swipe_left_on_element()
        if not wallet.element_by_text(dapp_data.wallets['watch-only']['name']).is_element_displayed():
            self.errors.append('Watch-only account is not shown')
        if not wallet.element_by_text(accounts['watch-only']['address']).is_element_displayed():
            self.errors.append('Address of watch-only account is not shown')

        self.errors.verify_no_errors()

    @marks.testrail_id(695810)
    def test_keycard_upgrade(self):
        user = transaction_senders['ETH_STT_ADI_1']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(passphrase=user['passphrase'], keycard=True)
        wallet = home.wallet_button.click()
        wallet.wait_balance_is_changed(asset='YEENUS', scan_tokens=True)
        home.upgrade_app()

        home.just_fyi('Check that can login with restored from mnemonic keycard account')
        sign_in.sign_in(keycard=True)
        home.wallet_button.click()
        for asset in ['ETH', 'YEENUS', 'STT']:
            if wallet.get_asset_amount_by_name(asset) == 0:
                self.errors.append('Asset %s was not restored' % asset)

        home.just_fyi('Check that can sign transaction in STT from wallet')
        transaction_amount = wallet.get_unique_amount()
        wallet.send_transaction(amount=transaction_amount, asset_name='STT', sign_transaction=True, keycard=True,
                                recipient=transaction_senders['ETH_STT_1']['address'])
        self.network_api.find_transaction_by_unique_amount(user['address'], transaction_amount, token=True)

        wallet.just_fyi('Check that transaction is appeared in transaction history')
        wallet.find_transaction_in_history(amount=transaction_amount, asset='STT')

        home.just_fyi('Check that can sign transaction in Dapp')
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        status_test_dapp.send_two_tx_in_batch_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction.sign_transaction(keycard=True)
        send_transaction.sign_transaction(keycard=True)
        self.errors.verify_no_errors()


@marks.upgrade
class TestUpgradeMultipleApplication(MultipleDeviceTestCase):

    @marks.testrail_id(695783)
    def test_commands_audio_backward_compatibility_upgrade(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_2_home = device_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()
        user = upgrade_users['chats']
        seed = user['passphrase']

        device_1.just_fyi("Import db, upgrade")
        home = device_1.import_db(seed_phrase=seed, import_db_folder_name='chats')
        home.upgrade_app()
        home = device_1.sign_in()

        device_1.just_fyi("Check messages in 1-1 chat")
        command_username = 'Royal Defensive Solenodon'
        messages = chat_data.chats[command_username]['messages']
        home.swipe_up()
        home.swipe_up()
        chat = home.get_chat(command_username).click()
        if chat.add_to_contacts.is_element_displayed():
            self.errors.append('User is deleted from contacts after upgrade')
        chat.scroll_to_start_of_history()
        if chat.audio_message_in_chat_timer.text != messages['audio']['length']:
            self.errors.append('Timer is not shown for audiomessage')
        device_1.just_fyi('Check command messages')
        commnad_messages = chat_data.chats[command_username]['commands']
        for key in commnad_messages:
            device_1.just_fyi('Checking %s command messages' % key)
            amount = commnad_messages[key]['value']
            chat.element_by_text(amount).scroll_to_element()
            if 'incoming' in key:
                message = chat.get_transaction_message_by_asset(amount, incoming=True)
            else:
                message = chat.get_transaction_message_by_asset(amount, incoming=False)
            if not message.transaction_status != commnad_messages[key]['status']:
                self.errors.append('%s case transaction status is not equal expected after upgrade' % key)
            if key == 'outgoing_STT_sign':
                chat.swipe_up()
                if not message.sign_and_send.is_element_displayed():
                    self.errors.append('No "sign and send" option is shown for %s' % key)
        chat.home_button.click()

        device_2.just_fyi("Create upgraded and non-upgraded app can exchange messages")
        message, response = "message after upgrade", "response"
        device_1_chat = home.add_contact(device_2_public_key)
        device_1_chat.send_message(message)
        device_2_chat = device_2_home.get_chat(user['username']).click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message sent from upgraded app is not shown on  previous release!")
        device_2_chat.send_message(response)
        if not device_1_chat.chat_element_by_text(response).is_element_displayed():
            self.errors.append("Message sent from previous release is not shown on upgraded app!")

        self.errors.verify_no_errors()

    @marks.testrail_id(695805)
    @marks.flaky
    def test_devices_sync_contact_management_upgrade(self):
        self.create_drivers(2)
        user = transaction_recipients['K']
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi("Import db, upgrade")
        seed = user['passphrase']
        home_1 = device_1.import_db(seed_phrase=seed, import_db_folder_name='pairing/main')
        home_2 = device_2.import_db(seed_phrase=seed, import_db_folder_name='pairing/secondary')
        for device in (device_1, device_2):
            device.upgrade_app()
            device.sign_in()

        device_1.just_fyi("Contacts: check blocked and removed contacts, contacts with ENS")
        if home_1.element_by_text(sync_data.chats['deleted']).is_element_displayed():
            self.error.append("Removed public chat reappears after upgrade!")
        profile_1 = home_1.profile_button.click()
        profile_1.contacts_button.click()
        synced = sync_data.contacts['synced']
        for username in list(synced.values()):
            if not profile_1.element_by_text(username).is_element_displayed():
                self.error.append("'%s' is not shown in contacts list after upgrade!" % username)
        if profile_1.element_by_text_part(sync_data.contacts['removed']).is_element_displayed():
            self.error.append("Removed user is shown in contacts list after upgrade!")
        profile_1.blocked_users_button.click()
        if not profile_1.element_by_text_part(sync_data.contacts['blocked']).is_element_displayed():
            self.error.append("Blocked user is not shown in contacts list after upgrade!")

        device_2.just_fyi("Pairing: check synced public chats on secondary device")
        for chat in sync_data.chats['synced_public']:
            if not home_2.element_by_text(chat).is_element_displayed():
                self.error.append("Synced public chat '%s' is not shown on secondary device after upgrade!" % chat)

        device_1.just_fyi("Pairing: check that can send messages to chats and they will appear on secondary device")
        main_1_1, secondary_1_1, group_name = synced['ens'], synced['username_ens'], sync_data.chats['group']
        message = 'Device pairing check'
        device_1.home_button.click()
        chat_1 = home_1.get_chat(main_1_1).click()
        chat_1.send_message(message)
        home_2.get_chat(secondary_1_1).wait_for_visibility_of_element()
        chat_2 = home_2.get_chat(secondary_1_1).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed():
            self.error.append(
                "Message in 1-1 chat does not appear on device 2 after sending from main device after upgrade")
        [chat.home_button.click() for chat in (chat_1, chat_2)]
        chat_1 = home_1.get_chat(group_name).click()
        chat_1.send_message(message)
        home_2.get_chat(group_name).wait_for_visibility_of_element()
        chat_2 = home_2.get_chat(group_name).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed():
            self.error.append(
                "Message in group chat does not appear on device 2 after sending from main device after upgrade")
        [chat.home_button.click() for chat in (chat_1, chat_2)]

        device_1.just_fyi("Pairing: add public chat and check it will appear on secondary device")
        public = sync_data.chats['added_public']
        chat_1 = home_1.join_public_chat(public[1:])
        chat_1.send_message(message)
        home_2.get_chat(public).wait_for_visibility_of_element()
        chat_2 = home_2.get_chat(public).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed():
            self.error.append(
                "Message in public chat does not appear on device 2 after sending from main device after upgrade")
        [chat.home_button.click() for chat in (chat_1, chat_2)]

        device_1.just_fyi("Pairing: add contact and check that it will appear on secondary device")
        added = sync_data.contacts['added']
        chat_1 = home_1.add_contact(added['public_key'], nickname=added['name'])
        chat_1.send_message(message)
        home_2.get_chat(added['name']).wait_for_visibility_of_element()
        chat_2 = home_2.get_chat(added['name']).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed():
            self.error.append(
                "Message in new 1-1 chat does not appear on device 2 after sending from main device after upgrade")

        device_2.just_fyi("Pairing: check that contacts/nicknames are synced")
        synced_secondary = {synced['nickname'], synced['username_nickname'], synced['username_ens'], added['name'],
                            added['username']}
        profile_2 = chat_2.profile_button.click()
        profile_2.contacts_button.click()
        for username in synced_secondary:
            if not profile_2.element_by_text(username).is_element_displayed():
                self.error.append("'%s' is not shown in contacts list on synced device after upgrade!" % username)

        self.errors.verify_no_errors()

    @marks.testrail_id(695811)
    @marks.flaky
    def test_devices_group_chats_upgrade(self):
        self.create_drivers(2)
        admin, member = ens_user, transaction_recipients['J']
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi("Import db, upgrade")
        home_1 = device_1.import_db(seed_phrase=admin['passphrase'], import_db_folder_name='group/admin')
        home_2 = device_2.import_db(seed_phrase=member['passphrase'], import_db_folder_name='group/member')
        for device in (device_1, device_2):
            device.upgrade_app()
            device.sign_in()

        home_1.just_fyi("Check that all group chats are preserved after upgrade")
        names = [sub["name"] for sub in
                 (group.main, group.empty_invite, group.make_admin, group.to_join, group.to_remove)]
        for home in home_1, home_2:
            for name in names:
                if not home.get_chat(name).is_element_displayed():
                    self.errors.append("%s is not shown on device %s" % (name, home.driver.number))
        if home_2.element_by_text(group.to_delete['name']).is_element_displayed():
            self.errors.append("Deleted group chat reappeared after upgrade")

        home_1.just_fyi("Check messages in main group chat and resolved ENS")
        chat_name, messages = group.main["name"], group.main["messages"]
        [chat_1, chat_2] = [home.get_chat(chat_name).click() for home in (home_1, home_2)]
        for chat in [chat_1, chat_2]:
            if not chat.chat_element_by_text(messages["text"]).is_element_displayed():
                self.errors.append("Text message in group chat is not shown after upgrade")
            reply_message = chat.chat_element_by_text(messages['reply'])
            if messages['text'] not in reply_message.replied_message_text:
                self.errors.append("Reply is not present in message received in group chat after upgrade")
        if not chat_2.chat_element_by_text(messages['invite']).uncollapse:
            self.errors.append("No uncollapse icon on long message is shown!")
        resolved_ens = '@%s' % admin['ens_upgrade']
        chat_2.chat_element_by_text(messages['text']).username.scroll_to_element(direction='up')
        if chat_2.chat_element_by_text(messages['text']).username.text != resolved_ens:
            self.errors.append("ENS '%s' is not resolved in group chat '%s'" % (resolved_ens, chat_name))
        [chat.home_button.click() for chat in [chat_1, chat_2]]

        home_1.just_fyi("Check that can join group chat after upgrade")
        chat_name = group.to_join['name']
        invite_message = chat_1.invite_system_message(resolved_ens, member['username'])
        [chat_1, chat_2] = [home.get_chat(chat_name).click() for home in (home_1, home_2)]
        if not chat_2.chat_element_by_text(invite_message).is_element_displayed():
            self.errors.append("'%s' is not shown after upgrade in '%s'" % (invite_message, chat_name))
        chat_2.join_chat_button.click()
        joined_system_message = chat_1.join_system_message(member['username'])
        if not chat_1.chat_element_by_text(joined_system_message).is_element_displayed(30):
            self.errors.append("'%s' is not shown after user was joined in '%s'" % (joined_system_message, chat_name))
        [chat.home_button.double_click() for chat in [chat_1, chat_2]]

        home_2.just_fyi("Check that removed member can't send messages")
        chat_name = group.to_remove['name']
        [chat_1, chat_2] = [home.get_chat(chat_name).click() for home in (home_1, home_2)]
        if chat_2.chat_message_input.is_element_displayed():
            self.errors.append("Message input is available for removed member")
        [chat.home_button.double_click() for chat in [chat_1, chat_2]]

        home_1.just_fyi("Check both users remain admins, audio and images in chat")
        chat_name = group.make_admin['name']
        [chat_1, chat_2] = [home.get_chat(chat_name).click() for home in (home_1, home_2)]
        for chat in [chat_1, chat_2]:
            if not chat.image_message_in_chat.is_element_displayed():
                self.errors.append("Image in group chat is not shown after upgrade")
            if not chat.audio_message_in_chat_timer.is_element_displayed():
                self.errors.append('Timer is not shown for audiomessage in group chat')
        chat_1.chat_options.click()
        chat_1.group_info.click()
        chat_1.element_by_text(ens_user['username']).scroll_to_element()
        admins = chat_1.element_by_text('Admin').find_elements()
        if len(admins) != 2:
            self.errors.append('Not 2 admins in group chat')
        [chat.home_button.double_click() for chat in [chat_1, chat_2]]

        home_1.just_fyi("Check that can see invite and pending membership request after upgrade")
        chat_name = group.empty_invite['name']
        home_1.swipe_up()
        [chat_1, chat_2] = [home.get_chat(chat_name).click() for home in (home_1, home_2)]
        for text in group.empty_invite['texts']:
            if not chat_2.element_by_text(text).is_element_displayed():
                self.errors.append("%s is not shown upon invite after upgrade" % text)
        if not chat_1.group_membership_request_button.is_element_displayed():
            self.errors.append("No pending membership requests are shown for Admin")

        self.errors.verify_no_errors()

    @marks.testrail_id(695812)
    @marks.flaky
    def test_devices_activity_centre_profile_settings_upgrade(self):
        self.create_drivers(2)
        user = ens_user
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi("Import db")
        home_1 = device_1.import_db(seed_phrase=user['passphrase'], import_db_folder_name='group/admin')
        home_2 = device_2.create_user()
        profile_2 = home_2.profile_button.click()
        public_key_2, username_2 = profile_2.get_public_key(e)

        device_1.just_fyi("Activity centre: send message to 1-1 and invite member to group chat")
        chat_1 = home_1.add_contact(public_key_2, add_in_contacts=False)
        message = home_1.get_random_message()
        chat_1.send_message(message)

        device_2.just_fyi("Set profile photo")
        profile_2.edit_profile_picture(file_name='sauce_logo.png')
        device_1.just_fyi('Upgrading apps')
        for device in (device_1, device_2):
            device.upgrade_app()
            device.sign_in()

        device_1.just_fyi("Check status")
        timeline = home_1.status_button.click()
        statuses = group.timeline
        for element in timeline.element_by_text(
                statuses['text']), timeline.image_message_in_chat, timeline.element_by_text(statuses['link']):
            if not element.is_element_displayed():
                self.errors.append("Status is not shown after upgrade!")
        timeline.element_by_text(statuses['link']).click()
        if not device_1.element_by_text(statuses['resolved_username']).is_element_displayed():
            self.errors.append("Deep link with ahother user profile couldn't be opened")
        device_1.click_system_back_button()

        device_1.just_fyi("Check profile settings")
        profile_1 = device_1.profile_button.click()
        profile_1.element_by_translation_id("ens-your-your-name").click()
        for ens in user['ens'], user['ens_another']:
            if not profile_1.element_by_text(ens).is_element_displayed():
                self.errors.append("ENS name %s is not shown after upgrade" % ens)
        profile_1.profile_button.click()
        profile_1.privacy_and_security_button.click()

        if not profile_1.accept_new_chats_from_contacts_only.is_element_displayed():
            self.errors.append("Accept contacts from setting is not preserved after upgrade!")
        profile_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.show_profile_pictures_of.scroll_to_element()
        if not profile_1.show_profile_pictures_of.is_element_image_similar_to_template('block_dark.png'):
            self.errors.append('Dark mode is not applied!')
        if not profile_1.element_by_translation_id("everyone").is_element_displayed():
            self.errors.append("Show profile picture setting is not preserved after upgrade!")
        profile_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        mailservers = ['%s.%s' % (i, staging_fleet) for i in (mailserver_gc, mailserver_ams, mailserver_hk)]
        profile_1.swipe_up()
        for node in mailservers:
            if not profile_1.element_by_text(node).is_element_displayed():
                self.errors.append("Seems auto selection is on after upgrade, as %s is shown" % node)
        profile_1.profile_button.click()
        profile_1.advanced_button.click()
        if not profile_1.element_by_text(group.profile['log_level']).is_element_displayed():
            self.errors.append("Log level setting is not preserved after upgrade!")
        profile_1.home_button.click()

        device_2.just_fyi("Check activity centre and profile photo")
        home_2.profile_button.click()
        if not profile_2.profile_picture.is_element_image_similar_to_template('sauce_logo_profile.png'):
            self.errors.append('Profile picture was not shown after upgrade')
        profile_2.home_button.click()
        if not home_2.notifications_unread_badge.is_element_displayed():
            self.errors.append("Notifications badge in Activity centre is gone after upgrade")

        device_2.just_fyi("Send message after upgrade and check that profile photo is visible")
        home_1.get_chat(username_2).click()
        message = chat_1.get_random_message()
        chat_1.add_to_contacts.click()
        chat_1.send_message(message)
        chat_1.home_button.click()
        chat_2 = home_2.get_chat('@%s' % user['ens']).click()
        chat_2.send_message(message)
        if not home_1.get_chat(username_2).chat_image.is_element_image_similar_to_template('dark_sauce_logo.png'):
            self.errors.append('User profile picture was not updated on Chats view')

        self.errors.verify_no_errors()
