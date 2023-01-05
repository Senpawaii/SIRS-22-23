# Initialize firewall
sudo ufw enable

# Check firewall status
sudo ufw status verbose

# Allow all TCP connections from VM1 (IPV4)
sudo ufw allow in on enp0s9 from 192.168.1.50 to any port 10000:10001 proto tcp

# Allow all TCP connections from VM1 (IPV6)
sudo ufw allow in on enp0s9 from fe80::a00:27ff:fe58:bb76 to any port 10000:10001 proto tcp

# Allow all connections from VM3 (IPV4)
sudo ufw allow in on enp0s3 from 192.168.0.100

# Allow all connections from VM3 (IPV6)
sudo ufw allow in on enp0s3 from fe80::a00:27ff:feb5:55cd

# Allow all TCP connections from NAT (IPV4 and IPV6)
sudo ufw allow in on enp0s8 to any port 10000:10001 proto tcp

# Deny all other connections
sudo gedit /etc/ufw/before.rules
Change all ACCEPT to DROP except these lines:
-A ufw-before-input -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A ufw-before-output -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A ufw-before-forward -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT

# Reload firewall
sudo ufw reload
