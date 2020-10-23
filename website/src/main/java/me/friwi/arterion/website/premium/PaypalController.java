package me.friwi.arterion.website.premium;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseBuyEntry;
import me.friwi.arterion.plugin.util.database.enums.ProductType;
import me.friwi.arterion.website.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class PaypalController {
    public static final String SUCCESS_URL = "/shop/success";
    public static final String CANCEL_URL = "/shop/cancel";

    public static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    @Autowired
    PaypalService service;

    @Autowired
    PaypalConfig paypalConfig;

    private Map<String, MCUser> paymentMap = new HashMap<>();

    @PostMapping("/premium")
    public String premium(@RequestParam String username, @RequestParam boolean acceptAgb) {
        if (!acceptAgb) {
            return "shop/failed";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "shop/invaliduser";
        }
        MCUser player = MCUserFetcher.fetchMCUser(username);
        if (player == null) {
            return "shop/invaliduser";
        }
        try {
            player.setProductType(ProductType.PREMIUM);
            Payment payment = service.createPayment(paypalConfig.getPremiumPrice(), "EUR", "paypal",
                    "Sale", "Buy premium for " + username, paypalConfig.getBaseUrl() + CANCEL_URL,
                    paypalConfig.getBaseUrl() + SUCCESS_URL);
            paymentMap.put(payment.getId(), player);
            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "shop/failed";
    }

    @PostMapping("/booster")
    public String booster(@RequestParam String username, @RequestParam boolean acceptAgb) {
        if (!acceptAgb) {
            return "shop/failed";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "shop/invaliduser";
        }
        MCUser player = MCUserFetcher.fetchMCUser(username);
        if (player == null) {
            return "shop/invaliduser";
        }
        try {
            player.setProductType(ProductType.BOOSTER);
            Payment payment = service.createPayment(paypalConfig.getBoosterPrice(), "EUR", "paypal",
                    "Sale", "Buy xp booster for " + username, paypalConfig.getBaseUrl() + CANCEL_URL,
                    paypalConfig.getBaseUrl() + SUCCESS_URL);
            paymentMap.put(payment.getId(), player);
            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "shop/failed";
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return "shop/failed";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(Model model, @RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        MCUser redeem = paymentMap.remove(paymentId);
        if (redeem == null) {
            return "shop/failed";
        }
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            //System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                //Give premium
                Database db = WebApplication.getDatabase();
                db.beginTransaction();
                DatabaseBuyEntry entry = new DatabaseBuyEntry(redeem.getUuid(), redeem.getProductType());
                db.save(entry);
                db.commit();
                //Display success message
                model.addAttribute("mcname", redeem.getName());
                return "shop/success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "shop/failed";
    }

}
