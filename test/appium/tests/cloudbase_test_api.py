from sauceclient import SauceClient, SauceException, http_client, json


class NewSauceClient(SauceClient):

    def request(self, method, url, body=None, content_type='application/json'):
        """This is to monkey patch further this method in order to use apibase"""
        headers = self.make_auth_headers(content_type)
        connection = http_client.HTTPSConnection('saucelabs.com')
        connection.request(method, url, body, headers=headers)
        response = connection.getresponse()
        data = response.read()
        connection.close()
        if response.status not in [200, 201]:
            raise SauceException('{}: {}.\nSauce Status NOT OK'.format(
                response.status, response.reason), response=response)
        return json.loads(data.decode('utf-8'))
