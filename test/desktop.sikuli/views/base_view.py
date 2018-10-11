import os

from views.base_element import BaseElement

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/base_view')


class HomeButton(BaseElement):
    def __init__(self):
        super(HomeButton, self).__init__(IMAGES_PATH + '/home_button.png')


class BaseView(object):

    def __init__(self):
        super(BaseView, self).__init__()
        self.home_button = HomeButton()
