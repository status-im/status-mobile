import datetime

from influxdb import InfluxDBClient


def convert_to_mb(value):
    number = float(value[:-2])
    unit = value[-2:]
    if unit == 'KB':
        number = number / 1000
    return number


class DeviceStatsDB:

    def __init__(self, host, port, username, password, database):
        self.client = InfluxDBClient(host, port, username, password, database)

    def save_stats(self, build_name, test_name, test_group, test_passed, device_stats):
        json_body = [
            {
                "measurement": "device",
                "time": datetime.datetime.utcnow().isoformat(),
                "fields": {
                    "battery_used_mah": device_stats.estimated_power_usage_mah,
                    "wifi_sent": convert_to_mb(device_stats.wifi_sent),
                    "wifi_received": convert_to_mb(device_stats.wifi_received),
                },
                "tags": {
                    "test_name": test_name,
                    "test_group": test_group,
                    "build_name": build_name,
                    "device_model": device_stats.device_model,
                    "device_os_version": device_stats.os_version,
                    "test_passed": test_passed
                }
            }
        ]
        self.client.write_points(json_body)
