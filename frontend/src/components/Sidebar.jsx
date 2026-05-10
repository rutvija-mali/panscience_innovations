export default function Sidebar({
  appTitle,
  uploading,
  pdfStatusText,
  onPdfUpload,
  onSummarize,
  summarizeDisabled,
  onAudioUpload,
  audioName,
  transcribing,
  transcript,
}) {
  return (
    <aside className="flex w-full flex-col border-b border-slate-800 bg-slate-950/60 p-5 backdrop-blur md:w-[360px] md:border-b-0 md:border-r">
      <div>
        <div className="flex items-center justify-between gap-3">
          <h1 className="text-base font-semibold tracking-tight text-white">
            {appTitle}
          </h1>
          <span className="rounded-full bg-slate-900/60 px-2 py-1 text-[11px] text-slate-300 ring-1 ring-slate-800">
            PDF Q&amp;A
          </span>
        </div>
        <p className="mt-2 text-sm text-slate-400">
          Upload a document, then chat with it.
        </p>
      </div>

      <div className="mt-6 space-y-6">
        <section className="rounded-2xl border border-slate-800 bg-slate-900/35 p-4">
          <p className="text-sm font-medium text-slate-200">PDF upload</p>
          <p className="mt-1 text-xs text-slate-500">Text is extracted on the server.</p>

          <input
            type="file"
            accept="application/pdf,.pdf"
            onChange={onPdfUpload}
            disabled={uploading}
            className="mt-3 block w-full cursor-pointer text-sm text-slate-300 file:mr-4 file:rounded-xl file:border-0 file:bg-indigo-600 file:px-4 file:py-2 file:text-sm file:font-medium file:text-white hover:file:bg-indigo-500 disabled:opacity-50"
          />

          <div className="mt-3 text-xs text-slate-400">
            {uploading ? 'Extracting text…' : pdfStatusText}
          </div>

          <button
            type="button"
            onClick={onSummarize}
            disabled={summarizeDisabled}
            className="mt-4 w-full rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-100 ring-1 ring-slate-700 hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Summarize
          </button>
        </section>

        <section className="rounded-2xl border border-slate-800 bg-slate-900/35 p-4">
          <p className="text-sm font-medium text-slate-200">Audio upload</p>
          <p className="mt-1 text-xs text-slate-500">
            Upload audio to transcribe and chat with it.
          </p>
          <input
            type="file"
            accept="audio/*"
            onChange={onAudioUpload}
            disabled={transcribing}
            className="mt-3 block w-full cursor-pointer text-sm text-slate-300 file:mr-4 file:rounded-xl file:border-0 file:bg-slate-800 file:px-4 file:py-2 file:text-sm file:font-medium file:text-slate-100 hover:file:bg-slate-700"
          />
          <div className="mt-3 text-xs text-slate-400">
            {audioName ? `Selected: ${audioName}` : 'No audio selected'}
          </div>

          <div className="mt-4">
            <div className="flex items-center justify-between">
              <p className="text-xs font-medium text-slate-400">Transcript</p>
              {transcribing ? (
                <span className="text-xs text-slate-500">Transcribing…</span>
              ) : null}
            </div>
            <div className="mt-2 max-h-56 overflow-y-auto rounded-xl border border-slate-800 bg-slate-950/30 p-3 text-xs text-slate-200">
              {transcript ? (
                <div className="whitespace-pre-wrap">{transcript}</div>
              ) : (
                <div className="text-slate-500">
                  Upload an audio file to generate a transcript.
                </div>
              )}
            </div>
          </div>
        </section>
      </div>

      <div className="mt-auto pt-6 text-xs text-slate-600">
        Tip: try “What are the key points?” or “List important definitions.”
      </div>
    </aside>
  )
}

