@component @gym @training
Feature: Gym Application training management

  Background:
    Given a trainee and trainer are registered and the trainee is authenticated

  @positive @endpoint_training_create
  Scenario: Authenticated user creates a training
    Given the request body is
      """
      {
        "traineeUsername": "${traineeUsername}",
        "trainerUsername": "${trainerUsername}",
        "trainingName": "BDD Cardio",
        "trainingDate": "2026-06-22",
        "trainingDuration": 60
      }
      """
    When I send POST request to "/api/v1/trainings"
    Then the response status is 200

    When I send GET request to "/api/v1/trainees/${traineeUsername}/trainings"
    Then the response status is 200
    And JSON array "$" has size 1

  @negative @validation @endpoint_training_create
  Scenario: Training with zero duration is rejected
    Given the request body is
      """
      {
        "traineeUsername": "${traineeUsername}",
        "trainerUsername": "${trainerUsername}",
        "trainingName": "Invalid training",
        "trainingDate": "2026-06-22",
        "trainingDuration": 0
      }
      """
    When I send POST request to "/api/v1/trainings"
    Then the response status is 400
    And the response body contains "Request validation failed"

  @negative @exception @endpoint_training_create
  Scenario: Training cannot be created for an unknown trainer
    Given the request body is
      """
      {
        "traineeUsername": "${traineeUsername}",
        "trainerUsername": "Unknown.Trainer",
        "trainingName": "Invalid training",
        "trainingDate": "2026-06-22",
        "trainingDuration": 60
      }
      """
    When I send POST request to "/api/v1/trainings"
    Then the response status is 404
    And the response body contains "Trainer not found"

  @negative @exception @endpoint_training_cancel
  Scenario: Unknown training cannot be cancelled
    When I send DELETE request to "/api/v1/trainings/999999"
    Then the response status is 404
    And the response body contains "Training not found"