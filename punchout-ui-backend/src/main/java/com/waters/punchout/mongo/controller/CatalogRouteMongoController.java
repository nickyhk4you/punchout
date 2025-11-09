package com.waters.punchout.mongo.controller;

import com.waters.punchout.dto.CatalogRouteDTO;
import com.waters.punchout.mongo.service.CatalogRouteMongoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CatalogRouteMongoController {
    
    private final CatalogRouteMongoService catalogRouteService;
    
    @GetMapping("/catalog-routes")
    public ResponseEntity<List<CatalogRouteDTO>> getAllRoutes() {
        log.info("GET /api/v1/catalog-routes - Fetching all catalog routes");
        
        List<CatalogRouteDTO> routes = catalogRouteService.getAllRoutes();
        
        log.info("Returning {} catalog routes", routes.size());
        return ResponseEntity.ok(routes);
    }
    
    @GetMapping("/catalog-routes/{id}")
    public ResponseEntity<CatalogRouteDTO> getRouteById(@PathVariable String id) {
        log.info("GET /api/v1/catalog-routes/{} - Fetching catalog route details", id);
        
        CatalogRouteDTO route = catalogRouteService.getRouteById(id);
        
        return ResponseEntity.ok(route);
    }
    
    @GetMapping("/catalog-routes/active")
    public ResponseEntity<List<CatalogRouteDTO>> getActiveRoutes() {
        log.info("GET /api/v1/catalog-routes/active - Fetching active catalog routes");
        
        List<CatalogRouteDTO> routes = catalogRouteService.getActiveRoutes();
        
        log.info("Returning {} active catalog routes", routes.size());
        return ResponseEntity.ok(routes);
    }
}
