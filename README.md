<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=1&height=250&section=header&text=Smart%20Hunter%20🔍&fontSize=60&fontAlignY=35&animation=twinkling&fontColor=ffffff" />
</div>

<div align="center">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&weight=700&size=22&pause=1000&color=10B981&center=true&vCenter=true&width=800&lines=AI-Powered+Job+Search+Assistant;Real-time+Web+Analysis+with+Tavily;Automated+Resume+Matching+with+Llama+3;Cloud+Native+Deployment+on+Render" alt="Typing SVG" />
</div>

<br>

<div align="center">
  <em>A cutting-edge, AI-driven career companion that streamlines the job hunting process. Smart Hunter leverages LLM-based analysis to match your unique skill set with high-potential opportunities across the web.</em>
</div>

<br>

## 🛠️ Tech Stack & Architecture

<div align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,spring,postgresql,docker,maven,git&theme=dark&perline=6" alt="Tech Stack" />
  </a>
</div>

<br>

| Layer | Technologies Used |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3, Spring Data JPA, Hibernate |
| **AI/LLM** | LangChain4j, Groq API (Llama 3), Tavily Web Search |
| **Database** | PostgreSQL (Render Managed) |
| **Container** | Docker (Multi-stage build) |
| **Deployment**| Render Web Services |

---

## 🚀 Core Features

### 🧠 AI-Driven Intelligence
* **Resume Analyst:** Uses Llama 3 to deconstruct resumes and identify core technical strengths.
* **Smart Job Hunter:** Performs targeted web searches via Tavily to find jobs that perfectly align with your extracted skills.
* **Intelligent Feedback:** Provides actionable insights on why a job is a good fit for your profile.

### ☁️ Cloud-Native Engineering
* **Dockerized Infrastructure:** Fully containerized backend ensuring consistency across development and production environments.
* **PostgreSQL Persistence:** Robust relational data management on high-availability cloud storage.
* **Optimized Build Pipeline:** Automated Maven build processes integrated with Docker for seamless CI/CD.

### 🔐 Security & Reliability
* **Secure OTP Verification:** Robust account activation and profile update flows using email-based tokens.
* **Fault-Tolerant:** Graceful handling of AI API timeouts and cloud-based database connectivity.

---

## 💡 Technical Challenges Overcome

1. **AI Orchestration:** Successfully integrated LangChain4j with custom prompt engineering to convert unstructured resume data into structured job-matching criteria.
2. **Cloud Database Migration:** Navigated strict cloud firewall constraints and JDBC URL formatting to achieve seamless communication between the Spring Boot container and external PostgreSQL.
3. **Environment Isolation:** Managed complex secret injection for Groq API keys and SMTP credentials across containerized cloud environments.

---

## 📸 Platform Gallery

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Admin Login (Dark Mode)</b><br><img src="screenshots/admin_loginpage.png" width="400" alt="Login Dark Mode"></td>
      <td align="center"><b>Admin Dashboard</b><br><img src="screenshots/Admin_dashboard.png" width="400" alt="Admin Dashboard"></td>
    </tr>
    <tr>
      <td align="center"><b>Student Dashboard</b><br><img src="screenshots/Student_dashboard.png" width="400" alt="Student Dashboard"></td>
      <td align="center"><b>Student Login</b><br><img src="screenshots/student_loginpage.png" width="400" alt="Student Login"></td>
    </tr>
  </table>
</div>

---
## ⚙️ Quick Start

**1. Clone the Repository:**
```bash
git clone [https://github.com/Mahesh-Konarasipalli/smart-hunter.git](https://github.com/Mahesh-Konarasipalli/smart-hunter.git)

**2. Configure Environment:**
Set the following environment variables locally or in your cloud platform:

SPRING_DATASOURCE_URL

SPRING_DATASOURCE_USERNAME

SPRING_DATASOURCE_PASSWORD

GROQ_API_KEY

**3. Build & Run:**
./mvnw clean spring-boot:run---