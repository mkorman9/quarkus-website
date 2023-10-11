# Manual deployment on DigitalOcean

NOTE: This is a simplistic, non-HA setup but can be easily scaled up and adjusted for production use

- Register domain and create DNS zone
```
example.com

NS ns1.digitalocean.com 1800
NS ns2.digitalocean.com 1800
NS ns3.digitalocean.com 1800
```

- Create VPC
```
Name: fra1-backend
IP Range: 10.1.0.0/16
```

- Create Droplet
```
Datacenter: Frankfurt FRA1
VPC: fra1-backend
Image: Debian 12
Size: Basic, Regular, 2 GB RAM / 2 vCPU
Name: fra1-backend-1
Tags: backend
```

- Create firewall
```
Name: fw-backend
Rules:
    Inbound:
    - TCP 22 -> All IPv4, All IPv6
    - TCP 8080 -> 10.1.0.0/16
    Outbound:
        <default>

Apply to: backend
```

- Connect to Droplet and configure it
```sh
# ssh root@<PUBLIC_DROPLET_IP>

export DEBIAN_FRONTEND=noninteractive
apt update && apt upgrade -y

# Docker
apt install -y ca-certificates curl gnupg

install -m 0755 -d /etc/apt/keyrings

curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

chmod a+r /etc/apt/keyrings/docker.

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt update

apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

- Create Load Balancer
```
VPC: fra1-backend
Number of nodes: 1
Apply to: backend
Rules:
    - HTTP 80 -> HTTP 8080
    - HTTPS 443 -> HTTP 8080 (Select "example.com" domain and Let's Encrypt certificate) 
Health checks: https://0.0.0.0:8080/
Redirect HTTP to HTTPS: Yes
Name: fra1-backend-lb
```

- Deploy app on the Droplet
```
# compose.yml

services:
  quarkus-website:
    image: "quarkus-website:${APP_VERSION}"
    restart: always
    ports:
      - "8080:8080"
```

```
APP_VERSION="latest" docker compose up --force-recreate --detach
```
