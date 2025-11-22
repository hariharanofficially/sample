#!/bin/sh

VERSION=$1
AWS_ACCOUNT_ID=533267169706
REGION=ap-southeast-2

aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com

docker build --platform linux/amd64 -t ${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/entitleguard/backend:$VERSION .

docker tag ${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/entitleguard/backend:$VERSION ${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/entitleguard/backend:latest

docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/entitleguard/backend:latest
