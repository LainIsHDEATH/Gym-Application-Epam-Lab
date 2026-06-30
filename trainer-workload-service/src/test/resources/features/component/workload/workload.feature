@component @workload
Feature: Trainer workload management

  Background:
    Given I use a valid service token

  @positive @endpoint_workload_update
  Scenario: ADD event creates a trainer workload document
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

    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads/monthly?year=2026&month=5"
    Then the response status is 200
    And JSON number "trainingSummaryDuration" equals 60

  @positive @edge @endpoint_workload_update
  Scenario: Multiple ADD events atomically accumulate duration
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

    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-10",
        "trainingDuration": 45,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200

    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads/monthly?year=2026&month=5"
    Then the response status is 200
    And JSON number "trainingSummaryDuration" equals 105

  @positive @edge @endpoint_workload_update
  Scenario: ADD event creates a missing year and month
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

    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2027-02-05",
        "trainingDuration": 30,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200

    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads/monthly?year=2027&month=2"
    Then the response status is 200
    And JSON number "trainingSummaryDuration" equals 30

  @positive @endpoint_workload_update
  Scenario: DELETE event subtracts existing duration
    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 100,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200

    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 40,
        "actionType": "DELETE"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200

    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads/monthly?year=2026&month=5"
    Then the response status is 200
    And JSON number "trainingSummaryDuration" equals 60

  @negative @edge @exception @endpoint_workload_update
  Scenario: DELETE cannot make duration negative
    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 30,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 200

    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 60,
        "actionType": "DELETE"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 409
    And the response body contains "accumulated duration is insufficient"

    When I send GET request to "/api/v1/trainers/Mike.Brown/workloads/monthly?year=2026&month=5"
    Then the response status is 200
    And JSON number "trainingSummaryDuration" equals 30

  @negative @validation @endpoint_workload_update
  Scenario: Zero duration is rejected
    Given the request body is
      """
      {
        "trainerUsername": "Mike.Brown",
        "trainerFirstName": "Mike",
        "trainerLastName": "Brown",
        "isActive": true,
        "trainingDate": "2026-05-05",
        "trainingDuration": 0,
        "actionType": "ADD"
      }
      """
    When I send POST request to "/api/v1/trainer-workloads"
    Then the response status is 400
    And the response body contains "Request validation failed"

  @negative @exception @endpoint_workload_get
  Scenario: Unknown trainer workload returns 404
    When I send GET request to "/api/v1/trainers/Unknown.Trainer/workloads"
    Then the response status is 404
    And the response body contains "Trainer workload not found"