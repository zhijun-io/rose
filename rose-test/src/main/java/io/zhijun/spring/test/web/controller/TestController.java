package io.zhijun.spring.test.web.controller;

import io.zhijun.spring.test.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Test {@link Controller @Controller} for WebMVC tests.
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/helloworld")
    @ResponseBody
    public String helloWorld() {
        return "Hello World";
    }

    @GetMapping("/greeting/{message}")
    @ResponseBody
    public String greeting(@PathVariable String message) {
        return "Greeting : " + message;
    }

    @PostMapping(path = "/user", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public User user(@RequestBody User user) {
        return user;
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(@RequestParam String message) {
        throw new RuntimeException(message);
    }

    @PutMapping("/response-entity")
    public ResponseEntity<String> responseEntity() {
        return ok("OK");
    }

    @GetMapping("/view")
    public String view() {
        return "test-view";
    }
}
