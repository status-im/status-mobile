def set_password_as_new_user(*args):
    for view in args:
        view.request_password_icon.click()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.find_full_text(
            "Here is your signing phrase. You will use it to verify your transactions. Write it down and keep it safe!")


def recover_access(home, passphrase, password, username):
    login = home.recover_button.click()
    login.passphrase_input.send_keys(passphrase)
    login.password_input.send_keys(password)
    login.confirm_recover_access.click()
    recovered_user = login.element_by_text(username, 'button')
    login.confirm()
    recovered_user.click()
    login.password_input.send_keys(password)
    login.sign_in_button.click()
    login.find_full_text('Chats', 60)
