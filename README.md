# Chirp

## Description

Chirp is a text-based microblogging platform inspired by Twitter (X). This project was developed as a final assignment for the Object-Oriented Programming course. It strictly adheres to the Model-View-Controller (MVC) design pattern using Java as backend, separating the logic, data, and user interface.

## Key Features

- **Account Management:** Secure registration, login, logout, and credential updates utilizing strict encapsulation principles.
- **Timeline:** A public feed displaying tweets in descending order by timestamp. Users can interact with threads through likes, reposts, and replies.
- **Content Filtering:** A dynamic search functionality allowing users to filter the timeline using specific keywords or hashtags.
- **Direct Messages:** Private, encapsulated one-on-one communication between users.
- **Chatbot Assistant:** A virtual entity within the Direct Message feature that provides automated, relevant text responses based on user queries.
- **News Summarization:** An automated system that aggregates the most liked tweets within 24 hours and provides a summarized news feed (maximum 300 words) using the Gemini Flash Latest AI.

## System Requirements

- **Hardware:** PC, laptop, tablet, or smartphone with a minimum Dual-Core processor (1.5 GHz) and 2 GB RAM.
- **Software:** Any operating system with a modern web browser (Google Chrome, Mozilla Firefox, Microsoft Edge, or Safari).
- **Network:** Active internet connection to reach the application server.

## Team Members

| Name                | Student ID (NIM) | Feature (PIC)      |
| :------------------ | :--------------- | :----------------- |
| Evo Abimanyu        | 103012400161     | Account Management |
| Farrel Malik Pirade | 103012400068     | News & Timeline    |
| Faza Fawzan Azima   | 103012400248     | Direct Message     |
| Ravi Adi Prakoso    | 103012430058     | Chatbot            |

## How to Run

> **Heads up!** > The automated `run_project.bat` script is strictly for **Windows** users since it relies on the built-in Command Prompt (`cmd.exe`).

Follow these simple steps to get Chirp up and running on your local machine:

**1. Clone the Repo**

```bash
git clone https://github.com/farrelpirade/Chirp.git
cd Chirp
```

**2. Run the App:**
Just double-click the run_project.bat file.

**3. Open in Browser:**
Go to http://localhost:3000
