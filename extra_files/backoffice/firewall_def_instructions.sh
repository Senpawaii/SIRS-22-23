#!/bin/bash

echo "-- Resetting and Initializing firewall --"
sudo ufw reset <<< y
sudo ufw enable

echo "-- Setting rules --"
echo "Allow all TCP connections from VM1 (IPV4)"
sudo ufw allow in on enp0s9 from 192.168.1.50 to any port 10000:10001 proto tcp

echo "Allow all TCP connections from VM1 (IPV6)"
sudo ufw allow in on enp0s9 from fe80::a00:27ff:fe58:bb76 to any port 10000:10001 proto tcp

echo "Allow all connections from VM3 (IPV4)"
sudo ufw allow in on enp0s3 from 192.168.0.100

echo "Allow all connections from VM3 (IPV6)"
sudo ufw allow in on enp0s3 from fe80::a00:27ff:feb5:55cd

echo "Allow all TCP connections from NAT (IPV4 and IPV6)"
sudo ufw allow in on enp0s8 to any port 10000:10001 proto tcp

echo "Deny all other connections"
sudo sed -i -e '/RELATED,ESTABLISHED -j ACCEPT/!s/ACCEPT/DROP/g' /etc/ufw/before.rules

echo "-- Activating changes --"
sudo ufw reload

echo "-- Firewall status --"
sudo ufw status verbose