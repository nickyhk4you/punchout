Feature: Gateway Request API Tests

  Background:
    * url baseUrl

  Scenario: Get gateway requests for a specific session
    Given path '/api/punchout-sessions/SESSION-2025-001/gateway-requests'
    When method GET
    Then status 200
    And match response == '#array'
    And match each response contains { sessionKey: '#string', datetime: '#string', uri: '#string' }

  Scenario: Create a new gateway request
    Given path '/api/gateway-requests'
    And request
      """
      {
        "sessionKey": "SESSION-2025-001",
        "datetime": "2025-11-02T10:30:00",
        "uri": "/punchout/test",
        "openLink": "https://supplier.example.com/test/SESSION-2025-001"
      }
      """
    When method POST
    Then status 201
    And match response.sessionKey == 'SESSION-2025-001'
    And match response.uri == '/punchout/test'
    And match response.openLink == 'https://supplier.example.com/test/SESSION-2025-001'

  Scenario: Verify gateway requests exist for a session
    Given path '/api/punchout-sessions/SESSION-2025-001/gateway-requests'
    When method GET
    Then status 200
    And match response == '#array'
    And assert response.length > 0
