import { lazy } from 'react';
import type { RouteObject } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Home from './pages/Home';

const Loans = lazy(() => import('./pages/Loans'));
const LoanDetail = lazy(() => import('./pages/LoanDetail'));
const EmiCalculator = lazy(() => import('./pages/EmiCalculator'));
const Eligibility = lazy(() => import('./pages/Eligibility'));
const Consultation = lazy(() => import('./pages/Consultation'));
const About = lazy(() => import('./pages/About'));
const Faq = lazy(() => import('./pages/Faq'));
const Contact = lazy(() => import('./pages/Contact'));
const Partners = lazy(() => import('./pages/Partners'));
const Privacy = lazy(() => import('./pages/Privacy'));
const Terms = lazy(() => import('./pages/Terms'));
const Disclaimer = lazy(() => import('./pages/Disclaimer'));
const NotFound = lazy(() => import('./pages/NotFound'));

export const routes: RouteObject[] = [
  {
    element: <Layout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/loans', element: <Loans /> },
      { path: '/loans/:slug', element: <LoanDetail /> },
      { path: '/emi-calculator', element: <EmiCalculator /> },
      { path: '/eligibility', element: <Eligibility /> },
      { path: '/consultation', element: <Consultation /> },
      { path: '/about', element: <About /> },
      { path: '/faq', element: <Faq /> },
      { path: '/contact', element: <Contact /> },
      { path: '/partners', element: <Partners /> },
      { path: '/privacy', element: <Privacy /> },
      { path: '/terms', element: <Terms /> },
      { path: '/disclaimer', element: <Disclaimer /> },
      { path: '*', element: <NotFound /> },
    ],
  },
];
