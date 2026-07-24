package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("purchase/")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SessionService sessionService;
    private final InvoiceService invoiceService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService,
                              SessionService sessionService, InvoiceService invoiceService) {
        this.purchaseService = purchaseService;
        this.sessionService = sessionService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("complete")
    @Transactional
    public ResponseEntity<?> createPurchase(@RequestBody @Valid PurchaseRequest request) {

        Session session = sessionService.getRequestSession();

        if (session.getIsGuest()) {
            if (request.email().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            SuccessfulPurchaseResponse response = purchaseService.completePurchaseForGuest(request, session);

            invoiceService.sentInvoiceEmailForGuest(response.purchaseCode(), request.email(), session.getSessionId());

            return ResponseEntity.ok(response);

        } else {
            Customer customer = session.getCustomer();
            SuccessfulPurchaseResponse response = purchaseService.completePurchaseForCustomer(request, customer);

            invoiceService.sentInvoiceEmailForCustomer(response.purchaseCode(), customer);

            return ResponseEntity.ok(response);
        }

    }

    @GetMapping("purchase-history/p/{page}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getPurchaseHistory(@PathVariable int page) {

        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();
        PageResponse<CompactPurchaseResponse> response = purchaseService.getPurchasesOfCustomer(customer, page);

//        System.out.println("Response: " + response);

        return ResponseEntity.ok(response);
    }


    @GetMapping("detail/c/{code}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getPurchaseDetails(@PathVariable String code) {
        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();

        DetailedPurchaseAdditionalDataResponse response = purchaseService.getDetailedPurchaseInfo(code, customer.getKeycloakId());

//        System.out.println("Response: " + response);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("cancel/c/{code}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> cancelPurchase(@PathVariable String code) {
        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();

        purchaseService.cancelPurchase(code, customer.getKeycloakId());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("request-refund/c/{code}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> requestRefundForPurchase(@PathVariable String code) {
        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();

        purchaseService.requestRefundForPurchase(code, customer.getKeycloakId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/invoice/{purchaseCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String purchaseCode) {

        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();

        byte[] pdfBytes = invoiceService.getInvoicePdfForPurchase(purchaseCode,customer.getKeycloakId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=invoice-" + purchaseCode + ".pdf")
                .body(pdfBytes);
    }

}
