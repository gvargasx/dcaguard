import { Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import DashboardLayout from './components/layout/DashboardLayout';
import Dashboard from './pages/Dashboard';
import PortfolioList from './pages/PortfolioList';
import PortfolioDetail from './pages/PortfolioDetail';
import DcaList from './pages/DcaList';
import DcaDetail from './pages/DcaDetail';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/app" element={<DashboardLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="portfolio" element={<PortfolioList />} />
        <Route path="portfolio/:id" element={<PortfolioDetail />} />
        <Route path="dca" element={<DcaList />} />
        <Route path="dca/:id" element={<DcaDetail />} />
      </Route>
    </Routes>
  );
}