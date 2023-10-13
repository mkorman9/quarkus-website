# Manual deployment on DigitalOcean

NOTE: This is a simplistic, non-HA setup but can be easily scaled up and adjusted for production use

- Generate API token

- Create Container Registry, log in and push image
```
Name: quarkus-website

docker login registry.digitalocean.com
Username: <TOKEN>
Password: <TOKEN>

docker build -t registry.digitalocean.com/quarkus-website/backend:1.0.0 .
docker push registry.digitalocean.com/quarkus-website/backend:1.0.0
```

- Register domain and create DNS zone on DigitalOcean
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
User Data: <as listed below>
```

```sh
#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive && \
	apt update && apt upgrade -y && \
	apt install -y ca-certificates curl gnupg && \
	curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker.gpg && \
	echo \
	  "deb [arch="$(dpkg --print-architecture)" signed-by=/usr/share/keyrings/docker.gpg] https://download.docker.com/linux/debian \
	  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
	  tee /etc/apt/sources.list.d/docker.list > /dev/null && \
	apt update && \
	apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
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
```sh
# ssh root@<PUBLIC_DROPLET_IP>

# create application.properties and compose.yml files as listed below

docker login registry.digitalocean.com
# Username: <TOKEN>
# Password: <TOKEN>

export APP_VERSION="1.0.0"
docker compose pull
docker compose up --force-recreate --detach
```

```properties
# application.properties

# all the application secrets belong here
# quarkus.datasource.password=...
```

```yaml
# compose.yml

services:
  quarkus-website:
    image: registry.digitalocean.com/quarkus-website/backend:${APP_VERSION}
    restart: always
    ports:
      - "8080:8080"
    volumes:
      - "${PWD}/application.properties:/config/application.properties:ro"
```
