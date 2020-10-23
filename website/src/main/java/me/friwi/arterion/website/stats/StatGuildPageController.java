package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.*;
import me.friwi.arterion.plugin.util.database.enums.*;
import me.friwi.arterion.website.WebApplication;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class StatGuildPageController {
    public static final int STAT_ELEMENTS = 7;
    public static final int PER_PAGE = 4;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats/guild/{uuid}")
    public String guild(Locale locale, Model model, @PathVariable UUID uuid, @RequestParam(defaultValue = "guild_fights") String fights, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        model.addAttribute("fights", fights);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabaseGuild guild = db.find(DatabaseGuild.class, uuid);
        if (guild == null) {
            db.commit();
            return "redirect:/error";
        }
        //Deleted
        if (guild.getDeleted() != -1) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(guild.getDeleted()), WebApplication.SERVER_TIME_ZONE);
            String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            model.addAttribute("deleted", messageSource.getMessage("page.stats.guild.deleted", new Object[]{time.split(" ")[0]}, locale));
        } else {
            model.addAttribute("deleted", null);
        }
        //Broad
        model.addAttribute("name", guild.getName());
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(guild.getCreated()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("active_since", time.split(" ")[0]);
        List<DatabaseStatComponent> pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.GUILD_ONLINE_MINUTES, StatObjectType.GUILD, uuid});
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
        model.addAttribute("vault", String.format("%.2f", guild.getGold() / 100f));
        model.addAttribute("leader", guild.getLeader());
        //Progress
        model.addAttribute("vault_prog", messageSource.getMessage("page.stats.guild.progress.entry", new Object[]{
                guild.getGuildUpgradeLevel().getOrDefault(GuildUpgradeEnum.VAULT, GuildUpgradeLevel.LEVEL_1).ordinal() + 1,
                GuildUpgradeEnum.VAULT.getMaxLevel().ordinal() + 1
        }, locale));
        model.addAttribute("officer_prog", messageSource.getMessage("page.stats.guild.progress.entry", new Object[]{
                guild.getGuildUpgradeLevel().getOrDefault(GuildUpgradeEnum.OFFICER, GuildUpgradeLevel.LEVEL_1).ordinal() + 1,
                GuildUpgradeEnum.OFFICER.getMaxLevel().ordinal() + 1
        }, locale));
        model.addAttribute("claim_prog", messageSource.getMessage("page.stats.guild.progress.entry", new Object[]{
                guild.getGuildUpgradeLevel().getOrDefault(GuildUpgradeEnum.REGION, GuildUpgradeLevel.LEVEL_1).ordinal() + 1,
                GuildUpgradeEnum.REGION.getMaxLevel().ordinal() + 1
        }, locale));
        model.addAttribute("kills_prog", guild.getClanKills());
        //Fight Stats
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.CLAN_KILLS, StatObjectType.GUILD, uuid});
        long kills = 0;
        if (pt.size() > 0) {
            kills = pt.get(0).getValue();
        }
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.CLAN_DEATHS, StatObjectType.GUILD, uuid});
        long deaths = 0;
        if (pt.size() > 0) {
            deaths = pt.get(0).getValue();
        }
        model.addAttribute("kills", kills);
        model.addAttribute("deaths", deaths);
        model.addAttribute("kd", String.format("%.2f", (kills + 0f) / ((deaths == 0 ? 1 : deaths) + 0f)));
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.GFIGHT_ATTACK_WINS, StatObjectType.GUILD, uuid});
        long win1 = 0;
        if (pt.size() > 0) {
            win1 = pt.get(0).getValue();
        }
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.GFIGHT_ATTACK_LOSS, StatObjectType.GUILD, uuid});
        long loss = 0;
        if (pt.size() > 0) {
            loss = pt.get(0).getValue();
        }
        model.addAttribute("gfight_offense", win1 + messageSource.getMessage("trophy", new Object[]{}, locale) + "/" + loss + messageSource.getMessage("cross", new Object[]{}, locale));
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.GFIGHT_DEFENSE_WINS, StatObjectType.GUILD, uuid});
        win1 = 0;
        if (pt.size() > 0) {
            win1 = pt.get(0).getValue();
        }
        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.GFIGHT_DEFENSE_LOSS, StatObjectType.GUILD, uuid});
        loss = 0;
        if (pt.size() > 0) {
            loss = pt.get(0).getValue();
        }
        model.addAttribute("gfight_defense", win1 + messageSource.getMessage("trophy", new Object[]{}, locale) + "/" + loss + messageSource.getMessage("cross", new Object[]{}, locale));

        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.CAPTURE_POINT_TAKEN, StatObjectType.GUILD, uuid});
        long x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("caps", x + messageSource.getMessage("dart", new Object[]{}, locale));

        pt = db.findAllByColumn(DatabaseStatComponent.class,
                new String[]{"contextType", "statType", "objectType", "targetObject"},
                new Object[]{StatContextType.GLOBAL_TOP, StatType.ARTEFACT_HOURS, StatObjectType.GUILD, uuid});
        x = 0;
        if (pt.size() > 0) {
            x = pt.get(0).getValue();
        }
        model.addAttribute("artefact_hours", x + "h");

        //Players
        model.addAttribute("officers", guild.getOfficers());
        model.addAttribute("members", guild.getMembers());

        //Fights
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        long count = 0;
        TraverseableList content = new TraverseableList(begin + 1);
        if (fights.equals("guild_fights")) {
            count = db.countAllConjunction(DatabaseGuildFight.class, new String[]{"attacker", "defender"}, new Object[]{guild, guild});
            List<DatabaseGuildFight> process = db.findAllConjunctionWithSortAndLimit(DatabaseGuildFight.class, new String[]{"attacker", "defender"}, new Object[]{guild, guild}, "timeBegin", false, begin, limit);
            for (DatabaseGuildFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getWinner() != null && fight.getWinner().equals(guild)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/guild_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.guild_fight", new Object[]{fight.getAttacker().getName(), fight.getDefender().getName()}, locale), win);
            }
        } else if (fights.equals("artefact_fights")) {
            count = db.countAllContainingWithNotNull(DatabaseArtefactFight.class, new String[]{"participating"}, guild, "winner");
            List<DatabaseArtefactFight> process = db.findAllContainingWithNotNullWithSortAndLimit(DatabaseArtefactFight.class, new String[]{"participating"}, guild, "winner", "timeBegin", false, begin, limit);
            for (DatabaseArtefactFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getWinner() != null && fight.getWinner().equals(guild)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/artefact_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.artefact_fight", new Object[]{fight.getWinner().getName()}, locale), win);
            }
        } else if (fights.equals("capture_point_fights")) {
            count = db.countAllContainingWithNotNull(DatabaseCapturePointFight.class, new String[]{"participating"}, guild, "winner");
            List<DatabaseCapturePointFight> process = db.findAllContainingWithNotNullWithSortAndLimit(DatabaseCapturePointFight.class, new String[]{"participating"}, guild, "winner", "timeEnd", false, begin, limit);
            for (DatabaseCapturePointFight fight : process) {
                String win = messageSource.getMessage("cross", new Object[0], locale);
                if (fight.getWinner() != null && fight.getWinner().equals(guild)) {
                    win = messageSource.getMessage("trophy", new Object[0], locale);
                }
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
                time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
                content.addEntry("/stats/capture_point_fight/" + fight.getUuid(), time.split(" ")[0], messageSource.getMessage("stats.player.capture_point_fight." + fight.getPoint(), new Object[]{fight.getWinner().getName()}, locale), win);
            }
        }
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        model.addAttribute("sort", "time");
        model.addAttribute("order", "desc");
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);

        db.commit();
        return "stats/guild";
    }
}
