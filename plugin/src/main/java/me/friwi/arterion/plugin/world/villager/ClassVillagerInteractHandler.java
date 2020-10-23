package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.ConfirmItemGUI;

public class ClassVillagerInteractHandler implements VillagerInteractHandler {
    private ClassEnum clasz;

    public ClassVillagerInteractHandler(ClassEnum clasz) {
        this.clasz = clasz;
    }

    @Override
    public void handleInteract(ArterionPlayer ep) {
        if (ep.getSelectedClass() == clasz) {
            ep.sendTranslation("class.change.alreadyselected");
            return;
        }
        if (ep.getGuild() != null && ep.getGuild().getLocalFight() != null && ep.getGuild().getLocalFight().getClassChangers().contains(ep.getBukkitPlayer().getUniqueId())) {
            ep.sendTranslation("class.change.inguildfight");
            return;
        }
        ep.openGui(new ConfirmItemGUI(ep, ep.getTranslation("gui.class.choose", ep.getTranslation("class." + clasz.name().toLowerCase())), () -> {
            //Yes
            if (ep.getGuild() != null && ep.getGuild().getLocalFight() != null && ep.getGuild().getLocalFight().getClassChangers().contains(ep.getBukkitPlayer().getUniqueId())) {
                ep.sendTranslation("class.change.inguildfight");
                return;
            }
            int truce = ArterionPlugin.getInstance().getFormulaManager().PLAYER_CLASS_FREECHANGE.evaluateInt();
            int price = ArterionPlugin.getInstance().getFormulaManager().PLAYER_CLASS_PRICECHANGE.evaluateInt();
            if (ep.getPersistenceHolder().getJoined() + truce > System.currentTimeMillis()) {
                price = 0;
                ep.sendTranslation("class.change.free", truce / 1000 / 60 / 60);
            }
            if (ep.getSelectedClass() == ClassEnum.NONE || ep.getSelectedClass() == null) {
                price = 0;
            }
            int finalPrice = price;
            ep.getBagMoneyBearer().addMoney(-price, success -> {
                if (!success) {
                    ep.sendTranslation("class.change.nogold", finalPrice / 100f);
                    return;
                }
                if (ep.getGuild() != null && ep.getGuild().getLocalFight() != null && ep.getGuild().getLocalFight().getClassChangers().contains(ep.getBukkitPlayer().getUniqueId())) {
                    ep.sendTranslation("class.change.inguildfight");
                    ep.getBagMoneyBearer().addMoney(finalPrice, s -> {
                    });
                    return;
                }
                ep.setSelectedClass(clasz, succ -> {
                    if (succ) {
                        if (ep.getGuild() != null && ep.getGuild().getLocalFight() != null) {
                            ep.getGuild().getLocalFight().getClassChangers().add(ep.getBukkitPlayer().getUniqueId());
                        }
                        ep.sendTranslation("class.change.success", ep.getTranslation("class." + clasz.name().toLowerCase()));
                    } else {
                        ep.sendTranslation("class.change.dberror");
                        ep.getBagMoneyBearer().addMoney(finalPrice, s -> {
                        });
                    }
                });
            });
        }, () -> {
            //No
            ep.closeGui();
        }));
    }
}
