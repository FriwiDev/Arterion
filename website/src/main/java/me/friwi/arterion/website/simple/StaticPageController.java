package me.friwi.arterion.website.simple;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class StaticPageController implements ErrorController {

    @RequestMapping(value = "/favicon.ico", method = RequestMethod.GET)
    public void getImageAsByteArray(HttpServletResponse response) throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/res/favicon.ico");
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
    }

    @GetMapping("/")
    public String home() {
        return "static/home";
    }

    @GetMapping("/about")
    public String about() {
        return "static/about";
    }

    @RequestMapping(value = "/error")
    public String error() {
        return "static/error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("/vote")
    public String vote(Model model) {
        model.addAttribute("vote1", "https://minecraft-server.eu/vote/index/21951");
        return "static/vote";
    }
}
