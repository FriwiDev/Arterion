package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public abstract class Skill<T extends SkillContainerData> {
    public static final long PARTICLE_RANGE = 48;
    public static final double PLAYER_MODEL_WIDTH_RADIUS = 0.299;
    public static final double PLAYER_MODEL_HEIGHT = 1.799;
    public static final double PLAYER_MODEL_WIDTH_RADIUS_SAFE = 0.4;
    public static final double PLAYER_MODEL_MISSING = 0.001;

    private SkillEnum skillType;
    private ClassEnum boundClass;

    public Skill(ClassEnum boundClass) {
        this.boundClass = boundClass;
    }

    public abstract Object[] getDescriptionValues(ArterionPlayer p);

    public abstract boolean isActive();

    public abstract void applyTo(ArterionPlayer p);

    public abstract void removeFrom(ArterionPlayer p);

    public abstract T getSkillDataContainer(ArterionPlayer p);

    public abstract void setSkillDataContainer(ArterionPlayer p, T container);

    public String getName(ArterionPlayer p) {
        return p.getTranslation("skill." + skillType.name().toLowerCase() + ".name");
    }

    public String getName(Language lang) {
        return lang.getTranslation("skill." + skillType.name().toLowerCase() + ".name").translate().getMessage();
    }

    public String getDescription(ArterionPlayer p) {
        return p.getTranslation("skill." + skillType.name().toLowerCase() + ".desc", this.getDescriptionValues(p));
    }

    public List<String> getDescriptionWithLimit(ArterionPlayer p, String prefix, int limit) {
        List<String> ret = new LinkedList<>();
        String[] desc = getDescription(p).split(" ");
        String s = "";
        for (String x : desc) {
            if (s.length() + x.length() < limit) s += x + " ";
            else {
                ret.add(prefix + s);
                s = "";
                s += x + " ";
            }
        }
        if (s.length() > 0) ret.add(prefix + s);
        return ret;
    }

    public SkillEnum getSkillType() {
        return skillType;
    }

    protected void setSkillType(SkillEnum skillType) {
        this.skillType = skillType;
    }

    public ClassEnum getBoundClass() {
        return boundClass;
    }

    public void printCastMessage(ArterionPlayer caster, Object target) {
        //Execute post skill hook
        Hooks.PLAYER_POST_SKILL_CAST_HOOK.execute(caster, this);

        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            String casterName = ap.getLanguage().translateObject(caster);
            if (ap != null && p.getWorld().equals(caster.getBukkitPlayer().getWorld()) && p.getLocation().distance(caster.getBukkitPlayer().getLocation()) < 120) {
                if (target == null) {
                    ap.sendTranslation("skill.withouttarget", casterName, getName(ap));
                } else {
                    ap.sendTranslation("skill.withtarget", casterName, getName(ap), ap.getLanguage().translateObject(target));
                }
            }
        }
        for (RecordingCreator r : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            String casterName = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(caster);
            if (r.isLocationRelevant(caster.getBukkitPlayer().getLocation())) {
                if (target == null) {
                    r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("skill.replay.withouttarget").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE))).getMessage());
                } else {
                    r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("skill.replay.withtarget").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE)), LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(target)).getMessage());
                }
            }
        }

        String casterName = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(caster);
        if (target == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("skill.withouttarget").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE))).getMessage());
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("skill.withtarget").translate(casterName, getName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE)), LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(target)).getMessage());
        }
    }
}
