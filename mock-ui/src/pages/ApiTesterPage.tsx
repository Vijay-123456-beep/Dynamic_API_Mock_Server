import { useState } from 'react';

export default function ApiTesterPage() {
  const [method, setMethod] = useState('GET');
  const [url, setUrl] = useState('http://localhost:8088');
  const [headersText, setHeadersText] = useState('');
  const [body, setBody] = useState('');
  const [response, setResponse] = useState<any>(null);
  const [status, setStatus] = useState<number | null>(null);
  const [timeMs, setTimeMs] = useState<number | null>(null);

  async function send() {
    try {
      setResponse(null);
      setStatus(null);
      const headers = headersText
        ? JSON.parse(headersText)
        : {};
      const init: RequestInit = {
        method,
        headers: headers as any,
      };
      if (method !== 'GET' && method !== 'HEAD' && body) {
        init.body = body;
        (init.headers as any)['Content-Type'] ||= 'application/json';
      }
      const start = performance.now();
      const token = localStorage.getItem('token');
      if (token) {
        (init.headers as any) = { ...(init.headers as any), Authorization: `Bearer ${token}` };
      }
      const res = await fetch(url, init);
      const end = performance.now();
      setTimeMs(Math.round(end - start));
      setStatus(res.status);
      const text = await res.text();
      try {
        setResponse(JSON.parse(text));
      } catch {
        setResponse(text);
      }
    } catch (e: any) {
      setResponse({ error: e?.message || 'Request failed' });
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <h2>API Tester</h2>
      <div>
        <label htmlFor="methodSel">Method</label>
        <select id="methodSel" aria-label="HTTP method" value={method} onChange={(e) => setMethod(e.target.value)}>
          {['GET','POST','PUT','PATCH','DELETE','HEAD','OPTIONS'].map(m => <option key={m} value={m}>{m}</option>)}
        </select>
        <label htmlFor="url" style={{ marginLeft: 8 }}>URL</label>
        <input id="url" aria-label="Request URL" placeholder="http://localhost:8088/api/..." style={{ width: '60%', marginLeft: 8 }} value={url} onChange={(e) => setUrl(e.target.value)} />
        <button style={{ marginLeft: 8 }} onClick={send}>Send</button>
      </div>
      <div style={{ marginTop: 12 }}>
        <label htmlFor="headers">Headers (JSON)</label>
        <textarea id="headers" aria-label="Request headers JSON" rows={6} style={{ width: '100%' }} value={headersText} onChange={(e) => setHeadersText(e.target.value)} />
      </div>
      <div>
        <label htmlFor="body">Body</label>
        <textarea id="body" aria-label="Request body" rows={8} style={{ width: '100%' }} value={body} onChange={(e) => setBody(e.target.value)} />
      </div>
      <div style={{ marginTop: 12 }}>
        <div>Status: {status ?? '-'}</div>
        <div>Time: {timeMs ?? '-'} ms</div>
        <pre style={{ background: '#111', color: '#0f0', padding: 12, overflow: 'auto' }}>{typeof response === 'string' ? response : JSON.stringify(response, null, 2)}</pre>
      </div>
    </div>
  );
}


