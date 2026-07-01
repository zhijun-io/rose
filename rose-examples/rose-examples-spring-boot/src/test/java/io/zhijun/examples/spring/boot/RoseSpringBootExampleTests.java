package io.zhijun.examples.spring.boot;

import io.zhijun.core.exception.ErrorCodes;
import io.zhijun.spring.boot.actuator.endpoint.ArtifactsEndpoint;
import io.zhijun.spring.boot.actuator.endpoint.WebEndpoints;
import io.zhijun.spring.web.ApplicationExceptionHandler;
import io.zhijun.spring.webmvc.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.zhijun.spring.webmvc.servlet.ContentCachingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RoseExamplesSpringBootApplication.class, properties = {
    "management.endpoints.web.exposure.include=artifacts,webEndpoints"
})
@AutoConfigureMockMvc
class RoseSpringBootExampleTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoadRoseWebAutoConfigurationBeans() {
        assertThat(applicationContext.getBeansOfType(ApplicationExceptionHandler.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(ContentCachingFilter.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(ConfigurableContentNegotiationManagerWebMvcConfigurer.class))
            .hasSize(1);
    }

    @Test
    void shouldTranslateApplicationExceptionToHttpResponse() throws Exception {
        mockMvc.perform(get("/example/errors/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(ErrorCodes.Common.NOT_FOUND))
            .andExpect(jsonPath("$.retryable").value(false));
    }

    @Test
    void shouldExposeArtifactsEndpoint() throws Exception {
        assertThat(applicationContext.getBeansOfType(ArtifactsEndpoint.class)).hasSize(1);

        mockMvc.perform(get("/actuator/artifacts").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldExposeAggregatedWebEndpoints() throws Exception {
        assertThat(applicationContext.getBeansOfType(WebEndpoints.class)).hasSize(1);

        mockMvc.perform(get("/actuator/webEndpoints").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.artifacts").isArray());
    }
}
