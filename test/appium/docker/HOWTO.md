## Instructions

Use device with Android 7.1 or newer.

1. Install needed tools

    - Install Docker https://docs.docker.com/install/
    - Install Python 3

    Go to https://realpython.com/installing-python and follow the instructions. For macOS with Homebrew, you can run `brew install python3` to install it. 

2. Build Appium docker image

    - Make sure Docker app is running
    - Build Appium image to have environment in which tests will be executed

    ```
    $ cd appium/docker
    $ docker build -t "appium/appium:local" .
    ```

    Make sure all build steps were successful. You should see:
    ```
    Successfully tagged appium/appium:local
    ```

3. Find out IP address of the device

    On device, open wifi settings and click on the current network. Write down the IP address as it will be needed for running the battery test wirelessly.

4. Run the battery test

    - Make sure the device is sufficiently charged
    - Disconnect the device USB cable
    - Create a directory that will be shared with the docker container. E.g. `mkdir /Users/lukas/Desktop/shared`
    - Put .apk to test into the shared directory. E.g. `mv StatusIm-181109-092655-009d97-e2e.apk /Users/lukas/Desktop/shared`  
    - Run a test with using appium container
        - `--device_ip` - IP address of the device (see 3.)
        - `--apk=StatusIm-181109-092655-009d97-e2e.apk` - name of the apk file
        - `--docker=True` - run the tests using appium docker container
        - `--docker_shared_volume=/Users/lukas/Desktop/shared` - path to the shared directory
        - `--bugreport=True` - generate bugreport file
        
        ```
        $ cd status-react/test/appium
        $ python3 -m pytest --apk=StatusIm-181109-092655-009d97-e2e.apk --device_ip=192.168.0.104 --bugreport=True --docker=true --docker_shared_volume=/Users/lukas/Desktop/shared tests/atomic/transactions/test_wallet.py::TestTransactionWalletSingleDevice::test_send_eth_from_wallet_to_contact
        ```
    - Follow the instructions shown in the command line

    **In case of issues:**
    
    Find out the Appium container id: `docker ps` and get the latest logs: `docker logs ebdf1761f51f` where `ebdf1761f51f` is the container id.
    
5. Analyse test results

    The test generates Android bugreport that is saved in shared directory. It contains data about battery, cpu, memory, network usage, and more. See [energy-efficient-bok](https://github.com/status-im/energy-efficient-bok/blob/master/QA_Android.md) or [Battery Historian](https://developer.android.com/studio/profile/battery-historian) to learn how to analyse it.


## Issues with adb connect

To run the battery test without charging the device, adb needs to connect to it wirelessly. To avoid wifi connection issues, you can connect to the device via bluetooth.

macOS instructions:
- On device, enable bluetooth on your device in Android settings -> connections -> bluetooth
- On device, go to mobile hotspots and tethering and enable bluetooth tethering
- On macOS, go to the bluetooth settings and connect with the device (click "Connect to Network")
