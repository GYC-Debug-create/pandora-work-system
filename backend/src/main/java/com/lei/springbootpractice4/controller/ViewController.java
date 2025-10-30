// 文件路径: backend/src/main/java/com/lei/springbootpractice4/controller/ViewController.java
package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.config.JwtUtil;
import com.lei.springbootpractice4.pojo.SubTaskPojo;
import com.lei.springbootpractice4.service.ViewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/view") // 视图接口的基础路径
public class ViewController {

    @Autowired
    private ViewService viewService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取月视图数据
    @GetMapping("/month")
    public ResponseEntity<Object> getMonthView(@RequestParam("date") String dateString, HttpServletRequest request) {
        return getViewData(dateString, request, "month");
    }

    // 获取周视图数据
    @GetMapping("/week")
    public ResponseEntity<Object> getWeekView(@RequestParam("date") String dateString, HttpServletRequest request) {
        return getViewData(dateString, request, "week");
    }

    // 获取日视图数据
    @GetMapping("/day")
    public ResponseEntity<Object> getDayView(@RequestParam("date") String dateString, HttpServletRequest request) {
        return getViewData(dateString, request, "day");
    }

    // --- 统一处理逻辑 ---
    private ResponseEntity<Object> getViewData(String dateString, HttpServletRequest request, String viewType) {
        LocalDate date;
        try {
            // 确保传入的日期格式为 YYYY-MM-DD
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "日期格式无效，必须为 YYYY-MM-DD"));
        }

        try {
            String currentUserId = getUserIdFromRequest(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "无法从Token获取用户信息"));
            }

            List<SubTaskPojo> tasks;
            switch (viewType) {
                case "month":
                    tasks = viewService.getMonthViewTasks(currentUserId, date);
                    break;
                case "week":
                    tasks = viewService.getWeekViewTasks(currentUserId, date);
                    break;
                case "day":
                    tasks = viewService.getDayViewTasks(currentUserId, date);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "无效的视图类型"));
            }
            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            System.err.println("获取" + viewType + "视图失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "获取视图数据失败"));
        }
    }

    // --- 辅助方法：从 Token 获取 UserId ---
    private String getUserIdFromRequest(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        String currentUserId = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                currentUserId = jwtUtil.getUserIdFromToken(jwtToken);
            } catch (Exception e) {
                System.err.println("从Token获取userId失败: " + e.getMessage());
            }
        }
        return currentUserId;
    }
}