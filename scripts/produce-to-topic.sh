docker exec -it $(docker ps -aqf name=kafka1) usr/bin/kafka-console-producer --broker-list localhost:9092 --topic $1
