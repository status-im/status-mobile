import asyncio
import logging
from datetime import datetime

from support.test_data import TestSuiteData


@asyncio.coroutine
def start_threads(quantity: int, func: type, returns: dict, *args):
    loop = asyncio.get_event_loop()
    for i in range(quantity):
        returns[i] = loop.run_in_executor(None, func, *args)
    for k in returns:
        returns[k] = yield from returns[k]
    return returns


def get_current_time():
    return datetime.now().strftime('%-m%-d%-H%-M%-S')


def debug(text: str):
    logging.debug(text)


test_suite_data = TestSuiteData()


basic_user = dict()
basic_user['password'] = "newuniquepassword12"
basic_user['passphrase'] = "tree weekend ceiling awkward universe pyramid glimpse raven pair lounge grant grief"
basic_user['username'] = "Little Weighty Iberianmole"
basic_user['public_key'] = "0x040d3400f0ba80b2f6017a9021a66e042abc33cf7051ddf98a24a815c93d6c052ce2b7873d799f096325" \
                           "9f41c5a1bf08133dd4f3fe63ea1cceaa1e86ebc4bc42c9"

transaction_users = dict()
transaction_users['A_USER'] = dict()
transaction_users['A_USER']['password'] = "qwerty"
transaction_users['A_USER']['passphrase'] = "heavy earn fence pool drift balcony act coast stairs basic juice hip"
transaction_users['A_USER']['username'] = "Simplistic Personal Isabellinewheatear"
transaction_users['A_USER']['address'] = "c8f8b3376814d0dfe61e4b9eaf7970697c24f6b2"
transaction_users['A_USER']['public_key'] = "0x042bb1943a6516510ecb57356f1dbeeaf928e3260b4d0b8e81e1e139ab9e4be75" \
                                            "185086bedb27992d554d6e7782515e4c00d022b89d46ea5c208f0e5003c7286a4"

transaction_users['B_USER'] = dict()
transaction_users['B_USER']['password'] = "qwerty"
transaction_users['B_USER']['passphrase'] = "subway size dial screen stomach route skin kitchen radar true coffee six"
transaction_users['B_USER']['username'] = "Unselfish Thrifty Racer"
transaction_users['B_USER']['address'] = "85b159bad0d500483b307ec6892c7fa3be591cbc"
transaction_users['B_USER']['public_key'] = "0x04d25784aec35405a870dba2e44e02103bc6a258fe9fa8580bb1da4deeedf8827" \
                                            "baa3c44def9a4685a7db7a1d30c46d3ca9e7ef46a6ca9037a59791e2b0eca04fd"

transaction_users['C_USER'] = dict()
transaction_users['C_USER']['password'] = "qwerty"
transaction_users['C_USER']['passphrase'] = "potato labor object reward minor casino dismiss size flame task winter report"
transaction_users['C_USER']['username'] = "Magnificent Earnest Bandicoot"
transaction_users['C_USER']['address'] = "549b9bcd069d720390a6819ff8b3d9dd252fa3be"
transaction_users['C_USER']['public_key'] = "0x04099019246c7a54e581dcb68d0d78969a76a8753c588a62f2e7f0e1c28f88e574d" \
                                            "ae7d9cd6db9cdc6318714b2aa2945e1be8e1a5cf0f1f876b0c0627f8c6bfdb4"

transaction_users['D_USER'] = dict()
transaction_users['D_USER']['password'] = "qwerty"
transaction_users['D_USER']['passphrase'] = "weekend unable empty crime blind rhythm crunch answer travel coast silly like"
transaction_users['D_USER']['username'] = "Warped Murky Avocet"
transaction_users['D_USER']['address'] = "616874dc6cc2810cdc930dea26496fcf217d58ca"
transaction_users['D_USER']['public_key'] = "0x049191be3d0b0258732a4c3bd66d2e68cf7e536a0a975fd501a3265cba9ff5f7c78" \
                                            "f51345417e42f56e16926b685f99448ac05b9608b0d42cf65ee58853f104696"

transaction_users['E_USER'] = dict()
transaction_users['E_USER']['password'] = "newuniquepassword12"
transaction_users['E_USER']['passphrase'] = "sea ill guard bounce gesture tomato walnut fitness plastic affair oven transfer"
transaction_users['E_USER']['username'] = "Chartreuse Comfortable Spadefoot"
transaction_users['E_USER']['address'] = "f7cb60839c0de25e37be0391c33bb34a8f0f8414"
transaction_users['E_USER']['public_key'] = "0x04d27bda317be9b3d943db5e098b3ab1c19ac8156d706f1d237c000e2ea0f553eec" \
                                            "09499522623dc686f5d0a3233c7f1f9724849127cedb5511945e12ca3b4d297"

