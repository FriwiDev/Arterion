package me.friwi.arterion.plugin.combat.gamemode.artefact;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;

public class ArtefactCarrier {
    private ArterionPlayer carrier;

    public ArtefactCarrier(ArterionPlayer carrier) {
        this.carrier = carrier;
        this.playEffects();
    }

    public ArterionPlayer getCarrier() {
        return carrier;
    }

    private void playEffects() {
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (carrier == null || !carrier.isArtefactCarrier()) {
                    cancel();
                    return;
                }
                ParticleEffect.SPELL_WITCH.display(0.5f, 0.5f, 0.5f, 0, 10, carrier.getBukkitPlayer().getLocation(), Skill.PARTICLE_RANGE);
            }
        }, 5, 5);
    }

    public void onDie() {
        carrier.setArtefactCarrier(null);
        carrier = null;
        Artefact.reset();
        Artefact.doReplayEvent();
    }
}
