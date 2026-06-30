@component @workload @security
Feature: Trainer Workload Service authorization

  @negative @authentication @endpoint_workload_get
  Scenario: Request without token is unauthorized
    Given no Bearer token is provided
    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads"
    Then the response status is 401
    And the response body contains "Authentication is required"

  @negative @authorization @endpoint_workload_get
  Scenario: Valid user token does not have service permission
    Given I use a valid user token
    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads"
    Then the response status is 403
    And the response body contains "Access denied"

  @positive @authorization @endpoint_workload_update
  Scenario: Service token allows access to workload API
    Given I use a valid service token
    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 60,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200