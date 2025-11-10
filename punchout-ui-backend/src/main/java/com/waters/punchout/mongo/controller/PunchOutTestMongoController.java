package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.PunchOutTestDTO;
import com.waters.punchout.mongo.service.PunchOutTestMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PunchOutTestMongoController {
    
    private final PunchOutTestMongoService punchOutTestService;
    
    @GetMapping("/punchout-tests")
    public ResponseEntity<List<PunchOutTestDTO>> getAllTests() {
        log.info("GET /api/v1/punchout-tests - Fetching all punchout tests");
        
        List<PunchOutTestDTO> tests = punchOutTestService.getAllTests();
        
        log.info("Returning {} punchout tests", tests.size());
        return ResponseEntity.ok(tests);
    }
    
    @GetMapping("/punchout-tests/{id}")
    public ResponseEntity<PunchOutTestDTO> getTestById(@PathVariable String id) {
        log.info("GET /api/v1/punchout-tests/{} - Fetching punchout test details", id);
        
        PunchOutTestDTO test = punchOutTestService.getTestById(id);
        
        return ResponseEntity.ok(test);
    }
    
    @PostMapping("/punchout-tests")
    public ResponseEntity<PunchOutTestDTO> createTest(@RequestBody PunchOutTestDTO testDTO) {
        log.info("POST /api/v1/punchout-tests - Creating new punchout test: {}", testDTO.getTestName());
        
        PunchOutTestDTO createdTest = punchOutTestService.createTest(testDTO);
        
        log.info("Created punchout test with id: {}", createdTest.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTest);
    }
    
    @PutMapping("/punchout-tests/{id}")
    public ResponseEntity<PunchOutTestDTO> updateTest(@PathVariable String id, @RequestBody PunchOutTestDTO testDTO) {
        log.info("PUT /api/v1/punchout-tests/{} - Updating punchout test", id);
        
        PunchOutTestDTO updatedTest = punchOutTestService.updateTest(id, testDTO);
        
        return ResponseEntity.ok(updatedTest);
    }
    
    @GetMapping("/catalog-routes/{routeId}/tests")
    public ResponseEntity<List<PunchOutTestDTO>> getTestsByCatalogRoute(@PathVariable String routeId) {
        log.info("GET /api/v1/catalog-routes/{}/tests - Fetching tests for catalog route", routeId);
        
        List<PunchOutTestDTO> tests = punchOutTestService.getTestsByCatalogRoute(routeId);
        
        log.info("Returning {} tests for catalog route {}", tests.size(), routeId);
        return ResponseEntity.ok(tests);
    }
}
