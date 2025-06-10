#!/bin/bash

# Set variables
IMAGE_NAME="tracealibility-prod"
CONTAINER_NAME="tracealibility-prod"
DOCKER_IMAGE="maven:3.9.9-amazoncorretto-17"
VERSION=$(git rev-parse --short HEAD)

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

# Save the Docker image to a tar file
echo "Saving Docker image to $IMAGE_NAME-$VERSION.tar..."
docker save $IMAGE_NAME:$VERSION -o $IMAGE_NAME-$VERSION.tar

# Transfer the Docker image tar file to the prod server
echo "Transferring Docker image to the remote server..."
scp $IMAGE_NAME-$VERSION.tar npl@192.168.251.111:/home/npl/code/docker-images/

# Log SSH and deployment steps
echo "SSHing into the production server to deploy the Docker image..."

# Pass the variables through environment variables and run commands in remote server
ssh npl@192.168.251.111 "export IMAGE_NAME=$IMAGE_NAME VERSION=$VERSION; bash -c '
    echo \"Removing old container if it exists...\"
    docker rm -f \$IMAGE_NAME || true

    echo \"Loading Docker image from tar file...\"
    docker load -i /home/npl/code/docker-images/\$IMAGE_NAME-\$VERSION.tar

    echo \"Deploying Docker container with version \$VERSION...\"
    docker run -d --restart always --network host --name \$IMAGE_NAME \$IMAGE_NAME:\$VERSION

    echo \"Deployment complete.\"
'"

# Log completion of the script
echo "Deployment process completed for Angular app version $VERSION."

# Step 7: Remove old unused Docker images
echo "🧹 Cleaning up old Docker images..."
docker rmi $IMAGE_NAME

echo "✅ Deployment completed successfully!"