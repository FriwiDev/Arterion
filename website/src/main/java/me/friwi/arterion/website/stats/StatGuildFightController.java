package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuildFight;
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
import java.util.Locale;
import java.util.UUID;

@Controller
public class StatGuildFightController {
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats/guild_fight/{uuid}")
    public String guild_fight(Locale locale, Model model, @PathVariable UUID uuid, @RequestParam(defaultValue = "damage_dealt_players") String detailstat, @RequestParam(defaultValue = "false") boolean timedependent) {
        model.addAttribute("detailstat", detailstat);
        model.addAttribute("timedependent", timedependent);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabaseGuildFight fight = db.find(DatabaseGuildFight.class, uuid);
        if (fight == null) {
            db.commit();
            return "redirect:/error";
        }

        //Subtitle
        model.addAttribute("vsstr",
                fight.getAttacker().getName()
                        + " vs "
                        + fight.getDefender().getName());

        //Replay
        if (fight.getReplayLocation() != null) {
            model.addAttribute("replaydl", "/replay/" + fight.getReplayLocation());
        }

        //Broad
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("begin", time);
        model.addAttribute("attacker", fight.getAttacker());
        model.addAttribute("defender", fight.getDefender());
        model.addAttribute("winner", fight.getWinner());
        if (fight.getTimeEnd() == -1) {
            model.addAttribute("duration", messageSource.getMessage("page.stats.guild_fight.broad.noduration", new Object[0], locale));
        } else {
            long seconds = (fight.getTimeEnd() - fight.getTimeBegin()) / 1000;
            long minutes = seconds / 60;
            seconds %= 60;
            model.addAttribute("duration", minutes + "m " + seconds + "s");
        }

        //Teams
        model.addAttribute("team_one", StatArenaFightController.playerListToClickableString(fight.getPlayersAttacking()));
        model.addAttribute("team_two", StatArenaFightController.playerListToClickableString(fight.getPlayersDefending()));

        //Charts
        String team1Name = fight.getAttacker().getName();
        String team2Name = fight.getDefender().getName();

        StatType type = StatType.KILLS;
        StatContextType contextType = StatContextType.GUILD_FIGHT;
        try {
            type = StatType.valueOf(detailstat.toUpperCase());
        } catch (Exception e) {

        }
        String statName = messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale);

        String[][] charts = new String[3][];

        charts[0] = new String[]{
                messageSource.getMessage("page.stats.guild_fight.stat_details_all", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "all", true, contextType, fight.getUuid(), new UUID[]{
                                fight.getAttacker().getUuid(), fight.getDefender().getUuid()
                        },
                        new String[]{
                                team1Name, team2Name
                        }, type, messageSource, locale)
        };

        charts[1] = new String[]{
                messageSource.getMessage("page.stats.guild_fight.stat_details_one", new Object[]{fight.getAttacker().getName(), statName}, locale),
                ChartGenerator.generateChart(timedependent, "one", false, contextType, fight.getUuid(),
                        StatArenaFightController.playerListToUUIDArray(fight.getPlayersAttacking()),
                        StatArenaFightController.playerListToNameArray(fight.getPlayersAttacking()),
                        type, messageSource, locale)
        };

        charts[2] = new String[]{
                messageSource.getMessage("page.stats.guild_fight.stat_details_two", new Object[]{fight.getDefender().getName(), statName}, locale),
                ChartGenerator.generateChart(timedependent, "two", false, contextType, fight.getUuid(),
                        StatArenaFightController.playerListToUUIDArray(fight.getPlayersDefending()),
                        StatArenaFightController.playerListToNameArray(fight.getPlayersDefending()),
                        type, messageSource, locale)
        };

        model.addAttribute("charts", charts);


        db.commit();
        return "stats/guild_fight";
    }
}
