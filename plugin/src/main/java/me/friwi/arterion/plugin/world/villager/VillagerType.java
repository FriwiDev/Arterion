package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Villager;

public enum VillagerType {
    BANK(Villager.Profession.LIBRARIAN, new BankVillagerInteractHandler()),
    PRIVATE_CLAIM(Villager.Profession.BLACKSMITH, new PrivateClaimVillagerInteractHandler()),
    GUILD(Villager.Profession.BLACKSMITH, new GuildVillagerInteractHandler()),
    CLASS_PALADIN(Villager.Profession.PRIEST, new ClassVillagerInteractHandler(ClassEnum.PALADIN)),
    CLASS_BARBAR(Villager.Profession.BUTCHER, new ClassVillagerInteractHandler(ClassEnum.BARBAR)),
    CLASS_SHADOWRUNNER(Villager.Profession.BUTCHER, new ClassVillagerInteractHandler(ClassEnum.SHADOWRUNNER)),
    CLASS_FORESTRUNNER(Villager.Profession.FARMER, new ClassVillagerInteractHandler(ClassEnum.FORESTRUNNER)),
    CLASS_MAGE(Villager.Profession.LIBRARIAN, new ClassVillagerInteractHandler(ClassEnum.MAGE)),
    CLASS_CLERIC(Villager.Profession.PRIEST, new ClassVillagerInteractHandler(ClassEnum.CLERIC)),
    BLACKSMITH(Villager.Profession.BLACKSMITH, new BlacksmithVillagerInteractHandler()),
    SIEGE_SHOP(Villager.Profession.BLACKSMITH, new SiegeItemShopVillagerInteractHandler()),
    BLACK_MARKET_SHOP(Villager.Profession.BLACKSMITH, new BlackMarketShopVillagerInteractHandler()),

    CLASS_PALADIN_TP(Villager.Profession.PRIEST, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), 28.5, 83, 21.5, 0, 0))),
    CLASS_BARBAR_TP(Villager.Profession.BUTCHER, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), -54.5, 55, -65.5, 180, 0))),
    CLASS_SHADOWRUNNER_TP(Villager.Profession.BUTCHER, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), -23.5, 55, -32.5, -45, 0))),
    CLASS_FORESTRUNNER_TP(Villager.Profession.FARMER, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), -24.5, 87, -51.5, 135, 0))),
    CLASS_MAGE_TP(Villager.Profession.LIBRARIAN, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), -74.5, 99, -31.5, -90, 0))),
    CLASS_CLERIC_TP(Villager.Profession.PRIEST, new TeleportVillagerInteractHandler(new Location(Bukkit.getWorlds().get(0), -24.5, 90, 15.5, 0, 0))),

    QUEST(Villager.Profession.BLACKSMITH, new QuestVillagerInteractHandler());

    private Villager.Profession profession;
    private VillagerInteractHandler handler;

    VillagerType(Villager.Profession profession, VillagerInteractHandler handler) {
        this.profession = profession;
        this.handler = handler;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public VillagerInteractHandler getHandler() {
        return handler;
    }

    public String getCustomName() {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("villager." + name().toLowerCase()).translate().getMessage();
    }
}
