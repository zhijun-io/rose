package io.zhijun.spring.test.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zhijun.spring.test.domain.User;
import io.zhijun.spring.test.web.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * WebMVC 测试基类
 */
@Disabled
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        TestController.class,
        RouterFunctionTestConfig.class
})
@EnableWebMvc
public abstract class AbstractWebMvcTest {

    @Autowired
    protected ConfigurableWebApplicationContext context;

    @Autowired
    private TestController testController;

    protected MockMvc mockMvc;

    @BeforeEach
    protected void setUp() {
        this.mockMvc = webAppContextSetup(this.context).build();
    }

    protected void testWebEndpoints() throws Exception {
        this.testHelloWorld();
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testResponseEntity();
        this.testUpdatePerson();
    }

    protected void testHelloWorld() throws Exception {
        this.mockMvc.perform(get("/test/helloworld"))
                .andExpect(status().isOk())
                .andExpect(content().string(this.testController.helloWorld()));
    }

    protected void testGreeting() throws Exception {
        String pattern = "/test/greeting/{message}";
        String message = "Mercy";
        this.mockMvc.perform(get(pattern, message))
                .andExpect(status().isOk())
                .andExpect(content().string(this.testController.greeting(message)));
    }

    protected void testUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/test/user")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(json));
    }

    protected void testError() {
        assertThrows(ServletException.class, () -> this.mockMvc.perform(get("/test/error")
                .param("message", "For testing")).andReturn());
    }

    protected void testResponseEntity() throws Exception {
        this.mockMvc.perform(put("/test/response-entity"))
                .andExpect(status().isOk())
                .andExpect(content().string(this.testController.responseEntity().getBody()));
    }

    protected void testView() throws Exception {
        this.mockMvc.perform(get("/test/view"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    protected void testUpdatePerson() throws Exception {
        this.mockMvc.perform(put("/test/person/{id}", "1"))
                .andExpect(status().isOk());
    }
}
