package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.DungeonMobSpawner;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;
import org.bukkit.entity.EntityType;

public class MorgothSecondRoom extends MorgothDungeonMonsterPart {

    public MorgothSecondRoom(MorgothDungeonFight fight) {
        super(fight);
        registerMobSpawner(new DungeonMobSpawner(at(161, 109, 164), 5, 7, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(161, 109, 164), 5, 7, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(149, 106, 186), 5, 7, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(149, 106, 186), 5, 7, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(164, 98, 185), 5, 7, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(164, 98, 185), 5, 7, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(187, 99, 173), 5, 7, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(187, 99, 173), 5, 7, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(191, 112, 172), 5, 7, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(191, 112, 172), 5, 7, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(175, 108, 178), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(183, 98, 157), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(175, 98, 164), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(165, 98, 172), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(162, 99, 180), 3, 5, EntityType.SKELETON));

    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 0) {
            //First jump n run
            getFight().sendTranslation("dungeon.morgoth.room.second.welcome");
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return new MorgothThirdRoom(getFight());
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return isAnyPlayerBetween(team,
                at(189, 107, 148),
                at(177, 114, 141));
    }
}
