package io.zhijun.devservice.boot.autoconfigure.openlit;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.command.InspectContainerResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.container.ContainerConfigurer;
import io.zhijun.devservice.core.util.OtlpPorts;

/**
 * OpenLit container configured for Rose DevService.
 */
final class OpenLitContainer extends GenericContainer<OpenLitContainer> {

    private static final Logger logger = LoggerFactory.getLogger(OpenLitContainer.class);

    static final String COMPATIBLE_IMAGE_NAME = DockerImageName.parse(OpenLitDevServiceProperties.DEFAULT_IMAGE_NAME)
            .getUnversionedPart();

    static final int UI_PORT = 3000;

    static final int OTLP_GRPC_PORT = OtlpPorts.GRPC;

    static final int OTLP_HTTP_PORT = OtlpPorts.HTTP;

    private static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse(OpenLitDevServiceProperties.DEFAULT_IMAGE_NAME);

    private static final DockerImageName DEFAULT_CLICKHOUSE_IMAGE_NAME =
            DockerImageName.parse(OpenLitDevServiceProperties.DEFAULT_CLICKHOUSE_IMAGE_NAME);

    private static final String CLICKHOUSE_NETWORK_ALIAS = "clickhouse";

    private static final int CLICKHOUSE_HTTP_PORT = 8123;

    private static final int CLICKHOUSE_NATIVE_PORT = 9000;

    private static final String CLICKHOUSE_DATABASE = "default";

    private static final String CLICKHOUSE_USERNAME = "test";

    private static final String CLICKHOUSE_PASSWORD = "test";

    private static final String OTEL_COLLECTOR_CONFIG_PATH = "/etc/otel/otel-collector-config.yaml";

    private static final String OTEL_COLLECTOR_CONFIG_TEMPLATE = "receivers:\n"
            + "  otlp:\n"
            + "    protocols:\n"
            + "      grpc:\n"
            + "        endpoint: 0.0.0.0:4317\n"
            + "      http:\n"
            + "        endpoint: 0.0.0.0:4318\n"
            + "processors:\n"
            + "  batch:\n"
            + "  memory_limiter:\n"
            + "    limit_mib: 1500\n"
            + "    spike_limit_mib: 512\n"
            + "    check_interval: 5s\n"
            + "exporters:\n"
            + "  clickhouse:\n"
            + "    endpoint: tcp://%s:9000?dial_timeout=10s\n"
            + "    database: %s\n"
            + "    username: %s\n"
            + "    password: %s\n"
            + "    ttl: 730h\n"
            + "    logs_table_name: otel_logs\n"
            + "    traces_table_name: otel_traces\n"
            + "    metrics_table_name: otel_metrics\n"
            + "    timeout: 5s\n"
            + "    retry_on_failure:\n"
            + "      enabled: true\n"
            + "      initial_interval: 5s\n"
            + "      max_interval: 30s\n"
            + "      max_elapsed_time: 300s\n"
            + "service:\n"
            + "  pipelines:\n"
            + "    logs:\n"
            + "      receivers: [otlp]\n"
            + "      processors: [batch]\n"
            + "      exporters: [clickhouse]\n"
            + "    traces:\n"
            + "      receivers: [otlp]\n"
            + "      processors: [memory_limiter, batch]\n"
            + "      exporters: [clickhouse]\n"
            + "    metrics:\n"
            + "      receivers: [otlp]\n"
            + "      processors: [memory_limiter, batch]\n"
            + "      exporters: [clickhouse]\n";

    private final OpenLitDevServiceProperties properties;

    private DockerImageName clickHouseImageName = DEFAULT_CLICKHOUSE_IMAGE_NAME;

