package com.dailytracker.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dailytracker.tracker.model.Habit;
import com.dailytracker.tracker.model.User;

public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUser(User user);

    List<Habit> findByUserAndCategory(User user, String category);
}