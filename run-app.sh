# Запуск PostgreSQL в Docker
echo "Starting PostgreSQL..."
docker-compose up -d

# Ждем пока PostgreSQL запустится
echo "Waiting for PostgreSQL to be ready..."
sleep 3

# Запускаем приложение
echo "Starting User Service application..."
mvn clean compile exec:java -Dexec.mainClass=ru.astondevs.UserServiceApplication

# Останавливаем контейнер при выходе из приложения
echo "Stopping PostgreSQL..."
docker-compose down