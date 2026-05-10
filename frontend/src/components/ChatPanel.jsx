import MessageBubble from './MessageBubble.jsx'

export default function ChatPanel({ title, messages, error, isThinking }) {
  return (
    <section className="flex min-w-0 flex-1 flex-col">
      <div className="flex items-center justify-between border-b border-slate-800 bg-slate-950/60 px-5 py-4 backdrop-blur">
        <div className="min-w-0">
          <h2 className="truncate text-sm font-medium text-slate-200">
            {title}
          </h2>
          <p className="text-xs text-slate-500">
            Ask questions, get answers as chat replies
          </p>
        </div>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto px-5 py-5">
        {messages.length === 0 ? (
          <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-5 text-sm text-slate-300">
            Upload a PDF on the left, then ask your first question.
          </div>
        ) : (
          messages.map((m, idx) => (
            <MessageBubble key={idx} role={m.role} content={m.content} />
          ))
        )}

        {isThinking && (
          <div className="flex justify-start">
            <div className="rounded-2xl bg-slate-900/60 px-4 py-3 text-sm text-slate-300 ring-1 ring-slate-800">
              Thinking…
            </div>
          </div>
        )}
      </div>

      {error ? (
        <div className="border-t border-slate-800 px-5 py-3">
          <div className="rounded-lg border border-red-900/70 bg-red-950/30 px-3 py-2 text-sm text-red-200">
            {error}
          </div>
        </div>
      ) : null}
    </section>
  )
}

