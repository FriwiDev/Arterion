package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.DungeonMobSpawner;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;
import org.bukkit.entity.EntityType;

public class MorgothThirdRoom extends MorgothDungeonMonsterPart {

    public MorgothThirdRoom(MorgothDungeonFight fight) {
        super(fight);
        registerMobSpawner(new DungeonMobSpawner(at(180, 107, 136), 5, 2, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(172, 107, 121), 6, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(212, 98, 136), 12, 2, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(222, 83, 133), 8, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(213, 98, 101), 10, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(207, 96, 121), 7, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(216, 105, 126), 6, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(223, 96, 143), 5, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(222, 87, 169), 4, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(248, 85, 153), 3, 3, EntityType.SKELETON));
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 0) {
            //Long way
            getFight().sendTranslation("dungeon.morgoth.room.third.welcome");
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return new MorgothFourthRoom(getFight());
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return isAnyPlayerBetween(team,
                at(255, 81, 138),
                at(274, 90, 122));
    }
}
