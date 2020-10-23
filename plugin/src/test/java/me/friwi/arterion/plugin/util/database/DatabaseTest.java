package me.friwi.arterion.plugin.util.database;

import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuildFight;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;

import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseTest {
    public static void main(String args[]) {
        //Test two times to emulate reload
        for (int i = 0; i < 2; i++) {
            HibernateUtil.setup();
            Database db = new Database();
            db.beginTransaction();


            long cd = 30000 * 60 * 60 * 1000;
            DatabaseGuild attacker = db.find(DatabaseGuild.class, UUID.fromString("11d9c032-61fa-4cb5-8cbd-ac0e3a706784"));
            DatabaseGuild defender = db.find(DatabaseGuild.class, UUID.fromString("5f8c71a6-f9ac-4ed7-a9a1-875f6cb6ad57"));
            List<DatabaseGuildFight> fights = db.findAllByCriteria(DatabaseGuildFight.class, (query, builder) -> {
                        Root root = query.from(DatabaseGuildFight.class);
                        query
                                .select(root)
                                .where(
                                        builder.equal(root.get("attacker"), attacker),
                                        builder.equal(root.get("defender"), defender),
                                        builder.gt(root.get("timeBegin"), System.currentTimeMillis() - cd)
                                );
                    }
            );
            System.out.println(Arrays.toString(fights.toArray()));
            /*DatabasePlayer player = new DatabasePlayer(UUID.randomUUID(), "Friwi", "de_DE", Rank.ADMIN, "unknown", 0, 0, 0, 0, ClassEnum.NONE, 0, 0, 0, 0, null, 0, 0, 0, null, false, null, 0, 0, 0, 0, 0, 0, new HashSet<>(), 10, 0);
            db.save(player);
            DatabasePlayer player1 = new DatabasePlayer(UUID.randomUUID(), "Friwi1", "de_DE", Rank.ADMIN, "unknown", 0, 0, 0, 0, ClassEnum.NONE, 0, 0, 0, 0, null, 0, 0, 0, null, false, null, 0, 0, 0, 0, 0, 0, new HashSet<>(), 20, 0);
            db.save(player1);
            DatabasePlayer player2 = new DatabasePlayer(UUID.randomUUID(), "Friwi2", "de_DE", Rank.ADMIN, "unknown", 0, 0, 0, 0, ClassEnum.NONE, 0, 0, 0, 0, null, 0, 0, 0, null, false, null, 0, 0, 0, 0, 0, 0, new HashSet<>(), 15, 0);
            db.save(player2);
            player.getClassXp().put(ClassEnum.BARBAR, 20000);
            db.save(player);
            System.out.println(db.findOneByColumn(DatabasePlayer.class, "name", "Friwi").getClassXp().get(ClassEnum.BARBAR));
            DatabaseGuild guild = new DatabaseGuild("Arterion", "ART", 0, -1, -1, 0, 0, false, false, null, 0, 0, 0, 0, player, new HashSet<>(), new HashSet<>(), 0, false, new HashMap<>(), 0);
            db.save(guild);
            DatabaseGuild guild2 = new DatabaseGuild("Arterion1", "ARD", 0, -1, -1, 0, 0, false, false, null, 0, 0, 0, 0, player, new HashSet<>(), new HashSet<>(), 0, false, new HashMap<>(), 0);
            db.save(guild2);
            db.commit();
            db.beginTransaction();
            guild.getMembers().add(player1);
            guild.getMembers().add(player2);
            guild.getOfficers().add(player1);
            guild.getOfficers().add(player2);
            guild2.getMembers().add(player1);
            guild2.getMembers().add(player2);
            guild2.getOfficers().add(player1);
            guild2.getOfficers().add(player2);
            db.save(guild);
            db.save(guild2);
            Set<DatabasePlayer> s = new HashSet<>();
            s.add(player);
            Set<DatabasePlayer> s1 = new HashSet<>();
            s1.add(player);
            DatabaseGuildFight fight = new DatabaseGuildFight(0, 0, guild, guild2, null, s, s1, null);
            db.save(fight);
            db.commit();
            db.beginTransaction();
            System.out.println(db.findOneByColumn(DatabasePlayer.class, "name", "Friwi"));
            System.out.println(Arrays.toString(db.findAll(DatabasePlayer.class).toArray()));
            Object[] objs = db.findAllSortByColumnDescWithLimit(DatabasePlayer.class, "kills", 50).toArray();
            for (Object obj : objs) {
                if (obj instanceof DatabasePlayer) {
                    System.out.println(" -> " + ((DatabasePlayer) obj).getName() + " - Kills: " + ((DatabasePlayer) obj).getKills());
                }
            }
            System.out.println(Arrays.toString(db.findAll(DatabaseGuild.class).toArray()));
            System.out.println(fight);
            System.out.println(Arrays.toString(db.findAll(DatabaseGuildFight.class).toArray()));
            List<DatabaseGuildFight> fights = db.findAllByCriteria(DatabaseGuildFight.class, (query, builder) -> query
                    .select(query.from(DatabaseGuildFight.class))
                    .where(builder.and(
                            builder.equal(query.from(DatabaseGuildFight.class).get("attacker"), guild)),
                            builder.equal(query.from(DatabaseGuildFight.class).get("defender"), guild),
                            builder.gt(query.from(DatabaseGuildFight.class).get("timeBegin"), -1)
                    )
            );
            System.out.println(Arrays.toString(fights.toArray()));
            Set<DatabasePlayer> s2 = new HashSet<>();
            s2.add(player);
            Set<DatabasePlayer> s3 = new HashSet<>();
            s3.add(player);
            DatabaseGuildFight fight2 = new DatabaseGuildFight(0, 0, guild, guild, null, s2, s3, null);
            db.save(fight2);*/
            List<DatabaseStatComponent> existing = db.findAllByColumn(DatabaseStatComponent.class,
                    new String[]{
                            "contextType", "targetContext", "objectType", "targetObject", "targetObjectParty", "statType", "timeSlot"
                    }, new Object[]{
                            StatContextType.GLOBAL, null, StatObjectType.PLAYER, UUID.fromString("cdd2f737-5d4a-43d1-9c70-63f1931b53a0"), null,
                            StatType.DESTROYED_BLOCKS, 443413
                    });
            System.out.println(Arrays.toString(existing.toArray()));
            db.commit();
            HibernateUtil.shutdown();
        }
    }
}