transaction_users['F_USER'] = dict()
transaction_users['F_USER']['password'] = "newuniquepassword12"
transaction_users['F_USER']['passphrase'] = "style case lazy pole general section dawn royal slice evoke crowd boat"
transaction_users['F_USER']['username'] = "Happygolucky Zigzag Stoat"
transaction_users['F_USER']['address'] = "a94a2a7584620677d290549a96046e59a18f09ef"
transaction_users['F_USER']['public_key'] = "0x042edc34542022b2017a8e9bbdbd7535fb4bdf98a5474edbe7179c4782dbe902704" \
                                            "380f4d7146a2f8e432dfb6fead596057782ae96fba82c3541cc67884e3a9224"

transaction_users['G_USER'] = dict()
transaction_users['G_USER']['password'] = "qwerty"
transaction_users['G_USER']['passphrase'] = "load other time gadget fury sudden gossip hurry entry curtain jaguar inflict"
transaction_users['G_USER']['username'] = "Unsung Yearly Amethystgemclam"
transaction_users['G_USER']['address'] = "876c331d60aedd175ca78cb6a50eaeb9681a1868"
transaction_users['G_USER']['public_key'] = "0x04e70078c562b4a65f08e57cc336e8cbd66d27f411324158807654ae82ef16d8f0f" \
                                            "583454ba2c98a2451a9a990ec3efc874dd845d8ba72a49fb9fb0454d0bd6bab"

transaction_users['H_USER'] = dict()
transaction_users['H_USER']['password'] = 'qwerty'
transaction_users['H_USER']['passphrase'] = "age three camp tip jump radio copper merry mention top panther motor"
transaction_users['H_USER']['username'] = "Lawngreen Rich Silverfox"
transaction_users['H_USER']['address'] = "762d87f77c88048281ddad78501fcfe0700f08ce"
transaction_users['H_USER']['public_key'] = "0x040ff9ff7c1fd9d325a0762bf36fdd6efce0190a02bba4367afc99f0313404a5a3ed" \
                                            "f74aa54ebf7221561e0d83728b4f7934b28809d69ba9e7434fc2fe9d4b9bd4"

transaction_users_wallet = dict()
transaction_users_wallet['A_USER'] = dict()
transaction_users_wallet['A_USER']['password'] = "new_unique_password"
transaction_users_wallet['A_USER']['passphrase'] = "six runway asthma blur secret rebuild parent logic horror decline rib buyer"
transaction_users_wallet['A_USER']['username'] = "Leading Practical Paradiseflycatcher"
transaction_users_wallet['A_USER']['address'] = "0887afe0ee3e1b195f596350bb04bba034514af9"
transaction_users_wallet['A_USER']['public_key'] = "0x04026c389530328076016ebcdc3f8e558e2e9e6e6c015adb1a668783ca1b11" \
                                                   "34260afe392967434cdbdf616138cbadcd009642edd62a5275ad0bc2ff460849fb74"

transaction_users_wallet['B_USER'] = dict()
transaction_users_wallet['B_USER']['password'] = "new_unique_password"
transaction_users_wallet['B_USER']['passphrase'] = "shrug dring breeze marcy install net reopen uniform atom guilt sadness elite"
transaction_users_wallet['B_USER']['username'] = "Blank Bowed Chrysomelid"
transaction_users_wallet['B_USER']['address'] = "13b36abe0be8fa607ab5ec755f2719e545705490"
transaction_users_wallet['B_USER']['public_key'] = "0x04e9299758f894d45d2515b254799cdfbe823d387f46896a52ec12e8f37f771" \
                                                   "fba9fb49a44f93198e581651be71132f68d6d7cca6d1d9c801edbbaab58641b5dc4"

transaction_users_wallet['C_USER'] = dict()
transaction_users_wallet['C_USER']['password'] = "new_unique_password"
transaction_users_wallet['C_USER']['passphrase'] = "purchase ensure mistake crystal person similar shaft family shield clog risk market"
transaction_users_wallet['C_USER']['username'] = "Official Scratchy Bernesemountaindog"
transaction_users_wallet['C_USER']['address'] = "8dce052ccda2f6f6b555759cee6957e04a6ddf5b"
transaction_users_wallet['C_USER']['public_key'] = "0x04fd6db8ae245ca351cb6e24bea29df6d81cb35b7f7a91c68a8b5a0c49da444" \
                                                   "8cdb356cc9c24b9d813094a86e1fb2dc2bf5252a3f117194bbb3bb29f4befbe898c"

