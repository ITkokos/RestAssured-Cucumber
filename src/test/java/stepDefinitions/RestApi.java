package stepDefinitions;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import cucumber.api.java8.En;
import io.cucumber.datatable.DataTable;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV4;
import static com.github.fge.jsonschema.main.JsonSchemaFactory.newBuilder;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class RestApi implements En {

    private RequestSpecification request = given().log().all();
    private Response response;
    private ValidatableResponse json;
    private String url;

    public RestApi() {

        Given("^User (.*)$", (String url) -> {
            this.url = url;
        });

        And("^Add headers to request$", (DataTable headers) -> {
            request.headers(headers.asMap(String.class, String.class));
        });

        And("^Add params to request$", (DataTable params) -> {
            request.params(params.asMap(String.class, String.class));
        });

        And("^Add body to request$", (DataTable body) -> {
            request.body(body.asMap(String.class, String.class));
        });

        And("^Add body to request (.*)$", (String filePath) -> {
            request.body(new FileInputStream(filePath));
        });

        When("^Make GET request$", () -> {
            response = request.when().get(url);
        });

        When("^Make POST request$", () -> {
            response = request.when().post(url);
        });

        When("^Make DELETE request$", () -> {
            response = request.when().delete(url);
        });

        Then("^Status code is (\\d+)$", (Integer statusCode) -> {
            json = response.then().statusCode(statusCode).log().all();
        });

        And("^Check response headers$", (DataTable headers) -> {
            Map<String, String> expected = headers.asMap(String.class, String.class);
            Map<String, String> actual = response.getHeaders().asList().stream()
                    .collect(Collectors.toMap(Header::getName, Header::getValue));
            boolean allMatch = expected.entrySet().stream()
                    .allMatch(headerEntry -> headerEntry.getValue().equals(actual.get(headerEntry.getKey())));
            assertThat(true).as("Headers don't match").isEqualTo(allMatch);
        });


        And("^Response includes the following$", (DataTable response) -> {
            Map<String, String> responseFields = response.asMap(String.class, String.class);
            for (Map.Entry<String, String> field : responseFields.entrySet()) {
                if (StringUtils.isNumeric(field.getValue())) {
                    json.body(field.getKey(), equalTo(Integer.parseInt(field.getValue())));
                } else {
                    json.body(field.getKey(), equalTo(field.getValue()));
                }
            }
        });

        And("^Response includes the following in any lines$", (DataTable response) -> {
            Map<String, String> responseFields = response.asMap(String.class, String.class);
            for (Map.Entry<String, String> field : responseFields.entrySet()) {
                if (StringUtils.isNumeric(field.getValue())) {
                    json.body(field.getKey(), hasItems(Integer.parseInt(field.getValue())));
                } else {
                    json.body(field.getKey(), hasItems(field.getValue()));
                }
            }
        });

        And("^Check error message (.*)$", (String message) -> {
            response.getBody().asString().contains(message);
        });

        And("^Check error message$", (DataTable message) -> {
            Map<String, String> expectedMessage = message.asMap(String.class, String.class);
            json.body(expectedMessage.keySet().toArray()[0].toString(),
                    equalTo(expectedMessage.values().toArray()[0].toString()));
        });

        And("^Check JSON schema (.*)$", (String filePath) -> {
            JsonSchemaFactory jsonSchemaFactory = newBuilder()
                    .setValidationConfiguration(ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV4).freeze()).freeze();
            json.body(matchesJsonSchemaInClasspath(filePath).using(jsonSchemaFactory));
        });

    }
}
