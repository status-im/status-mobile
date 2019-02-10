import requests


def get_jenkins_build_url(pr_number: str) -> str:
    jenkins_api_url = 'https://ci.status.im/job/end-to-end-tests/job/status-app-end-to-end-tests/'
    builds_list = requests.get(jenkins_api_url + 'api/json?tree=builds[id,url]').json()[
        'builds']
    for build in builds_list:
        data = requests.get('%s/%s/api/json' % (jenkins_api_url, build['id'])).json()
        if data['displayName'] == 'PR-' + pr_number:
            return build['url'] + '/console'
