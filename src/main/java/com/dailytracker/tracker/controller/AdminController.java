package com.dailytracker.tracker.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dailytracker.tracker.model.Habit;
import com.dailytracker.tracker.model.User;
import com.dailytracker.tracker.repository.HabitRepository;
import com.dailytracker.tracker.repository.UserRepository;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // 🔥 SECURITY (VERY IMPORTANT)
public class AdminController {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;

    public AdminController(UserRepository userRepository,
                           HabitRepository habitRepository) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
    }

    // ================= VIEW ALL USERS =================
    @GetMapping("/users")
    public String users(Model model) {

        List<User> users = userRepository.findAll();

        model.addAttribute("users", users);

        return "admin/users";
    }

    // ================= VIEW USER HABITS =================
    @GetMapping("/user/{id}")
    public String userHabits(@PathVariable Long id, Model model) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Habit> habits = habitRepository.findByUser(user);

        model.addAttribute("user", user);      // 🔥 important for UI
        model.addAttribute("habits", habits);

        return "admin/habits";
    }

    // ================= OPTIONAL: DELETE USER =================
    @GetMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {

        userRepository.deleteById(id);

        return "redirect:/admin/users";
    }
}