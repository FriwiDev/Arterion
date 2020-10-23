package me.friwi.arterion.website.wiki;

import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiGuildFightsController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/guild_fights")
    public String wiki_guild_fights(Model model) {
        model.addAttribute("fight_dur", WebApplication.getFormulaManager().FIGHT_DURATION.evaluateInt() / 60000);
        model.addAttribute("fight_end", WebApplication.getFormulaManager().FIGHT_POST_DURATION.evaluateInt() / 60000);
        return "wiki/guild_fights";
    }
}
