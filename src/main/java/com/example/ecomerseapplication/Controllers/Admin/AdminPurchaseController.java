package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.DateRangeRequest;
import com.example.ecomerseapplication.DTOs.requests.PurchaseActionRequest;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.Services.Admin.AdminPurchaseService;
import com.example.ecomerseapplication.Services.ReportPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/purchase/")
@PreAuthorize("hasRole(@roles.admin())")
@Validated
public class AdminPurchaseController {

    private final AdminPurchaseService adminPurchaseService;
    private final ReportPdfService reportPdfService;

    public AdminPurchaseController(AdminPurchaseService adminPurchaseService, ReportPdfService reportPdfService) {
        this.adminPurchaseService = adminPurchaseService;
        this.reportPdfService = reportPdfService;
    }

    @GetMapping("all/{page}")
    public ResponseEntity<?> getAllPurchases(@PathVariable int page) {
        return ResponseEntity.ok(adminPurchaseService.getPagedPurchasesCompact(page));
    }

    @GetMapping("pending-refund-count")
    public ResponseEntity<?> getRefundPendingCount() {
        return ResponseEntity.ok(adminPurchaseService.getRefundPendingCount());
    }

    @PatchMapping("purchase-action")
    public ResponseEntity<?> performPurchaseAction(@RequestBody PurchaseActionRequest request) {

        adminPurchaseService.executePurchaseStatusAction(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("revenue-report")
    public ResponseEntity<?> getRevenueReportForPeriod(@RequestBody @Valid DateRangeRequest request) {

        return ResponseEntity.ok(adminPurchaseService.revenueReport(request));
    }

    @PostMapping("revenue-report/pdf")
    public ResponseEntity<?> getRevenueReportForPeriodAsPdf(@RequestBody @Valid DateRangeRequest request) {
        ReportResponses.ReportResponse revenueResponse = adminPurchaseService.revenueReport(request);

        byte[] pdfBytes = reportPdfService.generateReportPdf(revenueResponse, List.of(request.startDate(), request.endDate()), request.timezone());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=revenue-report.pdf")
                .body(pdfBytes);
    }


    @PostMapping("top-selling-for-period")
    public ResponseEntity<?> getTopSellingProductsForPeriod(@RequestBody @Valid DateRangeRequest request,
                                                            @RequestParam("limit") Integer limit) {

//        System.out.println("Inside getTopSellingProductsForPeriod: " + request + "limit: " + limit + "");

        return ResponseEntity.ok(adminPurchaseService.getTopSellingProductsForPeriod(request, limit));
    }

    @PostMapping("top-selling-for-period/pdf")
    public ResponseEntity<?> getTopSellingProductsForPeriodAsPdf(@RequestBody @Valid DateRangeRequest request,
                                                                 @RequestParam("limit") Integer limit) {

        ReportResponses.ReportResponse productResponse = adminPurchaseService.getTopSellingProductsForPeriod(request, limit);

        byte[] pdfBytes = reportPdfService.generateReportPdf(productResponse, List.of(request.startDate(), request.endDate()), request.timezone());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=top-products-report.pdf")
                .body(pdfBytes);

    }

    @PostMapping("purchase-statuses-report")
    public ResponseEntity<?> getStatisticForPurchaseStatuses(@RequestBody @Valid DateRangeRequest request) {

       return ResponseEntity.ok( adminPurchaseService.getPurchaseStatusStatistic(request));
    }

    @PostMapping("purchase-statuses-report/pdf")
    public ResponseEntity<?> getStatisticForPurchaseStatusesPdf(@RequestBody @Valid DateRangeRequest request) {

        ReportResponses.ReportResponse productResponse = adminPurchaseService.getPurchaseStatusStatistic(request);
        ReportResponses.ReportResponse productResponseBG = adminPurchaseService.purchaseStatusReportWithBgTableNames(productResponse);

        byte[] pdfBytes = reportPdfService.generateReportPdf(productResponseBG, List.of(request.startDate(), request.endDate()), request.timezone());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=purchase-status-report.pdf")
                .body(pdfBytes);
    }
}
