import { useEffect, useState } from "react";

interface AnimatedNumberProps {
  value: number;
  duration?: number;
  formatter?: (n: number) => string;
  className?: string;
}

/** Counts up from 0 to `value` on mount (ease-out cubic). Re-plays whenever the component remounts. */
export function AnimatedNumber({ value, duration = 1100, formatter, className }: AnimatedNumberProps) {
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    let raf = 0;
    const start = performance.now();
    function tick(now: number) {
      const t = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - t, 3);
      setDisplay(value * eased);
      if (t < 1) raf = requestAnimationFrame(tick);
    }
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [value, duration]);

  const rounded = Math.round(display);
  return <span className={className}>{formatter ? formatter(rounded) : rounded.toLocaleString()}</span>;
}
