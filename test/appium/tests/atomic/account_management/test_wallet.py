import pytest

from tests import marks, transaction_users
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from views.web_views.base_web_view import BaseWebView


@marks.all
@marks.account
class TestWallet(SingleDeviceTestCase):

    @marks.testrail_id(3698)
    def test_wallet_set_up(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        text = 'Simple and secure cryptocurrency wallet'
        if not wallet.element_by_text(text).is_element_displayed():
            self.errors.append("'%s' is not displayed" % text)
        wallet.set_up_button.click()
        text = ('This is your personal transaction phrase that you’ll use everytime you make a transaction. '
                'Make sure to write it down on a piece of paper, store it somewhere, '
                'and only confirm transactions when you see these three words.')
        if not wallet.element_by_text(text).is_element_displayed():
            self.errors.append("'%s' text is not displayed" % text)
        phrase_length = len(wallet.sign_in_phrase.list)
        if phrase_length != 3:
            self.errors.append('Transaction phrase length is %s' % phrase_length)
        wallet.done_button.click()
        for text in ['Wrote it down?', 'You won’t be able to see your 3-word transaction phrase again after this.']:
            if not wallet.element_by_text(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        wallet.yes_button.click()
        for element in [wallet.send_transaction_button, wallet.receive_transaction_button,
                        wallet.transaction_history_button]:
            if not element.is_element_displayed():
                self.errors.append('%s button is not shown after wallet setup' % element.name)
        self.verify_no_errors()

    @marks.testrail_id(1449)
    def test_open_transaction_on_etherscan(self):
        user = transaction_users['A_USER']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(user['passphrase'], user['password'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        transactions_view = wallet_view.transaction_history_button.click()
        transaction_details = transactions_view.transactions_table.get_first_transaction().click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.open_transaction_on_etherscan_button.click()
        base_web_view = BaseWebView(self.driver)
        base_web_view.web_view_browser.click()
        base_web_view.always_button.click()
        base_web_view.find_text_part(transaction_hash)

    @marks.testrail_id(1450)
    def test_copy_transaction_hash(self):
        user = transaction_users['A_USER']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(user['passphrase'], user['password'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        transactions_view = wallet_view.transaction_history_button.click()
        transaction_details = transactions_view.transactions_table.get_first_transaction().click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.copy_transaction_hash_button.click()
        transaction_details.get_back_to_home_view()
        wallet_view.home_button.click()
        chat_view = home_view.get_chat_with_user('user').click()
        chat_view.chat_message_input.paste_text_from_clipboard()
        if chat_view.chat_message_input.text != transaction_hash:
            pytest.fail('Transaction hash was not copied')

    @marks.testrail_id(3713)
    def test_manage_assets(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        wallet.options_button.click()
        wallet.manage_assets_button.click()
        select_asset = 'MDS'
        deselect_asset = 'STT'
        wallet.asset_checkbox_by_name(select_asset).click()
        wallet.asset_checkbox_by_name(deselect_asset).click()
        wallet.done_button.click()
        if not wallet.asset_by_name(select_asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet' % select_asset)
        if wallet.asset_by_name(deselect_asset).is_element_displayed():
            self.errors.append('%s asset is shown in wallet but was deselected' % deselect_asset)
        self.verify_no_errors()
