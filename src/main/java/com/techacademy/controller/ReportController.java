package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;



//***************************************************************************************************************************************

@Controller
@RequestMapping("reports")
public class ReportController {

	//これで reportService のインスタンスを保持する変数を定義
    private final ReportService reportService;

    //コンストラクタで reportService を注入してる。Spring が自動的にインスタンスを作って渡してくれる。
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面 モデルに全日報リストとサイズを追加してる
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", reportService.findAll().size());
        model.addAttribute("reportList", reportService.findAll());

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {

        model.addAttribute("report", reportService.findByCode(code));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report) {

        return "reports/new";
    }

    // 日報新規登録処理 エラーチェックして登録したりしてる
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model) {

    	// パスワードが空白やったらエラーメッセージを表示して登録画面を再表示する
        if ("".equals(report.getPassword())) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
            ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(report);
        }

        // 入力チェックエラーがあれば登録画面を再表示する
        if (res.hasErrors()) {
            return create(report);
        }

        // 日報情報を保存しに行くで。エラーがあれば再度登録画面を表示する
        try {
            ErrorKinds result = reportService.save(report);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report);
            }

        // 重複エラーが発生したらエラーメッセージを表示して登録画面を再表示する
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(report);
        }

        // 登録成功したら日報一覧画面にリダイレクトする
        return "redirect:/reports";
    }

 //***************************************************************************************************************************************























//***************************************************************************************************************************************
//ここから追記__【課題② 日報更新画面の実装】
////***************************************************************************************************************************************
//
// // 日報登録情報の更新画面
//    @GetMapping(value = "/update/{code}")
//    public String showUpdateForm(@PathVariable String code, Model model) {
//
//        // 指定した日報コードの情報を取得して更新画面に表示する
//        report report = reportService.findByCode(code);
//
//        // モデルに日報情報を追加して更新画面に渡す
//        model.addAttribute("report", report);
//
//        return "reports/update";
//    }
//
// // 日報更新処理 エラーチェックしてから日報情報を更新する
//    @PostMapping(value = "/update")
//    public String update(@Validated report report, BindingResult res, Model model) {
//        if (res.hasErrors()) {
//            model.addAttribute("report", report);
//            return "reports/update";
//        }
//
//        // パスワードが指定されていたらチェックしてエラーがあればエラーメッセージを表示する
//        try {
//            if (report.getPassword() != null && !report.getPassword().isEmpty()) {
//                ErrorKinds result = reportService.reportPasswordCheckForUpdate(report.getPassword());
//
//                if (result != ErrorKinds.CHECK_OK) {
//                    model.addAttribute("passwordError", ErrorMessage.getErrorValue(result));
//                    model.addAttribute("report", report);
//                    return "reports/update";
//                }
//
//                // 新しいパスワードを暗号化してセットする
//                report.setPassword(reportService.encryptPassword(report.getPassword()));
//            } else {
//                // パスワードが空欄の場合、現在のパスワードを保持するようにする
//                report existingreport = reportService.findByCode(report.getCode());
//                report.setPassword(existingreport.getPassword());
//            }
//
//            // 日報情報を更新する
//            reportService.update(report);
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("passwordError", e.getMessage());
//            model.addAttribute("report", report);
//            return "reports/update";
//        } catch (Exception e) {
//            model.addAttribute("error", "更新に失敗しました。");
//            model.addAttribute("report", report);
//            return "reports/update";
//        }
//
//        // 更新成功したら日報一覧画面にリダイレクトする
//        return "redirect:/reports";
//    }

//***************************************************************************************************************************************
//ここまで追記__【課題② 日報更新画面の実装】
//***************************************************************************************************************************************























//***************************************************************************************************************************************
// 日報削除処理
//***************************************************************************************************************************************

//    @PostMapping(value = "/{code}/delete")
//    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {
//
//        ErrorKinds result = reportService.delete(code, userDetail);
//
//        if (ErrorMessage.contains(result)) {
//            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
//            model.addAttribute("report", reportService.findByCode(code));
//            return detail(code, model);
//        }
//
//        return "redirect:/reports";
//    }

}

//***************************************************************************************************************************************