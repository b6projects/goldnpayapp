import type { CSSProperties, ElementType, HTMLAttributes } from 'react';
import { useInView } from '../../hooks/useInView';

export interface RevealProps extends HTMLAttributes<HTMLElement> {
  as?: ElementType;
  /** Stagger delay in ms. */
  delay?: number;
  direction?: 'up' | 'left' | 'right' | 'none';
}

const hidden: Record<NonNullable<RevealProps['direction']>, string> = {
  up: 'translate-y-8',
  left: '-translate-x-8',
  right: 'translate-x-8',
  none: '',
};

/** Fades/slides its content in the first time it scrolls into view. */
export default function Reveal({
  as: Tag = 'div',
  delay = 0,
  direction = 'up',
  className = '',
  style,
  ...rest
}: RevealProps) {
  const { ref, inView } = useInView<HTMLElement>();
  const merged: CSSProperties = { transitionDelay: `${delay}ms`, ...style };
  return (
    <Tag
      ref={ref}
      style={merged}
      className={`transition-all duration-700 ease-out motion-reduce:transition-none ${
        inView ? 'translate-x-0 translate-y-0 opacity-100' : `opacity-0 ${hidden[direction]}`
      } ${className}`}
      {...rest}
    />
  );
}
