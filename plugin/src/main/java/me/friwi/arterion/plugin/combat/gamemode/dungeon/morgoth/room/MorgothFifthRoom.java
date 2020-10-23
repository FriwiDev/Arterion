package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.DungeonMobSpawner;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;
import org.bukkit.entity.EntityType;

public class MorgothFifthRoom extends MorgothDungeonMonsterPart {

    public MorgothFifthRoom(MorgothDungeonFight fight) {
        super(fight);
        registerMobSpawner(new DungeonMobSpawner(at(291, 76, 63), 7, 4, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(223, 65, 73), 7, 3, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(215, 63, 91), 6, 2, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(261, 67, 89), 8, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(240, 75, 128), 6, 3, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(166, 55, 189), 5, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(143, 48, 167), 4, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(122, 46, 192), 7, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(175, 45, 173), 5, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(309, 76, 62), 7, 4, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(253, 70, 61), 7, 4, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(243, 65, 92), 7, 4, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(252, 67, 113), 2, 3, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(252, 67, 113), 7, 6, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(221, 69, 134), 7, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(197, 56, 116), 7, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(178, 52, 150), 7, 4, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(178, 52, 150), 2, 2, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(152, 51, 169), 9, 4, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(152, 51, 169), 3, 4, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(130, 46, 190), 7, 4, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(130, 46, 190), 3, 4, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(106, 59, 195), 6, 4, EntityType.SPIDER));
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 0) {
            //Long way to boss room
            getFight().sendTranslation("dungeon.morgoth.room.fifth.welcome");
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return new MorgothSixthRoom(getFight());
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return isAnyPlayerBetween(team,
                at(113, 60, 186),
                at(106, 71, 203));
    }
}
