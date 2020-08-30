package com.thoughtworks.securityinourdna;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
public class HomeController {

    private final UserRepo userRepo;

    @Autowired
    public HomeController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String home(Model model) throws SQLException {
        model.addAttribute("vendors", userRepo.allVendors());
        return "home";
    }

    @GetMapping("/vendor")
    public String vendor() {
        return "vendor";
    }

    @PostMapping("/login")
    public String login(RedirectAttributes redirAttrs,
                        @RequestParam("vendor") String vendor,
                        @RequestParam("password") String password) throws Exception {
        final UserRepo.LoginResult loginResult = userRepo.login(vendor, password);

        if (loginResult.success) {
            redirAttrs.addFlashAttribute("vendor", loginResult.vendorName);
            return "redirect:/vendor";
        } else {
            redirAttrs.addFlashAttribute("error", "Bad username and password combination");
            return "redirect:/";
        }
    }
}
