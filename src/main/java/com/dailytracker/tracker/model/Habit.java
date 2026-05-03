package com.dailytracker.tracker.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "habits")
public class Habit {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;

    // ================= USER =================
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ================= TARGET =================
    private int dailyTarget = 0;

    // ================= COMPLETED DATES =================
    @ElementCollection
    @CollectionTable(name = "habit_completed_dates", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "date")
    private List<LocalDate> completedDates = new ArrayList<>();

    // ================= DAILY PROGRESS =================
    @ElementCollection
    @CollectionTable(name = "habit_progress", joinColumns = @JoinColumn(name = "habit_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "value")
    private Map<LocalDate, Integer> dailyProgress = new HashMap<>();

    // ================= NOTES =================
    @ElementCollection
    @CollectionTable(name = "habit_notes", joinColumns = @JoinColumn(name = "habit_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "note")
    private Map<LocalDate, String> dailyNotes = new HashMap<>();

    // ================= CONSTRUCTORS =================
    public Habit() {}

    public Habit(String title, String category, User user) {
        this.title = title;
        this.category = category;
        this.user = user;
    }

    // ================= GETTERS =================
    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getCategory() { return category; }

    public int getDailyTarget() { return dailyTarget; }

    public User getUser() { return user; }

    public List<LocalDate> getCompletedDates() { return completedDates; }

    public Map<LocalDate, Integer> getDailyProgress() { return dailyProgress; }

    public Map<LocalDate, String> getDailyNotes() { return dailyNotes; }

    // ================= SETTERS =================
    public void setDailyTarget(int target) {
        this.dailyTarget = target;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ================= DAILY PROGRESS =================
    public int getTodayActual() {
        return dailyProgress.getOrDefault(LocalDate.now(), 0);
    }

    public int getTotalActual() {
        return dailyProgress.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void addActual(int count) {
        LocalDate today = LocalDate.now();

        int updated = dailyProgress.getOrDefault(today, 0) + count;
        dailyProgress.put(today, updated);

        if (isTargetAchieved()) {
            addCompletion(today);
        }
    }

    // ================= NOTES =================
    public void addNote(String note) {
        if (note == null || note.isEmpty()) return;

        LocalDate today = LocalDate.now();

        String existing = dailyNotes.getOrDefault(today, "");
        if (!existing.isEmpty()) {
            existing += " | ";
        }

        dailyNotes.put(today, existing + note);
    }

    public List<String> getTodayNotes() {
        String raw = dailyNotes.getOrDefault(LocalDate.now(), "");
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String part : raw.split("\\s*\\|\\s*")) {
            if (!part.trim().isEmpty()) result.add(part.trim());
        }
        return result;
    }

    // ================= COMPLETION =================
    public void addCompletion(LocalDate date) {
        if (!completedDates.contains(date)) {
            completedDates.add(date);
        }
    }

    public boolean isTargetAchieved() {
        return dailyTarget > 0 &&
                getTodayActual() >= dailyTarget;
    }

    public boolean getTargetAchieved() {
        return isTargetAchieved();
    }

    // ================= STREAK =================
    public int getStreak() {
        int streak = 0;
        LocalDate today = LocalDate.now();

        while (completedDates.contains(today.minusDays(streak))) {
            streak++;
        }

        return streak;
    }

    public int getLongestStreak() {
        if (completedDates.isEmpty()) return 0;

        List<LocalDate> sorted = new ArrayList<>(completedDates);
        Collections.sort(sorted);

        int longest = 0;
        int current = 0;

        for (int i = 0; i < sorted.size(); i++) {
            if (i == 0 || sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                current++;
            } else {
                current = 1;
            }
            longest = Math.max(longest, current);
        }

        return longest;
    }

    // ================= CONSISTENCY =================
    public double getConsistency() {
        if (completedDates.isEmpty()) return 0;

        YearMonth month = YearMonth.now();
        int today = LocalDate.now().getDayOfMonth();

        long completedThisMonth = completedDates.stream()
                .filter(d -> d.getMonth() == month.getMonth()
                        && d.getYear() == month.getYear())
                .count();

        return (completedThisMonth * 100.0) / today;
    }
}