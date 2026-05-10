import { useMemo, useState } from 'react'
import ChatComposer from './components/ChatComposer.jsx'
import ChatPanel from './components/ChatPanel.jsx'
import Sidebar from './components/Sidebar.jsx'

const API = '/api'

export default function App() {
  const [documentId, setDocumentId] = useState(null)
  const [textLength, setTextLength] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [asking, setAsking] = useState(false)
  const [error, setError] = useState('')
  const [transcribing, setTranscribing] = useState(false)

  const [draft, setDraft] = useState('')
  const [messages, setMessages] = useState([])
  const [audioName, setAudioName] = useState('')
  const [transcript, setTranscript] = useState('')

  const pdfStatusText = useMemo(() => {
    if (!documentId) return 'No PDF uploaded'
    if (textLength == null) return 'Ready'
    return `Ready — ${textLength.toLocaleString()} characters`
  }, [documentId, textLength])

  async function uploadPdf(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    setError('')
    setMessages([])
    setDraft('')
    setUploading(true)

    try {
      const form = new FormData()
      form.append('file', file)
      const res = await fetch(`${API}/upload`, { method: 'POST', body: form })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setError(data.error || 'Upload failed')
        setDocumentId(null)
        setTextLength(null)
        return
      }
      setDocumentId(data.documentId)
      setTextLength(data.textLength)
      setMessages([
        {
          role: 'assistant',
          content: 'PDF loaded. Ask me anything about it.',
        },
      ])
    } catch {
      setError('Network error — is the backend running on port 8080?')
      setDocumentId(null)
      setTextLength(null)
    } finally {
      setUploading(false)
    }
  }

  function uploadAudio(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setAudioName(file.name)
    setError('')
    setTranscribing(true)

    ;(async () => {
      try {
        const form = new FormData()
        form.append('file', file)
        const res = await fetch(`${API}/audio/transcribe`, {
          method: 'POST',
          body: form,
        })
        const data = await res.json().catch(() => ({}))
        if (!res.ok) {
          setError(data.error || 'Transcription failed')
          setTranscript('')
          return
        }

        setTranscript(data.transcript || '')
        setDocumentId(data.documentId || null)
        setTextLength(data.textLength ?? null)
        setMessages([
          {
            role: 'assistant',
            content: 'Transcript ready. Ask me anything about it.',
          },
        ])
        setDraft('')
      } catch {
        setError('Network error — is the backend running on port 8080?')
        setTranscript('')
      } finally {
        setTranscribing(false)
      }
    })()
  }

  async function sendQuestion(nextQuestion) {
    const q = (nextQuestion ?? draft).trim()
    if (!documentId || !q || asking) return

    setError('')
    setAsking(true)
    setDraft('')
    setMessages((prev) => [...prev, { role: 'user', content: q }])

    try {
      const res = await fetch(`${API}/ask`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ documentId, question: q }),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setError(data.error || 'Request failed')
        return
      }
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: data.answer || '' },
      ])
    } catch {
      setError('Network error — is the backend running?')
    } finally {
      setAsking(false)
    }
  }

  function summarize() {
    sendQuestion(
      'Summarize this document in 8-12 bullet points. Then add 3 key takeaways.'
    )
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="relative mx-auto max-w-6xl">
        <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-gradient-to-b from-indigo-500/10 to-transparent" />

        <div className="relative flex min-h-screen flex-col md:flex-row">
          <Sidebar
            appTitle="DocuMind AI"
            uploading={uploading}
            pdfStatusText={pdfStatusText}
            onPdfUpload={uploadPdf}
            onSummarize={summarize}
            summarizeDisabled={!documentId || uploading || asking}
            onAudioUpload={uploadAudio}
            audioName={audioName}
            transcribing={transcribing}
            transcript={transcript}
          />

          <main className="flex min-w-0 flex-1 flex-col">
            <div className="flex min-h-0 flex-1 flex-col">
              <ChatPanel
                title={documentId ? 'Chat with your document' : 'Start by uploading a PDF'}
                messages={messages}
                error={error}
                isThinking={asking}
              />
              <ChatComposer
                disabled={!documentId || uploading || asking || transcribing}
                value={draft}
                onChange={setDraft}
                onSend={() => sendQuestion()}
                placeholder={
                  documentId
                    ? 'Ask a question about your PDF / transcript…'
                    : 'Upload a PDF to start…'
                }
              />
            </div>
          </main>
        </div>
      </div>
    </div>
  )
}
