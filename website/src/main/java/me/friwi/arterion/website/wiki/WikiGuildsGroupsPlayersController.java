package me.friwi.arterion.website.wiki;

import me.friwi.arterion.plugin.util.formulas.FakePlayer;
import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiGuildsGroupsPlayersController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/guilds_groups_players")
    public String wiki_guilds_groups_players(Model model) {
        Object obj = new Object();
        FakePlayer player = new FakePlayer();
        model.addAttribute("guild_price", String.format("%.2f", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(player) / 100d));
        model.addAttribute("min_x", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_MIN.evaluateInt(obj));
        model.addAttribute("max_x", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_MAX.evaluateInt(obj));
        model.addAttribute("min_y", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_MINY.evaluateInt(obj));
        model.addAttribute("max_y", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_MAXY.evaluateInt(obj));
        model.addAttribute("distance", WebApplication.getFormulaManager().GUILD_GUILDBLOCK_DISTANCE.evaluateInt(obj));
        model.addAttribute("guild_maxmembers", WebApplication.getFormulaManager().GUILD_MAXMEMBERS.evaluateInt());
        model.addAttribute("drop_percent", WebApplication.getFormulaManager().GUILD_VAULT_DROP.evaluateInt(obj));
        model.addAttribute("tpa_price", String.format("%.2f", WebApplication.getFormulaManager().TPA_PRICE.evaluateInt() / 100d));
        return "wiki/guilds_groups_players";
    }
}
