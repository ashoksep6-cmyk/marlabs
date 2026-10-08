python-test:
	cd python-service && PYTHONPATH=. pytest -q

spring-test:
	cd spring-api && mvn test

run-python:
	cd python-service && uvicorn app.main:app --host 127.0.0.1 --port 8001

run-spring:
	cd spring-api && mvn spring-boot:run
