package io.debezium.configserver.util;

import io.debezium.testing.testcontainers.ConnectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startables;

import java.util.stream.Stream;

public class Infrastructure {

    public enum DATABASE {
        POSTGRES, MYSQL, SQLSERVER, MONGODB
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Infrastructure.class);

    private static final Network network = Network.newNetwork();

    private static final KafkaContainer kafkaContainer = new KafkaContainer().withNetwork(network);

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("debezium/example-postgres:1.3")
            .withNetwork(network)
            .withNetworkAliases("postgres");

    private static final DebeziumContainer debeziumContainer = (DebeziumContainer) new DebeziumContainer("debezium/connect:nightly")
            .withNetwork(network)
            .withKafka(kafkaContainer)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .dependsOn(kafkaContainer);

    public static void startContainers(DATABASE database) {
        GenericContainer<?> dbContainer = null;
        switch (database) {
            case POSTGRES:
                dbContainer = postgresContainer;
                break;
            default:
                break;
        }
        Startables.deepStart(Stream.of(kafkaContainer, dbContainer, debeziumContainer)).join();
    }

    public static DebeziumContainer getDebeziumContainer() {
        return debeziumContainer;
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }

    public static KafkaContainer getKafkaContainer() {
        return kafkaContainer;
    }

    public static ConnectorConfiguration getPostgresConnectorConfiguration(int id, String... options) {
        final ConnectorConfiguration config = ConnectorConfiguration.forJdbcContainer(postgresContainer)
                .with("database.server.name", "dbserver" + id)
                .with("slot.name", "debezium_" + id);

        if (options != null && options.length > 0) {
            for (int i = 0; i < options.length; i += 2) {
                config.with(options[i], options[i + 1]);
            }
        }
        return config;
    }

}
