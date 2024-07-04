package com.techacademy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import com.techacademy.repository.EmployeeRepository;

//************************************************************************************************************************************************************
//【基本設定】

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

//************************************************************************************************************************************************************
//【一覧画面に関するメソッド】

    public boolean isAdmin(String username) {
        Employee employee = employeeRepository.findByCode(username);
        return employee != null && employee.getRole() == Employee.Role.ADMIN;
    }

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
//【新規登録に関するメソッド】

    public void saveReport(Report report) {
        reportRepository.save(report);
    }

//************************************************************************************************************************************************************



}
