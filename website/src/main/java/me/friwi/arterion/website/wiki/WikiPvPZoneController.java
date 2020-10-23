package me.friwi.arterion.website.wiki;

import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiPvPZoneController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/pvp_zone")
    public String wiki_pvp_zone(Model model) {
        model.addAttribute("graveruin_boost", (int) ((WebApplication.getFormulaManager().CAPTUREPOINT_GRAVERUIN_XP_MULTIPLIER.evaluateDouble() - 1) * 100d));
        model.addAttribute("deserttemple_boost", (int) ((WebApplication.getFormulaManager().CAPTUREPOINT_DESERTTEMPLE_GOLD_MULTIPLIER.evaluateDouble() - 1) * 100d));
        return "wiki/pvp_zone";
    }
}
