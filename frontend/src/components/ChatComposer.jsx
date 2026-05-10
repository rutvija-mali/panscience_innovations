import { useEffect, useRef } from 'react'

export default function ChatComposer({
  disabled,
  value,
  onChange,
  onSend,
  placeholder,
}) {
  const inputRef = useRef(null)

  useEffect(() => {
    if (!disabled) inputRef.current?.focus()
  }, [disabled])

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      onSend()
    }
  }

  return (
    <div className="border-t border-slate-800 bg-slate-950/70 px-5 py-4 backdrop-blur">
      <div className="flex items-end gap-3">
        <div className="flex-1">
          <textarea
            ref={inputRef}
            rows={1}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            disabled={disabled}
            className="max-h-40 w-full resize-none rounded-xl border border-slate-800 bg-slate-900/40 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 disabled:opacity-50"
          />
          <p className="mt-2 text-xs text-slate-500">
            Enter to send, Shift+Enter for a new line
          </p>
        </div>
        <button
          type="button"
          onClick={onSend}
          disabled={disabled || !value.trim()}
          className="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-40"
        >
          Send
        </button>
      </div>
    </div>
  )
}

