package com.dailytracker.tracker.service;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.dailytracker.tracker.model.Habit;
import com.dailytracker.tracker.model.User;
import com.dailytracker.tracker.repository.HabitRepository;
import com.dailytracker.tracker.repository.UserRepository;

@Service
public class ExcelExportService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    private static final String FILE_PATH = "habits.xlsx";

    public ExcelExportService(HabitRepository habitRepository,
                              UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    // ================= EXPORT SINGLE USER =================
    public void exportUserDataToExcel(String email) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("My Habits");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Title");
            header.createCell(1).setCellValue("Category");
            header.createCell(2).setCellValue("Daily Target");
            header.createCell(3).setCellValue("Today Progress");

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Habit> habits = habitRepository.findByUser(user);

            int rowNum = 1;

            for (Habit habit : habits) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(habit.getTitle());
                row.createCell(1).setCellValue(habit.getCategory());
                row.createCell(2).setCellValue(habit.getDailyTarget());
                row.createCell(3).setCellValue(habit.getTodayActual());
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EXPORT ALL USERS (ADMIN) =================
    public void exportAllDataToExcel() {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("All Users Data");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Daily Target");
            header.createCell(4).setCellValue("User Email");

            List<Habit> habits = habitRepository.findAll();

            int rowNum = 1;

            for (Habit habit : habits) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(habit.getId());
                row.createCell(1).setCellValue(habit.getTitle());
                row.createCell(2).setCellValue(habit.getCategory());
                row.createCell(3).setCellValue(habit.getDailyTarget());

                // ✅ SAFE NULL CHECK
                String email = (habit.getUser() != null)
                        ? habit.getUser().getEmail()
                        : "N/A";

                row.createCell(4).setCellValue(email);
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}