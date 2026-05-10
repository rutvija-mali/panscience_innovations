export default function MessageBubble({ role, content }) {
  const isUser = role === 'user'
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div
        className={[
          'max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed shadow-sm',
          isUser
            ? 'bg-indigo-600 text-white'
            : 'bg-slate-900/70 text-slate-100 ring-1 ring-slate-800',
        ].join(' ')}
      >
        <div className="whitespace-pre-wrap">{content}</div>
      </div>
    </div>
  )
}

