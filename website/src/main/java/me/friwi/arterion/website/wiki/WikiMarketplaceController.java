package me.friwi.arterion.website.wiki;

import me.friwi.arterion.plugin.util.formulas.FakePlayer;
import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiMarketplaceController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/marketplace")
    public String wiki_marketplace(Model model) {
        FakePlayer player = new FakePlayer();
        model.addAttribute("guild_price", String.format("%.2f", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(player) / 100d));
        model.addAttribute("home_price", String.format("%.2f", WebApplication.getFormulaManager().PLAYER_HOMEBLOCK_FEE.evaluateInt(player) / 100d));
        return "wiki/marketplace";
    }
}
