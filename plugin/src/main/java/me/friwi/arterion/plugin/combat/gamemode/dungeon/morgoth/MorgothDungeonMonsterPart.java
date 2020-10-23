package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import java.util.LinkedList;
import java.util.List;

public abstract class MorgothDungeonMonsterPart extends MorgothDungeonPart {
    private List<DungeonMobSpawner> mobSpawnerList = new LinkedList<>();

    public MorgothDungeonMonsterPart(MorgothDungeonFight fight) {
        super(fight);
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        for (DungeonMobSpawner mobSpawner : mobSpawnerList) mobSpawner.tick(fight, tick);
    }

    public void registerMobSpawner(DungeonMobSpawner mobSpawner) {
        mobSpawnerList.add(mobSpawner);
    }
}
