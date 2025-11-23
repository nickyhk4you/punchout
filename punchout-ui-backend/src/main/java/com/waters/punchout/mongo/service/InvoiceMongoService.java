package com.waters.punchout.mongo.service;

import com.waters.punchout.dto.InvoiceDTO;
import com.waters.punchout.mongo.entity.InvoiceDocument;
import com.waters.punchout.mongo.repository.InvoiceMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceMongoService {
    
    private final InvoiceMongoRepository repository;
    
    public List<InvoiceDTO> getAllInvoices() {
        log.info("Fetching all invoices");
        return repository.findAllByOrderByReceivedDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public InvoiceDTO getInvoiceByNumber(String invoiceNumber) {
        log.info("Fetching invoice: {}", invoiceNumber);
        return repository.findByInvoiceNumber(invoiceNumber)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    public List<InvoiceDTO> getInvoicesByOrderId(String orderId) {
        log.info("Fetching invoices for order: {}", orderId);
        return repository.findByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<InvoiceDTO> getInvoicesByStatus(String status) {
        log.info("Fetching invoices by status: {}", status);
        return repository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<InvoiceDTO> getInvoicesByEnvironment(String environment) {
        log.info("Fetching invoices by environment: {}", environment);
        return repository.findByEnvironment(environment).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private InvoiceDTO convertToDTO(InvoiceDocument doc) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(doc.getId());
        dto.setInvoiceNumber(doc.getInvoiceNumber());
        dto.setOrderId(doc.getOrderId());
        dto.setSessionKey(doc.getSessionKey());
        dto.setPoNumber(doc.getPoNumber());
        dto.setRouteName(doc.getRouteName());
        dto.setEnvironment(doc.getEnvironment());
        dto.setFlags(doc.getFlags());
        dto.setInvoiceTotal(doc.getInvoiceTotal());
        dto.setCurrency(doc.getCurrency());
        dto.setReceivedDate(doc.getReceivedDate());
        dto.setInvoiceDate(doc.getInvoiceDate());
        dto.setDueDate(doc.getDueDate());
        dto.setStatus(doc.getStatus());
        dto.setCustomerId(doc.getCustomerId());
        dto.setCustomerName(doc.getCustomerName());
        dto.setSupplierName(doc.getSupplierName());
        dto.setTaxAmount(doc.getTaxAmount());
        dto.setShippingAmount(doc.getShippingAmount());
        dto.setSubtotal(doc.getSubtotal());
        dto.setShipTo(doc.getShipTo());
        dto.setBillTo(doc.getBillTo());
        dto.setLineItems(doc.getLineItems());
        dto.setPaymentTerms(doc.getPaymentTerms());
        dto.setNotes(doc.getNotes());
        dto.setProcessedAt(doc.getProcessedAt());
        dto.setPaidAt(doc.getPaidAt());
        dto.setSource(doc.getSource());
        return dto;
    }
}
