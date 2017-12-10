#!/bin/bash

# Delete current rules
iptables -F

# default outgoing policies
iptables -P OUTPUT DROP

# Accept outgoing packets that are going to port 37 and works on TCP
iptables -A OUTPUT -j ACCEPT -p tcp --dport 37 -i eth0

# List iptables chains with 'iptables -L -v'
iptables -L -v