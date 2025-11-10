package com.waters.punchout.controller;

import com.waters.punchout.dto.CxmlTemplateDTO;
import com.waters.punchout.mongo.service.CxmlTemplateMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cxml-templates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CxmlTemplateController {
    
    private final CxmlTemplateMongoService templateService;
    
    @GetMapping
    public ResponseEntity<List<CxmlTemplateDTO>> getAllTemplates() {
        log.info("GET /api/v1/cxml-templates - Fetching all templates");
        List<CxmlTemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/environment/{environment}")
    public ResponseEntity<List<CxmlTemplateDTO>> getTemplatesByEnvironment(
            @PathVariable String environment) {
        log.info("GET /api/v1/cxml-templates/environment/{} - Fetching templates", environment);
        List<CxmlTemplateDTO> templates = templateService.getTemplatesByEnvironment(environment);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/environment/{environment}/customer/{customerId}")
    public ResponseEntity<CxmlTemplateDTO> getTemplateByEnvironmentAndCustomer(
            @PathVariable String environment,
            @PathVariable String customerId) {
        log.info("GET /api/v1/cxml-templates/environment/{}/customer/{}", environment, customerId);
        CxmlTemplateDTO template = templateService.getTemplateByEnvironmentAndCustomer(environment, customerId);
        if (template != null) {
            return ResponseEntity.ok(template);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/environment/{environment}/default")
    public ResponseEntity<CxmlTemplateDTO> getDefaultTemplate(@PathVariable String environment) {
        log.info("GET /api/v1/cxml-templates/environment/{}/default", environment);
        CxmlTemplateDTO template = templateService.getDefaultTemplateForEnvironment(environment);
        if (template != null) {
            return ResponseEntity.ok(template);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<CxmlTemplateDTO> saveTemplate(@RequestBody CxmlTemplateDTO template) {
        log.info("POST /api/v1/cxml-templates - Saving template: {}", template.getTemplateName());
        CxmlTemplateDTO saved = templateService.saveTemplate(template);
        return ResponseEntity.ok(saved);
    }
}
