#!/usr/bin/env bash

set -e

SERVICES=(
  "order-service"
  "orchestrator-service"
  "product-validation-service"
  "payment-service"
  "inventory-service"
)

build_application() {
    local app="$1"

    echo "Building application ${app}..."

    (
        cd "$app"
        gradle build -x test
    )

    echo "Application ${app} finished building!"
}

remove_remaining_containers() {
    echo "Removing all containers..."

    docker-compose down || true

    containers=$(docker ps -aq)

    if [[ -n "$containers" ]]; then
        echo "Stopping remaining containers..."

        for container in $containers; do
            echo "Stopping container $container"
            docker container stop "$container" >/dev/null 2>&1 || true
        done

        docker container prune -f
    fi
}

docker_compose_up() {
    echo "Running containers..."

    docker-compose up --build -d

    echo "Pipeline finished!"
}

echo "Pipeline started!"
echo "Starting to build applications!"

# Build everything in parallel
for service in "${SERVICES[@]}"; do
    build_application "$service" &
done

# Wait for all background jobs
wait

remove_remaining_containers

docker_compose_up