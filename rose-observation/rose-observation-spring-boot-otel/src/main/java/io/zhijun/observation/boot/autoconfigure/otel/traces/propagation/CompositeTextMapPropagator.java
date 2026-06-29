package io.zhijun.observation.boot.autoconfigure.otel.traces.propagation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;

import org.springframework.lang.Nullable;

import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties.PropagationType;

/**
 * Composite {@link TextMapPropagator} supporting W3C and B3 formats.
 */
class CompositeTextMapPropagator implements TextMapPropagator {

    private final Collection<TextMapPropagator> injectors;
    private final Collection<TextMapPropagator> extractors;
    private final Set<String> fields;

    CompositeTextMapPropagator(Collection<TextMapPropagator> injectors, Collection<TextMapPropagator> extractors) {
        this.injectors = injectors;
        this.extractors = extractors;
        Set<String> initFields = new LinkedHashSet<String>();
        fields(this.injectors).forEach(initFields::add);
        fields(this.extractors).forEach(initFields::add);
        this.fields = Collections.unmodifiableSet(initFields);
    }

    private Stream<String> fields(Collection<TextMapPropagator> propagators) {
        return propagators.stream().flatMap(propagator -> propagator.fields().stream());
    }

    @Override
    public Collection<String> fields() {
        return fields;
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
        if (context != null && setter != null) {
            for (TextMapPropagator injector : injectors) {
                injector.inject(context, carrier, setter);
            }
        }
    }

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
        if (context == null) {
            return Context.root();
        }
        if (getter == null) {
            return context;
        }
        Context result = context;
        for (TextMapPropagator extractor : extractors) {
            Context extracted = extractor.extract(context, carrier, getter);
            if (extracted != context) {
                result = extracted;
                break;
            }
        }
        return result;
    }

    static TextMapPropagator create(OpenTelemetryPropagationProperties properties) {
        List<TextMapPropagator> injectors = mapTypes(properties.getProduce());
        List<TextMapPropagator> extractors = mapTypes(properties.getConsume());
        return new CompositeTextMapPropagator(injectors, extractors);
    }

    private static List<TextMapPropagator> mapTypes(List<PropagationType> types) {
        List<TextMapPropagator> propagators = new ArrayList<TextMapPropagator>();
        for (PropagationType type : types) {
            propagators.add(mapType(type));
        }
        return propagators;
    }

    private static TextMapPropagator mapType(PropagationType type) {
        if (type == PropagationType.B3) {
            return B3Propagator.injectingSingleHeader();
        }
        if (type == PropagationType.B3_MULTI) {
            return B3Propagator.injectingMultiHeaders();
        }
        return W3CTraceContextPropagator.getInstance();
    }
}
