package io.zhijun.multitenancy.web.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.zhijun.core.annotation.Incubating;

/**
 * Writes the HTTP response when a required multitenancy identifier is missing.
 */
@Incubating
@FunctionalInterface
public interface TenantContextMissingTenantHandler {

    void handle(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
