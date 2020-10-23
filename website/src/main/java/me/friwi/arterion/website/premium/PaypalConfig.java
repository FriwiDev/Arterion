package me.friwi.arterion.website.premium;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaypalConfig {

    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.base_url}")
    private String baseUrl;
    @Value("${premium.price}")
    private double premiumPrice;
    @Value("${booster.price}")
    private double boosterPrice;

    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        APIContext context = new APIContext(clientId, clientSecret, mode);
        return context;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public double getPremiumPrice() {
        return premiumPrice;
    }

    public double getBoosterPrice() {
        return boosterPrice;
    }
}
