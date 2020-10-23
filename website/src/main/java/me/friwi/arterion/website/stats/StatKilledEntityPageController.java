package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;
import me.friwi.arterion.plugin.util.database.enums.StatContextType;
import me.friwi.arterion.plugin.util.database.enums.StatType;
import me.friwi.arterion.website.WebApplication;
import me.friwi.arterion.website.langutils.EnumEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
public class StatKilledEntityPageController {
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats/killed_entity/{id}")
    public String killed_entity(Locale locale, Model model, @PathVariable int id, @RequestParam(defaultValue = "done") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        EnumEntity entity = EnumEntity.byId(id);
        if (entity == null) {
            return "redirect:/error";
        }

        int begin = (page - 1) * StatListPageController.PER_PAGE;
        int limit = StatListPageController.PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        String[] column = new String[]{"contextType", "statType", "statData"};
        Object[] value = new Object[]{StatContextType.GLOBAL_TOP, StatType.MOB_KILLS, id};
        long count = db.countAllWithMatches(DatabaseStatComponent.class, column, value);
        long maxpage = count / StatListPageController.PER_PAGE + (count % StatListPageController.PER_PAGE == 0 ? 0 : 1);
        String sortColumn = "value";
        sort = "done";
        boolean asc = false;
        if (order.equals("asc")) asc = true;
        List<DatabaseStatComponent> display = db.findAllWithMatchesWithSortAndLimit(DatabaseStatComponent.class, column, value, sortColumn, asc, begin, limit);
        TraverseableList content = new TraverseableList(begin + 1);
        for (DatabaseStatComponent comp : display) {
            DatabasePlayer player = db.find(DatabasePlayer.class, comp.getTargetObject());
            content.addEntry("/stats/player/" + comp.getTargetObject(), player.getName(), comp.getValue());
        }
        db.commit();
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("page", page);
        model.addAttribute("maxpage", maxpage);
        model.addAttribute("content", content);
        model.addAttribute("entityName", entity.getName(messageSource, locale));
        model.addAttribute("entityClass", "entities-28-" + id);
        return "stats/killed_entity";
    }
}
