# ====== Stage 1: Build the application ======
FROM maven:3.6.3-openjdk-8 AS build

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Move to your main module (adjust if needed)
WORKDIR /app/xamplify-prm

# Build project without running tests
RUN mvn clean install -DskipTests


# ====== Stage 2: Create the runtime image ======
FROM tomcat:8.5-jdk8-openjdk

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your built WAR file into Tomcat webapps directory
COPY --from=build /app/xamplify-prm/xamplify-prm-api/target/*.war /usr/local/tomcat/webapps/xamplify-prm-api.war

# Create logs directory
RUN mkdir -p /usr/local/tomcat/xamplify-logs

# Set environment variables for Java and Spring Boot
ENV JAVA_OPTS="-Dspring.profiles.active=production \
-Dmail.smtp.ssl.protocols=TLSv1.2 \
-Dhttps.protocols=TLSv1.2 \
-Djdk.tls.client.protocols=TLSv1.2 \
-Dmail.smtp.starttls.enable=true \
-Dmail.smtp.starttls.required=true \
-Dmail.smtp.ssl.enable=false \
-Dmail.smtp.ssl.trust=smtp.gmail.com \
-Dmail.smtp.auth=true \
-Dmail.smtp.ssl.checkserveridentity=true"

# Expose port 8080 for Tomcat
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
