package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.*;
import me.friwi.arterion.website.WebApplication;
import me.friwi.arterion.website.langutils.EnumBlock;
import me.friwi.arterion.website.langutils.EnumEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class StatListPageController {
    public static final int PER_PAGE = 15;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats")
    public String stats() {
        return "redirect:/stats/guilds";
    }

    @GetMapping("/stats/guilds")
    public String list_guilds(Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "kills") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        String columnQuery = "name";
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAllByColumnStartAndMatch(DatabaseGuild.class, columnQuery, query, "deleted", DatabaseGuild.NOT_DELETED);
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "clanKills";
        if (sort.equals("name")) sortColumn = "name";
        else if (sort.equals("active_since")) sortColumn = "created";
        else if (sort.equals("vault")) sortColumn = "gold";
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseGuild> display = db.findAllByColumnStartWithSortAndLimitAndMatch(DatabaseGuild.class, columnQuery, query, sortColumn, asc, begin, limit, "deleted", DatabaseGuild.NOT_DELETED);
        db.commit();
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseGuild guild : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(guild.getCreated()), WebApplication.SERVER_TIME_ZONE);
            String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            content.addEntry("/stats/guild/" + guild.getUuid(), guild.getName(), time.split(" ")[0], String.format("%.2f", guild.getGold() / 100f), guild.getClanKills());
        }
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        model.addAttribute("query", query);
        return "stats/guilds";
    }

    @GetMapping("/stats/players")
    public String list_players(Locale locale, Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "kills") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        String columnQuery = "name";
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAllByColumnStart(DatabasePlayer.class, columnQuery, query);
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "kills";
        if (sort.equals("name")) sortColumn = "name";
        else if (sort.equals("class")) sortColumn = "selectedClass";
        else if (sort.equals("level")) sortColumn = "level";
        else if (sort.equals("active_since")) sortColumn = "joined";
        else if (sort.equals("gold")) sortColumn = "bank";
        else if (sort.equals("inv")) sortColumn = "gold";
        else if (sort.equals("kills")) sortColumn = "kills";
        else if (sort.equals("deaths")) sortColumn = "deaths";
        else if (sort.equals("plevel")) sortColumn = "prestigeLevel";
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabasePlayer> display = db.findAllByColumnStartWithSortAndLimit(DatabasePlayer.class, columnQuery, query, sortColumn, asc, begin, limit);
        db.commit();
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabasePlayer player : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(player.getJoined()), WebApplication.SERVER_TIME_ZONE);
            String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            content.addEntry("/stats/player/" + player.getUuid(), player.getName(), time.split(" ")[0], messageSource.getMessage("player.class." + player.getSelectedClass().name().toLowerCase(), new Object[0], locale), player.getLevel(), player.getPrestigeLevel(), String.format("%.2f", player.getGold() / 100f), String.format("%.2f", player.getBank() / 100f), player.getKills(), player.getDeaths());
        }
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        model.addAttribute("query", query);
        return "stats/players";
    }

    @GetMapping("/stats/guild_fights")
    public String list_guild_fights(Model model, @RequestParam(defaultValue = "begin") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAll(DatabaseGuildFight.class);
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "timeBegin";
        if (sort.equals("end")) sortColumn = "timeEnd";
        else if (sort.equals("begin")) sortColumn = "timeBegin";
        else {
            sort = "begin";
        }
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseGuildFight> display = db.findAllWithSortAndLimit(DatabaseGuildFight.class, sortColumn, asc, begin, limit);
        db.commit();
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseGuildFight fight : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
            String timeBegin = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            String timeEnd = "-";
            if (fight.getTimeEnd() != DatabaseGuildFight.FIGHT_UNFINISHED) {
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeEnd()), WebApplication.SERVER_TIME_ZONE);
                timeEnd = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            }
            content.addEntry("/stats/guild_fight/" + fight.getUuid(), fight.getAttacker().getName(), fight.getDefender().getName(), timeBegin, timeEnd, fight.getWinner() == null ? "-" : fight.getWinner().getName());
        }
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        return "stats/guild_fights";
    }

    @GetMapping("/stats/artefact_fights")
    public String list_artefact_fights(Model model, @RequestParam(defaultValue = "begin") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAllWithNotNull(DatabaseArtefactFight.class, "winner");
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "timeBegin";
        if (sort.equals("end")) sortColumn = "timeEnd";
        else if (sort.equals("begin")) sortColumn = "timeBegin";
        else {
            sort = "begin";
        }
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseArtefactFight> display = db.findAllWithSortAndLimitWithNotNull(DatabaseArtefactFight.class, sortColumn, asc, begin, limit, "winner");
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseArtefactFight fight : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
            String timeBegin = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            String timeEnd = "-";
            if (fight.getTimeEnd() != DatabaseGuildFight.FIGHT_UNFINISHED) {
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeEnd()), WebApplication.SERVER_TIME_ZONE);
                timeEnd = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            }
            String participating = "-";
            if (!fight.getParticipating().isEmpty()) {
                participating = "";
                int size = fight.getParticipating().size();
                ArrayList<DatabaseGuild> sorted = new ArrayList<>();
                sorted.addAll(fight.getParticipating());
                Collections.sort(sorted, Comparator.comparing(DatabaseGuild::getName));
                int i = 0;
                for (DatabaseGuild g : sorted) {
                    i++;
                    participating += g.getName() + (i < size ? ", " : "");
                }
            }
            content.addEntry("/stats/artefact_fight/" + fight.getUuid(), timeBegin, participating, timeEnd, fight.getWinner() == null ? "-" : fight.getWinner().getName());
        }
        db.commit();
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        return "stats/artefact_fights";
    }

    @GetMapping("/stats/capture_point_fights")
    public String list_capture_point_fights(Locale locale, Model model, @RequestParam(defaultValue = "begin") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAllWithNotNull(DatabaseArtefactFight.class, "winner");
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "timeBegin";
        if (sort.equals("end")) sortColumn = "timeEnd";
        else if (sort.equals("begin")) sortColumn = "timeBegin";
        else {
            sort = "begin";
        }
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseCapturePointFight> display = db.findAllWithSortAndLimitWithNotNull(DatabaseCapturePointFight.class, sortColumn, asc, begin, limit, "winner");
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseCapturePointFight fight : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
            String timeBegin = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            String timeEnd = "-";
            if (fight.getTimeEnd() != DatabaseGuildFight.FIGHT_UNFINISHED) {
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeEnd()), WebApplication.SERVER_TIME_ZONE);
                timeEnd = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            }
            String participating = "-";
            if (!fight.getParticipating().isEmpty()) {
                participating = "";
                int size = fight.getParticipating().size();
                ArrayList<DatabaseGuild> sorted = new ArrayList<>();
                sorted.addAll(fight.getParticipating());
                Collections.sort(sorted, Comparator.comparing(DatabaseGuild::getName));
                int i = 0;
                for (DatabaseGuild g : sorted) {
                    i++;
                    participating += g.getName() + (i < size ? ", " : "");
                }
            }
            content.addEntry("/stats/capture_point_fight/" + fight.getUuid(), messageSource.getMessage("page.stats.capture_point_fights.type." + fight.getPoint(), new Object[0], locale), timeBegin, participating, timeEnd, fight.getWinner() == null ? "-" : fight.getWinner().getName());
        }
        db.commit();
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        return "stats/capture_point_fights";
    }

    @GetMapping("/stats/morgoth_dungeon_fights")
    public String list_morgoth_dungeon_fights(Model model, @RequestParam(defaultValue = "begin") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAll(DatabaseMorgothDungeonFight.class);
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "timeBegin";
        if (sort.equals("end")) sortColumn = "timeEnd";
        else if (sort.equals("begin")) sortColumn = "timeBegin";
        else {
            sort = "begin";
        }
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseMorgothDungeonFight> display = db.findAllWithSortAndLimit(DatabaseMorgothDungeonFight.class, sortColumn, asc, begin, limit);
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseMorgothDungeonFight fight : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
            String timeBegin = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            String timeEnd = "-";
            if (fight.getTimeEnd() != DatabaseGuildFight.FIGHT_UNFINISHED) {
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeEnd()), WebApplication.SERVER_TIME_ZONE);
                timeEnd = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            }
            String participating = "-";
            if (!fight.getPlayersParticipating().isEmpty()) {
                participating = "";
                int size = fight.getPlayersParticipating().size();
                ArrayList<DatabasePlayer> sorted = new ArrayList<>();
                sorted.addAll(fight.getPlayersParticipating());
                Collections.sort(sorted, Comparator.comparing(DatabasePlayer::getName));
                int i = 0;
                for (DatabasePlayer p : sorted) {
                    i++;
                    participating += p.getName() + (i < size ? ", " : "");
                }
            }
            String winning = "-";
            if (!fight.getPlayersWinning().isEmpty()) {
                winning = "";
                int size = fight.getPlayersWinning().size();
                ArrayList<DatabasePlayer> sorted = new ArrayList<>();
                sorted.addAll(fight.getPlayersWinning());
                Collections.sort(sorted, Comparator.comparing(DatabasePlayer::getName));
                int i = 0;
                for (DatabasePlayer p : sorted) {
                    i++;
                    winning += p.getName() + (i < size ? ", " : "");
                }
            }
            content.addEntry("/stats/morgoth_dungeon_fight/" + fight.getUuid(), timeBegin, participating, timeEnd, winning);
        }
        db.commit();
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        return "stats/morgoth_dungeon_fights";
    }

    @GetMapping("/stats/arena_fights")
    public String list_arena_fights(Locale locale, Model model, @RequestParam(defaultValue = "begin") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        long count = db.countAll(DatabaseArenaFight.class);
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "timeBegin";
        if (sort.equals("end")) sortColumn = "timeEnd";
        else if (sort.equals("begin")) sortColumn = "timeBegin";
        else {
            sort = "begin";
        }
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseArenaFight> display = db.findAllWithSortAndLimit(DatabaseArenaFight.class, sortColumn, asc, begin, limit);
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseArenaFight fight : display) {
            LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeBegin()), WebApplication.SERVER_TIME_ZONE);
            String timeBegin = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            String timeEnd = "-";
            if (fight.getTimeEnd() != DatabaseGuildFight.FIGHT_UNFINISHED) {
                localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fight.getTimeEnd()), WebApplication.SERVER_TIME_ZONE);
                timeEnd = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(WebApplication.SERVER_TIME_ZONE).withZoneSameInstant(WebApplication.TIME_ZONE)).replaceFirst("T", " ");
            }
            String team1 = "-";
            if (!fight.getPlayersTeamOne().isEmpty()) {
                team1 = "";
                int size = fight.getPlayersTeamOne().size();
                ArrayList<DatabasePlayer> sorted = new ArrayList<>();
                sorted.addAll(fight.getPlayersTeamOne());
                Collections.sort(sorted, Comparator.comparing(DatabasePlayer::getName));
                int i = 0;
                for (DatabasePlayer p : sorted) {
                    i++;
                    team1 += p.getName() + (i < size ? ", " : "");
                }
            }
            String team2 = "-";
            if (!fight.getPlayersTeamTwo().isEmpty()) {
                team2 = "";
                int size = fight.getPlayersTeamTwo().size();
                ArrayList<DatabasePlayer> sorted = new ArrayList<>();
                sorted.addAll(fight.getPlayersTeamTwo());
                Collections.sort(sorted, Comparator.comparing(DatabasePlayer::getName));
                int i = 0;
                for (DatabasePlayer p : sorted) {
                    i++;
                    team2 += p.getName() + (i < size ? ", " : "");
                }
            }
            content.addEntry("/stats/arena_fight/" + fight.getUuid(), timeBegin, team1, fight.getRemaining1(), fight.getRemaining2(), team2, timeEnd, messageSource.getMessage("page.stats.arena_fights.winner." + fight.getWinner(), new Object[0], locale));
        }
        db.commit();
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        return "stats/arena_fights";
    }

    @GetMapping("/stats/placed_blocks")
    public String list_placed_blocks(Locale locale, Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String order, @RequestParam(defaultValue = "1") int page) {
        return list_blocks("placed", locale, model, query, sort, order, page);
    }

    @GetMapping("/stats/mined_blocks")
    public String list_mined_blocks(Locale locale, Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String order, @RequestParam(defaultValue = "1") int page) {
        return list_blocks("mined", locale, model, query, sort, order, page);
    }

    public String list_blocks(String type, Locale locale, Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        List<EnumBlock> filtered = new ArrayList<>(EnumBlock.values().length);
        for (EnumBlock block : EnumBlock.values()) {
            String id = block.getMaterial().getId() + ":" + block.getMetadata();
            if (id.startsWith(query) || block.getName(messageSource, locale).toLowerCase().contains(query.toLowerCase())) {
                filtered.add(block);
            }
        }
        long count = filtered.size();
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        boolean asc = order.equals("asc");
        if (sort.equals("name")) {
            Collections.sort(filtered, (b1, b2) -> {
                int m = asc ? 1 : -1;
                return m * b1.getName(messageSource, locale).compareTo(b2.getName(messageSource, locale));
            });
        } else {
            sort = "id";
            Collections.sort(filtered, (b1, b2) -> {
                int m = asc ? 1 : -1;
                int c1 = new Integer(b1.getMaterial().getId()).compareTo(b2.getMaterial().getId());
                if (c1 == 0) {
                    return m * new Integer(b1.getMetadata()).compareTo(b2.getMetadata());
                } else {
                    return m * c1;
                }
            });
        }
        if (begin >= filtered.size()) {
            begin = filtered.size() - 1;
        }
        if (begin < 0) begin = 0;
        List<EnumBlock> display = filtered.subList(begin, begin + limit >= filtered.size() ? (filtered.size()) : (begin + limit));
        TraverseableList content = new TraverseableList(begin + 1);
        for (EnumBlock block : display) {
            content.addEntry("/stats/" + type + "_block/" + (block.getMaterial().getId() << 8 | block.getMetadata()),
                    block.getMaterial().getId() + (block.getMetadata() == 0 ? "" : (":" + block.getMetadata())),
                    "items-28-" + block.getMaterial().getId() + "-" + block.getMetadata(), block.getName(messageSource, locale));
        }
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        model.addAttribute("query", query);
        return "stats/" + type + "_blocks";
    }

    @GetMapping("/stats/killed_entities")
    public String list_killed_entities(Locale locale, Model model, @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int begin = (page - 1) * PER_PAGE;
        int limit = PER_PAGE;
        List<EnumEntity> filtered = new ArrayList<>(EnumEntity.values().length);
        for (EnumEntity entity : EnumEntity.values()) {
            String id = entity.getType().getTypeId() + "";
            if (id.startsWith(query) || entity.getName(messageSource, locale).toLowerCase().contains(query.toLowerCase())) {
                filtered.add(entity);
            }
        }
        long count = filtered.size();
        long maxpage = count / PER_PAGE + (count % PER_PAGE == 0 ? 0 : 1);
        boolean asc = order.equals("asc");
        if (sort.equals("name")) {
            Collections.sort(filtered, (b1, b2) -> {
                int m = asc ? 1 : -1;
                return m * b1.getName(messageSource, locale).compareTo(b2.getName(messageSource, locale));
            });
        } else {
            sort = "id";
            Collections.sort(filtered, (b1, b2) -> {
                int m = asc ? 1 : -1;
                return m * new Integer(b1.getType().getTypeId()).compareTo((int) b2.getType().getTypeId());
            });
        }
        if (begin >= filtered.size()) {
            begin = filtered.size() - 1;
        }
        if (begin < 0) begin = 0;
        List<EnumEntity> display = filtered.subList(begin, begin + limit >= filtered.size() ? (filtered.size()) : (begin + limit));
        TraverseableList content = new TraverseableList(begin + 1);
        for (EnumEntity entity : display) {
            content.addEntry("/stats/killed_entity/" + (entity.getType().getTypeId()),
                    entity.getType().getTypeId(),
                    "entities-28-" + entity.getType().getTypeId(), entity.getName(messageSource, locale));
        }
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        model.addAttribute("query", query);
        return "stats/killed_entities";
    }
}
