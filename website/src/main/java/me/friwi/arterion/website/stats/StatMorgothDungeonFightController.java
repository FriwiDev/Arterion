package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseMorgothDungeonFight;
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
public class StatMorgothDungeonFightController {
    @Autowired
    private MessageSource messageSource;


    @GetMapping("/stats/morgoth_dungeon_fight/{uuid}")
    public String morgoth_dungeon_fight(Locale locale, Model model, @PathVariable UUID uuid, @RequestParam(defaultValue = "damage_dealt_mobs") String detailstat, @RequestParam(defaultValue = "false") boolean timedependent) {
        model.addAttribute("detailstat", detailstat);
        model.addAttribute("timedependent", timedependent);
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        DatabaseMorgothDungeonFight fight = db.find(DatabaseMorgothDungeonFight.class, uuid);
        if (fight == null) {
            db.commit();
            return "redirect:/error";
        }

        //Subtitle
        model.addAttribute("vsstr",
                messageSource.getMessage("page.stats.morgoth_dungeon_fight.subtitle", new Object[]{StatPlayerPageController.playerListToString(fight.getPlayersParticipating())}, locale));

        //Replay
        if (fight.getReplayLocation() != null) {
            model.addAttribute("replaydl", "/replay/" + fight.getReplayLocation());
        }

        //Broad
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
        model.addAttribute("begin", time);
        if (fight.getTimeEnd() == -1) {
            model.addAttribute("duration", messageSource.getMessage("page.stats.morgoth_dungeon_fight.broad.noduration", new Object[0], locale));
        } else {
            if (fight.getPlayersWinning().size() > 0) {
                model.addAttribute("winner", StatArenaFightController.playerListToClickableString(fight.getPlayersWinning()));
            } else {
                model.addAttribute("winner", messageSource.getMessage("page.stats.morgoth_dungeon_fight.broad.winner2", new Object[0], locale));
            }
            long seconds = (fight.getTimeEnd() - fight.getTimeBegin()) / 1000;
            long minutes = seconds / 60;
            seconds %= 60;
            model.addAttribute("duration", minutes + "m " + seconds + "s");
        }

        //Teams
        model.addAttribute("team_one", StatArenaFightController.playerListToClickableString(fight.getPlayersParticipating()));

        //Charts
        StatType type = StatType.DAMAGE_DEALT_MOBS;
        StatContextType contextType = StatContextType.DUNGEON_MORGOTH_FIGHT;
        try {
            type = StatType.valueOf(detailstat.toUpperCase());
        } catch (Exception e) {

        }
        String statName = messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale);

        String[][] charts = new String[1][];

        charts[0] = new String[]{
                messageSource.getMessage("page.stats.morgoth_dungeon_fight.stat_details_all", new Object[]{statName}, locale),
                ChartGenerator.generateChart(timedependent, "one", false, contextType, fight.getUuid(),
                        StatArenaFightController.playerListToUUIDArray(fight.getPlayersParticipating()),
                        StatArenaFightController.playerListToNameArray(fight.getPlayersParticipating()),
                        type, messageSource, locale)
        };

        model.addAttribute("charts", charts);


        db.commit();
        return "stats/morgoth_dungeon_fight";
    }
}
