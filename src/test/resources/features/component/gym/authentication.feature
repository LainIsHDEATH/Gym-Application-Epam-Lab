@component @gym @authentication
Feature: Gym Application authentication

  @positive @endpoint_login
  Scenario: Registered trainee can log in
    Given the request body is
      """
      {
        "firstName": "Login${runId}",
        "lastName": "Trainee",
        "dateOfBirth": "2000-05-10",
        "address": "London"
      }
      """
    When I send POST request to "/api/v1/trainees"
    Then the response status is 200
    And I save JSON field "username" as "username"
    And I save JSON field "password" as "password"

    Given the request body is
      """
      {
        "username": "${username}",
        "password": "${password}"
      }
      """
    When I send POST request to "/api/v1/login"
    Then the response status is 200
    And I save JSON field "token" as "token"
    And JSON string "tokenType" equals "Bearer"

  @negative @endpoint_login
  Scenario: Login fails for an invalid password
    Given the request body is
      """
      {
        "firstName": "WrongPassword${runId}",
        "lastName": "Trainee"
      }
      """
    When I send POST request to "/api/v1/trainees"
    Then the response status is 200
    And I save JSON field "username" as "username"

    Given the request body is
      """
      {
        "username": "${username}",
        "password": "incorrect-password"
      }
      """
    When I send POST request to "/api/v1/login"
    Then the response status is 401

  @negative @security @endpoint_trainee_profile
  Scenario: Protected endpoint rejects unauthenticated request
    Given no Bearer token is provided
    When I send GET request to "/api/v1/trainees/Unknown.User"
    Then the response status is 401
    And the response body contains "Authentication is required"

  @negative @validation @endpoint_login
  Scenario: Empty login request is rejected
    Given the request body is
      """
      {
        "username": "",
        "password": ""
      }
      """
    When I send POST request to "/api/v1/login"
    Then the response status is 400
    And the response body contains "Request validation failed"