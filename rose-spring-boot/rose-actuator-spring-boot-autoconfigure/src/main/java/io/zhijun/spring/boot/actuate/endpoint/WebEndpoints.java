package io.zhijun.spring.boot.actuate.endpoint;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.invoke.reflect.OperationMethod;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import static java.util.Collections.emptyMap;
import static org.springframework.boot.actuate.endpoint.OperationType.READ;
import static org.springframework.boot.actuate.endpoint.SecurityContext.NONE;

@WebEndpoint(id = "webEndpoints")
public class WebEndpoints {

    private final WebEndpointsSupplier webEndpointsSupplier;

    public WebEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        this.webEndpointsSupplier = webEndpointsSupplier;
    }

    @ReadOperation
    public Map<String, Object> invokeReadOperations() {
        Collection<ExposableWebEndpoint> webEndpoints = this.webEndpointsSupplier.getEndpoints();

        Map<String, Object> readWebOperationResults = new HashMap<>(webEndpoints.size());

        InvocationContext context = createInvocationContext();

        for (ExposableWebEndpoint webEndpoint : webEndpoints) {
            if (!isExposableWebEndpoint(webEndpoint)) {
                continue;
            }
            DiscoveredEndpoint discoveredEndpoint = (DiscoveredEndpoint) webEndpoint;
            Object endpointBean = discoveredEndpoint.getEndpointBean();
            if (endpointBean == this) {
                continue;
            }

            Collection<WebOperation> webOperations = webEndpoint.getOperations();
            for (WebOperation webOperation : webOperations) {
                if (isReadWebOperationCandidate(webOperation)) {
                    String readWebOperationId = webOperation.getId();
                    Object readWebOperationResult = webOperation.invoke(context);
                    readWebOperationResults.put(readWebOperationId, readWebOperationResult);
                }
            }
        }

        return readWebOperationResults;
    }

    private static InvocationContext createInvocationContext() {
        return new InvocationContext(NONE, emptyMap());
    }

    static boolean isExposableWebEndpoint(ExposableWebEndpoint webEndpoint) {
        return webEndpoint instanceof DiscoveredEndpoint;
    }

    static boolean isReadWebOperationCandidate(WebOperation webOperation) {
        if (webOperation instanceof AbstractDiscoveredOperation) {
            AbstractDiscoveredOperation discoveredOperation = (AbstractDiscoveredOperation) webOperation;
            OperationMethod operationMethod = discoveredOperation.getOperationMethod();
            if (READ.equals(operationMethod.getOperationType())) {
                Method method = operationMethod.getMethod();
                if (method.getParameterCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
