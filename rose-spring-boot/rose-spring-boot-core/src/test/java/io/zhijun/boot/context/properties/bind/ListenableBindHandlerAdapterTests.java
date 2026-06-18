package io.zhijun.boot.context.properties.bind;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ListenableBindHandlerAdapterTests {

    @Test
    void shouldNotifyBindListenerDuringBinding() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("rose.test.value", "hello");

        RecordingRoseBindListener listener = new RecordingRoseBindListener();
        RoseBinder binder = RoseBinder.get(environment, listener);

        String value = binder.bindString("rose.test.value", "default");

        assertThat(value).isEqualTo("hello");
        assertThat(listener.started).isTrue();
        assertThat(listener.finished).isTrue();
        assertThat(listener.succeeded).isTrue();
    }

    private static final class RecordingRoseBindListener implements RoseBindListener {

        private boolean started;
        private boolean succeeded;
        private boolean finished;

        @Override
        public void onStart(ConfigurationPropertyName name, Bindable<?> target,
                org.springframework.boot.context.properties.bind.BindContext context) {
            started = true;
        }

        @Override
        public void onSuccess(ConfigurationPropertyName name, Bindable<?> target,
                org.springframework.boot.context.properties.bind.BindContext context, Object result) {
            succeeded = true;
        }

        @Override
        public void onFinish(ConfigurationPropertyName name, Bindable<?> target,
                org.springframework.boot.context.properties.bind.BindContext context, Object result) {
            finished = true;
        }
    }
}
