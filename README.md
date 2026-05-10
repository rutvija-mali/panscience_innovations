# DocuMind AI

Upload a **PDF** (text extraction via PDFBox) or an **audio file** (transcription via Groq Whisper), then chat with the extracted text using Groq’s OpenAI-compatible chat API.

## Tech
- **Backend**: Spring Boot, PDFBox
- **AI**: Groq (Chat Completions + Whisper Transcription)
- **Frontend**: React + Vite + Tailwind (dark chat UI)
- **Storage**: in-memory only (no DB; resets on restart)

## Setup
### 1) API key
Set `GROQ_API_KEY` in your environment (recommended).

PowerShell:

```bash
$env:GROQ_API_KEY="gsk_..."
```

### 2) Run backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`.

### 3) Run frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` and proxies `/api` → `http://localhost:8080`.

## Deploy (Vercel + Railway)
- **Railway (backend)**: set env
  - `GROQ_API_KEY=...`
  - `CORS_ALLOWED_ORIGINS=https://YOUR_VERCEL_APP.vercel.app`
- **Vercel (frontend)**: set env
  - `VITE_API_BASE_URL=https://YOUR_RAILWAY_BACKEND.up.railway.app`

## API
- **POST** `/api/upload` (multipart `file` = PDF) → `{ documentId, textLength }`
- **POST** `/api/ask` (JSON `{ documentId, question }`) → `{ answer }`
- **POST** `/api/audio/transcribe` (multipart `file` = audio) → `{ documentId, transcript, textLength }`

## Notes
- PDFs/audio are stored as extracted text in memory and are **not persisted**.
- Audio upload supports transcription + chatting, but not audio playback.

