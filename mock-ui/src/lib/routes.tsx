import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import LoginPage from '../pages/LoginPage';
import SignupPage from '../pages/SignupPage';
import DashboardPage from '../pages/DashboardPage';
import MocksPage from '../pages/MocksPage';
import MockEditorPage from '../pages/MockEditorPage';
import ApiTesterPage from '../pages/ApiTesterPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'mocks', element: <MocksPage /> },
      { path: 'mocks/:id', element: <MockEditorPage /> },
      { path: 'tester', element: <ApiTesterPage /> },
    ],
  },
  { path: '/login', element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
]);


