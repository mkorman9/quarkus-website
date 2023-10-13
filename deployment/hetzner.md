# Manual deployment on Hetzner

NOTE: This is a simplistic, non-HA setup but can be easily scaled up and adjusted for production use

- Create project and upload SSH certificate

- Register domain and create DNS zone on Hetzner
```
example.com

NS hydrogen.ns.hetzner.com 1800
NS oxygen.ns.hetzner.com   1800
NS helium.ns.hetzner.de    1800
```

- Create VPC
```
Name: eu-quarkus-website-vpc
IP Range: 10.1.0.0/16
```

- Create firewall
```
Rules:
    Inbound:
    - All IPv4, All IPv6 -> TCP 22
    - 10.1.0.0/16        -> TCP 8080
    Outbound:
        <empty>

Apply to Label: quarkus-website
Name: eu-quarkus-website-fw
```

- Create server
```
Location: Falkenstein (eu-central)
Image: Debian 12
Type: Shared vCPU, x86, CPX11, 2 GB RAM / 2 vCPU
Networking: Public IPv4 (billed extra) + Private Network (eu-quarkus-website-vpc)
SSH keys: <previously uploaded key>
Backups: <optionally when running production>
Labels: quarkus-website
Name: eu-quarkus-website-1
Cloud config: <as listed below>
```

```sh
#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive && \
	apt update && apt upgrade -y && \
	apt install -y ca-certificates curl gnupg && \
	install -m 0755 -d /etc/apt/keyrings && \
	curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
	chmod a+r /etc/apt/keyrings/docker.gpg && \
	echo \
	  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
	  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
	  tee /etc/apt/sources.list.d/docker.list > /dev/null && \
	apt update && \
	apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

- Create Load Balancer
```
Location: Falkenstein (eu-central)
Type: LB11 (5 services / 25 targets)
Network: eu-quarkus-website-vpc
Targets by Label: quarkus-website (Private)
Services:
    - HTTPS 443 -> HTTP 8080
    - HTTP-Redirect: On
    - Create certificate: example-com-cert (*.example.com)
    - Healthcheck: :8080 /
Algorithm: Round Robin
Name: eu-quarkus-website-lb
```

- Add DNS records for Load Balancer
```
A    @ <Load Balancer IPv4> 1800
AAAA @ <Load Balancer IPv6> 1800
```

- Hetzner doesn't offer private Docker registries yet, so either docker.com paid plan or another cloud provider (DigitalOcean, GCP, AWS...) needs to be used to store images
```sh
# Push image on local machine
docker login <PRIVATE_REGISTRY>

docker build -t <PRIVATE_REGISTRY>/quarkus-website:1.0.0 .
docker push <PRIVATE_REGISTRY>/quarkus-website:1.0.0

# And deploy later on the server using application.properties and compose.yml files listed below
docker login <PRIVATE_REGISTRY>

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
    image: <PRIVATE_REGISTRY>/quarkus-website:${APP_VERSION}
    restart: always
    ports:
      - "8080:8080"
    volumes:
      - "${PWD}/application.properties:/config/application.properties:ro"
```
