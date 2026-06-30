@integration @messaging @workload_synchronization
Feature: Synchronization between Gym Application and Trainer Workload Service

  @positive @endpoint_training_create
  Scenario: Creating a training asynchronously increases trainer workload
    Given a trainee and trainer are registered through Gym Application
    And the trainee is authenticated
    When a 60 minute training is created for date "2026-05-05"
    Then Gym Application returns status 200
    And eventually trainer workload for year 2026 and month 5 equals 60

  @positive @endpoint_training_cancel
  Scenario: Cancelling a training asynchronously decreases trainer workload
    Given a trainee and trainer are registered through Gym Application
    And the trainee is authenticated
    And a 60 minute training exists for date "2026-05-05"
    And trainer workload eventually equals 60 for year 2026 and month 5
    When the training is cancelled
    Then Gym Application returns status 200
    And eventually trainer workload for year 2026 and month 5 equals 0

  @negative @edge @messaging
  Scenario: Invalid training is not persisted and does not create workload
    Given a trainee and trainer are registered through Gym Application
    And the trainee is authenticated
    When a 0 minute training is submitted for date "2026-05-05"
    Then Gym Application returns status 400
    And trainer workload is not created