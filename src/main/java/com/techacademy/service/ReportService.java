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
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {
        // パスワードチェック
        ErrorKinds result = reportPasswordCheck(report);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 日報番号重複チェック
        if (findByCode(report.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public void update(Report updatedReport) {
        // まず、更新対象の日報をデータベースから取得
        Report existingReport = reportRepository.findById(updatedReport.getCode()).orElse(null);

        // もしデータベースに日報の情報があったら、以下の処理をする
        if (existingReport != null) {
            // 更新された名前を既存の日報情報にセットする
            existingReport.setName(updatedReport.getName());

            // 更新されたタイトルを既存の日報情報にセットする
//            existingReport.setTitle(updatedReport.getTitle());

            // パスワードはすでに暗号化されているものをセットする
            existingReport.setPassword(updatedReport.getPassword());

            // 更新日時を現在の日時に設定する
            existingReport.setUpdatedAt(LocalDateTime.now());

            // 最後に、データベースに更新された日報情報を保存する
            reportRepository.save(existingReport);
        } else {
            // もしデータベースに該当する日報が見つからなかったら、エラーを投げる
            throw new EntityNotFoundException("Report with code " + updatedReport.getCode() + " not found");
        }
    }

    // 更新時の日報パスワードチェック
    public ErrorKinds reportPasswordCheckForUpdate(String password) {
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

    // 日報パスワードの暗号化
    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }







    // 日報削除
//    @Transactional
//    public ErrorKinds delete(String code, UserDetail userDetail) {
//        // 自分を削除しようとした場合はエラーメッセージを表示
//        if (code.equals(userDetail.getReport().getCode())) {
//            return ErrorKinds.LOGINCHECK_ERROR;
//        }
//        Report report = findByCode(code);
//        LocalDateTime now = LocalDateTime.now();
//        report.setUpdatedAt(now);
//        report.setDeleteFlg(true);
//
//        return ErrorKinds.SUCCESS;
//    }



    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findByCode(String code) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    // 日報パスワードチェック
    private ErrorKinds reportPasswordCheck(Report report) {
        // 日報パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(report)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 日報パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(report)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        report.setPassword(passwordEncoder.encode(report.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 日報パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Report report) {
        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(report.getPassword());
        return !matcher.matches();
    }

    // 日報パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Report report) {
        // 桁数チェック
        int passwordLength = report.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }
}
