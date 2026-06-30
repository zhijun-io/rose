package io.zhijun.spring.boot.actuator.endpoint;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.springframework.boot.actuate.endpoint.OperationType.READ;
import static org.springframework.boot.actuate.endpoint.SecurityContext.NONE;

/**
 * Aggregate {@link WebEndpoint} for all registered web endpoints.
 * <p>
 * Invokes each READ operation with an empty context and collects results by operation ID.
 * The endpoint itself (ID "webEndpoints") is skipped to avoid recursion.
 * Operations that fail (e.g., those requiring arguments) are silently skipped.
 */
@WebEndpoint(id = "webEndpoints")
public class WebEndpoints {

    private static final String SELF_ENDPOINT_ID = "webEndpoints";

    private final WebEndpointsSupplier webEndpointsSupplier;

    public WebEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        this.webEndpointsSupplier = webEndpointsSupplier;
    }

    @ReadOperation
    public Map<String, Object> invokeReadOperations() {
        Collection<ExposableWebEndpoint> webEndpoints = this.webEndpointsSupplier.getEndpoints();

        Map<String, Object> readWebOperationResults = new HashMap<>();

        InvocationContext context = createInvocationContext();

        for (ExposableWebEndpoint endpoint : webEndpoints) {
            if (isSelf(endpoint)) {
                continue;
            }

            Collection<WebOperation> webOperations = endpoint.getOperations();
            for (WebOperation operation : webOperations) {
                if (operation.getType() != READ) {
                    continue;
                }
                try {
                    Object result = operation.invoke(context);
                    readWebOperationResults.put(operation.getId(), result);
                } catch (Exception ignored) {
                    // Skip operations that cannot be invoked without arguments
                }
            }
        }

        return readWebOperationResults;
    }

    private static InvocationContext createInvocationContext() {
        return new InvocationContext(NONE, emptyMap());
    }

    private static boolean isSelf(ExposableWebEndpoint endpoint) {
        return SELF_ENDPOINT_ID.equals(endpoint.getEndpointId().toString());
    }
}
