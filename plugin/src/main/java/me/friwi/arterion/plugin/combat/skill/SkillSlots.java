package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.card.InvalidSkillCard;
import me.friwi.arterion.plugin.combat.skill.card.NotHereCard;
import me.friwi.arterion.plugin.combat.skill.card.SkillUnlockCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.mod.packet.Packet06Objective;
import me.friwi.arterion.plugin.ui.mod.packet.Packet07SkillSlotData;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import me.friwi.arterion.plugin.world.item.SkillItem;
import net.badlion.timers.api.BadlionTimer;
import net.badlion.timers.api.BadlionTimerApi;
import net.badlion.timers.impl.BadlionTimerImpl;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkillSlots {
    public static final int EVENT_OBJECTIVE_PRIORITY = 1;
    public static final int GUILD_OBJECTIVE_PRIORITY = 2;
    Map<SkillSlotEnum, Object[]> update = new HashMap<>();
    private Material cooldownDisc = Material.RECORD_8;
    private ArterionPlayer player;
    private Skill[] skillSlotsActive = new Skill[SkillSlotEnum.values().length];
    private BadlionTimer[] activeTimers = new BadlionTimer[SkillSlotEnum.values().length + 1];
    private Skill skillSlotPassive;
    private SkillContainerData[] activeSkillData = new SkillContainerData[SkillSlotEnum.values().length];
    private SkillContainerData passiveSkillData = null;
    private Objective objective;
    private int priority = 0;
    //Performance
    private boolean[] skillDisksExisting = new boolean[SkillSlotEnum.values().length];
    private boolean allSkillsDiscsDropped = false;
    private boolean usesBadlion = true;
    private boolean usesMod = false;

    private long lastTnt = 0;
    private long lastObsiTnt = 0;

    public SkillSlots(ArterionPlayer player) {
        this.player = player;
        for (int i = 0; i < activeTimers.length; i++) {
            if (i == 0) {
                activeTimers[i] = BadlionTimerApi.getInstance().createTimeTimer("no objective", new ItemStack(Material.REDSTONE, 1), true, 1, TimeUnit.SECONDS);
            } else {
                activeTimers[i] = BadlionTimerApi.getInstance().createTimeTimer("Skill " + i, new ItemStack(SkillSlotEnum.values()[i - 1].getDiskMaterial(), 1), true, 1, TimeUnit.SECONDS);
                ((BadlionTimerImpl) activeTimers[i]).setCustomMaterial(SkillSlotEnum.values()[i - 1].getNmsMaterial());
            }
        }
        for (int i = 0; i < skillDisksExisting.length; i++) {
            skillDisksExisting[i] = true;
        }
    }

    //Add and remove skills, but do not reset cooldowns or effects of remaining ones
    public void updateSkillSlots() {
        this.updateSkillSlots(false);
    }

    //Add and remove skills, but do not reset cooldowns or effects of remaining ones
    public void updateSkillSlots(boolean levelup) {
        boolean updated = false;
        //Remove old skills
        for (int i = 0; i < skillSlotsActive.length; i++) {
            Skill s = skillSlotsActive[i];
            if (s != null) {
                if ((s.getBoundClass() != ClassEnum.NONE && s.getBoundClass() != this.player.getSelectedClass()) || ((ActiveSkill) s).getUnlockLevel() > this.player.getLevel()) {
                    s.removeFrom(this.player);
                    skillSlotsActive[i] = null;
                    activeSkillData[i] = null;
                    updated = true;
                }
            }
        }
        if (skillSlotPassive != null && skillSlotPassive.getBoundClass() != ClassEnum.NONE && skillSlotPassive.getBoundClass() != this.player.getSelectedClass()) {
            skillSlotPassive.removeFrom(this.player);
            skillSlotPassive = null;
            passiveSkillData = null;
            updated = true;
        }
        //Add new skills
        if (this.player.getSelectedClass() != null && this.player.getSelectedClass() != ClassEnum.NONE) {
            for (SkillEnum s : SkillEnum.values()) {
                Skill check = s.getSkill();
                if (check.getBoundClass() == ClassEnum.NONE || check.getBoundClass() == this.player.getSelectedClass()) {
                    if (check instanceof ActiveSkill) {
                        if (((ActiveSkill) check).getUnlockLevel() <= player.getLevel()) {
                            if (skillSlotsActive[((ActiveSkill) check).getSkillSlot().ordinal()] == null) {
                                skillSlotsActive[((ActiveSkill) check).getSkillSlot().ordinal()] = check;
                                check.applyTo(this.player);
                                if (levelup && this.player.getLevel() == ((ActiveSkill) check).getUnlockLevel()) {
                                    this.player.scheduleHotbarCard(new SkillUnlockCard(player, check));
                                }
                                ((ActiveSkill) check).updateToMod(player);
                            }
                        }
                    } else {
                        if (skillSlotPassive == null) {
                            skillSlotPassive = check;
                            check.applyTo(this.player);
                        }
                    }
                }
            }
            updated = true;
        }
        this.updateSkillDisks();
        if (updated) {
            reapplyBadlionTimers();
            if (usesMod()) {
                int i = 0;
                for (Skill s : skillSlotsActive) {
                    if (s != null) {
                        ((ActiveSkill) s).updateToMod(player);
                    } else {
                        Skill possibleSkill = null;
                        for (SkillEnum ps : SkillEnum.values()) {
                            if (ps.getSkill().getBoundClass() == player.getSelectedClass() && ps.getSkill() instanceof ActiveSkill) {
                                if (((ActiveSkill) ps.getSkill()).getSkillSlot().ordinal() == i) {
                                    possibleSkill = ps.getSkill();
                                    break;
                                }
                            }
                        }
                        ModConnection.sendModPacket(player, new Packet07SkillSlotData((byte) i, possibleSkill == null ? -1 : possibleSkill.getSkillType().ordinal(), false, possibleSkill == null ? "" : possibleSkill.getName(player), possibleSkill == null ? "" : ((ActiveSkill) possibleSkill).getDescriptionWithMana(player), 0, 0, SkillSlotEnum.values()[i].getARGB(), 0, 0, possibleSkill == null ? 0 : ((ActiveSkill) possibleSkill).getMana(player)));
                    }
                    i++;
                }
            }
        }
    }

    //Remove all skills, reset all data and effects, add all skills back
    public void resetSkillSlots() {
        //Remove all skills and reset
        for (int i = 0; i < skillSlotsActive.length; i++) {
            Skill s = skillSlotsActive[i];
            if (s != null) {
                s.removeFrom(this.player);
                skillSlotsActive[i] = null;
                activeSkillData[i] = null;
            }
        }
        if (skillSlotPassive != null) {
            skillSlotPassive.removeFrom(this.player);
            skillSlotPassive = null;
            passiveSkillData = null;
        }

        if (usesMod()) {
            for (SkillSlotEnum i : SkillSlotEnum.values()) {
                Skill possibleSkill = null;
                for (SkillEnum ps : SkillEnum.values()) {
                    if (ps.getSkill().getBoundClass() == player.getSelectedClass() && ps.getSkill() instanceof ActiveSkill) {
                        if (((ActiveSkill) ps.getSkill()).getSkillSlot() == i) {
                            possibleSkill = ps.getSkill();
                            break;
                        }
                    }
                }
                ModConnection.sendModPacket(player, new Packet07SkillSlotData((byte) i.ordinal(), possibleSkill == null ? -1 : possibleSkill.getSkillType().ordinal(), false, possibleSkill == null ? "" : possibleSkill.getName(player), possibleSkill == null ? "" : ((ActiveSkill) possibleSkill).getDescriptionWithMana(player), 0, 0, i.getARGB(), 0, 0, possibleSkill == null ? 0 : ((ActiveSkill) possibleSkill).getMana(player)));
            }
        }

        //Add all currently available skills back to the player
        this.updateSkillSlots();
    }

    public void updateSkillDisks() {
        update.clear();
        for (int i = 0; i < skillSlotsActive.length; i++) {
            Skill s = skillSlotsActive[i];
            if (s == null) {
                if (skillDisksExisting[i]) {
                    update.put(SkillSlotEnum.values()[i], new Object[]{player.getTranslation("skill.disc.none"), "", "", new String[0], 1});
                }
            } else if (skillDisksExisting[i] || usesBadlion) {
                List<String> des = s.getDescriptionWithLimit(player, "\2477", 30);
                String[] desc = des.toArray(new String[des.size()]);
                int cd = ((ActiveSkill) s).getCooldown(player);
                if (skillDisksExisting[i]) {
                    update.put(SkillSlotEnum.values()[i], new Object[]{player.getTranslation("skill.disc.ability", i + 1, ((ActiveSkill) s).getSkillSlot().getColor() + s.getName(player)), player.getTranslation("skill.disc.mana", ((ActiveSkill) s).getMana(player)), player.getTranslation("skill.disc.cooldown", ((ActiveSkill) s).getMaxCooldown(player) / 1000d), desc, cd});
                }
                if (usesBadlion) {
                    String post = "";
                    if (cd != 0) {
                        post = "\247r: " + TimeFormatUtil.formatSeconds(cd - 1);
                        ((BadlionTimerImpl) activeTimers[i + 1]).setCustomMaterial("record_stal");
                    } else {
                        ((BadlionTimerImpl) activeTimers[i + 1]).setCustomMaterial(SkillSlotEnum.values()[i].getNmsMaterial());
                    }
                    activeTimers[i + 1].setName((cd == 0 ? ((ActiveSkill) s).getSkillSlot().getColor() : "\2478") + s.getName(player) + post);
                    activeTimers[i + 1].setTime(cd == 0 ? 0 : (cd - 1), TimeUnit.SECONDS);
                }
            }
        }
        updateSlots();
        if (usesBadlion) {
            if (objective != null) {
                if (objective.isExpired()) {
                    this.setObjective(null, priority);
                } else {
                    activeTimers[0].setName(objective.getMessage(player));
                    activeTimers[0].setItem(objective.getItem());
                    ((BadlionTimerImpl) activeTimers[0]).setCustomMaterial(objective.getNmsMaterial());
                    activeTimers[0].setTime(0, TimeUnit.SECONDS);
                }
            }
            for (BadlionTimer timer : activeTimers) {
                ((BadlionTimerImpl) timer).tick();
            }
        }
    }

    public void castSkill(SkillSlotEnum skillSlot) {
        if (player.getBukkitPlayer().getHealth() <= 0) return;
        if (skillSlotsActive[skillSlot.ordinal()] != null) {
            castSkill(((ActiveSkill) skillSlotsActive[skillSlot.ordinal()]));
        } else {
            this.player.scheduleHotbarCard(new InvalidSkillCard(player));
        }
    }

    public void castSkill(String name) {
        name = name.replace("_", " ");
        if (player.getBukkitPlayer().getHealth() <= 0) return;
        for (Skill s : skillSlotsActive) {
            if (s != null) {
                if (s.getName(this.player).equalsIgnoreCase(name.trim())) {
                    castSkill(((ActiveSkill) s));
                    return;
                }
            }
        }
        //Skill not found
        this.player.scheduleHotbarCard(new InvalidSkillCard(player));
    }

    public void castSkill(SkillEnum skill) {
        if (player.getBukkitPlayer().getHealth() <= 0) return;
        for (Skill s : skillSlotsActive) {
            if (s != null) {
                if (s.getSkillType() == skill) {
                    castSkill(((ActiveSkill) s));
                    return;
                }
            }
        }
        //Skill not found
        this.player.scheduleHotbarCard(new InvalidSkillCard(player));
    }

    public void castSkill(int slot) {
        if (player.getBukkitPlayer().getHealth() <= 0) return;
        if (slot - 1 >= 0 && slot - 1 < skillSlotsActive.length && skillSlotsActive[slot - 1] != null) {
            castSkill(((ActiveSkill) skillSlotsActive[slot - 1]));
        } else {
            //Skill not found
            this.player.scheduleHotbarCard(new InvalidSkillCard(player));
        }
    }

    public void castSkill(ActiveSkill s) {
        if (s.canBeCastInCurrentRegion(player)) {
            s.attemptCast(player);
        } else {
            player.scheduleHotbarCard(new NotHereCard(player, s));
        }
    }

    public Skill getSkill(String name) {
        name = name.replace("_", " ");
        for (Skill s : skillSlotsActive) {
            if (s != null) {
                if (s.getName(this.player).equalsIgnoreCase(name.trim())) {
                    return s;
                }
            }
        }
        return null;
    }

    public void printSkills() {
        if (this.player.getSelectedClass() == ClassEnum.NONE || this.player.getSelectedClass() == null) {
            this.player.sendTranslation("command.skills.younotchosen");
            return;
        }
        this.player.openGui(new ItemGUI(player, player.getTranslation("command.skills.title"), () -> {
            ItemStack[] stacks = new ItemStack[18];
            if (skillSlotPassive != null) {
                stacks[4] = NamedItemUtil.create(Material.REDSTONE, player.getTranslation("command.skills.passive", skillSlotPassive.getName(this.player)), skillSlotPassive.getDescriptionWithLimit(this.player, "\2477", 50));
            }
            int ind = 9;
            for (SkillSlotEnum i : SkillSlotEnum.values()) {
                ActiveSkill possibleSkill = null;
                for (SkillEnum ps : SkillEnum.values()) {
                    if ((ps.getSkill().getBoundClass() == ClassEnum.NONE || ps.getSkill().getBoundClass() == player.getSelectedClass()) && ps.getSkill() instanceof ActiveSkill) {
                        if (((ActiveSkill) ps.getSkill()).getSkillSlot() == i) {
                            possibleSkill = (ActiveSkill) ps.getSkill();
                            break;
                        }
                    }
                }
                if (possibleSkill != null) {
                    List<String> dl = possibleSkill.getDescriptionWithLimit(player, "\2477", 30);
                    String[] desc = new String[dl.size()];
                    dl.toArray(desc);
                    SkillItem item = new SkillItem(player.getTranslation("skill.disc.ability", i.ordinal() + 1, (possibleSkill.getUnlockLevel() > player.getLevel() ? "\2478" : i.getColor()) + possibleSkill.getName(player)), player.getTranslation("skill.disc.mana", possibleSkill.getMana(player)), player.getTranslation("skill.disc.cooldown", possibleSkill.getMaxCooldown(player) / 1000d), desc,
                            i, possibleSkill.getUnlockLevel() > player.getLevel() ? cooldownDisc : i.getDiskMaterial());
                    stacks[ind] = item.toItemStack();
                }
                ind += 2;
            }
            return stacks;
        }, ((clickType, integer) -> {
        })));
    }

    public void updateSlots() {
        if (allSkillsDiscsDropped) return;
        boolean[] found = new boolean[SkillSlotEnum.values().length];
        for (int i = 0; i < player.getBukkitPlayer().getInventory().getContents().length; i++) {
            ItemStack stack = player.getBukkitPlayer().getInventory().getContents()[i];
            if (stack == null) continue;
            CustomItem item = CustomItemUtil.getCustomItem(stack);
            if (item.getType() == CustomItemType.SKILL) {
                SkillItem si = (SkillItem) item;
                Object[] data = update.get(si.getSkillSlot());
                found[si.getSkillSlot().ordinal()] = true;
                if (data != null) {
                    SkillItem ni = new SkillItem((String) data[0], (String) data[1], (String) data[2], (String[]) data[3], si.getSkillSlot(), ((int) data[4]) > 0 ? cooldownDisc : si.getSkillSlot().getDiskMaterial());
                    ItemStack stack2 = ni.toItemStack();
                    if (((int) data[4]) > 0) {
                        stack2.setAmount(((int) data[4]));
                    }
                    if (!stack.equals(stack2)) player.getBukkitPlayer().getInventory().setItem(i, stack2);
                }
            }
        }
        ItemStack stack = player.getBukkitPlayer().getItemOnCursor();
        if (stack != null) {
            CustomItem item = CustomItemUtil.getCustomItem(stack);
            if (item.getType() == CustomItemType.SKILL) {
                SkillItem si = (SkillItem) item;
                Object[] data = update.get(si.getSkillSlot());
                found[si.getSkillSlot().ordinal()] = true;
                if (data != null) {
                    SkillItem ni = new SkillItem((String) data[0], (String) data[1], (String) data[2], (String[]) data[3], si.getSkillSlot(), ((int) data[4]) > 0 ? cooldownDisc : si.getSkillSlot().getDiskMaterial());
                    ItemStack stack2 = ni.toItemStack();
                    if (((int) data[4]) > 0) {
                        stack2.setAmount(((int) data[4]));
                    }
                    if (!stack.equals(stack2)) player.getBukkitPlayer().setItemOnCursor(stack2);
                }
            }
        }
        allSkillsDiscsDropped = true;
        for (int i = 0; i < found.length; i++) {
            if (skillDisksExisting[i] || found[i]) {
                allSkillsDiscsDropped = false;
                break;
            }
        }
        for (int i = 0; i < found.length; i++) {
            skillDisksExisting[i] = found[i];
        }
    }

    public void giveSkillDisks() {
        removeSkillDisks();
        for (int i = 0; i < SkillSlotEnum.values().length; i++) {
            ItemStack stack = new SkillItem("Loading...", "", "", new String[0], SkillSlotEnum.values()[i], cooldownDisc).toItemStack();
            int preferedslot = i + 4;
            ItemStack existing = player.getBukkitPlayer().getInventory().getItem(preferedslot);
            if (existing == null || existing.getType() == Material.AIR)
                player.getBukkitPlayer().getInventory().setItem(preferedslot, stack);
            else player.getBukkitPlayer().getInventory().addItem(stack);
            this.skillDisksExisting[i] = true;
        }
        allSkillsDiscsDropped = false;
        updateSkillDisks();
    }

    public void removeSkillDisks() {
        for (int i = 0; i < player.getBukkitPlayer().getInventory().getContents().length; i++) {
            ItemStack stack = player.getBukkitPlayer().getInventory().getContents()[i];
            if (stack == null) continue;
            CustomItem item = CustomItemUtil.getCustomItem(stack);
            if (item.getType() == CustomItemType.SKILL) {
                player.getBukkitPlayer().getInventory().setItem(i, null);
            }
        }
        if (player.getBukkitPlayer().getOpenInventory() != null && player.getBukkitPlayer().getItemOnCursor() != null) {
            ItemStack stack = player.getBukkitPlayer().getItemOnCursor();
            if (stack != null) {
                CustomItem item = CustomItemUtil.getCustomItem(stack);
                if (item.getType() == CustomItemType.SKILL) {
                    player.getBukkitPlayer().setItemOnCursor(null);
                }
            }
        }
    }

    public boolean hasSkillDisks() {
        for (int i = 0; i < player.getBukkitPlayer().getInventory().getContents().length; i++) {
            ItemStack stack = player.getBukkitPlayer().getInventory().getContents()[i];
            if (stack == null) continue;
            CustomItem item = CustomItemUtil.getCustomItem(stack);
            if (item.getType() == CustomItemType.SKILL) {
                return true;
            }
        }
        if (player.getBukkitPlayer().getOpenInventory() != null && player.getBukkitPlayer().getItemOnCursor() != null) {
            ItemStack stack = player.getBukkitPlayer().getItemOnCursor();
            if (stack != null) {
                CustomItem item = CustomItemUtil.getCustomItem(stack);
                if (item.getType() == CustomItemType.SKILL) {
                    return true;
                }
            }
        }
        return false;
    }

    public SkillContainerData[] getActiveSkillData() {
        return activeSkillData;
    }

    public SkillContainerData getPassiveSkillData() {
        return passiveSkillData;
    }

    public void setPassiveSkillData(SkillContainerData passiveSkillData) {
        this.passiveSkillData = passiveSkillData;
    }

    public void reapplyBadlionTimers() {
        if (!usesBadlion) return;
        for (int i = 0; i < activeTimers.length; i++) {
            activeTimers[i].removeReceiver(player.getBukkitPlayer());
            if (i == 0) {
                if (objective != null) activeTimers[i].addReceiver(player.getBukkitPlayer());
            } else {
                if (skillSlotsActive[i - 1] != null) activeTimers[i].addReceiver(player.getBukkitPlayer());
            }
        }
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective, int priority) {
        if (this.priority > priority) return;
        this.objective = objective;
        this.priority = priority;
        reapplyBadlionTimers();
        if (usesMod()) {
            if (objective == null) {
                ModConnection.sendModPacket(player, new Packet06Objective(-1000, ""));
            } else {
                objective.sendToMod(this.player);
            }
        }
    }

    public void setAllOffCooldown() {
        for (SkillContainerData data : activeSkillData) {
            if (data != null) {
                data.setLastUsed(0);
            }
        }
        if (usesMod()) {
            for (Skill s : skillSlotsActive) {
                if (s != null) {
                    ((ActiveSkill) s).updateToMod(player);
                }
            }
        }
    }

    public void markAsModUser() {
        if (usesMod) return;
        usesBadlion = false;
        usesMod = true;
        activeTimers = null;
        this.resetSkillSlots();
        this.setObjective(this.getObjective(), this.priority);
        ArterionPlugin.getInstance().getTablistManager().updatePlayerListName(player);
    }

    public boolean usesMod() {
        return usesMod;
    }

    public long getLastTnt() {
        return lastTnt;
    }

    public void setLastTnt(long lastTnt) {
        this.lastTnt = lastTnt;
    }

    public long getLastObsiTnt() {
        return lastObsiTnt;
    }

    public void setLastObsiTnt(long lastObsiTnt) {
        this.lastObsiTnt = lastObsiTnt;
    }

    public Skill[] getSkillSlotsActive() {
        return skillSlotsActive;
    }
}
