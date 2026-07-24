package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.Services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@Controller
//@RequestMapping("emails/")
//@RequiredArgsConstructor
public class TestMailController {

//    private final EmailService emailService;
//    private final PDFService pdfService;
//    private final InvoiceService invoiceService;
//    private final PurchaseService purchaseService;
//    private final InvoiceHtmlService invoiceHtmlService;
//
//    @PostMapping("/test-email")
//    public ResponseEntity<?> testEmail() {
//
//        emailService.sendEmail(
//                "test@test.com",
//                "Test",
//                "MailHog works"
//        );
//
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/test-pdf-email")
//    public ResponseEntity<?> testPdfEmail() {
//
//        byte[] pdf =
//                pdfService.generateTestPdf();
//
//        emailService.sendHTMLEmailWithPDFAttachment(
//                "test@test.com",
//                "PDF Test",
//                "PDF attachment works.",
//                pdf,
//                "invoice"
//        );
//
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/test-pdf-invoice")
//    public ResponseEntity<?> testPdfInvoice(@RequestParam String identifierId, @RequestParam String purchaseCode) {
//        InvoiceFullDTO invoiceFullDTO = invoiceService.buildInvoiceForGuest(purchaseCode, identifierId);
//        String pdfHTML = invoiceHtmlService.buildInvoicePdfString(invoiceFullDTO);
//        byte[] invoicePdf = pdfService.generateInvoicePdf(pdfHTML);
////        String emailHTML = invoiceHtmlService.buildEmailHTML(invoiceFullDTO);
//        emailService.sendHTMLEmailWithPDFAttachment( "test@test.com",
//                "PDF Test",
//                "PDF attachment for invoice.",
//                invoicePdf,
//                "invoice");
//        return ResponseEntity.ok().build();
//    }
}
