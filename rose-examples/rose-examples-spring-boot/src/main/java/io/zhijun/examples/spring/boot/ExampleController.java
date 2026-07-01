package io.zhijun.examples.spring.boot;

import io.zhijun.core.exception.ApplicationException;
import io.zhijun.core.exception.ErrorCodes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    @GetMapping("/example/errors/not-found")
    public String notFound() {
        throw ApplicationException.of(ErrorCodes.Common.NOT_FOUND);
    }
}
