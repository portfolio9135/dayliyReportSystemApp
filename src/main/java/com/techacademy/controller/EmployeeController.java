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

import com.techacademy.entity.Employee;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;



//***************************************************************************************************************************************

@Controller
@RequestMapping("employees")
public class EmployeeController {

	//これで EmployeeService のインスタンスを保持する変数を定義
    private final EmployeeService employeeService;

    //コンストラクタで EmployeeService を注入してる。Spring が自動的にインスタンスを作って渡してくれる。
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面 モデルに全従業員リストとサイズを追加してる
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());

        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {

        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {

        return "employees/new";
    }

    // 従業員新規登録処理 エラーチェックして登録したりしてる
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {

    	// パスワードが空白やったらエラーメッセージを表示して登録画面を再表示する
        if ("".equals(employee.getPassword())) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
            ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(employee);
        }

        // 入力チェックエラーがあれば登録画面を再表示する
        if (res.hasErrors()) {
            return create(employee);
        }

        // 従業員情報を保存しに行くで。エラーがあれば再度登録画面を表示する
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

        // 重複エラーが発生したらエラーメッセージを表示して登録画面を再表示する
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        // 登録成功したら従業員一覧画面にリダイレクトする
        return "redirect:/employees";
    }

 //***************************************************************************************************************************************























//***************************************************************************************************************************************
//ここから追記__【課題① 従業員更新画面の実装】
//***************************************************************************************************************************************

 // 従業員登録情報の更新画面
    @GetMapping(value = "/update/{code}")
    public String showUpdateForm(@PathVariable String code, Model model) {

        // 指定した従業員コードの情報を取得して更新画面に表示する
        Employee employee = employeeService.findByCode(code);

        // モデルに従業員情報を追加して更新画面に渡す
        model.addAttribute("employee", employee);

        return "employees/update";
    }

 // 従業員更新処理 エラーチェックしてから従業員情報を更新する
    @PostMapping(value = "/update")
    public String update(@Validated Employee employee, BindingResult res, Model model) {
        if (res.hasErrors()) {
            model.addAttribute("employee", employee);
            return "employees/update";
        }

        // パスワードが指定されていたらチェックしてエラーがあればエラーメッセージを表示する
        try {
            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                ErrorKinds result = employeeService.employeePasswordCheckForUpdate(employee.getPassword());

                if (result != ErrorKinds.CHECK_OK) {
                    model.addAttribute("passwordError", ErrorMessage.getErrorValue(result));
                    model.addAttribute("employee", employee);
                    return "employees/update";
                }

                // 新しいパスワードを暗号化してセットする
                employee.setPassword(employeeService.encryptPassword(employee.getPassword()));
            } else {
                // パスワードが空欄の場合、現在のパスワードを保持するようにする
                Employee existingEmployee = employeeService.findByCode(employee.getCode());
                employee.setPassword(existingEmployee.getPassword());
            }

            // 従業員情報を更新する
            employeeService.update(employee);
        } catch (IllegalArgumentException e) {
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("employee", employee);
            return "employees/update";
        } catch (Exception e) {
            model.addAttribute("error", "更新に失敗しました。");
            model.addAttribute("employee", employee);
            return "employees/update";
        }

        // 更新成功したら従業員一覧画面にリダイレクトする
        return "redirect:/employees";
    }

//***************************************************************************************************************************************
//ここまで追記__【課題① 従業員更新画面の実装】
//***************************************************************************************************************************************























//***************************************************************************************************************************************
// 従業員削除処理
//***************************************************************************************************************************************

    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = employeeService.delete(code, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

}

//***************************************************************************************************************************************