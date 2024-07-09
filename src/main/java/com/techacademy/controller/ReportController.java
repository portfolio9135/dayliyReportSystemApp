package com.techacademy.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

        List<Report> reports;


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


        // ログインユーザーが管理者かどうかを判断して、リストと、リストサイズを取得する
        if (reportService.isAdmin(username)) {
            reports = reportService.getAllReports();
        } else {
            reports = reportService.getReportsByUsername(username);
        }

        // レポートリストに対して社員名をセットする
        for (Report report : reports) {
            Employee employee = employeeService.getEmployeeByCode(report.getEmployeeCode());
            report.setEmployee(employee);
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
// 【日報詳細画面】

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            // エラーメッセージを表示するための処理
            model.addAttribute("error", "指定された日報が存在しません");
            return "error";
        }
        model.addAttribute("report", report);
        return "reports/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id, Model model) {
        try {
            reportService.deleteReportById(id);
            return "redirect:/reports";
        } catch (Exception e) {
            model.addAttribute("deleteError", "削除に失敗しました");
            return "reports/detail";
        }
    }

//************************************************************************************************************************************************************
    //【日報更新画面】
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        Report report = reportService.getReport(id);
        System.out.println("デバッグ: 取得したレポートの日付: " + report.getReportDate());
        model.addAttribute("report", report);

        // 日報を書いた人の氏名を取得してモデルに追加
        Employee reportAuthor = report.getEmployee();
        String reportAuthorName = reportAuthor != null ? reportAuthor.getName() : "不明";
        model.addAttribute("reportAuthorName", reportAuthorName);

        // フォーマット済みの日付をモデルに追加
        String formattedDate = report.getReportDate().toString();
        model.addAttribute("formattedDate", formattedDate);

        return "reports/update";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id, @Validated Report report, BindingResult res, Model model) {
        System.out.println("デバッグ: updateメソッドが呼び出されました。ID: " + id);
        System.out.println("デバッグ: 更新するレポートの日付: " + report.getReportDate());

        if (res.hasErrors()) {
            System.out.println("デバッグ: バリデーションエラーが発生しました。");
            model.addAttribute("report", report);
            return "reports/update";
        }

        try {
            // 既存のレポートを取得してから、更新する
            Report existingReport = reportService.getReport(id);
            System.out.println("デバッグ: 既存のレポートを取得しました。Report: " + existingReport);

            existingReport.setReportDate(report.getReportDate());
            existingReport.setTitle(report.getTitle());
            existingReport.setContent(report.getContent());

            // ここで更新
            reportService.update(existingReport);
            System.out.println("デバッグ: レポートを更新しました。");
        } catch (Exception e) {
            System.out.println("エラー: 更新に失敗しました。" + e.getMessage());
            model.addAttribute("error", "更新に失敗しました。");
            model.addAttribute("report", report);
            return "reports/update";
        }

        return "redirect:/reports";
    }




//************************************************************************************************************************************************************

}


