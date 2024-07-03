package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techacademy.entity.Report;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findAllByOrderByReportDateDesc();
    List<Report> findByEmployeeCodeOrderByReportDateDesc(String employeeCode);
}
