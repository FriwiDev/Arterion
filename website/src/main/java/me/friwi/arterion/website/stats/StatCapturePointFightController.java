package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseCapturePointFight;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Controller
public class StatCapturePointFightController {
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats/capture_point_fight/{uuid}")
    public String capture_point_fight(Locale locale, Model model, @PathVariable UUID uuid, @RequestParam(defaultValue = "damage_dealt_players") String detailstat, @RequestParam(defaultValue = "false") boolean timedependent) {
        model.addAttribute("detailstat", detailstat);
        model.addAttribute("timedependent", timedependent);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabaseCapturePointFight fight = db.find(DatabaseCapturePointFight.class, uuid);
        if (fight == null) {
            db.commit();
            return "redirect:/error";
        }

        //Title
        model.addAttribute("capname", messageSource.getMessage("page.stats.capture_point_fights.type." + fight.getPoint(), new Object[0], locale));

        //Subtitle
        String vs = "";
        int size = fight.getParticipating().size();
        int i = 0;
        for (DatabaseGuild guild : fight.getParticipating()) {
            vs += guild.getName() + (i < size - 1 ? " vs " : "");
            i++;
        }
        model.addAttribute("vsstr", vs);

        //Replay
        if (fight.getReplayLocation() != null) {
            model.addAttribute("replaydl", "/replay/" + fight.getReplayLocation());
        }

        //Broad
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("begin", time);
        model.addAttribute("winner", fight.getWinner());
        if (fight.getTimeEnd() == -1) {
            model.addAttribute("duration", messageSource.getMessage("page.stats.capture_point_fight.broad.noduration", new Object[0], locale));
        } else {
            long seconds = (fight.getTimeEnd() - fight.getTimeBegin()) / 1000;
            long minutes = seconds / 60;
            seconds %= 60;
            model.addAttribute("duration", minutes + "m " + seconds + "s");
        }

        //Teams
        size = fight.getGuilds().size();
        String[][] teams = new String[size][];
        String[] teamNames = new String[size];
        UUID[] teamUUIDs = new UUID[size];

        Map<DatabaseGuild, DatabasePlayer[]> guilds = new HashMap<>();
        for (Map.Entry<DatabaseGuild, UUID[]> entry : fight.getGuilds().entrySet()) {
            DatabasePlayer[] players = new DatabasePlayer[entry.getValue().length];
            for (int j = 0; j < players.length; j++) {
                players[j] = db.find(DatabasePlayer.class, entry.getValue()[j]);
            }
            guilds.put(entry.getKey(), players);
        }

        i = 0;
        for (Map.Entry<DatabaseGuild, DatabasePlayer[]> entry : guilds.entrySet()) {
            teams[i] = new String[]{entry.getKey().getName(), StatArenaFightController.playerArrayToClickableString(entry.getValue())};
            teamNames[i] = entry.getKey().getName();
            teamUUIDs[i] = entry.getKey().getUuid();
            i++;
        }
        model.addAttribute("teams", teams);

        //Charts
        StatType type = StatType.KILLS;
        StatContextType contextType = StatContextType.CAPTURE_POINT_FIGHT;
        try {
            type = StatType.valueOf(detailstat.toUpperCase());
        } catch (Exception e) {

        }
        String statName = messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale);

        String[][] charts = new String[1 + size][];

        charts[0] = new String[]{
                messageSource.getMessage("page.stats.capture_point_fight.stat_details_all", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "all", true, contextType, fight.getUuid(), teamUUIDs,
                        teamNames, type, messageSource, locale)
        };

        i = 0;
        for (Map.Entry<DatabaseGuild, DatabasePlayer[]> entry : guilds.entrySet()) {
            charts[1 + i] = new String[]{
                    messageSource.getMessage("page.stats.capture_point_fight.stat_details_one", new Object[]{entry.getKey().getName(), statName}, locale),
                    ChartGenerator.generateChart(timedependent, "stat-" + i, false, contextType, fight.getUuid(),
                            StatArenaFightController.playerArrayToUUIDArray(entry.getValue()),
                            StatArenaFightController.playerArrayToNameArray(entry.getValue()),
                            type, messageSource, locale)
            };
            i++;
        }

        model.addAttribute("charts", charts);


        db.commit();
        return "stats/capture_point_fight";
    }
}
