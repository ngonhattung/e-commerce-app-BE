FROM maven:3.9.8-amazoncorretto-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Cài đặt vào Maven local repo của container
RUN mvn clean install -DskipTests

# Tùy chọn: xuất jar nếu muốn đẩy đi đâu đó
# RUN cp target/*.jar /output/

# Nếu bạn dùng Nexus thì có thể RUN mvn deploy (yêu cầu config .m2/settings.xml)
