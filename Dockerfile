FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y \
    tree \
    vim \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY data/ /app/data/

ENV SAFE_DIR=/app/data/public
ENV SECRET_DIR=/app/data/secret

CMD ["sh", "-c", "javac src/*.java && java -cp src Main; sleep 3"]