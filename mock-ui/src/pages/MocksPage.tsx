import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { Link } from 'react-router-dom';
import { useState } from 'react';

type Page<T> = { content: T[]; totalElements: number; number: number; size: number };
type Mock = {
  id: string;
  name?: string;
  method: string;
  path: string;
  responseStatus: number;
  enabled: boolean;
};

export default function MocksPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [q, setQ] = useState('');
  const { data, isLoading, refetch } = useQuery<Page<Mock>>({
    queryKey: ['mocks', { page, size, q }],
    queryFn: async () => (await api.get('/api/mocks', { params: { page, size, q } })).data,
    keepPreviousData: true,
  });

  async function onExport() {
    const res = await api.get('/api/mocks/export', { responseType: 'blob' });
    const url = URL.createObjectURL(res.data);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'mocks.json';
    a.click();
    URL.revokeObjectURL(url);
  }

  async function onImport(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    const form = new FormData();
    form.append('file', file);
    await api.post('/api/mocks/import', form, { headers: { 'Content-Type': 'multipart/form-data' } });
    refetch();
  }

  return (
    <div style={{ padding: 16 }}>
      <h2>Mocks</h2>
      <div style={{ marginBottom: 12 }}>
        <input aria-label="Search mocks" placeholder="Search" value={q} onChange={(e) => setQ(e.target.value)} />
        <Link to="/mocks/new" style={{ marginLeft: 8 }}>New Mock</Link>
        <button style={{ marginLeft: 8 }} onClick={onExport}>Export</button>
        <label style={{ marginLeft: 8 }}>
          Import
          <input aria-label="Import mocks" type="file" accept="application/json" onChange={onImport} />
        </label>
      </div>
      {isLoading && <p>Loading...</p>}
      <table width="100%" cellPadding={6} style={{ borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>Method</th>
            <th>Path</th>
            <th>Status</th>
            <th>Enabled</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {data?.content?.map((m) => (
            <tr key={m.id}>
              <td>{m.method}</td>
              <td>{m.path}</td>
              <td>{m.responseStatus}</td>
              <td>{String(m.enabled)}</td>
              <td>
                <Link to={`/mocks/${m.id}`}>Edit</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={{ marginTop: 12 }}>
        <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Prev</button>
        <span style={{ margin: '0 8px' }}>Page {page + 1}</span>
        <button disabled={(data?.content?.length || 0) < size} onClick={() => setPage((p) => p + 1)}>Next</button>
        <label htmlFor="page-size" style={{ marginLeft: 8 }}>Page size</label>
        <select id="page-size" aria-label="Page size" value={size} onChange={(e) => setSize(Number(e.target.value))} style={{ marginLeft: 8 }}>
          <option value={10}>10</option>
          <option value={20}>20</option>
          <option value={50}>50</option>
        </select>
      </div>
    </div>
  );
}


