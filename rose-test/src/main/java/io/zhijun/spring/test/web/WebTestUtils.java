package io.zhijun.spring.test.web;

/**
 * Web 测试常量
 */
public abstract class WebTestUtils {

    public static final String TEST_ROOT_PATH = "/test";

    public static final String ATTRIBUTE_NAME = "test-name";

    public static final String NOT_FOUND_ATTRIBUTE_NAME = "not-found-name";

    public static final String ATTRIBUTE_VALUE = "test-value";

    public static final String HEADER_NAME = "test-header-name";

    public static final String HEADER_VALUE = "test-header-value";

    public static final String HEADER_NAME_2 = "test-header-name-2";

    public static final String HEADER_VALUE_2 = "test-header-value-2";

    public static final String PARAM_NAME = "test-param-name";

    public static final String PARAM_VALUE = "test-param-value";

    public static final String PARAM_NAME_2 = "test-param-name-2";

    public static final String PARAM_VALUE_2 = "test-param-value-2";

    public static final String PERSON_PATH = "/person";

    public static final String PERSON_TEST_PATH = TEST_ROOT_PATH + PERSON_PATH;

    public static final String PERSON_ID_PATH = "/{id}";

    public static final String AUTH_NAME = "_auth";

    public static final String AUTH_VALUE = "123456789";

    public static final String GET_PERSON_PATH = PERSON_TEST_PATH + PERSON_ID_PATH;

    private WebTestUtils() {
    }
}
