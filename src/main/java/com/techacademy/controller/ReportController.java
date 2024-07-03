package com.techacademy.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.service.ReportService;

@Controller
@RequestMapping("reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String list(Model model, Principal principal) {
        // ログインユーザーのユーザー名を取得
        String username = principal.getName();

        // ログインユーザーが管理者かどうかを判断して、リストを取得
        if (reportService.isAdmin(username)) {
            model.addAttribute("reportList", reportService.getAllReports());
        } else {
            model.addAttribute("reportList", reportService.getReportsByUsername(username));
        }

        // 日報のリストサイズを追加
        model.addAttribute("listSize", reportService.getReportListSize());

        return "reports/list";
    }
}
