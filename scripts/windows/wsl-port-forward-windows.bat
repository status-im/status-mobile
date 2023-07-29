@echo off

REM Execute the netsh command to delete the existing port proxy.
netsh interface portproxy delete v4tov4 listenport=8081 listenaddress=127.0.0.1

REM Use bash to retrieve the IP address from WSL's eth0 interface using the ip command.
FOR /F "tokens=*" %%i IN ('bash -c "ip -4 addr show eth0 | grep -oP '(?<=inet\s)\d+\.\d+\.\d+\.\d+'"') DO SET WSL_CLIENT=%%i

REM Use the retrieved IP address to set up the new port proxy.
netsh interface portproxy add v4tov4 listenport=8081 listenaddress=127.0.0.1 connectport=8081 connectaddress=%WSL_CLIENT%

echo.
echo Operations executed:
echo 1. Deleted any existing port proxy on port 8081 and address 127.0.0.1.
echo 2. Retrieved WSL eth0 IP address: %WSL_CLIENT%.
echo 3. Created a new port proxy on port 8081, local address 127.0.0.1, forwarding to WSL IP address on the same port.

pause
