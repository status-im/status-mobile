from tests import test_dapp_name
from tests.users import basic_user, transaction_recipients
dapps = {
    'favourites' : {
        'Our fav simpleDAPP': 'https://simpledapp.eth'
    },
    'history': {
        'deleted': 'consent.google.com',
        'visited':{
            'Join GitHub · GitHub' : 'https://github.com/join?ref_cta=Sign+up&ref_loc=header+logged+out&ref_page=%2F&source=header-home',
            'DAPP' : 'https://simpledapp.eth'
        },

    },
    'browsed_page': {
        'name': 'Join GitHub · GitHub',
        'previous_url': 'https://github.com/',
        'previous_text': 'Where the world builds software'
    },
    'permissions': {
        'added': {
            test_dapp_name: {'dapp', 'Chat key'}
        },
        'deleted': 'consent.google.com'
    }
}
wallets = {
    'default' : {
        'name': 'Ethereum account1',
        'address': '0x%s' % transaction_recipients['K']['address']
        },
    'generated' : {
        'name': 'dapp',
        'address': '0x6a0e09b209eEa2a448B2361dD27c06Fc1f316e6c'
        },
    'watch-only': {
        'name': 'basic_user',
        'address': '0x%s' % basic_user['address']
        }
}