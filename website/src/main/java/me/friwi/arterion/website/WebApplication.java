package me.friwi.arterion.website;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.HibernateUtil;
import me.friwi.arterion.plugin.util.formulas.ArterionFormulaManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SpringBootApplication
public class WebApplication implements WebMvcConfigurer {
    public static final ZoneId TIME_ZONE = ZoneId.of("CET");
    public static final ZoneId SERVER_TIME_ZONE = ZoneId.of("Z");
    private static ArterionFormulaManager formulaManager;
    private static long lastCache = 0;
    private static final Map<Long, Database> DATABASE_REFS = new HashMap<>();

    public static void main(String[] args) {
        HibernateUtil.setup();
        lastCache = System.currentTimeMillis();
        formulaManager = new ArterionFormulaManager();
        SpringApplication.run(WebApplication.class, args);
        HibernateUtil.shutdown();
    }

    public static Database getDatabase() {
        Database ret = DATABASE_REFS.get(Thread.currentThread().getId());
        if (ret == null) {
            ret = new Database();
            DATABASE_REFS.put(Thread.currentThread().getId(), ret);
        }
        return ret;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.GERMANY);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    public static ArterionFormulaManager getFormulaManager() {
        if (lastCache + 2 * 60 * 1000 < System.currentTimeMillis()) {
            lastCache = System.currentTimeMillis();
            formulaManager = new ArterionFormulaManager();
        }
        return formulaManager;
    }
}
