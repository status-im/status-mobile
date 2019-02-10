import docker
import pytest
import argparse
import os
import re
import requests
from datetime import datetime
from urllib.request import urlretrieve
from tests.report import TEST_REPORT_DIR_CONTAINER
from tests.report import TestrailReportDesktop


def main(**kwargs):

    if not os.path.exists(kwargs['test_results_path']):
        os.makedirs(kwargs['test_results_path'])
    if kwargs['linux_app_url']:
        linux_app_url = kwargs['linux_app_url']
    else:
        linux_app_url = re.findall('https\S*AppImage', requests.get('https://status.im/nightly/').text)[0]
    app_version = ([i for i in [i for i in linux_app_url.split('/') if '.AppImage' in i]])[0]
    urlretrieve(linux_app_url, 'nightly.AppImage')
    client = docker.from_env()
    client.images.build(tag='status_desktop', path='.')
    pytest.main(['--collect-only', '-m %s' % kwargs['mark']])
    from tests import test_data
    if kwargs['testrail_report']:
        testrail_report = TestrailReportDesktop(kwargs['test_results_path'])
        testrail_report.add_run(app_version if app_version else '%s' % datetime.now().strftime("%Y-%m-%d %H:%M"))
    for test in test_data.tests_to_run:
        print(test)
        try:
            container = client.containers.run("status_desktop",
                                              detach=True, tty=True, ports={'5900/tcp': 5900},
                                              volumes={kwargs['test_results_path']: {'bind': TEST_REPORT_DIR_CONTAINER,
                                                                                     'mode': 'rw'}})
            outcome = container.exec_run(['/jython-2.7.1/bin/jython', '-m', 'pytest', '/home/tests/' + test],
                                         stdout=True, stderr=False, stdin=True)
        except Exception:
            print(outcome)
        finally:
            for line in outcome.output.decode("utf-8").split("\n"):
                print(line)
            if container:
                container.stop()
    if kwargs['testrail_report']:
        testrail_report.add_results(kwargs['jenkins_build_num'])
    for container in client.containers.list(all=True):
        container.remove()
    for image_id in [image.id for image in client.images.list() if not image.attrs["RepoTags"]]:
        try:
            client.images.remove(image_id)
            print("%s is removed" % image_id)
        except client.errors.APIError:
            print("%s is not removed, the image is still in use" % image_id)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--test_results_path',
                        action='store',
                        default=os.path.dirname(os.path.abspath(__file__)))
    parser.add_argument('--linux_app_url',
                        action='store',
                        default=None)
    parser.add_argument('--mark',
                        action='store',
                        default='testrail_id')
    parser.add_argument('--testrail_report',
                        action='store',
                        default=False)
    parser.add_argument('--jenkins_build_num',
                        action='store',
                        default=None)
    args = parser.parse_args()
    main(**vars(args))
