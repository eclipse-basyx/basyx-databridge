package basyx.components.databridge.core.regression.routebuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckHelper;
import org.apache.camel.health.HealthCheckRegistry;
import org.apache.camel.impl.health.DefaultHealthCheckRegistry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import basyx.components.databridge.core.routebuilder.HealthCheckRouteBuilder;

public class HealthCheckRouteBuilderTest extends CamelTestSupport {
	
	/**
     * tells the test runner that we'll start and stop the camel context manually. this ensures the camel context
     * doesn't start before we've set up the camel registry and routes.
     *
     * @return
     */
    @Override
    public boolean isUseAdviceWith() { return true; }
    
    CamelContext context;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        context = super.createCamelContext();
        context.getPropertiesComponent().setLocation("ref:prop");

        // install health check manually (yes a bit cumbersome)
        HealthCheckRegistry registry = new DefaultHealthCheckRegistry();
        registry.setCamelContext(context);
        Object hc = registry.resolveById("context");
        registry.register(hc);
        hc = registry.resolveById("routes");
        registry.register(hc);
        hc = registry.resolveById("consumers");
        registry.register(hc);
        context.setExtension(HealthCheckRegistry.class, registry);

        return context;
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new HealthCheckRouteBuilder();
    }
    
    @Test
    public void testConnectivity() {

        Collection<HealthCheck.Result> res = HealthCheckHelper.invokeLiveness(context);
        boolean up = res.stream().allMatch(r -> r.getState().equals(HealthCheck.State.UP));
        Assertions.assertTrue(up, "liveness check");

//        // health-check readiness should be down
//        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
//            Collection<HealthCheck.Result> res2 = HealthCheckHelper.invokeReadiness(context);
//            boolean down = res2.stream().allMatch(r -> r.getState().equals(HealthCheck.State.DOWN));
//            boolean containsAws2DdbHealthCheck = res2.stream()
//                    .filter(result -> result.getCheck().getId().startsWith("aws2-ddb-client"))
//                    .findAny()
//                    .isPresent();
//            boolean hasRegionMessage = res2.stream()
//                    .anyMatch(r -> r.getMessage().stream().anyMatch(msg -> msg.contains("region")));
//            Assertions.assertTrue(down, "liveness check");
//            Assertions.assertTrue(containsAws2DdbHealthCheck, "aws2-ddb check");
//            Assertions.assertTrue(hasRegionMessage, "aws2-ddb check error message");
//        });

    }


}
