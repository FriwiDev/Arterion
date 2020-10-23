package me.friwi.arterion.website.premium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopController {

    @Autowired
    PaypalConfig paypalConfig;

    @GetMapping("/shop")
    public String premium(Model model) {
        model.addAttribute("premium_price", paypalConfig.getPremiumPrice());
        model.addAttribute("booster_price", paypalConfig.getBoosterPrice());
        return "shop/enter_name";
    }
}
