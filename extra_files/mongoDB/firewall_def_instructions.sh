#!/bin/bash

echo "-- Resetting and Initializing firewall --"
sudo ufw reset <<< y
sudo ufw enable

echo "-- Setting rules --"
echo "Allow all TCP connections from VM2 (IPV4)"
sudo ufw allow in on enp0s3 from 192.168.0.10 to any port 27017 proto tcp

echo "Allow all TCP connections from VM2 (IPV6)"
sudo ufw allow in on enp0s3 from fe80::a00:27ff:fe1d:2892 to any port 27017 proto tcp

echo "Deny all other connections"
sudo sed -i -e '/RELATED,ESTABLISHED -j ACCEPT/!s/ACCEPT/DROP/g' /etc/ufw/before.rules

echo "-- Activating changes --"
sudo ufw reload

echo "-- Firewall status --"
sudo ufw status verbose