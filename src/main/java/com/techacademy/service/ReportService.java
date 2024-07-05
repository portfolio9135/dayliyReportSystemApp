package com.techacademy.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

import jakarta.transaction.Transactional;

import com.techacademy.repository.EmployeeRepository;

//************************************************************************************************************************************************************
//【基本的なメソッド】

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public boolean isAdmin(String username) {
        Employee employee = employeeRepository.findByCode(username);
        return employee != null && employee.getRole() == Employee.Role.ADMIN;
    }

  //************************************************************************************************************************************************************
  //【一覧表示画面に関するメソッド】

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByReportDateDesc();
    }

    public List<Report> getReportsByUsername(String username) {
        return reportRepository.findByEmployeeCodeOrderByReportDateDesc(username);
    }

    public int getReportListSize() {
        return reportRepository.findAll().size();
    }

  //************************************************************************************************************************************************************
  //【新規登録画面に関するメソッド】

    public void saveReport(Report report) {
        System.out.println("デバッグ: 日報をデータベースに保存します");
        reportRepository.save(report);
    }

    public boolean isReportDateDuplicate(String employeeCode, LocalDate reportDate) {
        List<Report> reports = reportRepository.findByEmployeeCodeAndReportDate(employeeCode, reportDate);
        return !reports.isEmpty();
    }

//************************************************************************************************************************************************************
//【詳細画面に関するメソッド】

    public Report getReportById(Integer id) {
        return reportRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteReportById(Integer id) {
        Optional<Report> report = reportRepository.findById(id);
        if (report.isPresent()) {
            Report r = report.get();
            r.setDeleteFlg(true);  // 論理削除フラグを立てる
            reportRepository.save(r);
        } else {
            throw new RuntimeException("レポートが見つかりませんでした");
        }
    }

//************************************************************************************************************************************************************


}