    OpenLitContainer(OpenLitDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME));
        this.properties = properties;
        this.clickHouseImageName = DockerImageName.parse(properties.getClickhouseImageName())
                .asCompatibleSubstituteFor(DEFAULT_CLICKHOUSE_IMAGE_NAME);

        addExposedPorts(UI_PORT, OTLP_GRPC_PORT, OTLP_HTTP_PORT);
        waitingFor(Wait.forHttp("/").forPort(UI_PORT).forStatusCodeMatching(status -> status < 500));

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();

        org.testcontainers.containers.Network network = getNetwork();
        if (network == null) {
            withNetwork(org.testcontainers.containers.Network.newNetwork());
            network = getNetwork();
        }

        ClickHouseSidecar clickHouseContainer = new ClickHouseSidecar(clickHouseImageName.asCompatibleSubstituteFor(
                DockerImageName.parse(OpenLitDevServiceProperties.DEFAULT_CLICKHOUSE_IMAGE_NAME)
                        .getUnversionedPart()));
        clickHouseContainer.withNetwork(network);
        clickHouseContainer.withNetworkAliases(CLICKHOUSE_NETWORK_ALIAS);
        clickHouseContainer.withStartupTimeout(Duration.ofMinutes(3));
        clickHouseContainer.withReuse(isShouldBeReused());

        clickHouseContainer.start();
        dependsOn(clickHouseContainer);

        String otelCollectorConfig = String.format(
                OTEL_COLLECTOR_CONFIG_TEMPLATE,
                CLICKHOUSE_NETWORK_ALIAS,
                CLICKHOUSE_DATABASE,
                CLICKHOUSE_USERNAME,
                CLICKHOUSE_PASSWORD);

        withEnv("PORT", String.valueOf(UI_PORT));
        withEnv("TELEMETRY_ENABLED", "false");
        withEnv("INIT_DB_HOST", CLICKHOUSE_NETWORK_ALIAS);
        withEnv("INIT_DB_PORT", "8123");
        withEnv("INIT_DB_DATABASE", CLICKHOUSE_DATABASE);
        withEnv("INIT_DB_USERNAME", CLICKHOUSE_USERNAME);
        withEnv("INIT_DB_PASSWORD", CLICKHOUSE_PASSWORD);
        withEnv("SQLITE_DATABASE_URL", "file:/app/client/data/data.db");

        Map<String, String> tmpFs = new HashMap<String, String>();
        tmpFs.put("/app/client/data", "rw");
        withTmpFs(tmpFs);

        withEnv("DEMO_ACCOUNT_EMAIL", "user@openlit.io");
        withEnv("DEMO_ACCOUNT_PASSWORD", "openlituser");

        withCopyToContainer(Transferable.of(otelCollectorConfig), OTEL_COLLECTOR_CONFIG_PATH);

        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), UI_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getOtlpGrpcPort())) {
            addFixedExposedPort(properties.getOtlpGrpcPort(), OTLP_GRPC_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getOtlpHttpPort())) {
            addFixedExposedPort(properties.getOtlpHttpPort(), OTLP_HTTP_PORT);
        }
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        logger.info("OpenLit UI: {}", getOpenLitUrl());
    }

    String getOpenLitUrl() {
        return "http://" + getHost() + ":" + getUiPort();
    }

    Integer getUiPort() {
        return getMappedPort(UI_PORT);
    }

    Integer getOtlpGrpcPort() {
        return getMappedPort(OTLP_GRPC_PORT);
    }

    Integer getOtlpHttpPort() {
        return getMappedPort(OTLP_HTTP_PORT);
    }

    String getOtlpHttpUrl() {
        return "http://" + getHost() + ":" + getOtlpHttpPort();
    }

    /**
     * ClickHouse sidecar without JDBC startup checks (avoids driver/auth issues on modern images).
     */
    private static final class ClickHouseSidecar extends GenericContainer<ClickHouseSidecar> {

        ClickHouseSidecar(DockerImageName dockerImageName) {
            super(dockerImageName);
            addExposedPorts(CLICKHOUSE_HTTP_PORT, CLICKHOUSE_NATIVE_PORT);
            waitingFor(Wait.forHttp("/")
                    .forPort(CLICKHOUSE_HTTP_PORT)
                    .forStatusCode(200)
                    .forResponsePredicate(response -> "Ok.".equals(response))
                    .withStartupTimeout(Duration.ofMinutes(3)));
        }

        @Override
        protected void configure() {
            withEnv("CLICKHOUSE_DB", CLICKHOUSE_DATABASE);
            withEnv("CLICKHOUSE_USER", CLICKHOUSE_USERNAME);
            withEnv("CLICKHOUSE_PASSWORD", CLICKHOUSE_PASSWORD);
        }
    }
}
