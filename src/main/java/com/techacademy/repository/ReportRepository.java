package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techacademy.entity.Report;
import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findAllByOrderByReportDateDesc();
    List<Report> findByEmployeeCodeOrderByReportDateDesc(String employeeCode);

    // 新しいメソッドを追加
    List<Report> findByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate);
}
