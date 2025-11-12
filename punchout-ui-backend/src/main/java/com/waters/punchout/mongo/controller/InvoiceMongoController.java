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

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

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
    
    private byte[] generateInvoicePdf(InvoiceDTO invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.RED);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
        
        // Title
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph supplier = new Paragraph(invoice.getSupplierName() != null ? invoice.getSupplierName() : "Waters Corporation", normalFont);
        supplier.setAlignment(Element.ALIGN_CENTER);
        supplier.setSpacingAfter(20);
        document.add(supplier);
        
        // Separator line
        document.add(Chunk.NEWLINE);
        
        // Invoice Information
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);
        
        addInfoCell(infoTable, "Invoice Number:", invoice.getInvoiceNumber(), boldFont, normalFont);
        addInfoCell(infoTable, "PO Number:", invoice.getPoNumber(), boldFont, normalFont);
        addInfoCell(infoTable, "Invoice Date:", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(dateFormatter) : "", boldFont, normalFont);
        addInfoCell(infoTable, "Due Date:", invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormatter) : "", boldFont, normalFont);
        
        addInfoCell(infoTable, "Customer:", invoice.getCustomerName(), boldFont, normalFont);
        addInfoCell(infoTable, "Customer ID:", invoice.getCustomerId(), boldFont, normalFont);
        addInfoCell(infoTable, "Status:", invoice.getStatus(), boldFont, normalFont);
        addInfoCell(infoTable, "Payment Terms:", invoice.getPaymentTerms() != null ? invoice.getPaymentTerms() : "", boldFont, normalFont);
        
        document.add(infoTable);
        
        // Line Items Header
        Paragraph lineItemsHeader = new Paragraph("Line Items", headerFont);
        lineItemsHeader.setSpacingBefore(10);
        lineItemsHeader.setSpacingAfter(10);
        document.add(lineItemsHeader);
        
        // Line Items Table
        PdfPTable itemsTable = new PdfPTable(6);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{1, 2, 5, 1.5f, 2, 2});
        
        // Table headers
        addTableHeader(itemsTable, "Line", boldFont);
        addTableHeader(itemsTable, "Part Number", boldFont);
        addTableHeader(itemsTable, "Description", boldFont);
        addTableHeader(itemsTable, "Qty", boldFont);
        addTableHeader(itemsTable, "Unit Price", boldFont);
        addTableHeader(itemsTable, "Extended", boldFont);
        
        // Line items
        if (invoice.getLineItems() != null) {
            for (Map<String, Object> item : invoice.getLineItems()) {
                addTableCell(itemsTable, String.valueOf(item.get("lineNumber")), normalFont);
                addTableCell(itemsTable, String.valueOf(item.get("partNumber")), normalFont);
                addTableCell(itemsTable, String.valueOf(item.get("description")), normalFont);
                addTableCellRight(itemsTable, String.valueOf(item.get("quantity")), normalFont);
                addTableCellRight(itemsTable, "$" + item.get("unitPrice"), normalFont);
                addTableCellRight(itemsTable, "$" + item.get("extendedAmount"), normalFont);
            }
        }
        
        // Totals
        addTotalRow(itemsTable, "Subtotal:", "$" + invoice.getSubtotal(), boldFont);
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0) {
            addTotalRow(itemsTable, "Tax:", "$" + invoice.getTaxAmount(), boldFont);
        }
        if (invoice.getShippingAmount() != null && invoice.getShippingAmount().doubleValue() > 0) {
            addTotalRow(itemsTable, "Shipping:", "$" + invoice.getShippingAmount(), boldFont);
        }
        addTotalRow(itemsTable, "Total:", "$" + invoice.getInvoiceTotal(), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        
        document.add(itemsTable);
        
        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            Paragraph notesHeader = new Paragraph("Notes", headerFont);
            notesHeader.setSpacingBefore(20);
            notesHeader.setSpacingAfter(10);
            document.add(notesHeader);
            
            Paragraph notes = new Paragraph(invoice.getNotes(), normalFont);
            document.add(notes);
        }
        
        document.close();
        
        return baos.toByteArray();
    }
    
    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(8);
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }
    
    private void addTableCellRight(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }
    
    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setColspan(4);
        emptyCell.setBorder(Rectangle.TOP);
        emptyCell.setBackgroundColor(new BaseColor(249, 250, 251));
        table.addCell(emptyCell);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBorder(Rectangle.TOP);
        labelCell.setBackgroundColor(new BaseColor(249, 250, 251));
        labelCell.setPadding(8);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(Rectangle.TOP);
        valueCell.setBackgroundColor(new BaseColor(249, 250, 251));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }
}
