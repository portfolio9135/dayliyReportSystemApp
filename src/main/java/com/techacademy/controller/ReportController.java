package com.techacademy.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;

//************************************************************************************************************************************************************

@Controller
@RequestMapping("reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

//************************************************************************************************************************************************************
//【一覧画面】

    @GetMapping
    public String list(Model model, Principal principal) {
        // ログインユーザーのユーザー名を取得
        String username = principal.getName();

        // ログインユーザーが管理者かどうかを判断して、リストと、リストサイズを取得する
        //もしも管理者だったら全ての日報と日報数を取得
        if (reportService.isAdmin(username)) {
            model.addAttribute("reportList", reportService.getAllReports());
            model.addAttribute("listSize", reportService.getAllReports().size()); // ここで全リストのサイズを設定
          //そうじゃなかったら一般ユーザなので、そのユーザーの日報と日報数を取得
        } else {
            model.addAttribute("reportList", reportService.getReportsByUsername(username));
            model.addAttribute("listSize", reportService.getReportsByUsername(username).size()); // ここでユーザーのリストのサイズを設定
        }

        //reportsディレクトリのlist.htmlを返却
        return "reports/list";
    }

//************************************************************************************************************************************************************
// 【日報新規登録画面】

    @GetMapping("/add")
    public String create(Model model) {
        model.addAttribute("report", new Report());
        return "reports/new";
    }

//************************************************************************************************************************************************************






}
