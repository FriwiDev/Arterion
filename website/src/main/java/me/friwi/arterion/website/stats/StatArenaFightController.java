package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseArenaFight;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.enums.StatContextType;
import me.friwi.arterion.plugin.util.database.enums.StatType;
import me.friwi.arterion.website.WebApplication;
import me.friwi.arterion.website.stats.chart.ChartGenerator;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
public class StatArenaFightController {
    @Autowired
    private MessageSource messageSource;

    public static String playerListToClickableString(Set<DatabasePlayer> players) {
        String build = "";
        int size = players.size();
        int i = 0;
        Iterator<DatabasePlayer> it = players.iterator();
        while (it.hasNext()) {
            DatabasePlayer p = it.next();
            build += "<a style=\"color: black;\" href=\"/stats/player/" + p.getUuid() + "\">" + p.getName() + "</a>";
            i++;
            if (i < size - 1) {
                build += ", ";
            } else if (i == size - 1) {
                build += " & ";
            }
        }
        return build;
    }

    public static UUID[] playerListToUUIDArray(Set<DatabasePlayer> players) {
        UUID[] ret = new UUID[players.size()];
        int i = 0;
        for (DatabasePlayer p : players) {
            ret[i] = p.getUuid();
            i++;
        }
        return ret;
    }

    public static String[] playerListToNameArray(Set<DatabasePlayer> players) {
        String[] ret = new String[players.size()];
        int i = 0;
        for (DatabasePlayer p : players) {
            ret[i] = p.getName();
            i++;
        }
        return ret;
    }

    public static String playerArrayToClickableString(DatabasePlayer[] players) {
        String build = "";
        int size = players.length;
        int i = 0;
        for (DatabasePlayer p : players) {
            build += "<a style=\"color: black;\" href=\"/stats/player/" + p.getUuid() + "\">" + p.getName() + "</a>";
            i++;
            if (i < size - 1) {
                build += ", ";
            } else if (i == size - 1) {
                build += " & ";
            }
        }
        return build;
    }

    public static UUID[] playerArrayToUUIDArray(DatabasePlayer[] players) {
        UUID[] ret = new UUID[players.length];
        int i = 0;
        for (DatabasePlayer p : players) {
            ret[i] = p.getUuid();
            i++;
        }
        return ret;
    }

    public static String[] playerArrayToNameArray(DatabasePlayer[] players) {
        String[] ret = new String[players.length];
        int i = 0;
        for (DatabasePlayer p : players) {
            ret[i] = p.getName();
            i++;
        }
        return ret;
    }

    @GetMapping("/stats/arena_fight/{uuid}")
    public String arena_fight(Locale locale, Model model, @PathVariable UUID uuid, @RequestParam(defaultValue = "damage_dealt_players") String detailstat, @RequestParam(defaultValue = "false") boolean timedependent) {
        model.addAttribute("detailstat", detailstat);
        model.addAttribute("timedependent", timedependent);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabaseArenaFight fight = db.find(DatabaseArenaFight.class, uuid);
        if (fight == null) {
            db.commit();
            return "redirect:/error";
        }

        //Subtitle
        model.addAttribute("vsstr",
                StatPlayerPageController.playerListToString(fight.getPlayersTeamOne())
                        + " vs "
                        + StatPlayerPageController.playerListToString(fight.getPlayersTeamTwo()));

        //Replay
        if (fight.getReplayLocation() != null) {
            model.addAttribute("replaydl", "/replay/" + fight.getReplayLocation());
        }

        //Broad
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("begin", time);
        model.addAttribute("winner", messageSource.getMessage("page.stats.arena_fight.broad.winner" + fight.getWinner(), new Object[0], locale));
        model.addAttribute("score", fight.getRemaining1() + ":" + fight.getRemaining2());
        if (fight.getTimeEnd() == -1) {
            model.addAttribute("duration", messageSource.getMessage("page.stats.arena_fight.broad.noduration", new Object[0], locale));
        } else {
            long seconds = (fight.getTimeEnd() - fight.getTimeBegin()) / 1000;
            long minutes = seconds / 60;
            seconds %= 60;
            model.addAttribute("duration", minutes + "m " + seconds + "s");
        }

        //Teams
        model.addAttribute("team_one", playerListToClickableString(fight.getPlayersTeamOne()));
        model.addAttribute("team_two", playerListToClickableString(fight.getPlayersTeamTwo()));

        //Charts
        String team1Name = messageSource.getMessage("page.stats.arena_fight.stat_details.team_one", new Object[0], locale);
        String team2Name = messageSource.getMessage("page.stats.arena_fight.stat_details.team_two", new Object[0], locale);

        StatType type = StatType.KILLS;
        StatContextType contextType = StatContextType.ARENA_FIGHT;
        try {
            type = StatType.valueOf(detailstat.toUpperCase());
        } catch (Exception e) {

        }
        String statName = messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale);

        String[][] charts = new String[3][];

        charts[0] = new String[]{
                messageSource.getMessage("page.stats.arena_fight.stat_details_all", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "all", true, contextType, fight.getUuid(), new UUID[]{
                                new UUID(0, 0), new UUID(0, 1)
                        },
                        new String[]{
                                team1Name, team2Name
                        }, type, messageSource, locale)
        };

        charts[1] = new String[]{
                messageSource.getMessage("page.stats.arena_fight.stat_details_one", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "one", false, contextType, fight.getUuid(),
                        playerListToUUIDArray(fight.getPlayersTeamOne()),
                        playerListToNameArray(fight.getPlayersTeamOne()),
                        type, messageSource, locale)
        };

        charts[2] = new String[]{
                messageSource.getMessage("page.stats.arena_fight.stat_details_two", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "two", false, contextType, fight.getUuid(),
                        playerListToUUIDArray(fight.getPlayersTeamTwo()),
                        playerListToNameArray(fight.getPlayersTeamTwo()),
                        type, messageSource, locale)
        };

        model.addAttribute("charts", charts);


        db.commit();
        return "stats/arena_fight";
    }
}
