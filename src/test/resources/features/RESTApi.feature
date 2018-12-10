Feature: REST API

  @smoke
  Scenario: User calls web service using GET method
    Given User https://reqres.in/api/users
    And Add params to request
      | page | 2 |
    When Make GET request
    Then Status code is 200
    And Response includes the following
      | page        | 2  |
      | per_page    | 3  |
      | total       | 12 |
      | total_pages | 4  |
    And Response includes the following in any lines
      | data.id         | 4                                                                   |
      | data.first_name | Eve                                                                 |
      | data.last_name  | Holt                                                                |
      | data.avatar     | https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg |

  @smoke
  Scenario: User calls web service in order to create new record
    Given User https://reqres.in/api/users
    And Add headers to request
      | Content-Type  | application/json |
      | Cache-Control | np-cache         |
    And Add body to request
      | name | Kokos  |
      | job  | Father |
    When Make POST request
    Then Status code is 201
    And Response includes the following
      | name | Kokos  |
      | job  | Father |
    And Check JSON schema jsonSchema/schema.json

  @smoke
  Scenario: User calls web service in order to delete some record
    Given User https://reqres.in/api/users/2
    When Make DELETE request
    Then Status code is 204

  @smoke
  Scenario: User calls web service to check response header
    Given User http://httpbin.org/get
    And Add headers to request
      | Content-Type  | application/json |
      | Cache-Control | np-cache         |
    When Make GET request
    Then Status code is 200
    And Check response headers
      | Access-Control-Allow-Credentials | true             |
      | Access-Control-Allow-Origin      | *                |
      | Connection                       | keep-alive       |
      | Content-Type                     | application/json |
      | Server                           | gunicorn/19.9.0  |
      | Via                              | 1.1 vegur        |

  @smoke
  Scenario: User calls web service to check error message
    Given User https://reqres.in/api/register
    And Add headers to request
      | Content-Type  | application/json |
      | Cache-Control | np-cache         |
    And Add body to request src/test/resources/json/request.json
    When Make POST request
    Then Status code is 400
    And Check error message Missing password
    And Check error message
      | error | Missing password |
