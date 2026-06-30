 package io.zhijun.core.spi;
 
 import io.zhijun.core.spi.annotation.Priority;
 import org.junit.jupiter.api.Test;
 
 import java.util.List;
 import java.util.Optional;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;
 
 import static org.assertj.core.api.Assertions.assertThat;
 
 public class SpiServiceLoaderTest {
 
     // ---- SPI type
 
     public interface GreetingService {
         String greet();
     }
 
     // ---- implementations with different priorities
 
     @Priority(1)
     public static class FormalGreeting implements GreetingService {
         @Override
         public String greet() {
             return "Good day";
         }
     }
 
     @Priority(5)
     public static class DefaultGreeting implements GreetingService {
         @Override
         public String greet() {
             return "Hello";
         }
     }
 
     @Priority(10)
     public static class CasualGreeting implements GreetingService {
         @Override
         public String greet() {
             return "Hey";
         }
     }
 
     // no @Priority → defaults to Integer.MAX_VALUE
     public static class NoPriorityGreeting implements GreetingService {
         @Override
         public String greet() {
             return "Hi";
         }
     }

    // ---- loadAll

    @Test
    void loadAll_returnsAllImplementationsSortedByPriority() {
        List<GreetingService> services = SpiServiceLoader.loadAll(GreetingService.class);

        assertThat(services)
                .hasSize(4)
                .extracting(GreetingService::greet)
                .containsExactly("Good day", "Hello", "Hey", "Hi");
    }

    @Test
    void loadAll_withCustomClassLoader_returnsSameOrder() {
        List<GreetingService> services = SpiServiceLoader.loadAll(
                GreetingService.class, getClass().getClassLoader());

        assertThat(services)
                .hasSize(4)
                .extracting(GreetingService::greet)
                .containsExactly("Good day", "Hello", "Hey", "Hi");
    }

    @Test
    void loadAll_returnsUnmodifiableList() {
        List<GreetingService> services = SpiServiceLoader.loadAll(GreetingService.class);

        assertThat(services).isUnmodifiable();
    }

    // ---- loadFirst

    @Test
    void loadFirst_returnsHighestPriorityImplementation() {
        Optional<GreetingService> service = SpiServiceLoader.loadFirst(GreetingService.class);

        assertThat(service).isPresent();
        assertThat(service.get().greet()).isEqualTo("Good day");
    }

    @Test
    void loadFirst_whenNoImplementations_returnsEmpty() {
        Optional<CharSequence> result = SpiServiceLoader.loadFirst(CharSequence.class);

        assertThat(result).isEmpty();
    }

    // ---- stream

    @Test
    void stream_returnsAllImplementations() {
        try (Stream<GreetingService> stream = SpiServiceLoader.stream(GreetingService.class)) {
            List<String> names = stream
                    .map(GreetingService::greet)
                    .collect(Collectors.toList());
            assertThat(names).containsExactlyInAnyOrder("Good day", "Hello", "Hey", "Hi");
        }
    }

    // ---- no @Priority defaults to end

    @Test
    void noPriorityClass_comesAfterAllPrioritized() {
        List<GreetingService> services = SpiServiceLoader.loadAll(GreetingService.class);

        assertThat(services.get(3))
                .isInstanceOf(NoPriorityGreeting.class);
    }
}
