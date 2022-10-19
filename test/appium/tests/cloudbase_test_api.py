from os import environ
import json
import requests
from io import BytesIO
from time import sleep

from sauceclient import SauceClient, SauceException

try:
    import http.client as http_client
    from urllib.parse import urlencode
except ImportError:
    import httplib as http_client
    from urllib import urlencode


sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')

sauce = SauceClient(sauce_username, sauce_access_key)
apibase = 'eu-central-1.saucelabs.com'


def request(method, url, body=None, content_type='application/json'):
    """This is to monkey patch further this method in order to use apibase"""
    headers = sauce.make_auth_headers(content_type)
    connection = http_client.HTTPSConnection(apibase)
    connection.request(method, url, body, headers=headers)
    response = connection.getresponse()
    data = response.read()
    connection.close()
    if response.status not in [200, 201]:
        raise SauceException('{}: {}.\nSauce Status NOT OK'.format(
            response.status, response.reason), response=response)
    return json.loads(data.decode('utf-8'))

sauce.request = request

def upload_from_url(apk_path = str()):
    response = requests.get(apk_path, stream=True)
    response.raise_for_status()
    apk_name = apk_path.split("/")[-1]
    file = BytesIO(response.content)
    del response
    for _ in range(3):
        try:
            requests.post('https://eu-central-1.saucelabs.com/rest/v1/storage/'
                          + sauce_username + '/' + apk_name + '?overwrite=true',
                          auth=(sauce_username, sauce_access_key),
                          data=file,
                          headers={'Content-Type': 'application/octet-stream'})
            break
        except ConnectionError:
            sleep(10)
