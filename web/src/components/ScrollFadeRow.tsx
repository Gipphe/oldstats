import { useEffect, useRef, useState, type HTMLAttributes, type ReactNode } from "react";
import "./ScrollFadeRow.css";

interface ScrollFadeRowProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
}

/** Wraps a horizontally-scrolling row, fading its edges (with a chevron hint) whenever there's more content to scroll to. */
export function ScrollFadeRow({ children, className, ...rest }: ScrollFadeRowProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const update = () => {
      setCanScrollLeft(el.scrollLeft > 4);
      setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 4);
    };
    update();

    el.addEventListener("scroll", update, { passive: true });
    const resizeObserver = new ResizeObserver(update);
    resizeObserver.observe(el);
    return () => {
      el.removeEventListener("scroll", update);
      resizeObserver.disconnect();
    };
  }, []);

  return (
    <div className="scroll-fade-wrap">
      <div ref={ref} className={className} {...rest}>
        {children}
      </div>
      <div className={"scroll-fade-edge left" + (canScrollLeft ? " visible" : "")} aria-hidden="true" />
      <div className={"scroll-fade-edge right" + (canScrollRight ? " visible" : "")} aria-hidden="true" />
    </div>
  );
}
