import datetime
import logging
import re
import subprocess
import time

import docker
from docker.errors import NotFound


DOCKER_SHARED_VOLUME_PATH = '/root/shared_volume'


class DeviceStats(object):

    def __init__(self):
        self.total_battery_capacity_mah = None
        # Est power used (mAh) by the app
        self.estimated_power_usage_mah = None


class AppiumContainer:

    def start_appium_container(self, shared_volume):
        docker_client = docker.from_env()
        try:
            self.container = docker_client.containers.get("appium")
            self.container.restart()
        except NotFound:
            self.container = docker_client.containers.run("appium/appium:local", detach=True, name="appium",
                                                          ports={'4723/tcp': 4723},
                                                          volumes={shared_volume: {
                                                              'bind': DOCKER_SHARED_VOLUME_PATH, 'mode': 'rw'}})
        logging.info("Running Appium container. ID: %s" % self.container.short_id)

    def exec_run(self, cmd):
        return self.container.exec_run(cmd, stdout=True, stderr=True, stdin=True)

    def connect_device(self, device_ip):
        device_address = device_ip + ':5555'
        connected_state = "connected to %s" % device_address

        cmd = self.exec_run(['adb', 'connect', device_address])
        if connected_state in cmd.output.decode("utf-8"):
            logging.info("adb is already connected with the device")
        else:
            logging.info("Connecting the device with adb..")
            # Restart adb on host machine
            subprocess.call(['adb', 'kill-server'])
            input("Connect USB cable to the device and press ENTER..")
            # Reset USB on host machine
            subprocess.call(['adb', 'usb'])
            time.sleep(5)  # wait until adb usb is restarted
            # Set the target device to listen for a TCP/IP connection on port 5555 on host machine
            subprocess.call(['adb', 'tcpip', '5555'])
            # restarting in TCP mode port: 5555
            input("Now, disconnect the USB cable and press ENTER..")
            # Connect to the device in docker container
            self.exec_run(['adb', 'connect', device_address])
            input("Please check your device and allow for USB debugging if necessary. Then press ENTER to continue "
                  "the test..\n")

    def reset_battery_stats(self):
        logging.info("Resetting device stats..")
        self.exec_run(['adb', 'shell', 'dumpsys', 'batterystats', '--reset'])

    def generate_bugreport(self, report_name):
        now = datetime.datetime.now()
        bugreport_name = "bugreport_%s_%s" % (report_name, now.strftime("%Y-%m-%d_%H-%M-%S"))

        print("\nGenerating device report from the test..")
        self.exec_run(['adb', 'bugreport', "/%s/%s" % (DOCKER_SHARED_VOLUME_PATH, bugreport_name)])
        print("Device report saved in the shared volume as %s.zip" % bugreport_name)

    def get_device_stats(self):
        stats = DeviceStats()

        # Find process uid
        uid_line = self.exec_run(['adb', 'shell', 'ps', '|', 'grep', 'im.status.ethereum']).output \
            .decode('utf-8')
        # Match first word which is the uid
        match = re.match(r'(?:^|(?:[.!?]\s))(\w+)', uid_line, re.M | re.I)
        uid = match.group(1).replace('_', '')

        # Battery stats
        batterystats = self.exec_run(['adb', 'shell', 'dumpsys', 'batterystats', 'im.status.ethereum']).output \
            .decode('utf-8').splitlines()
        battery_usage_line = [s for s in batterystats if "Uid %s" % uid in s][0]
        match = re.match(r'.* Uid %s: ([^\s]+)' % uid, battery_usage_line, re.M | re.I)
        stats.estimated_power_usage_mah = float(match.group(1))
        capacity_line = [s for s in batterystats if "Capacity" in s][0]
        match = re.match(r'.* Capacity: (.*), Computed drain: (.*), .*', capacity_line, re.M | re.I)
        stats.total_battery_capacity_mah = float(match.group(1))
        stats.total_computed_drain_mah = float(match.group(2))

        # Wi-Fi stats
        wifi_stats = self.exec_run(['adb', 'shell', 'dumpsys', 'batterystats', 'im.status.ethereum', '|', 'grep',
                                'Wi-Fi total']).output.decode('utf-8')
        stats.wifi_received = wifi_stats.split()[3].replace(',', '')
        stats.wifi_sent = wifi_stats.split()[5]

        # OS stats
        stats.os_version = self.exec_run(['adb', 'shell', 'getprop', 'ro.build.version.release'])\
            .output.decode('utf-8').rstrip("\n")
        stats.device_model = self.exec_run(['adb', 'shell', 'getprop', 'ro.product.model'])\
            .output.decode('utf-8').rstrip("\n")

        return stats

    def stop_container(self):
        self.container.stop()
