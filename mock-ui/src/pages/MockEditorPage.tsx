import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../lib/api';

type Mock = {
  id?: string;
  name?: string;
  method: string;
  path: string;
  matchHeaders?: Record<string, string>;
  matchQuery?: Record<string, string>;
  matchBody?: any;
  responseStatus: number;
  responseJson: any;
  delayMs?: number;
  enabled: boolean;
};

const emptyMock: Mock = {
  method: 'GET',
  path: '/api/example',
  responseStatus: 200,
  responseJson: {},
  enabled: true,
};

export default function MockEditorPage() {
  const { id } = useParams();
  const isNew = id === 'new' || !id;
  const [mock, setMock] = useState<Mock>(emptyMock);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isNew && id) {
      api.get(`/api/mocks/${id}`).then(({ data }) => setMock(data));
    }
  }, [id, isNew]);

  async function onSave() {
    setError(null);
    try {
      if (isNew) {
        await api.post('/api/mocks', mock);
      } else {
        await api.put(`/api/mocks/${id}`, mock);
      }
      navigate('/mocks');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Save failed');
    }
  }

  async function onDelete() {
    if (!id || isNew) return;
    try {
      await api.delete(`/api/mocks/${id}`);
      navigate('/mocks');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Delete failed');
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <h2>{isNew ? 'New Mock' : `Edit Mock ${id}`}</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <div>
        <label htmlFor="method">Method</label>
        <select id="method" value={mock.method} onChange={(e) => setMock({ ...mock, method: e.target.value })}>
          {['GET','POST','PUT','PATCH','DELETE','HEAD','OPTIONS'].map(m => <option key={m} value={m}>{m}</option>)}
        </select>
      </div>
      <div>
        <label htmlFor="path">Path</label>
        <input id="path" placeholder="/api/example" value={mock.path} onChange={(e) => setMock({ ...mock, path: e.target.value })} />
      </div>
      <div>
        <label htmlFor="status">Status</label>
        <input id="status" placeholder="200" type="number" value={mock.responseStatus} onChange={(e) => setMock({ ...mock, responseStatus: Number(e.target.value) })} />
      </div>
      <div>
        <label htmlFor="delay">Delay (ms)</label>
        <input id="delay" placeholder="0" type="number" value={mock.delayMs || 0} onChange={(e) => setMock({ ...mock, delayMs: Number(e.target.value) })} />
      </div>
      <div>
        <label htmlFor="enabled">Enabled</label>
        <input id="enabled" type="checkbox" checked={mock.enabled} onChange={(e) => setMock({ ...mock, enabled: e.target.checked })} />
      </div>
      <div>
        <label htmlFor="resp">Response JSON</label>
        <textarea id="resp" rows={12} value={JSON.stringify(mock.responseJson, null, 2)} onChange={(e) => {
          try {
            const v = JSON.parse(e.target.value || 'null');
            setMock({ ...mock, responseJson: v });
            setError(null);
          } catch {
            setError('Invalid JSON');
          }
        }} />
      </div>
      <div style={{ marginTop: 12 }}>
        <button onClick={onSave}>Save</button>
        {!isNew && <button onClick={onDelete} style={{ marginLeft: 8 }}>Delete</button>}
      </div>
    </div>
  );
}


