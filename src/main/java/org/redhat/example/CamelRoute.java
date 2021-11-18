package org.redhat.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().component("undertow").bindingMode(RestBindingMode.auto).host("localhost").port(8181);

        rest("/greetings")
                .consumes("application/json")
                .produces("application/json")
                .get()
                .to("direct:greetingsImpl");

        from("direct:greetingsImpl")
                .transform().constant("Hello World").log("Route Ended");
    }
}
