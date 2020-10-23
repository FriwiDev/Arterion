package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.*;
import me.friwi.arterion.plugin.util.database.enums.*;
import me.friwi.arterion.plugin.util.formulas.PlayerJobLevelCalculator;
import me.friwi.arterion.website.WebApplication;
import me.friwi.arterion.website.langutils.EnumBlock;
import me.friwi.arterion.website.langutils.EnumEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class StatPlayerPageController {
    public static final int STAT_ELEMENTS = 7;
    public static final int PER_PAGE = 4;

    @Autowired
    private MessageSource messageSource;

    public static String playerListToString(Set<DatabasePlayer> players) {
        String build = "";
        int size = players.size();
        int i = 0;
        Iterator<DatabasePlayer> it = players.iterator();
        while (it.hasNext()) {
            build += it.next().getName();
            i++;
            if (i < size - 1) {
                build += ", ";
            } else if (i == size - 1) {
                build += " & ";
            }
        }
        return build;
    }

    @GetMapping("/stats/player/{u}")
    public String player(Locale locale, Model model, @PathVariable String u, @RequestParam(defaultValue = "guild_fights") String fights, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        model.addAttribute("fights", fights);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabasePlayer player = null;
        UUID uuid = null;
        try {
            uuid = UUID.fromString(u);
            player = db.find(DatabasePlayer.class, uuid);
        } catch (Exception e) {
            player = db.findOneByColumn(DatabasePlayer.class, "name", u);
        }
        if (player == null) {
            db.commit();
            return "redirect:/error";
        }
        uuid = player.getUuid();
        //Broad
        model.addAttribute("name", player.getName());
        model.addAttribute("face", "https://crafatar.com/avatars/" + player.getUuid() + "?size=64");
        model.addAttribute("rank", messageSource.getMessage("player.rank." + player.getRank().name().toLowerCase(), new Object[0], locale));
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(player.getJoined()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("active_since", time.split(" ")[0]);
        List<DatabaseStatComponent> pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.ONLINE_MINUTES, StatObjectType.PLAYER, uuid});
        long play_minutes = 0;
        if (pt.size() > 0) {
            play_minutes = pt.get(0).getValue();
        }
        String play_time = "";
        if (play_minutes == 0) {
            play_time = "0m";
        }
        if (play_minutes >= 24 * 60) {
            play_time += (play_minutes / (24 * 60)) + "d ";
            play_minutes %= 24 * 60;
        }
        if (play_minutes >= 60) {
            play_time += (play_minutes / (60)) + "h ";
            play_minutes %= 60;
        }
        if (play_minutes > 0) {
            play_time += play_minutes + "m";
        }
        model.addAttribute("play_time", play_time);
        List<DatabaseGuild> guilds = db.findAllByColumn(DatabaseGuild.class, new String[]{"leader", "deleted"}, new Object[]{player, -1});
        DatabaseGuild guild = null;
        String guild_role = "-";
        if (guilds.size() == 0) {
            guilds = db.findAllContainingWithMatch(DatabaseGuild.class, new String[]{"officers", "members"}, player, "deleted", -1);
            if (guilds.size() > 0) {
                guild = guilds.get(0);
                guild_role = messageSource.getMessage("page.stats.player.broad.guild_role_" + (guild.getOfficers().contains(player) ? "officer" : "member"), new Object[0], locale);
            }
        } else {
            guild = guilds.get(0);
            guild_role = messageSource.getMessage("page.stats.player.broad.guild_role_leader", new Object[0], locale);
        }
        model.addAttribute("guild", guild);
        model.addAttribute("guild_role", guild_role);
        //Progress
        if (player.getSelectedClass() == null || player.getSelectedClass() == ClassEnum.NONE) {
            model.addAttribute("class", messageSource.getMessage("player.class.none", new Object[0], locale));
            model.addAttribute("level", "-");
            model.addAttribute("xp_earned", "-");
        } else {
            model.addAttribute("class", messageSource.getMessage("player.class." + player.getSelectedClass().name().toLowerCase(), new Object[0], locale));
            if (player.getPrestigeLevel() == 0) {
                model.addAttribute("level", messageSource.getMessage("player.level", new Object[]{player.getLevel()}, locale));
            } else {
                model.addAttribute("level", messageSource.getMessage("player.plevel", new Object[]{player.getLevel(), player.getPrestigeLevel()}, locale));
            }
            pt = db.findAllByColumn(DatabaseStatComponent.class,
                    new String[]{"contextType", "statType", "objectType", "targetObject"},
                    new Object[]{StatContextType.GLOBAL_TOP, StatType.XP, StatObjectType.PLAYER, uuid});
            long xp = 0;
            if (pt.size() > 0) {
                xp = pt.get(0).getValue();
            }
            model.addAttribute("xp_earned", xp);
        }
        Integer woodworkerxp = player.getJobXp().get(JobEnum.WOODWORKER);
        if (woodworkerxp == null) woodworkerxp = 0;
        Integer farmerxp = player.getJobXp().get(JobEnum.FARMER);
        if (farmerxp == null) farmerxp = 0;
        Integer fisherxp = player.getJobXp().get(JobEnum.FISHER);
        if (fisherxp == null) fisherxp = 0;
        Integer minerxp = player.getJobXp().get(JobEnum.MINER);
        if (minerxp == null) minerxp = 0;
        model.addAttribute("woodworker_level", messageSource.getMessage("player.joblevel", new Object[]{PlayerJobLevelCalculator.getLevelFromXP(woodworkerxp)}, locale));
        model.addAttribute("farmer_level", messageSource.getMessage("player.joblevel", new Object[]{PlayerJobLevelCalculator.getLevelFromXP(farmerxp)}, locale));
        model.addAttribute("fisher_level", messageSource.getMessage("player.joblevel", new Object[]{PlayerJobLevelCalculator.getLevelFromXP(fisherxp)}, locale));
        model.addAttribute("miner_level", messageSource.getMessage("player.joblevel", new Object[]{PlayerJobLevelCalculator.getLevelFromXP(minerxp)}, locale));
        //Fight stats
        model.addAttribute("kills", player.getKills());
        model.addAttribute("deaths", player.getDeaths());
        float kd = (player.getKills() + 0f) / ((player.getDeaths() == 0 ? 1 : player.getDeaths()) + 0f);
        model.addAttribute("kd", String.format("%.2f", kd));
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.DAMAGE_DEALT_PLAYERS, StatObjectType.PLAYER, uuid});
        long x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("damage_players", x);
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.DAMAGE_DEALT_MOBS, StatObjectType.PLAYER, uuid});
        x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("damage_entities", x);
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.DAMAGE_RECEIVED, StatObjectType.PLAYER, uuid});
        x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("damage_self", x);
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.HEAL, StatObjectType.PLAYER, uuid});
        x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("heal", x);
        //Stats
        //Mined blocks
        pt = db.findAllWithMatchesWithSortAndLimit(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.DESTROYED_BLOCKS, StatObjectType.PLAYER, uuid},
                "value",
                false,
                0,
                STAT_ELEMENTS);
        Object[][] entries = new Object[pt.size()][];
        int i = 0;
        for (DatabaseStatComponent stat : pt) {
            int id = stat.getStatData() >> 8;
            int meta = stat.getStatData() & 0xFF;
            EnumBlock block = EnumBlock.byIdAndData(id, meta);
            if (block == null) block = EnumBlock.STONE;
            Object[] entry = new Object[4];
            entry[0] = block.getName(messageSource, locale);
            entry[1] = "items-28-" + id + "-" + meta;
            entry[2] = stat.getValue();
            entry[3] = "/stats/mined_block/" + stat.getStatData();
            entries[i] = entry;
            i++;
        }
        model.addAttribute("mined_blocks", entries);
        pt = db.findAllWithMatchesWithSortAndLimit(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.PLACED_BLOCKS, StatObjectType.PLAYER, uuid},
                "value",
                false,
                0,
                STAT_ELEMENTS);
        entries = new Object[pt.size()][];
        i = 0;
        for (DatabaseStatComponent stat : pt) {
            int id = stat.getStatData() >> 8;
            int meta = stat.getStatData() & 0xFF;
            EnumBlock block = EnumBlock.byIdAndData(id, meta);
            if (block == null) block = EnumBlock.STONE;
            Object[] entry = new Object[4];
            entry[0] = block.getName(messageSource, locale);
            entry[1] = "items-28-" + id + "-" + meta;
            entry[2] = stat.getValue();
            entry[3] = "/stats/placed_block/" + stat.getStatData();
            entries[i] = entry;
            i++;
        }
        model.addAttribute("placed_blocks", entries);
        pt = db.findAllWithMatchesWithSortAndLimit(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.MOB_KILLS, StatObjectType.PLAYER, uuid},
                "value",
                false,
                0,
                STAT_ELEMENTS);
        entries = new Object[pt.size()][];
        i = 0;
        for (DatabaseStatComponent stat : pt) {
            int id = stat.getStatData();
            EnumEntity entity = EnumEntity.byId(id);
            if (entity == null) entity = EnumEntity.CHICKEN;
            Object[] entry = new Object[4];
            entry[0] = entity.getName(messageSource, locale);
            entry[1] = "entities-28-" + id;
            entry[2] = stat.getValue();
            entry[3] = "/stats/killed_entity/" + stat.getStatData();
            entries[i] = entry;
            i++;
        }
        model.addAttribute("killed_entities", entries);

        //Fights
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        long count = 0;
        TraverseableList content = new TraverseableList(begin + 1);
        if (fights.equals("guild_fights")) {
            count = db.countAllContaining(DatabaseGuildFight.class, new String[]{"playersAttacking", "playersDefending"}, player);
            List<DatabaseGuildFight> process = db.findAllContainingWithSortAndLimit(DatabaseGuildFight.class, new String[]{"playersAttacking", "playersDefending"}, player, "timeBegin", false, begin, limit);
            for (DatabaseGuildFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getWinner() != null) {
                    if (fight.getAttacker().equals(fight.getWinner()) && fight.getPlayersAttacking().contains(player)) {
                        win = messageSource.getMessage("trophy", new Object[0], locale);
                    } else if (fight.getDefender().equals(fight.getWinner()) && fight.getPlayersDefending().contains(player)) {
                        win = messageSource.getMessage("trophy", new Object[0], locale);
                    }
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/guild_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.guild_fight", new Object[]{fight.getAttacker().getName(), fight.getDefender().getName()}, locale), win);
            }
        } else if (fights.equals("artefact_fights")) {
            count = db.countAllContainingWithNotNull(DatabaseArtefactFight.class, new String[]{"playersParticipating"}, player, "winner");
            List<DatabaseArtefactFight> process = db.findAllContainingWithNotNullWithSortAndLimit(DatabaseArtefactFight.class, new String[]{"playersParticipating"}, player, "winner", "timeBegin", false, begin, limit);
            for (DatabaseArtefactFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getPlayersWinning().contains(player)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/artefact_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.artefact_fight", new Object[]{fight.getWinner().getName()}, locale), win);
            }
        } else if (fights.equals("capture_point_fights")) {
            count = db.countAllContainingWithNotNull(DatabaseCapturePointFight.class, new String[]{"playersParticipating"}, player, "winner");
            List<DatabaseCapturePointFight> process = db.findAllContainingWithNotNullWithSortAndLimit(DatabaseCapturePointFight.class, new String[]{"playersParticipating"}, player, "winner", "timeEnd", false, begin, limit);
            for (DatabaseCapturePointFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getPlayersWinning().contains(player)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/capture_point_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.capture_point_fight." + fight.getPoint(), new Object[]{fight.getWinner().getName()}, locale), win);
            }
        } else if (fights.equals("morgoth_dungeon_fights")) {
            count = db.countAllContaining(DatabaseMorgothDungeonFight.class, new String[]{"playersParticipating"}, player);
            List<DatabaseMorgothDungeonFight> process = db.findAllContainingWithSortAndLimit(DatabaseMorgothDungeonFight.class, new String[]{"playersParticipating"}, player, "timeBegin", false, begin, limit);
            for (DatabaseMorgothDungeonFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getPlayersWinning().contains(player)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/morgoth_dungeon_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.morgoth_dungeon_fight", new Object[]{playerListToString(fight.getPlayersParticipating())}, locale), win);
            }
        } else if (fights.equals("arena_fights")) {
            count = db.countAllContaining(DatabaseArenaFight.class, new String[]{"playersTeamOne", "playersTeamTwo"}, player);
            List<DatabaseArenaFight> process = db.findAllContainingWithSortAndLimit(DatabaseArenaFight.class, new String[]{"playersTeamOne", "playersTeamTwo"}, player, "timeBegin", false, begin, limit);
            for (DatabaseArenaFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getWinner() == 1 && fight.getPlayersTeamOne().contains(player)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                } else if (fight.getWinner() == 2 && fight.getPlayersTeamTwo().contains(player)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/arena_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.arena_fight", new Object[]{playerListToString(fight.getPlayersTeamOne()), playerListToString(fight.getPlayersTeamTwo())}, locale), win);
            }
        }

        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        model.addAttribute("sort", "time");
        model.addAttribute("order", "desc");
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        db.commit();
        return "stats/player";
    }
}
