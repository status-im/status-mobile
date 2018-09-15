import requests
import re
import docker
from urllib.request import urlretrieve

raw_data = requests.request('GET', 'https://status-im.github.io/nightly/').text
app_url = re.findall('href="(.*AppImage)', raw_data)[0]
urlretrieve(app_url, 'nightly.AppImage')


ps = None


client = docker.from_env()
client.images.build(tag='status_desktop', path='.')

try:
    a = client.containers.run("status_desktop",
                              detach=True, tty=True, ports={'5900/tcp': 5900})

    it = a.attach(stdout=True, stderr=True)

    print(it)

    ps = a.exec_run(['/jython-2.7.1/bin/jython', '-m', 'pytest', '/home/tests/test_create_account.py'],
                    stdout=True, stderr=False)
except Exception:
    pass
finally:
    a.stop()

for line in ps.output.decode("utf-8").split("\n"):
    print(line)
