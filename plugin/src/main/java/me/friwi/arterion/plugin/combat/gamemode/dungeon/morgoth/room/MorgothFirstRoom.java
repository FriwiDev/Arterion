package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.DungeonMobSpawner;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

public class MorgothFirstRoom extends MorgothDungeonMonsterPart {

    public MorgothFirstRoom(MorgothDungeonFight fight) {
        super(fight);
        registerMobSpawner(new DungeonMobSpawner(at(106, 129, 237), 10, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(106, 129, 237), 10, 10, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(123, 116, 206), 10, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(123, 116, 206), 10, 10, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(82, 118, 173), 10, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(82, 118, 173), 10, 10, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(130, 112, 196), 10, 10, EntityType.SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(130, 112, 196), 10, 10, EntityType.CAVE_SPIDER));
        registerMobSpawner(new DungeonMobSpawner(at(99, 125, 230), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(114, 120, 222), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(109, 116, 204), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(91, 114, 192), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(91, 111, 172), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(112, 110, 177), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(122, 108, 185), 3, 5, EntityType.SKELETON));
        registerMobSpawner(new DungeonMobSpawner(at(139, 106, 180), 3, 5, EntityType.SKELETON));
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 3 * 20) {
            getFight().sendTranslation("dungeon.morgoth.room.first.intro1");
        } else if (tick == 6 * 20) {
            getFight().sendTranslation("dungeon.morgoth.room.first.intro2");
        } else if (tick == 9 * 20) {
            getFight().sendTranslation("dungeon.morgoth.room.first.intro3", getFight().getTeam().getMembers().size());
        } else if (tick == 10 * 20) {
            //Open gate
            for (int z = 0; z <= 2; z++) {
                for (int y = 0; y <= 2; y++) {
                    getFight().getWorld().getBlockAt(81, 132 + y, 231 + z).setType(Material.AIR);
                }
            }
            getFight().getWorld().playSound(at(81, 132, 232), Sound.DOOR_OPEN, 1f, 1f);
            getFight().sendTranslation("dungeon.morgoth.room.first.welcome");
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return new MorgothSecondRoom(getFight());
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return isAnyPlayerBetween(team,
                at(140, 106, 183),
                at(150, 110, 177));
    }
}
