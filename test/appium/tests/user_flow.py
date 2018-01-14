from tests import get_current_time


def create_user(console):
    console.request_password_icon.click()
    console.chat_request_input.send_keys("qwerty1234")
    console.confirm()
    console.chat_request_input.send_keys("qwerty1234")
    console.confirm()
    console.find_full_text(
        "Here is your signing phrase. You will use it to verify your transactions. Write it down and keep it safe!")


def recover_access(console, passphrase, password, username):
    recover_access_view = console.recover_button.click()
    recover_access_view.passphrase_input.send_keys(passphrase)
    recover_access_view.password_input.send_keys(password)
    recover_access_view.confirm_recover_access.click()
    recovered_user = recover_access_view.element_by_text(username, 'button')
    recover_access_view.confirm()
    recovered_user.click()
    recover_access_view.password_input.send_keys(password)
    recover_access_view.sign_in_button.click()
    recover_access_view.find_full_text('Wallet', 60)


def get_public_key(home):
    profile_view = home.profile_button.click()
    return profile_view.public_key_text.text


def get_address(home):
    profile_view = home.profile_button.click()
    return profile_view.profile_address_text.text


def add_contact(home, public_key):
    start_new_chat = home.plus_button.click()
    start_new_chat.add_new_contact.click()
    start_new_chat.public_key_edit_box.send_keys(public_key)
    start_new_chat.confirm()
    start_new_chat.confirm_public_key_button.click()


def create_group_chat(home, username_to_add, group_chat_name='new_group_chat'):
    start_new_chat = home.plus_button.click()
    start_new_chat.new_group_chat_button.click()
    user_contact = start_new_chat.element_by_text(username_to_add, 'button')
    user_contact.scroll_to_element()
    user_contact.click()
    start_new_chat.next_button.click()
    start_new_chat.name_edit_box.send_keys(group_chat_name)
    start_new_chat.save_button.click()


def get_new_username_and_status(profile):
    users_details = dict()
    current_time = get_current_time()
    new_status = '#newstatus_%s' % current_time
    new_username = 'New_User_Name_%s' % current_time

    profile.user_status_box.click()
    profile.user_status_input.clear()
    profile.user_status_input.send_keys(new_status)
    profile.username_input.clear()
    profile.username_input.send_keys(new_username)
    profile.save_button.click()
    users_details['status'] = new_status
    users_details['name'] = new_username
    return users_details
