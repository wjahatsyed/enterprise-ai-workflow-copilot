import { Link } from 'react-router-dom';
import Card from '../components/Card';

const items = [
  {
    title: 'Documents',
    text: 'Upload knowledge content and trigger embeddings.',
    to: '/documents'
  },
  {
    title: 'Agents',
    text: 'Create workspace agents and keep them active.',
    to: '/agents'
  },
  {
    title: 'Workflows',
    text: 'Create definitions, start runs, and inspect statuses.',
    to: '/workflows'
  },
  {
    title: 'Approvals',
    text: 'Review waiting workflow runs and approve or reject.',
    to: '/approvals'
  }
];

export default function DashboardPage() {
  return (
    <div className="pageStack">
      <section className="pageIntro">
        <p className="eyebrow">Operations dashboard</p>
        <h2>Run AI-assisted workflow demos from one workspace.</h2>
        <p>
          Set a workspace ID above, then use the pages below to drive the
          end-to-end portfolio scenario.
        </p>
      </section>
      <div className="cardGrid">
        {items.map((item) => (
          <Card key={item.title} title={item.title}>
            <p>{item.text}</p>
            <Link className="textLink" to={item.to}>
              Open {item.title}
            </Link>
          </Card>
        ))}
      </div>
    </div>
  );
}
