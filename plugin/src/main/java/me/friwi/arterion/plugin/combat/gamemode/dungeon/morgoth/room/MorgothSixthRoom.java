package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.room;

import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonFight;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonMonsterPart;
import me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth.MorgothDungeonPart;
import me.friwi.arterion.plugin.combat.team.Team;

public class MorgothSixthRoom extends MorgothDungeonMonsterPart {
    public MorgothSixthRoom(MorgothDungeonFight fight) {
        super(fight);
    }

    @Override
    public void tick(MorgothDungeonFight fight, long tick) {
        super.tick(fight, tick);
        if (tick == 0) {
            //Boss room
            getFight().sendTranslation("dungeon.morgoth.room.sixth.welcome");
            getFight().spawnBoss();
        }
    }

    @Override
    public MorgothDungeonPart next() {
        return null;
    }

    @Override
    public boolean isGoalFulfilled(Team team) {
        return false; //Fight will end when morgoth is defeated
    }
}
