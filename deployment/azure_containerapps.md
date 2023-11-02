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

NOTE: This value should be tuned after observing metrics under `Monitoring -> Metrics`

- Set up custom domain

```
Custom Domains -> Add custom domain

TLS/SSL certificate: Managed certificate
Domain: example.com

Add given A/TXT records to the DNS Zone and wait for validation
```

NOTE: Azure provides client's source address in `X-Forwarded-For` header.
