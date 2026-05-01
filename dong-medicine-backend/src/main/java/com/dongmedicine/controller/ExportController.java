package com.dongmedicine.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.dongmedicine.entity.*;
import com.dongmedicine.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/export")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class ExportController {

    private final UserService userService;
    private final PlantService plantService;
    private final KnowledgeService knowledgeService;
    private final InheritorService inheritorService;
    private final ResourceService resourceService;
    private final QaService qaService;
    private final CommentService commentService;
    private final FeedbackService feedbackService;
    private final QuizService quizService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_EXPORT_ROWS = 10000;

    @GetMapping("/{entity}")
    public void export(@PathVariable String entity,
                       @RequestParam(defaultValue = "csv") String format,
                       HttpServletResponse response) throws IOException {
        if (!"csv".equalsIgnoreCase(format)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "仅支持CSV格式");
            return;
        }

        List<?> records = fetchRecords(entity);
        if (records == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不支持的导出实体: " + entity);
            return;
        }

        String filename = entity + "_export_" + java.time.LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        // Write BOM for Excel UTF-8 compatibility
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            if (!records.isEmpty()) {
                writeCsv(writer, records);
            }
            writer.flush();
        }
    }

    private List<?> fetchRecords(String entity) {
        return switch (entity.toLowerCase()) {
            case "users" -> userService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "plants" -> plantService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "knowledge" -> knowledgeService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "inheritors" -> inheritorService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "resources" -> resourceService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "qa" -> qaService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "comments" -> commentService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "feedback" -> feedbackService.list().stream().limit(MAX_EXPORT_ROWS).toList();
            case "quiz-questions" -> quizService.getAllQuestions().stream().limit(MAX_EXPORT_ROWS).toList();
            default -> null;
        };
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private void writeCsv(PrintWriter writer, List<?> records) {
        if (records.isEmpty()) return;

        Class<?> clazz = records.get(0).getClass();
        List<Field> fields = getAllFields(clazz);
        for (Field f : fields) {
            f.setAccessible(true);
        }

        // Write header
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) header.append(",");
            header.append(escapeCsv(fields.get(i).getName()));
        }
        writer.println(header);

        // Write data rows
        for (Object record : records) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) row.append(",");
                try {
                    Object value = fields.get(i).get(record);
                    row.append(formatCsvValue(value));
                } catch (IllegalAccessException e) {
                    row.append("");
                }
            }
            writer.println(row);
        }
    }

    private String formatCsvValue(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDateTime dt) {
            return escapeCsv(dt.format(DATE_FMT));
        }
        if (value instanceof Collection || value.getClass().isArray()) {
            try {
                return escapeCsv(objectMapper.writeValueAsString(value));
            } catch (Exception e) {
                return escapeCsv(value.toString());
            }
        }
        return escapeCsv(value.toString());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
