# 🍃 GreenLeaf Paper Plates — Full Stack Web Application

A production-ready website for a paper plate factory featuring:
- Beautiful responsive frontend (HTML + CSS)
- Spring Boot REST API backend
- MySQL/H2 database for contact storage
- Gmail SMTP email integration
- AWS deployment ready (Elastic Beanstalk / EC2 / ECS)

---

## 📁 Project Structure

```
paperplate/
├── frontend/
│   ├── index.html          ← Main website
│   └── style.css           ← All styles
│
└── backend/
    ├── Dockerfile
    ├── pom.xml
    ├── .ebextensions/      ← AWS Elastic Beanstalk config
    └── src/main/
        ├── java/com/greenleaf/paperplate/
        │   ├── PaperPlateApplication.java
        │   ├── config/AsyncConfig.java
        │   ├── controller/ContactController.java
        │   ├── dto/ContactRequest.java
        │   ├── exception/GlobalExceptionHandler.java
        │   ├── model/Contact.java
        │   ├── repository/ContactRepository.java
        │   └── service/
        │       ├── ContactService.java
        │       └── EmailService.java
        └── resources/
            ├── application.properties
            └── static/          ← Copy frontend files here
                ├── index.html
                └── style.css
```

---

## ⚡ Quick Start (Local Development)

### Prerequisites
- Java 17+
- Maven 3.8+
- Gmail account with 2FA enabled

### Step 1: Generate Gmail App Password

1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable **2-Step Verification**
3. Go to [App Passwords](https://myaccount.google.com/apppasswords)
4. Create a new app password → select **Mail** → **Other (Custom name)**
5. Copy the 16-character password

### Step 2: Copy frontend into Spring Boot static folder

```bash
cp frontend/index.html backend/src/main/resources/static/
cp frontend/style.css  backend/src/main/resources/static/
```

### Step 3: Set environment variable for mail password

```bash
# Linux / Mac
export MAIL_PASSWORD=your_16_char_app_password

# Windows CMD
set MAIL_PASSWORD=your_16_char_app_password
```

### Step 4: Run the backend

```bash
cd backend
mvn spring-boot:run
```

Open http://localhost:8080 in your browser. ✅

The H2 console (dev only) is at: http://localhost:8080/h2-console

---

## 🗄️ Switch to MySQL (Production)

1. Create a MySQL database:
```sql
CREATE DATABASE paperplatedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. In `application.properties`, comment out the H2 section and uncomment the MySQL section:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paperplatedb?useSSL=false&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## ☁️ AWS Deployment Options

### Option A: AWS Elastic Beanstalk (Recommended — easiest)

**Step 1: Build the JAR**
```bash
cd backend
mvn clean package -DskipTests
# Creates: target/paperplate-1.0.0.jar
```

**Step 2: Create Elastic Beanstalk application**
```bash
# Install AWS CLI and EB CLI first
pip install awsebcli

cd backend
eb init paperplate-app --platform java-17 --region ap-south-1
eb create paperplate-prod --instance-type t3.small
```

**Step 3: Set environment variables in EB console**

Go to: EB Console → Your App → Configuration → Software → Environment Properties

Add:
```
MAIL_PASSWORD         = your_gmail_app_password
RDS_HOSTNAME          = your-rds-endpoint.rds.amazonaws.com
RDS_PORT              = 3306
RDS_DB_NAME           = paperplatedb
RDS_USERNAME          = admin
RDS_PASSWORD          = your_rds_password
SPRING_PROFILES_ACTIVE = prod
```

**Step 4: Deploy**
```bash
eb deploy
```

Your app will be live at: `http://paperplate-prod.ap-south-1.elasticbeanstalk.com`

---

### Option B: Docker on AWS ECS / EC2

**Step 1: Build Docker image**
```bash
cd backend
docker build -t greenleaf-paperplate .
```

**Step 2: Push to AWS ECR**
```bash
aws ecr create-repository --repository-name greenleaf-paperplate --region ap-south-1

aws ecr get-login-password --region ap-south-1 | \
  docker login --username AWS --password-stdin \
  YOUR_ACCOUNT_ID.dkr.ecr.ap-south-1.amazonaws.com

docker tag greenleaf-paperplate:latest \
  YOUR_ACCOUNT_ID.dkr.ecr.ap-south-1.amazonaws.com/greenleaf-paperplate:latest

docker push \
  YOUR_ACCOUNT_ID.dkr.ecr.ap-south-1.amazonaws.com/greenleaf-paperplate:latest
```

**Step 3: Deploy via ECS (Fargate)**

Create a Task Definition with:
- Container image: your ECR image URI
- Port: 8080
- Environment variables: MAIL_PASSWORD, RDS_* etc.
- Memory: 512 MB, CPU: 256

Then create a Service with an Application Load Balancer.

---

### Option C: AWS EC2 (Manual)

```bash
# On EC2 instance (Amazon Linux 2023)
sudo dnf install java-17-amazon-corretto -y

# Upload JAR and run
scp target/paperplate-1.0.0.jar ec2-user@EC2_IP:/home/ec2-user/

ssh ec2-user@EC2_IP
export MAIL_PASSWORD=your_app_password
java -jar paperplate-1.0.0.jar --server.port=8080 &
```

Add a systemd service for auto-restart:
```ini
# /etc/systemd/system/paperplate.service
[Unit]
Description=GreenLeaf Paper Plate Backend
After=network.target

[Service]
User=ec2-user
ExecStart=/usr/bin/java -jar /home/ec2-user/paperplate-1.0.0.jar
Environment="MAIL_PASSWORD=your_app_password"
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl enable paperplate
sudo systemctl start paperplate
```

---

## 🔒 AWS Security Checklist

- [ ] Store `MAIL_PASSWORD` in **AWS Secrets Manager** or EB environment variables — never commit it
- [ ] Enable **HTTPS** via ACM certificate + ALB listener (port 443)
- [ ] Restrict `@CrossOrigin` to your actual domain in `ContactController.java`
- [ ] Use **RDS MySQL** with private subnet (not publicly accessible)
- [ ] Enable **RDS encryption at rest**
- [ ] Set up **CloudWatch** logs for the application
- [ ] Configure **Security Groups**: only allow 8080 from ALB, not public

---

## 📧 Email Flow

```
Customer submits form
        │
        ▼
POST /api/contact
        │
        ├─── Save to DB (synchronous)
        │
        ├─── Send thank-you email to customer (async)
        │    From: nagamahendram098@gmail.com
        │    To:   customer's email
        │    Subject: "Thank you for reaching out — GreenLeaf Paper Plates 🍃"
        │
        └─── Send internal notification (async)
             From: nagamahendram098@gmail.com
             To:   nagamahendram098@gmail.com
             Subject: "📩 New Enquiry from [Name]"
```

---

## 🧪 Test the API

```bash
curl -X POST http://localhost:8080/api/contact \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ravi Kumar",
    "phone": "9876543210",
    "email": "ravi@example.com",
    "location": "Chennai, Tamil Nadu",
    "message": "I need 50,000 dinner plates for our hotel chain."
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Thank you! We'll get back to you soon.",
  "id": 1
}
```

---

## 📞 Support

Factory: GreenLeaf Paper Plates Pvt. Ltd.  
Address: Plot No. 47, APIIC Industrial Estate, Tirupati — 517520  
Email: nagamahendram098@gmail.com  
Phone: +91 98765 43210
