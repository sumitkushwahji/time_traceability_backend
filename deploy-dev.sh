#!/bin/bash

# Set variables
IMAGE_NAME="time-traceability-backend-dev"
CONTAINER_NAME="time-traceability-backend-dev"
DOCKER_IMAGE="maven:3.9.9-amazoncorretto-17"

# Step 3: Pull the required Maven Docker image
echo "🚀 Pulling Docker image with Java 17 and Maven..."
docker pull $DOCKER_IMAGE

# Step 4: Build the Maven project inside Docker
echo "🔧 Building the project using Maven inside Docker..."
docker run --rm -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven $DOCKER_IMAGE mvn clean install

#GET Current Image ID
IMAGE_ID=$(docker inspect --format '{{.Image}}' $CONTAINER_NAME)
echo "🐳 IMAGE ID: $IMAGE_ID"

# Step 5: Build the Docker image with unique and latest tags
IMAGE_TAG=$(git rev-parse --short HEAD)  # Get the latest commit hash
echo "🐳 Building Docker image with tags: $IMAGE_NAME:$IMAGE_TAG and $IMAGE_NAME:latest..."
docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .

# Step 6: Stop and remove the existing container (if running)
echo "🛑 Stopping and removing the existing container (if any)..."
docker rm -f $CONTAINER_NAME || true

# Step 7: Remove old unused Docker images
echo "🧹 Cleaning up old Docker images..."
docker rmi $IMAGE_ID

# Step 8: Deploy the new container
echo "🚀 Deploying the new Docker container..."
docker run -d \
  --network host \
  -v /home/time-tracebility/time_traceability_rrsl/data:/common-view-data \
  --name $CONTAINER_NAME \
  $IMAGE_NAME:latest

sudo rm -rf target

echo "✅ Deployment completed successfully!"