transaction_users_wallet['D_USER'] = dict()
transaction_users_wallet['D_USER']['password'] = "new_unique_password"
transaction_users_wallet['D_USER']['passphrase'] = "night grit town donate length zoo meat collect vapor brush topic check"
transaction_users_wallet['D_USER']['username'] = "Joyful Complete Crow"
transaction_users_wallet['D_USER']['address'] = "1fc705a336a0a2e48eb3c953ce577e591f2767ad"
transaction_users_wallet['D_USER']['public_key'] = "0x04b785a16763a8f9a139ea53c2da4dc976a40e3ca288aaff52e3e9527d19150" \
                                                   "21ea6dda3083f37bc5c6a160028faf16dc9d7f684ed59473830fe422700abf826ea"


group_chat_users = dict()
group_chat_users['A_USER'] = dict()
group_chat_users['A_USER']['password'] = "qwerty"
group_chat_users['A_USER']['passphrase'] = "thank fruit brisk pond opera census grid husband claw sight chunk arena"
group_chat_users['A_USER']['username'] = "Astonishing Low Johndory"
group_chat_users['A_USER']['public_key'] = "0x047c67e698c27527e4cac58a3e67103f4fe59849287171464088b321f1c323ac4bfbb54" \
                                           "c87a3f27179be7404cb840f90f92edddb36bcf1bc3d462c79a2d73566f7"

group_chat_users['B_USER'] = dict()
group_chat_users['B_USER']['password'] = "qwerty"
group_chat_users['B_USER']['passphrase'] = "label ill slender audit atom love vote snap edit program climb beyond"
group_chat_users['B_USER']['username'] = "Milky Major Albatross"
group_chat_users['B_USER']['public_key'] = "0x04b5457d527c337083a2d0c0295492239ff98f577875049d4d09cefbe5af3b76c18c48d" \
                                           "312aafb788b2b9922249819932bbc570a738e5c3b49d5e84ebcdec247cb"

group_chat_users['C_USER'] = dict()
group_chat_users['C_USER']['password'] = "qwerty"
group_chat_users['C_USER']['passphrase'] = "you salmon ticket antique spray panther flee neck scale mad trial exile"
group_chat_users['C_USER']['username'] = "Optimistic Gigantic Bagworm"
group_chat_users['C_USER']['public_key'] = "0x04dcdb5cac266328c41bdb0e33a266544a4ac1f2655a68170e5fe4452baff1a413a1d40" \
                                           "3dba7e295445505ee55ea03ee99cb7d26bee05e6b486a9bdaaf6be73a0b"

common_password = 'qwerty'
unique_password = 'unique' + get_current_time()

bootnode_address = "enode://a8a97f126f5e3a340cb4db28a1187c325290ec08b2c9a6b1f19845ac86c46f9fac2ba13328822590" \
                   "fd3de3acb09cc38b5a05272e583a2365ad1fa67f66c55b34@167.99.210.203:30404"
mailserver_address = "enode://531e252ec966b7e83f5538c19bf1cde7381cc7949026a6e499b6e998e695751aadf26d4c98d5a4eab" \
                     "fb7cefd31c3c88d600a775f14ed5781520a88ecd25da3c6:status-offline-inbox@35.225.227.79:30504"

camera_access_error_text = "To grant the required camera permission, please go to your system settings " \
                           "and make sure that Status > Camera is selected."

photos_access_error_text = "To grant the required photos permission, please go to your system settings " \
                           "and make sure that Status > Photos is selected."

connection_not_secure_text = "Connection is not secure! " \
                             "Do not sign transactions or send personal data on this site."
connection_is_secure_text = "Connection is secure. Make sure you really trust this site " \
                            "before signing transactions or entering personal data."
test_fairy_warning_text = "You are using an app installed from a nightly build. If you're connected to WiFi, " \
                          "your interactions with the app will be saved as video and logs. " \
                          "These recordings do not save your passwords. They are used by our development team " \
                          "to investigate possible issues and only occur if the app is install from a nightly build. " \
                          "Nothing is recorded if the app is installed from PlayStore or TestFlight."
