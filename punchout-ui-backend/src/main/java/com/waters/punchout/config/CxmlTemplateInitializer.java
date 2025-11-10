package com.waters.punchout.config;

import com.waters.punchout.mongo.entity.CxmlTemplateDocument;
import com.waters.punchout.mongo.repository.CxmlTemplateMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CxmlTemplateInitializer implements CommandLineRunner {
    
    private final CxmlTemplateMongoRepository repository;
    
    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Initializing default cXML templates...");
            
            createDefaultTemplates();
            
            log.info("Default cXML templates initialized successfully!");
        } else {
            log.info("cXML templates already exist. Skipping initialization.");
        }
    }
    
    private void createDefaultTemplates() {
        String[] environments = {"dev", "stage", "prod", "s4-dev"};
        
        for (String env : environments) {
            CxmlTemplateDocument template = new CxmlTemplateDocument();
            template.setTemplateName("Default " + env.toUpperCase() + " Template");
            template.setEnvironment(env);
            template.setCustomerId("DEFAULT");
            template.setCustomerName("Default Template");
            template.setCxmlTemplate(generateDefaultTemplate(env));
            template.setDescription("Default cXML template for " + env.toUpperCase() + " environment");
            template.setIsDefault(true);
            template.setCreatedAt(LocalDateTime.now());
            template.setUpdatedAt(LocalDateTime.now());
            template.setCreatedBy("system");
            
            repository.save(template);
            log.info("Created default template for environment: {}", env);
        }
    }
    
    private String generateDefaultTemplate(String environment) {
        return String.format(
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<cXML payloadID=\"{{PAYLOAD_ID}}\" timestamp=\"{{TIMESTAMP}}\">\n" +
"  <Header>\n" +
"    <From>\n" +
"      <Credential domain=\"NetworkID\">\n" +
"        <Identity>{{BUYER_ID}}</Identity>\n" +
"      </Credential>\n" +
"    </From>\n" +
"    <To>\n" +
"      <Credential domain=\"NetworkID\">\n" +
"        <Identity>supplier456</Identity>\n" +
"      </Credential>\n" +
"    </To>\n" +
"    <Sender>\n" +
"      <Credential domain=\"NetworkID\">\n" +
"        <Identity>{{DOMAIN}}</Identity>\n" +
"        <SharedSecret>secret123</SharedSecret>\n" +
"      </Credential>\n" +
"      <UserAgent>BuyerApp 1.0</UserAgent>\n" +
"    </Sender>\n" +
"  </Header>\n" +
"  <Request>\n" +
"    <PunchOutSetupRequest operation=\"create\">\n" +
"      <BuyerCookie>{{SESSION_KEY}}</BuyerCookie>\n" +
"      <Extrinsic name=\"User\">developer@waters.com</Extrinsic>\n" +
"      <Extrinsic name=\"Environment\">%s</Extrinsic>\n" +
"      <Extrinsic name=\"CustomerName\">{{CUSTOMER_NAME}}</Extrinsic>\n" +
"      <BrowserFormPost>\n" +
"        <URL>https://{{DOMAIN}}/punchout/return</URL>\n" +
"      </BrowserFormPost>\n" +
"      <Contact role=\"buyer\">\n" +
"        <Name xml:lang=\"en\">Developer Test</Name>\n" +
"        <Email>developer@waters.com</Email>\n" +
"      </Contact>\n" +
"    </PunchOutSetupRequest>\n" +
"  </Request>\n" +
"</cXML>", environment);
    }
}
