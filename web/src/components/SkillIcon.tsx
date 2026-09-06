/**
 * Official skill icon sprites, extracted from the RuneLite client
 * (`skill_icons/*.png` in `net.runelite:client`, which sources them from the
 * live game cache) rather than redrawn — see `web/public/skills/`.
 */
interface SkillIconProps {
  skill: string;
  className?: string;
}

export function SkillIcon({ skill, className }: SkillIconProps) {
  const name = skill.toLowerCase();
  return (
    <img
      src={`/skills/${name}.png`}
      alt=""
      width={16}
      height={16}
      className={className}
      onError={(e) => {
        e.currentTarget.onerror = null;
        e.currentTarget.src = "/skills/overall.png";
      }}
    />
  );
}
