package io.zhijun.multitenancy.spring.web.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiguardian.api.API;

/**
 * Writes the HTTP response when a required multitenancy identifier is missing.
 */
@API(status = API.Status.EXPERIMENTAL)
@FunctionalInterface
public interface TenantContextMissingTenantHandler {

    void handle(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
