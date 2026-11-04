FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py simulate.py test_app.py ./
EXPOSE 9101
CMD ["python", "app.py"]
