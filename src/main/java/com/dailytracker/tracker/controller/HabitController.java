package com.dailytracker.tracker.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dailytracker.tracker.model.Habit;
import com.dailytracker.tracker.service.ExcelExportService;
import com.dailytracker.tracker.service.HabitService;

@Controller
public class HabitController {

    private final HabitService habitService;
    private final ExcelExportService excelExportService;

    public HabitController(HabitService habitService,
                           ExcelExportService excelExportService) {
        this.habitService = habitService;
        this.excelExportService = excelExportService;
    }

    // ================= HOME =================

@GetMapping("/")
public String home(Model model, Authentication auth) {

    String email = auth.getName();

    // ✅ Get user habits
    List<Habit> habits = habitService.getUserHabits(email);

    // ✅ Calculate stats
    int totalHabits = habits.size();

    int completedToday = (int) habits.stream()
            .filter(Habit::isTargetAchieved)
            .count();

    double percentage = 0;
    if (totalHabits > 0) {
        percentage = (completedToday * 100.0) / totalHabits;
    }

    // ✅ Send to UI
    // Build list of day numbers for current month up to today
    java.time.LocalDate today = java.time.LocalDate.now();
    java.util.List<Integer> days = new java.util.ArrayList<>();
    for (int d = 1; d <= today.getDayOfMonth(); d++) days.add(d);

    // Send to UI
    model.addAttribute("habits", habits);
    model.addAttribute("today", today);
    model.addAttribute("days", days);
    model.addAttribute("completedToday", completedToday);
    model.addAttribute("totalHabits", totalHabits);
    model.addAttribute("percentage", percentage);

    return "index";
}

    // ================= ADD =================

    @PostMapping("/add")
    public String addHabit(@RequestParam String title,
                           @RequestParam String category,
                           Authentication auth) {

        String email = auth.getName();

        habitService.addHabit(email, title, category);

        return "redirect:/";
    }

    // ================= UPDATE PROGRESS =================

    @PostMapping("/update-progress")
    public String updateProgress(@RequestParam Long id,
                                 @RequestParam int count,
                                 @RequestParam(required = false) String note,
                                 Authentication auth) {

        habitService.updateProgress(id, count, note);

        return "redirect:/";
    }

    // ================= SET TARGET =================

    @PostMapping("/set-target")
    public String setTarget(@RequestParam Long id,
                            @RequestParam int target,
                            @RequestParam(required = false) String category) {

        habitService.setTarget(id, target);

        if (category != null && !category.isBlank()) {
            return "redirect:/category/" + category;
        }
        return "redirect:/";
    }

    // ================= MARK (toggle completion) =================

    @GetMapping("/mark/{category}/{id}")
    public String markHabit(@PathVariable String category,
                            @PathVariable Long id) {

        habitService.toggleToday(id);

        return "redirect:/";
    }

    // ================= DELETE =================

    @GetMapping("/delete/{id}")
    public String deleteHabit(@PathVariable Long id) {

        habitService.deleteHabit(id);

        return "redirect:/";
    }

    // ================= CATEGORY =================

    @GetMapping("/category/{name}")
    public String categoryPage(@PathVariable String name,
                               Model model,
                               Authentication auth) {

        String email = auth.getName();

        model.addAttribute("habits",
                habitService.getUserHabitsByCategory(email, name.toUpperCase()));

        model.addAttribute("categoryName", name.toUpperCase());

        return "category";
    }

    // ================= ANALYTICS =================

    @GetMapping("/analytics")
    public String analytics(Model model, Authentication auth) {

        String email = auth.getName();

        model.addAttribute("habits", habitService.getUserHabits(email));

        return "analytics";
    }

    // ================= NOTES =================

    @GetMapping("/notes")
    public String notesPage() {
        return "notes";
    }

    // ================= LIVE DATA VIEW (BEST FEATURE) =================

    @GetMapping("/view-data")
    public String viewData(Model model, Authentication auth) {

        String email = auth.getName();

        model.addAttribute("habits", habitService.getUserHabits(email));

        return "data-view";
    }

    // ================= EXCEL VIEW =================

    @GetMapping("/view-excel")
    public String viewExcel(Model model, Authentication auth) {

        String email = auth.getName();

        // 🔥 ALWAYS generate latest data
        excelExportService.exportUserDataToExcel(email);

        try {
            File file = new File("habits.xlsx");

            if (file.exists()) {
                try (Workbook workbook = WorkbookFactory.create(file)) {

                    List<Map<String, Object>> sheets = readWorkbook(workbook);

                    model.addAttribute("sheets", sheets);
                    model.addAttribute("fileName", "habits.xlsx");
                }
            } else {
                model.addAttribute("error", "habits.xlsx not found.");
            }

        } catch (Exception e) {
            model.addAttribute("error", "Failed to read file: " + e.getMessage());
        }

        return "excel-view";
    }

    // ================= OPTIONAL (SYLLABUS VIEW - KEEP) =================

    @GetMapping("/view-syllabus")
    public String viewSyllabus(Model model) {

        try {
            File file = new File("D:\\tracker\\ELITE 2027 Syllabus +Coding.xlsx");

            if (file.exists()) {
                try (Workbook workbook = WorkbookFactory.create(file)) {

                    List<Map<String, Object>> sheets = readWorkbook(workbook);

                    model.addAttribute("sheets", sheets);
                    model.addAttribute("fileName", "ELITE 2027 Syllabus +Coding.xlsx");
                }
            } else {
                model.addAttribute("error", "Syllabus file not found.");
            }

        } catch (Exception e) {
            model.addAttribute("error", "Failed to read file: " + e.getMessage());
        }

        return "excel-view";
    }

    // ================= DOWNLOAD EXCEL =================

    @GetMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcel(Authentication auth) {

        String email = auth.getName();

        // Always regenerate with latest data before download
        excelExportService.exportUserDataToExcel(email);

        try {
            File file = new File("habits.xlsx");

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = new FileInputStream(file).readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"habits.xlsx\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================= DOWNLOAD SYLLABUS =================

    @GetMapping("/download-syllabus")
    public ResponseEntity<byte[]> downloadSyllabus() {

        try {
            File file = new File("D:\\tracker\\ELITE 2027 Syllabus +Coding.xlsx");

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = new FileInputStream(file).readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"ELITE 2027 Syllabus +Coding.xlsx\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================= EXCEL READER =================

    private List<Map<String, Object>> readWorkbook(Workbook workbook) {

        List<Map<String, Object>> sheets = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

            Sheet sheet = workbook.getSheetAt(i);
            List<List<String>> rows = new ArrayList<>();

            for (Row row : sheet) {

                List<String> cells = new ArrayList<>();

                int lastCell = row.getLastCellNum();
                if (lastCell < 0) lastCell = 0;

                for (int c = 0; c < lastCell; c++) {

                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cells.add(formatter.formatCellValue(cell));
                }

                rows.add(cells);
            }

            Map<String, Object> sheetData = new HashMap<>();
            sheetData.put("name", sheet.getSheetName());
            sheetData.put("rows", rows);

            sheets.add(sheetData);
        }

        return sheets;
    }
}