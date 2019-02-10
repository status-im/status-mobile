import os
# import pytesseract
import re
# from PIL import Image
from subprocess import check_output

try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass

from views.base_element import BaseElement, TextElement, InputField
from views.base_view import BaseView

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/profile_view')


class MailServerElement(TextElement):
    def __init__(self, server_name):
        super(MailServerElement, self).__init__(server_name)

    class ActiveImage(BaseElement):
        def __init__(self):
            super(MailServerElement.ActiveImage, self).__init__(IMAGES_PATH + '/mail_server_active_image.png')

    def is_active(self):
        print(self.element_line)


class ProfileView(BaseView):
    def __init__(self):
        super(ProfileView, self).__init__()
        self.share_my_code_button = TextElement('Share my contact code')
        self.copy_code_button = BaseElement(IMAGES_PATH + '/copy_code_button.png')
        self.log_out_button = BaseElement(IMAGES_PATH + '/log_out_button.png')
        self.back_up_recovery_phrase_button = TextElement('Backup your recovery')
        self.ok_continue_button = BaseElement(IMAGES_PATH + '/ok_continue_button.png')
        self.recovery_phrase_word_input = InputField(IMAGES_PATH + '/recovery_phrase_word_input.png')

    def get_mail_servers_list(self):
        server_names = []
        for line in collectLines():
            line_text = line.getText().encode('ascii', 'ignore')
            if 'mail-0' in line_text:
                server_names.append(line_text)

    def get_mail_server(self, name):
        return MailServerElement(name)

    def get_recovery_phrase(self):
        self.share_my_code_button.find_element()
        reg = Region(370, 130, 600, 240)
        current_text = reg.text().encode('ascii', 'ignore')
        phrase_list = re.findall(r"[\w']+", current_text.replace('1O', '10'))
        phrase_dict = dict()
        for i in range(len(phrase_list) - 1):
            if phrase_list[i].isdigit():
                phrase_dict[phrase_list[i]] = phrase_list[i + 1]
        return phrase_dict

    def get_recovery_phrase_word_number(self):
        image_name = 'recovery.png'
        # check_output(['import', '-window', 'root', image_name])
        # text = pytesseract.image_to_string(Image.open(image_name))
        # os.remove(image_name)
        # return re.findall('#(.*)\n', text)[0]
