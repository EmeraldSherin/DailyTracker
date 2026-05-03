package com.dailytracker.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dailytracker.tracker.model.Habit;
import com.dailytracker.tracker.model.User;
import com.dailytracker.tracker.repository.HabitRepository;
import com.dailytracker.tracker.repository.UserRepository;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitService(HabitRepository habitRepository,
                        UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    // ================= GET ALL USER HABITS =================
    public List<Habit> getUserHabits(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return habitRepository.findByUser(user);
    }

    // ================= GET BY CATEGORY =================
    public List<Habit> getUserHabitsByCategory(String email, String category) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return habitRepository.findByUserAndCategory(user, category);
    }

    // ================= ADD HABIT =================
    public void addHabit(String email, String title, String category) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Habit habit = new Habit(title, category, user);

        habitRepository.save(habit);
    }

    // ================= UPDATE PROGRESS =================
    public void updateProgress(Long id, int count, String note) {

        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        habit.addActual(count);

        if (note != null && !note.isEmpty()) {
            habit.addNote(note);
        }

        habitRepository.save(habit);
    }

    // ================= SET TARGET =================
    public void setTarget(Long id, int target) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        habit.setDailyTarget(target);

        habitRepository.save(habit);
    }

    // ================= DELETE =================
    public void deleteHabit(Long id) {
        habitRepository.deleteById(id);
    }

    // ================= TOGGLE TODAY =================
    public void toggleToday(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        java.time.LocalDate today = java.time.LocalDate.now();

        if (habit.getCompletedDates().contains(today)) {
            habit.getCompletedDates().remove(today);
        } else {
            habit.addCompletion(today);
        }

        habitRepository.save(habit);
    }
}