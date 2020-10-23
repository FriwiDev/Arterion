package me.friwi.arterion.website.stats;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;
import me.friwi.arterion.plugin.util.database.enums.StatContextType;
import me.friwi.arterion.plugin.util.database.enums.StatType;
import me.friwi.arterion.website.WebApplication;
import me.friwi.arterion.website.langutils.EnumBlock;
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
public class StatBlockPageController {
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/stats/placed_block/{concatId}")
    public String placed_block(Locale locale, Model model, @PathVariable int concatId, @RequestParam(defaultValue = "done") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        return block("placed", StatType.PLACED_BLOCKS, locale, model, concatId, sort, order, page);
    }

    @GetMapping("/stats/mined_block/{concatId}")
    public String mined_block(Locale locale, Model model, @PathVariable int concatId, @RequestParam(defaultValue = "done") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        return block("mined", StatType.DESTROYED_BLOCKS, locale, model, concatId, sort, order, page);
    }

    public String block(String action, StatType type, Locale locale, Model model, @PathVariable int concatId, @RequestParam(defaultValue = "done") String sort, @RequestParam(defaultValue = "desc") String order, @RequestParam(defaultValue = "1") int page) {
        if (page < 1) return "redirect:/error";
        int id = concatId >> 8;
        int data = concatId & 0xFF;
        EnumBlock block = EnumBlock.byIdAndData(id, data);
        if (block == null) {
            return "redirect:/error";
        }

        int begin = (page - 1) * StatListPageController.PER_PAGE;
        int limit = StatListPageController.PER_PAGE;
        Database db = WebApplication.getDatabase();
        db.beginTransaction();
        String[] column = new String[]{"contextType", "statType", "statData"};
        Object[] value = new Object[]{StatContextType.GLOBAL_TOP, type, concatId};
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
        model.addAttribute("blockName", block.getName(messageSource, locale));
        model.addAttribute("blockClass", "items-28-" + id + "-" + data);
        return "stats/" + action + "_block";
    }
}
