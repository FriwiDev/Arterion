package me.friwi.arterion.website.auth;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseLoginToken;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.website.WebApplication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam Optional<String> token) {
        if (token.isPresent()) {
            UUID tokenUUID = null;
            try {
                tokenUUID = UUID.fromString(token.get());
            } catch (Exception e) {
                return "redirect:/login_failed";
            }
            Database db = WebApplication.getDatabase();
            db.beginTransaction();
            DatabaseLoginToken loginToken = db.find(DatabaseLoginToken.class, tokenUUID);
            if (loginToken == null || loginToken.getExpires() < System.currentTimeMillis()) {
                return "redirect:/login_failed";
            }
            db.delete(loginToken);
            DatabasePlayer dbp = db.find(DatabasePlayer.class, loginToken.getPlayer());
            Authentication authentication = new UsernamePasswordAuthenticationToken(dbp, null,
                    AuthorityUtils.createAuthorityList("ROLE_USER"));
            //Clean up tokens
            for (DatabaseLoginToken dbt : db.findAll(DatabaseLoginToken.class)) {
                if (dbt.getExpires() < System.currentTimeMillis()) {
                    db.delete(dbt);
                }
            }
            db.commit();
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/login_success";
        } else {
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/logout_success";
    }

    @GetMapping("/logout_success")
    public String logoutSuccess() {
        return "auth/logout_success";
    }

    @GetMapping("/login_success")
    public String loginSuccess() {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof DatabasePlayer) {
            return "auth/login_success";
        } else {
            return "auth/login_failed";
        }
    }

    @GetMapping("/login_failed")
    public String loginFailed() {
        return "auth/login_failed";
    }
}
