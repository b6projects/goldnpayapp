import type { AnchorHTMLAttributes } from 'react';
import { Link } from 'react-router-dom';
import { buttonClasses } from './buttonStyles';
import type { ButtonSize, ButtonVariant } from './buttonStyles';

export interface LinkButtonProps extends Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'href'> {
  to: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
}

function isInternal(to: string): boolean {
  return to.startsWith('/') && !to.startsWith('//');
}

export default function LinkButton({
  to,
  variant = 'primary',
  size = 'md',
  className,
  children,
  ...rest
}: LinkButtonProps) {
  const classes = buttonClasses(variant, size, className);
  if (isInternal(to)) {
    return (
      <Link to={to} className={classes} {...rest}>
        {children}
      </Link>
    );
  }
  const external = /^https?:/i.test(to);
  return (
    <a
      href={to}
      className={classes}
      {...(external ? { target: '_blank', rel: 'noopener noreferrer' } : {})}
      {...rest}
    >
      {children}
    </a>
  );
}
