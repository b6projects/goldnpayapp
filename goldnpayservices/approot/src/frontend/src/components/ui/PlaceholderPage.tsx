interface PlaceholderPageProps {
  title: string;
}

export default function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <section className="mx-auto max-w-5xl p-6">
      <h1 className="font-heading text-3xl font-semibold text-primary">{title}</h1>
      <p className="mt-2 text-muted">Content coming soon.</p>
    </section>
  );
}
