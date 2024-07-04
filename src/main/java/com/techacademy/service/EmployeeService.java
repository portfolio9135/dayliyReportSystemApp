package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

//***************************************************************************************************************************************

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {
        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

 //***************************************************************************************************************************************





//*************************************************************************************************************************************************************
//【従業員取得メソッド（ユーザー名で）】
//*************************************************************************************************************************************************************



//*************************************************************************************************************************************************************
//【従業員取得メソッド（ユーザー名で）】
//*************************************************************************************************************************************************************




//*************************************************************************************************************************************************************
//【更新メソッド】
//*************************************************************************************************************************************************************

    @Transactional
    public void update(Employee updatedEmployee) {
        // まず、更新対象の従業員をデータベースから取得
        Employee existingEmployee = employeeRepository.findById(updatedEmployee.getCode()).orElse(null);

        // もしデータベースに従業員の情報があったら、以下の処理をする
        if (existingEmployee != null) {
            // 更新された名前を既存の従業員情報にセットする
            existingEmployee.setName(updatedEmployee.getName());

            // 更新された権限を既存の従業員情報にセットする
            existingEmployee.setRole(updatedEmployee.getRole());

            // パスワードはすでに暗号化されているものをセットする
            existingEmployee.setPassword(updatedEmployee.getPassword());

            // 更新日時を現在の日時に設定する
            existingEmployee.setUpdatedAt(LocalDateTime.now());

            // 最後に、データベースに更新された従業員情報を保存する
            employeeRepository.save(existingEmployee);
        } else {
            // もしデータベースに該当する従業員が見つからなかったら、エラーを投げる
            throw new EntityNotFoundException("Employee with code " + updatedEmployee.getCode() + " not found");
        }
    }

    // 更新時の従業員パスワードチェック
    public ErrorKinds employeePasswordCheckForUpdate(String password) {
        // もしパスワードが空欄やnullだったら、チェックOKを返す
        if (password == null || password.isEmpty()) {
            return ErrorKinds.CHECK_OK;
        }

        // パスワードの長さが8文字未満または16文字を超えていたら、桁数チェックエラーを返す
        if (password.length() < 8 || password.length() > 16) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        // パスワードが半角英数字でなかったら、半角英数字チェックエラーを返す
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches()) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 問題なければ、チェックOKを返す
        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの暗号化
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

//*************************************************************************************************************************************************************
//【更新メソッド】
//*************************************************************************************************************************************************************





//*************************************************************************************************************************************************************
//【削除メソッド】
//*************************************************************************************************************************************************************

    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {
        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {
        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {
        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {
        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }
}

//*************************************************************************************************************************************************************
//【削除メソッド】
//*************************************************************************************************************************************************************



