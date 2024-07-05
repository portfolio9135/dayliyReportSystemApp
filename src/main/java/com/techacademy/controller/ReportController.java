package com.techacademy.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;

//************************************************************************************************************************************************************
//【基本設定】

@Controller
@RequestMapping("reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmployeeService employeeService;  // ここで EmployeeService を注入

//************************************************************************************************************************************************************
//【一覧画面】

    @GetMapping
    public String list(Model model, Principal principal) {
        System.out.println("デバッグ: 一覧画面表示");
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
    public String create(Model model, Principal principal) {
        Report report = new Report();
        report.setEmployeeCode(principal.getName()); // ログインユーザーの社員番号を設定
        report.setDeleteFlg(false); // 削除フラグをfalseに設定
        model.addAttribute("report", report);

        // ログインユーザー名を取得してモデルに追加
        String username = principal.getName();
        Employee loggedInUser = employeeService.getEmployeeByCode(username);
        model.addAttribute("loggedInUserName", loggedInUser.getName());

        return "reports/new";
    }

    @PostMapping("/add")
    public String create(@ModelAttribute("report") @Validated Report report, BindingResult result, Principal principal, Model model) {
        String employeeCode = principal.getName();

        if (reportService.isReportDateDuplicate(employeeCode, report.getReportDate())) {
            result.rejectValue("reportDate", "error.report", "既に登録されている日付です");
        }

        if (result.hasErrors()) {
            System.out.println("デバッグ: バリデーションエラーが発生しました");
            result.getFieldErrors().forEach(error -> {
                System.out.println("エラー: " + error.getField() + " - " + error.getDefaultMessage());
            });
            Employee loggedInUser = employeeService.getEmployeeByCode(employeeCode);
            model.addAttribute("loggedInUserName", loggedInUser.getName());
            return "reports/new";
        }

        report.setEmployeeCode(employeeCode);
        System.out.println("デバッグ: 日報を保存します");
        reportService.saveReport(report);
        System.out.println("デバッグ: リダイレクトします");
        return "redirect:/reports";
    }

//************************************************************************************************************************************************************

}