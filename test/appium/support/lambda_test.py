import requests

from conftest import lambda_test_username, lambda_test_access_key
from tests import test_suite_data

session = requests.Session()
session.auth = (lambda_test_username, lambda_test_access_key)


def upload_apk(apk_file_path):
    resp = session.post(
        url="https://manual-api.lambdatest.com/app/upload/virtualDevice",
        files={'appFile': open(apk_file_path, 'rb')},
        data={'name': test_suite_data.apk_name}
    )
    assert resp.status_code == 200
    return resp.json()['app_url']


def update_session(session_id, session_name, status):
    resp = session.get(
        url="https://mobile-api.lambdatest.com/mobile-automation/api/v1/sessions/%s" % session_id,
        data={
            "name": session_name  # , "status_ind": status
        }
    )
    assert resp.status_code == 200


def get_session_info(session_id):
    resp = session.get(url="https://mobile-api.lambdatest.com/mobile-automation/api/v1/sessions/%s" % session_id)
    assert resp.status_code == 200
    return resp.json()['data']


def upload_image():
    'curl -u "yevheniia:fZaXHEAFEWOVCZLnSVrwHY11eJGsWAknibtG572PiZsvT1h57V" -X POST https://mobile-mgm.lambdatest.com/mfs/v1.0/media/upload -F media_file=@/Users/yberdnyk/Downloads/aaa.png -F type=image -F custom_id=SampleImage'
    pass
