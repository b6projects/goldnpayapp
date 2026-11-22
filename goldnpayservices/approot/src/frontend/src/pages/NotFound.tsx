import PlaceholderPage from '../components/ui/PlaceholderPage';
import Seo from '../components/seo/Seo';
import { BRAND } from '../seo/meta';

export default function NotFound() {
  return (
    <>
      <Seo
        title={`Page Not Found | ${BRAND}`}
        description="The page you are looking for does not exist. Browse our loans or talk to an expert."
        path="/404"
        noindex
      />
      <PlaceholderPage title="Page Not Found" />
    </>
  );
}
