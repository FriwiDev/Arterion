package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.DungeonMobSpawner;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;
import org.bukkit.entity.EntityType;

public class MorgothFourthRoom extends MorgothDungeonMonsterPart {

    public MorgothFourthRoom(MorgothDungeonFight fight) {
        super(fight);
        registerMobSpawner(new DungeonMobSpawner(at(296, 83, 95), 5, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(248, 84, 119), 5, 3, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(287, 83, 143), 5, 3, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(266, 83, 96), 5, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(244, 83, 81), 5, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(229, 83, 73), 5, 3, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(267, 83, 75), 5, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(288, 83, 92), 5, 3, EntityType.SPIDER));
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 0) {
            //Labyrinth
            getFight().sendTranslation("dungeon.morgoth.room.fourth.welcome");
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return new MorgothFifthRoom(getFight());
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return isAnyPlayerBetween(team,
                at(318, 90, 74),
                at(309, 71, 50));
    }
}
