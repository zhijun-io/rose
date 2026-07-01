package io.zhijun.spring.test.webmvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static io.zhijun.spring.test.web.WebTestUtils.AUTH_NAME;
import static io.zhijun.spring.test.web.WebTestUtils.AUTH_VALUE;
import static io.zhijun.spring.test.web.WebTestUtils.GET_PERSON_PATH;
import static io.zhijun.spring.test.web.WebTestUtils.PERSON_ID_PATH;
import static io.zhijun.spring.test.web.WebTestUtils.PERSON_PATH;
import static io.zhijun.spring.test.web.WebTestUtils.PERSON_TEST_PATH;
import static io.zhijun.spring.test.web.WebTestUtils.TEST_ROOT_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.DELETE;
import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RequestPredicates.PUT;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RequestPredicates.param;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.nest;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * {@link RouterFunction} 测试配置
 */
@Import(PersonHandler.class)
public class RouterFunctionTestConfig {

    @Bean
    public RouterFunction<ServerResponse> personRouterFunction(PersonHandler handler) {
        return route(GET(GET_PERSON_PATH).and(accept(APPLICATION_JSON)), handler::getPerson)
                .andRoute(GET(PERSON_TEST_PATH).and(contentType(APPLICATION_JSON)), handler::listPeople)
                .andRoute(POST(PERSON_TEST_PATH).and(param(AUTH_NAME, AUTH_VALUE)), handler::createPerson);
    }

    @Bean
    public RouterFunction<ServerResponse> nestedPersonRouterFunction(PersonHandler handler) {
        RouterFunction<ServerResponse> routes = route(PUT(PERSON_ID_PATH), handler::updatePerson)
                .andRoute(DELETE(PERSON_ID_PATH), handler::deletePerson);
        return nest(path(TEST_ROOT_PATH), nest(path(PERSON_PATH), routes));
    }
}
