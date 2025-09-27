import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';

export default function DashboardPage() {
  const health = useQuery({
    queryKey: ['health'],
    queryFn: async () => (await api.get('/actuator/health')).data,
  });
  const metrics = useQuery({
    queryKey: ['metrics'],
    queryFn: async () => (await api.get('/actuator/metrics')).data,
  });

  return (
    <div style={{ padding: 16 }}>
      <h2>Dashboard</h2>
      <section>
        <h3>Health</h3>
        <pre>{JSON.stringify(health.data, null, 2)}</pre>
      </section>
      <section>
        <h3>Metrics</h3>
        <pre>{JSON.stringify(metrics.data, null, 2)}</pre>
      </section>
    </div>
  );
}


