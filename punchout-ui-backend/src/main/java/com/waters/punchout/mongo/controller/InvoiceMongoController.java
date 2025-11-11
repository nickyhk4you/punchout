package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.InvoiceDTO;
import com.waters.punchout.dto.NetworkRequestDTO;
import com.waters.punchout.mongo.service.InvoiceMongoService;
import com.waters.punchout.mongo.service.NetworkRequestMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InvoiceMongoController {
    
    private final InvoiceMongoService invoiceService;
    private final NetworkRequestMongoService networkRequestService;
    
    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String customerId
    ) {
        log.info("GET /api/v1/invoices - status={}, environment={}, customerId={}", status, environment, customerId);
        
        List<InvoiceDTO> invoices;
        
        if (status != null && !status.isEmpty()) {
            invoices = invoiceService.getInvoicesByStatus(status);
        } else if (environment != null && !environment.isEmpty()) {
            invoices = invoiceService.getInvoicesByEnvironment(environment);
        } else if (customerId != null && !customerId.isEmpty()) {
            invoices = invoiceService.getInvoicesByOrderId(customerId);
        } else {
            invoices = invoiceService.getAllInvoices();
        }
        
        log.info("Returning {} invoices", invoices.size());
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.info("GET /api/v1/invoices/{}", invoiceNumber);
        
        InvoiceDTO invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        
        if (invoice == null) {
            log.warn("Invoice not found: {}", invoiceNumber);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping("/{invoiceNumber}/network-requests")
    public ResponseEntity<List<NetworkRequestDTO>> getNetworkRequestsForInvoice(@PathVariable String invoiceNumber) {
        log.info("GET /api/v1/invoices/{}/network-requests", invoiceNumber);
        
        // Get invoice to find session key
        InvoiceDTO invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<NetworkRequestDTO> allRequests = networkRequestService.getAllNetworkRequests();
        
        // Filter by invoice number in network requests
        List<NetworkRequestDTO> invoiceRequests = allRequests.stream()
                .filter(req -> invoiceNumber.equals(req.getInvoiceNumber()) || 
                               (invoice.getSessionKey() != null && invoice.getSessionKey().equals(req.getSessionKey())))
                .collect(Collectors.toList());
        
        log.info("Found {} network requests for invoice {}", invoiceRequests.size(), invoiceNumber);
        return ResponseEntity.ok(invoiceRequests);
    }
    
    @GetMapping("/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String invoiceNumber) {
        log.info("GET /api/v1/invoices/{}/pdf - Generating PDF", invoiceNumber);
        
        InvoiceDTO invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        if (invoice == null) {
            log.warn("Invoice not found: {}", invoiceNumber);
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] pdfBytes = generateInvoicePdf(invoice);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", invoiceNumber + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            log.info("PDF generated successfully for invoice: {}", invoiceNumber);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice: {}", invoiceNumber, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private byte[] generateInvoicePdf(InvoiceDTO invoice) {
        // Simple HTML-to-PDF generation (can be enhanced with iText, Apache PDFBox, etc.)
        StringBuilder html = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }");
        html.append(".header h1 { color: #ea580c; margin: 0; }");
        html.append(".section { margin: 20px 0; }");
        html.append(".section h2 { color: #333; border-bottom: 1px solid #ddd; padding-bottom: 10px; }");
        html.append(".info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }");
        html.append(".info-item { margin: 10px 0; }");
        html.append(".label { font-weight: bold; color: #666; }");
        html.append(".value { color: #333; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f3f4f6; font-weight: bold; }");
        html.append(".total-row { font-weight: bold; background-color: #f9fafb; }");
        html.append(".text-right { text-align: right; }");
        html.append(".badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold; }");
        html.append(".badge-paid { background-color: #d1fae5; color: #065f46; }");
        html.append(".badge-pending { background-color: #fef3c7; color: #92400e; }");
        html.append(".badge-confirmed { background-color: #dbeafe; color: #1e40af; }");
        html.append("</style></head><body>");
        
        // Header
        html.append("<div class=\"header\">");
        html.append("<h1>INVOICE</h1>");
        html.append("<p>").append(invoice.getSupplierName() != null ? invoice.getSupplierName() : "Waters Corporation").append("</p>");
        html.append("</div>");
        
        // Invoice Info
        html.append("<div class=\"section\">");
        html.append("<div class=\"info-grid\">");
        html.append("<div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Invoice Number:</span> <span class=\"value\">").append(invoice.getInvoiceNumber()).append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">PO Number:</span> <span class=\"value\">").append(invoice.getPoNumber()).append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Invoice Date:</span> <span class=\"value\">").append(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(dateFormatter) : "").append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Due Date:</span> <span class=\"value\">").append(invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormatter) : "").append("</span></div>");
        html.append("</div>");
        html.append("<div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Customer:</span> <span class=\"value\">").append(invoice.getCustomerName()).append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Customer ID:</span> <span class=\"value\">").append(invoice.getCustomerId()).append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Status:</span> <span class=\"badge badge-").append(invoice.getStatus().toLowerCase()).append("\">").append(invoice.getStatus()).append("</span></div>");
        html.append("<div class=\"info-item\"><span class=\"label\">Payment Terms:</span> <span class=\"value\">").append(invoice.getPaymentTerms() != null ? invoice.getPaymentTerms() : "").append("</span></div>");
        html.append("</div>");
        html.append("</div></div>");
        
        // Line Items
        html.append("<div class=\"section\">");
        html.append("<h2>Line Items</h2>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Line</th><th>Part Number</th><th>Description</th><th class=\"text-right\">Qty</th><th class=\"text-right\">Unit Price</th><th class=\"text-right\">Extended</th>");
        html.append("</tr></thead><tbody>");
        
        if (invoice.getLineItems() != null) {
            for (Map<String, Object> item : invoice.getLineItems()) {
                html.append("<tr>");
                html.append("<td>").append(item.get("lineNumber")).append("</td>");
                html.append("<td>").append(item.get("partNumber")).append("</td>");
                html.append("<td>").append(item.get("description")).append("</td>");
                html.append("<td class=\"text-right\">").append(item.get("quantity")).append("</td>");
                html.append("<td class=\"text-right\">$").append(item.get("unitPrice")).append("</td>");
                html.append("<td class=\"text-right\">$").append(item.get("extendedAmount")).append("</td>");
                html.append("</tr>");
            }
        }
        
        // Totals
        html.append("<tr class=\"total-row\"><td colspan=\"5\" class=\"text-right\">Subtotal:</td><td class=\"text-right\">$").append(invoice.getSubtotal()).append("</td></tr>");
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0) {
            html.append("<tr class=\"total-row\"><td colspan=\"5\" class=\"text-right\">Tax:</td><td class=\"text-right\">$").append(invoice.getTaxAmount()).append("</td></tr>");
        }
        if (invoice.getShippingAmount() != null && invoice.getShippingAmount().doubleValue() > 0) {
            html.append("<tr class=\"total-row\"><td colspan=\"5\" class=\"text-right\">Shipping:</td><td class=\"text-right\">$").append(invoice.getShippingAmount()).append("</td></tr>");
        }
        html.append("<tr class=\"total-row\"><td colspan=\"5\" class=\"text-right\"><strong>Total:</strong></td><td class=\"text-right\"><strong>$").append(invoice.getInvoiceTotal()).append("</strong></td></tr>");
        html.append("</tbody></table>");
        html.append("</div>");
        
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            html.append("<div class=\"section\">");
            html.append("<h2>Notes</h2>");
            html.append("<p>").append(invoice.getNotes()).append("</p>");
            html.append("</div>");
        }
        
        html.append("</body></html>");
        
        // For now, return HTML as PDF placeholder
        // In production, use library like iText or Flying Saucer to convert HTML to PDF
        return html.toString().getBytes();
    }
}
