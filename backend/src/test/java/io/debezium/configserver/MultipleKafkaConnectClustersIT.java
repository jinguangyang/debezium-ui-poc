package io.debezium.configserver;

import io.debezium.configserver.rest.ConnectorResource;
import io.debezium.configserver.util.MultipleKafkaConnectClustersTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(MultipleKafkaConnectClustersTestProfile.class)
public class MultipleKafkaConnectClustersIT {

    @Test
    public void testMultipleClustersEndpoint() {
        given()
          .when().get(ConnectorResource.API_PREFIX + ConnectorResource.CONNECT_CLUSTERS_ENDPOINT)
          .then().log().all()
             .statusCode(200)
             .body("size()", is(3))
             .and().body("[0]", equalTo("http://localhost:1234"))
             .and().body("[1]", equalTo("http://localhorst:4567"))
             .and().body("[2]", equalTo("http://localhosst:7890"));
    }

}
