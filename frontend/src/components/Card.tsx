import type { ReactNode } from 'react';

type CardProps = {
  title?: string;
  eyebrow?: string;
  children: ReactNode;
  actions?: ReactNode;
};

export default function Card({ title, eyebrow, children, actions }: CardProps) {
  return (
    <section className="card">
      {(title || eyebrow || actions) && (
        <div className="cardHeader">
          <div>
            {eyebrow && <p className="eyebrow">{eyebrow}</p>}
            {title && <h2>{title}</h2>}
          </div>
          {actions && <div className="cardActions">{actions}</div>}
        </div>
      )}
      {children}
    </section>
  );
}
