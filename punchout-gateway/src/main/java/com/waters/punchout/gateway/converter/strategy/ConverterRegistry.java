package com.waters.punchout.gateway.converter.strategy;

import com.waters.punchout.gateway.converter.resolve.ConversionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ConverterRegistry {
    
    private final Map<String, PunchOutConverterStrategy> converters;
    
    public ConverterRegistry(List<PunchOutConverterStrategy> strategies) {
        this.converters = new HashMap<>();
        
        for (PunchOutConverterStrategy strategy : strategies) {
            String key = makeKey(strategy.customerId(), strategy.version());
            converters.put(key, strategy);
            log.info("Registered converter: {} version {}", strategy.customerId(), strategy.version());
        }
        
        log.info("Total converters registered: {}", converters.size());
    }
    
    public PunchOutConverterStrategy get(ConversionKey key) {
        String lookupKey = makeKey(key.getCustomerId(), key.getVersion());
        PunchOutConverterStrategy strategy = converters.get(lookupKey);
        
        if (strategy == null) {
            log.warn("No converter found for {}, using default", lookupKey);
            strategy = converters.get(makeKey("default", "v1"));
        }
        
        if (strategy == null) {
            throw new IllegalStateException("No default converter registered!");
        }
        
        log.debug("Using converter: {} version {}", strategy.customerId(), strategy.version());
        return strategy;
    }
    
    private String makeKey(String customerId, String version) {
        return customerId + ":" + version;
    }
    
    public int getConverterCount() {
        return converters.size();
    }
}
