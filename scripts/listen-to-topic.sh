docker exec -it $(docker ps -aqf name=kafka1) usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic $1
