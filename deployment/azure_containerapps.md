# Manual deployment on Azure Container Apps

Azure Container Apps provide cheap and easy way of deploying the app. It fits production needs such as autoscaling.

- Create Resource Group `eu-quarkus-website` in `West Europe`

- Create Container Registry and push app image

```
Name: euquarkuswebsite
Resource Group: eu-quarkus-website
Region: West Europe
Pricing Plan: Basic
```

```sh
az acr login --name euquarkuswebsite
docker tag quarkus-website:latest euquarkuswebsite.azurecr.io/quarkus-website:latest
docker push euquarkuswebsite.azurecr.io/quarkus-website:latest
```

After successful deployment go to `Access Keys` and enable `Admin user`

- Create Container App

```
Name: eu-quarkus-website
Region: West Europe
Container Apps Environment: Create New
    Name: eu-quarkus-website-env
    Environment type: Workload Profiles
    Log Analytics Workspace: Create New
        Name: eu-quarkus-website-env-logs
Container:
    Name: eu-quarkus-website
    Registry/Image/Tag: <select pushed app image>
    Workload Profile: Consumption
    CPU and Memory: 0.75 CPU, 1.5 GiB
    Environment Variables: <specify all needed>
Ingress:
    Enabled
    Ingress traffic: Accept traffic from anywhere
    Client certificate mode: Ignore
    Transport: Auto
    Insecure connections: NOT allowed
    Target port: 8080
```

- Wait for the deployment and set up autoscaling `Revisions -> Create new revision -> Scale`

```
Min/max replicas: 1-3
Scale Rule: Remove existing rule and add new one
    Name: http-rule-1000
    Type: HTTP scaling
    Concurrent requests: 1000

Create
```

NOTE: These values (max replicas and concurrent requests, but also CPU and Memory for containers) 
should be tuned after observing metrics under `Monitoring -> Metrics`

- Set up custom domain

```
Custom Domains -> Add custom domain

TLS/SSL certificate: Managed certificate
Domain: example.com

Add given A/TXT records to the DNS Zone and wait for validation
```

### Notes

- Azure provides client's source address in `X-Forwarded-For` header.

### Bonus: Postgres deployment

Create new `Azure Database for PostgreSQL servers`

```
Choose Flexible server
    Resource Group: eu-quarkus-website
    Server name: eu-quarkus-website-db
    Region: West Europe
    PostgreSQL version: 15
    Authentication method: PostgreSQL authentication only
    Admin username: pgadmin
    Password: <openssl rand 32 | base64>
    
    Connectivity method: Public access (allowed IP addresses)
    Allow public access from any Azure service within Azure to this server: ON
```

In Secrets of the app add

```
Key: db-password
Type: Container Apps Secret
Value: <password of pgadmin user>
```

In Environment variables of the app container add

```
QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://eu-quarkus-website-db.postgres.database.azure.com:5432/postgres?sslmode=require
QUARKUS_DATASOURCE_USERNAME: pgadmin@eu-quarkus-website-db
QUARKUS_DATASOURCE_PASSWORD: <reference to the db-password secret>
```
