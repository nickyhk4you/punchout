package com.waters.punchout.config;

import com.waters.punchout.entity.GatewayRequest;
import com.waters.punchout.entity.OrderObject;
import com.waters.punchout.entity.PunchOutSession;
import com.waters.punchout.repository.GatewayRequestRepository;
import com.waters.punchout.repository.OrderObjectRepository;
import com.waters.punchout.repository.PunchOutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile({"!test", "!render"})
public class DataInitializer implements CommandLineRunner {

    private final PunchOutSessionRepository sessionRepository;
    private final OrderObjectRepository orderObjectRepository;
    private final GatewayRequestRepository gatewayRequestRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing mock data...");

        createPunchOutSessions();
        createOrderObjects();
        createGatewayRequests();

        log.info("Mock data initialization completed!");
        log.info("Total Sessions: {}", sessionRepository.count());
        log.info("Total Order Objects: {}", orderObjectRepository.count());
        log.info("Total Gateway Requests: {}", gatewayRequestRepository.count());
    }

    private void createPunchOutSessions() {
        PunchOutSession session1 = new PunchOutSession();
        session1.setSessionKey("SESSION-2025-001");
        session1.setCartReturn("https://buyer.example.com/cart");
        session1.setOperation("CREATE");
        session1.setContactEmail("john.doe@acme.com");
        session1.setRouteName("acme-route");
        session1.setEnvironment("PRODUCTION");
        session1.setFlags("ACTIVE");
        session1.setSessionDate(LocalDateTime.now().minusDays(5));
        session1.setPunchedIn(LocalDateTime.now().minusDays(5).plusHours(1));
        session1.setPunchedOut(LocalDateTime.now().minusDays(5).plusHours(3));
        session1.setOrderId("ORD-1001");
        session1.setOrderValue(new BigDecimal("2500.00"));
        session1.setLineItems(5);
        session1.setItemQuantity(15);
        session1.setCatalog("ACME-CATALOG-2025");
        session1.setNetwork("ARIBA");
        session1.setParser("cXML-Parser-v2");
        session1.setBuyerCookie("BUYER_COOKIE_ABC123");

        PunchOutSession session2 = new PunchOutSession();
        session2.setSessionKey("SESSION-2025-002");
        session2.setCartReturn("https://buyer.globex.com/shopping");
        session2.setOperation("EDIT");
        session2.setContactEmail("jane.smith@globex.com");
        session2.setRouteName("globex-route");
        session2.setEnvironment("STAGING");
        session2.setFlags("PENDING");
        session2.setSessionDate(LocalDateTime.now().minusDays(3));
        session2.setPunchedIn(LocalDateTime.now().minusDays(3).plusMinutes(30));
        session2.setOrderId("ORD-1002");
        session2.setOrderValue(new BigDecimal("4750.50"));
        session2.setLineItems(8);
        session2.setItemQuantity(24);
        session2.setCatalog("GLOBEX-PREMIUM");
        session2.setNetwork("OCI");
        session2.setParser("cXML-Parser-v2");
        session2.setBuyerCookie("BUYER_COOKIE_XYZ789");

        PunchOutSession session3 = new PunchOutSession();
        session3.setSessionKey("SESSION-2025-003");
        session3.setCartReturn("https://buyer.initech.com/procure");
        session3.setOperation("INSPECT");
        session3.setContactEmail("bob.wilson@initech.com");
        session3.setRouteName("initech-route");
        session3.setEnvironment("DEVELOPMENT");
        session3.setFlags("TESTING");
        session3.setSessionDate(LocalDateTime.now().minusDays(1));
        session3.setPunchedIn(LocalDateTime.now().minusDays(1).plusHours(2));
        session3.setPunchedOut(LocalDateTime.now().minusDays(1).plusHours(4));
        session3.setOrderId("ORD-1003");
        session3.setOrderValue(new BigDecimal("1200.75"));
        session3.setLineItems(3);
        session3.setItemQuantity(10);
        session3.setCatalog("INITECH-STANDARD");
        session3.setNetwork("ARIBA");
        session3.setParser("cXML-Parser-v1");
        session3.setBuyerCookie("BUYER_COOKIE_DEF456");

        PunchOutSession session4 = new PunchOutSession();
        session4.setSessionKey("SESSION-2025-004");
        session4.setCartReturn("https://buyer.umbrella.com/cart");
        session4.setOperation("CREATE");
        session4.setContactEmail("alice.johnson@umbrella.com");
        session4.setRouteName("umbrella-route");
        session4.setEnvironment("PRODUCTION");
        session4.setFlags("COMPLETED");
        session4.setSessionDate(LocalDateTime.now().minusHours(12));
        session4.setPunchedIn(LocalDateTime.now().minusHours(12).plusMinutes(15));
        session4.setPunchedOut(LocalDateTime.now().minusHours(11));
        session4.setOrderId("ORD-1004");
        session4.setOrderValue(new BigDecimal("8900.00"));
        session4.setLineItems(12);
        session4.setItemQuantity(45);
        session4.setCatalog("UMBRELLA-ENTERPRISE");
        session4.setNetwork("SAP");
        session4.setParser("cXML-Parser-v2");
        session4.setBuyerCookie("BUYER_COOKIE_GHI321");

        PunchOutSession session5 = new PunchOutSession();
        session5.setSessionKey("SESSION-2025-005");
        session5.setCartReturn("https://buyer.cyberdyne.com/orders");
        session5.setOperation("CREATE");
        session5.setContactEmail("sarah.connor@cyberdyne.com");
        session5.setRouteName("cyberdyne-route");
        session5.setEnvironment("PRODUCTION");
        session5.setFlags("ACTIVE");
        session5.setSessionDate(LocalDateTime.now().minusHours(6));
        session5.setPunchedIn(LocalDateTime.now().minusHours(6).plusMinutes(45));
        session5.setOrderValue(new BigDecimal("15500.25"));
        session5.setLineItems(20);
        session5.setItemQuantity(75);
        session5.setCatalog("CYBERDYNE-TECH");
        session5.setNetwork("ARIBA");
        session5.setParser("cXML-Parser-v2");
        session5.setBuyerCookie("BUYER_COOKIE_JKL654");

        sessionRepository.saveAll(Arrays.asList(session1, session2, session3, session4, session5));
        log.info("Created {} PunchOut sessions", 5);
    }

    private void createOrderObjects() {
        OrderObject order1 = new OrderObject();
        order1.setSessionKey("SESSION-2025-001");
        order1.setType("PURCHASE_ORDER");
        order1.setOperation("CREATE");
        order1.setMode("ONLINE");
        order1.setUniqueName("john.doe");
        order1.setUserEmail("john.doe@acme.com");
        order1.setCompanyCode("ACME001");
        order1.setUserFirstName("John");
        order1.setUserLastName("Doe");
        order1.setFromIdentity("ACME_BUYER_DOMAIN");
        order1.setSoldToLookup("SOLDTO-12345");
        order1.setContactEmail("john.doe@acme.com");

        OrderObject order2 = new OrderObject();
        order2.setSessionKey("SESSION-2025-002");
        order2.setType("REQUISITION");
        order2.setOperation("EDIT");
        order2.setMode("OFFLINE");
        order2.setUniqueName("jane.smith");
        order2.setUserEmail("jane.smith@globex.com");
        order2.setCompanyCode("GLOBEX002");
        order2.setUserFirstName("Jane");
        order2.setUserLastName("Smith");
        order2.setFromIdentity("GLOBEX_BUYER_DOMAIN");
        order2.setSoldToLookup("SOLDTO-67890");
        order2.setContactEmail("jane.smith@globex.com");

        OrderObject order3 = new OrderObject();
        order3.setSessionKey("SESSION-2025-003");
        order3.setType("QUOTE");
        order3.setOperation("INSPECT");
        order3.setMode("ONLINE");
        order3.setUniqueName("bob.wilson");
        order3.setUserEmail("bob.wilson@initech.com");
        order3.setCompanyCode("INITECH003");
        order3.setUserFirstName("Bob");
        order3.setUserLastName("Wilson");
        order3.setFromIdentity("INITECH_BUYER_DOMAIN");
        order3.setSoldToLookup("SOLDTO-11223");
        order3.setContactEmail("bob.wilson@initech.com");

        OrderObject order4 = new OrderObject();
        order4.setSessionKey("SESSION-2025-004");
        order4.setType("PURCHASE_ORDER");
        order4.setOperation("CREATE");
        order4.setMode("ONLINE");
        order4.setUniqueName("alice.johnson");
        order4.setUserEmail("alice.johnson@umbrella.com");
        order4.setCompanyCode("UMBRELLA004");
        order4.setUserFirstName("Alice");
        order4.setUserLastName("Johnson");
        order4.setFromIdentity("UMBRELLA_BUYER_DOMAIN");
        order4.setSoldToLookup("SOLDTO-44556");
        order4.setContactEmail("alice.johnson@umbrella.com");

        orderObjectRepository.saveAll(Arrays.asList(order1, order2, order3, order4));
        log.info("Created {} Order objects", 4);
    }

    private void createGatewayRequests() {
        GatewayRequest req1 = new GatewayRequest();
        req1.setSessionKey("SESSION-2025-001");
        req1.setDatetime(LocalDateTime.now().minusDays(5));
        req1.setUri("/punchout/setup");
        req1.setOpenLink("https://supplier.example.com/punchout/SESSION-2025-001");

        GatewayRequest req2 = new GatewayRequest();
        req2.setSessionKey("SESSION-2025-001");
        req2.setDatetime(LocalDateTime.now().minusDays(5).plusHours(1));
        req2.setUri("/punchout/addToCart");
        req2.setOpenLink("https://supplier.example.com/cart/SESSION-2025-001");

        GatewayRequest req3 = new GatewayRequest();
        req3.setSessionKey("SESSION-2025-001");
        req3.setDatetime(LocalDateTime.now().minusDays(5).plusHours(3));
        req3.setUri("/punchout/checkout");
        req3.setOpenLink("https://supplier.example.com/checkout/SESSION-2025-001");

        GatewayRequest req4 = new GatewayRequest();
        req4.setSessionKey("SESSION-2025-002");
        req4.setDatetime(LocalDateTime.now().minusDays(3));
        req4.setUri("/punchout/setup");
        req4.setOpenLink("https://supplier.globex.com/punchout/SESSION-2025-002");

        GatewayRequest req5 = new GatewayRequest();
        req5.setSessionKey("SESSION-2025-002");
        req5.setDatetime(LocalDateTime.now().minusDays(3).plusMinutes(30));
        req5.setUri("/punchout/editOrder");
        req5.setOpenLink("https://supplier.globex.com/edit/SESSION-2025-002");

        GatewayRequest req6 = new GatewayRequest();
        req6.setSessionKey("SESSION-2025-003");
        req6.setDatetime(LocalDateTime.now().minusDays(1));
        req6.setUri("/punchout/setup");
        req6.setOpenLink("https://supplier.initech.com/punchout/SESSION-2025-003");

        GatewayRequest req7 = new GatewayRequest();
        req7.setSessionKey("SESSION-2025-004");
        req7.setDatetime(LocalDateTime.now().minusHours(12));
        req7.setUri("/punchout/setup");
        req7.setOpenLink("https://supplier.umbrella.com/punchout/SESSION-2025-004");

        GatewayRequest req8 = new GatewayRequest();
        req8.setSessionKey("SESSION-2025-004");
        req8.setDatetime(LocalDateTime.now().minusHours(11));
        req8.setUri("/punchout/checkout");
        req8.setOpenLink("https://supplier.umbrella.com/checkout/SESSION-2025-004");

        GatewayRequest req9 = new GatewayRequest();
        req9.setSessionKey("SESSION-2025-005");
        req9.setDatetime(LocalDateTime.now().minusHours(6));
        req9.setUri("/punchout/setup");
        req9.setOpenLink("https://supplier.cyberdyne.com/punchout/SESSION-2025-005");

        gatewayRequestRepository.saveAll(Arrays.asList(req1, req2, req3, req4, req5, req6, req7, req8, req9));
        log.info("Created {} Gateway requests", 9);
    }
}
