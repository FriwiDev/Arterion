package me.friwi.arterion.plugin.combat.skill.impl.cleric;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Binding;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.RestrictedActiveSkill;
import me.friwi.arterion.plugin.combat.skill.SkillContainerData;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

public class DivineBlessingSkill extends RestrictedActiveSkill<SkillContainerData> {

    private ParticleEffect.ParticleColor[][] pixels = null;

    public DivineBlessingSkill() {
        super(ClassEnum.CLERIC, SkillSlotEnum.ACTIVE5);
        this.loadColors();
    }

    @Override
    public boolean cast(ArterionPlayer p) {
        if (p.getHealth() <= 0) return false;

        int ticks = ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_DIVINE_BLESSING_DURATION.evaluateInt(p) / 50;

        this.printCastMessage(p, null);

        int tickInterval = 2;

        Binding<Entity> binding = Hooks.FINAL_DAMAGE_RECEIVED_HOOK.subscribe(p.getBukkitPlayer(), v -> 0.0001);

        p.getBukkitPlayer().getWorld().playSound(p.getBukkitPlayer().getLocation(), Sound.ENDERDRAGON_WINGS, 1f, 1f);
        p.scheduleHotbarCard(new DivineBlessingHotbarCard(ticks * 50));

        float yOffset = 0.3f;
        float xzOffset = 0.2f;
        float height = 2.2f;
        float radius = 1.5f;
        double wingAngle = Math.PI / 3;
        int pixelHeight = pixels[0].length;
        int pixelWidth = pixels.length;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (p.getHealth() <= 0 || !p.getBukkitPlayer().isOnline()) {
                    tick = ticks;
                }

                Vector dir = p.getBukkitPlayer().getLocation().getDirection().clone();
                dir.setY(0);
                dir.normalize().multiply(-1);

                Location basePoint = p.getBukkitPlayer().getLocation().clone().add(dir.clone().multiply(xzOffset));
                basePoint.add(0, yOffset, 0);

                float perPixelHeight = height / pixelHeight;
                float perPixelWidth = radius / pixelWidth;

                Vector[] vectors = new Vector[]{rotateXZ(dir, wingAngle), rotateXZ(dir, -wingAngle)};

                for (int y = 0; y < pixelHeight; y++) {
                    for (int x = 0; x < pixelWidth; x++) {
                        float pixelRadius = perPixelWidth * (x + 0.5f);
                        ParticleEffect.ParticleColor color = getColorAt(x, y);
                        if (color != null) {
                            for (Vector rotated : vectors) {
                                ParticleEffect.REDSTONE.display(color, basePoint.clone().add(rotated.clone().multiply(pixelRadius)), PARTICLE_RANGE);
                            }
                        }
                    }
                    basePoint.add(0, perPixelHeight, 0);
                }


                tick += tickInterval;
                if (tick >= ticks) {
                    Hooks.FINAL_DAMAGE_RECEIVED_HOOK.unsubscribe(binding);
                    cancel();
                }
            }
        }, 0, tickInterval);

        return true;
    }

    public ParticleEffect.ParticleColor getColorAt(int x, int y) {
        if (pixels == null) return null;
        return pixels[x][y];
    }

    private void loadColors() {
        try {
            BufferedImage image = ImageIO.read(DivineBlessingSkill.class.getResourceAsStream("/divine_blessing_wings.bmp"));
            int height = image.getHeight();
            pixels = new ParticleEffect.ParticleColor[image.getWidth()][];
            for (int x = 0; x < image.getWidth(); x++) {
                pixels[x] = new ParticleEffect.ParticleColor[height];
                for (int y = 0; y < height; y++) {
                    Color color = new Color(image.getRGB(x, height - 1 - y));
                    if (color.getRed() > 0 || color.getGreen() > 0 || color.getBlue() > 0) {
                        pixels[x][y] = new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pixels = null;
        }
    }

    public Vector rotateXZ(Vector dir, double theta) {
        return new Vector(Math.cos(theta) * dir.getX() - Math.sin(theta) * dir.getZ(),
                dir.getY(),
                Math.sin(theta) * dir.getX() + Math.cos(theta) * dir.getZ());
    }

    public void heal(ArterionPlayer p, ArterionPlayer x, int heal, Map<ArterionPlayer, Integer> prev) {
        if (x.getHealth() <= 0) return;
        if (prev.containsKey(x)) {
            x.heal(prev.get(x));
        } else {
            prev.put(x, x.getClericHealManager().heal(this, heal, getMaxCooldown(p), p));
        }
    }

    @Override
    public Object[] getDescriptionValues(ArterionPlayer p) {
        return new Object[]{ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_DIVINE_BLESSING_DURATION.evaluateDouble(p) / 1000d};
    }

    @Override
    public void applyTo(ArterionPlayer p) {
        setSkillDataContainer(p, new SkillContainerData());
    }

    @Override
    public void removeFrom(ArterionPlayer p) {
        setSkillDataContainer(p, null);
    }

    @Override
    public boolean canBeCastInCurrentRegion(ArterionPlayer p) {
        return true;
    }

    @Override
    public long getActiveTime(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_CLERIC_DIVINE_BLESSING_DURATION.evaluateInt(p);
    }
}
