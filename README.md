# 🩺 MedBot — Ask the Medical Encyclopedia

MedBot is a ChatGPT-style web app, written in plain Java, that answers medical questions using passages from a medical encyclopedia text file you provide.

Type a question like *"What is asthma?"* and MedBot searches the book, finds the most relevant passages, and shows them in a chat interface.

## Live demo

The `docs/` folder is a static version that runs entirely in the browser (hosted with GitHub Pages). It searches a small set of demo notes, or any `book.txt` you load from your own computer. The file is processed locally and never uploaded.

**Demo:** https://YOUR-USERNAME.github.io/MedBot/

## Features

- Chat interface with a dark glass design and a light theme
- Animated heartbeat line, typing effect for answers, highlighted keywords
- Topic cards on the welcome screen, sidebar with recent questions
- Copy and read-aloud buttons, voice input (in supported browsers)
- Works on phones
- No libraries or frameworks. Only the JDK is needed

## How it works

1. On start-up, the Java server reads `book.txt` and splits it into overlapping passages of about 140 words.
2. For each question, every passage gets a TF-IDF style relevance score based on the question's keywords.
3. The top 3 passages are sent to the web page, which displays them as the answer.

Answers always come straight from the book text, so nothing is invented. MedBot retrieves text, it does not write new text.

## Run it

**Requirements:** JDK 11 or newer

1. Download or clone this repository.
2. Add your own `book.txt` (plain text of the book) next to `Server.java`. See the note below.
3. In the project folder, run:
   ```
   javac Server.java
   java Server
   ```
   On Windows you can also double-click `run.bat`.
4. Open **http://localhost:8080** in your browser.

## Project structure

```
MedBot/
├── Server.java        Java server: loads the book, scores passages, serves the page
├── run.bat            Windows launcher
├── book.txt           NOT included, you supply this
├── public/
│   └── index.html     Chat interface for the Java server
└── docs/              Static version for GitHub Pages
    ├── index.html     Same interface, search runs in the browser
    └── demo.txt       Short demo notes
```

## Note about the book text

This project was built to work with *The Gale Encyclopedia of Medicine*. The book is copyrighted, so its text is **not included** in this repository. To use MedBot, create `book.txt` from a copy you legally have. Any plain-text document will work.

## Disclaimer

For learning purposes only. This is not medical advice. Always consult a qualified doctor.

## Ideas for the future

- Use an LLM API to write natural answers from the retrieved passages
- Host the server online so it is always available
- Show the entry title and page number with each answer
