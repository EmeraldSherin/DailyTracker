package com.dailytracker.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailytracker.tracker.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ─── LOGIN PAGE ────────────────────────────────
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {

        if (error != null)
            model.addAttribute("error", "Invalid email or password.");

        if (logout != null)
            model.addAttribute("message", "You've been logged out.");

        if (registered != null)
            model.addAttribute("message", "Account created successfully. Please login.");

        return "auth/login";
    }

    // ─── SIGNUP PAGE ──────────────────────────────
    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    // ─── SIGNUP SUBMIT ────────────────────────────
    @PostMapping("/signup")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {

        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email already exists!");
            return "auth/signup";
        }

        userService.registerUser(name, email, password);

        return "redirect:/login?registered=true";
    }
}