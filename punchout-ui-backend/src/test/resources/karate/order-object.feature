Feature: Order Object API Tests

  Background:
    * url baseUrl

  Scenario: Get order object for a specific session
    Given path '/api/punchout-sessions/SESSION-2025-001/order-object'
    When method GET
    Then status 200
    And match response.sessionKey == 'SESSION-2025-001'
    And match response contains { sessionKey: '#string', type: '#string', operation: '#string' }

  Scenario: Get order object for non-existing session returns 404
    Given path '/api/punchout-sessions/NON-EXISTING-SESSION/order-object'
    When method GET
    Then status 404

  Scenario: Create a new order object
    Given path '/api/punchout-sessions/TEST-SESSION-ORDER/order-object'
    And request
      """
      {
        "sessionKey": "TEST-SESSION-ORDER",
        "type": "PURCHASE_ORDER",
        "operation": "CREATE",
        "mode": "ONLINE",
        "uniqueName": "test.user",
        "userEmail": "test.user@example.com",
        "companyCode": "TEST001",
        "userFirstName": "Test",
        "userLastName": "User",
        "fromIdentity": "TEST_BUYER_DOMAIN",
        "soldToLookup": "SOLDTO-TEST",
        "contactEmail": "test.user@example.com"
      }
      """
    When method POST
    Then status 201
    And match response.sessionKey == 'TEST-SESSION-ORDER'
    And match response.type == 'PURCHASE_ORDER'
    And match response.operation == 'CREATE'
    And match response.userEmail == 'test.user@example.com'

  Scenario: Update existing order object
    Given path '/api/punchout-sessions/SESSION-2025-001/order-object'
    And request
      """
      {
        "sessionKey": "SESSION-2025-001",
        "type": "REQUISITION",
        "operation": "EDIT",
        "mode": "OFFLINE",
        "uniqueName": "john.doe.updated",
        "userEmail": "john.doe.updated@acme.com",
        "companyCode": "ACME001",
        "userFirstName": "John",
        "userLastName": "Doe",
        "fromIdentity": "ACME_BUYER_DOMAIN",
        "soldToLookup": "SOLDTO-12345",
        "contactEmail": "john.doe.updated@acme.com"
      }
      """
    When method POST
    Then status 201
    And match response.sessionKey == 'SESSION-2025-001'
    And match response.operation == 'EDIT'
