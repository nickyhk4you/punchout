Feature: PunchOut Session API Tests

  Background:
    * url baseUrl
    * def sessionPath = '/api/punchout-sessions'

  Scenario: Get all punchout sessions
    Given path sessionPath
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Get a specific punchout session by sessionKey
    Given path sessionPath + '/SESSION-2025-001'
    When method GET
    Then status 200
    And match response.sessionKey == 'SESSION-2025-001'
    And match response contains { sessionKey: '#string', operation: '#string' }

  Scenario: Create a new punchout session
    Given path sessionPath
    And request
      """
      {
        "sessionKey": "TEST-SESSION-001",
        "cartReturn": "https://test.example.com/cart",
        "operation": "CREATE",
        "contactEmail": "test@example.com",
        "routeName": "test-route",
        "environment": "TEST",
        "flags": "ACTIVE",
        "sessionDate": "2025-11-02T10:00:00",
        "orderId": "ORD-TEST-001",
        "orderValue": 1000.00,
        "lineItems": 3,
        "itemQuantity": 10,
        "catalog": "TEST-CATALOG",
        "network": "ARIBA",
        "parser": "cXML-Parser-v2"
      }
      """
    When method POST
    Then status 201
    And match response.sessionKey == 'TEST-SESSION-001'
    And match response.operation == 'CREATE'

  Scenario: Update an existing punchout session
    Given path sessionPath + '/SESSION-2025-001'
    And request
      """
      {
        "sessionKey": "SESSION-2025-001",
        "cartReturn": "https://updated.example.com/cart",
        "operation": "EDIT",
        "contactEmail": "updated@example.com",
        "routeName": "updated-route",
        "environment": "PRODUCTION",
        "flags": "UPDATED",
        "sessionDate": "2025-11-02T10:00:00",
        "orderId": "ORD-1001",
        "orderValue": 3000.00,
        "lineItems": 7,
        "itemQuantity": 20,
        "catalog": "UPDATED-CATALOG",
        "network": "OCI",
        "parser": "cXML-Parser-v2"
      }
      """
    When method PUT
    Then status 200
    And match response.sessionKey == 'SESSION-2025-001'
    And match response.operation == 'EDIT'
    And match response.orderValue == 3000.00

  Scenario: Get sessions with filter by environment
    Given path sessionPath
    And param environment = 'PRODUCTION'
    When method GET
    Then status 200
    And match response == '#array'
