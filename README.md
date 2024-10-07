Spring Batch with Mysql 

To run mysql as docker container 

docker pull mysql:8.0.23 

docker run --name mysql8-container \
    -e MYSQL_ROOT_PASSWORD=root \
    -e MYSQL_DATABASE=mydb \
    -e MYSQL_USER=user \
    -e MYSQL_PASSWORD=password \
    -v /home/devuser/local/data:/var/lib/mysql \
    -p 3306:3306 \
    -d mysql:8.0.23
