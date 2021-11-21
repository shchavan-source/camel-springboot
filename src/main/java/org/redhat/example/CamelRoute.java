package org.redhat.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.cglib.core.Converter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CamelRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().component("undertow").bindingMode(RestBindingMode.auto).host("localhost").port(9101);

        rest("/greetings")
                .consumes("application/json")
                .produces("application/json")
                .get().to("direct:greetingsImpl")
                .post("/jmxOperation").to("direct:jmxOperationImpl");

        from("direct:greetingsImpl")
                .setHeader("Authorization", constant("Basic "+Base64.getEncoder().encodeToString(("admin:admin").getBytes(StandardCharsets.UTF_8))))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeader("Authorization", "Basic "+Base64.getEncoder().encodeToString(("admin:admin").getBytes(StandardCharsets.UTF_8)));
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        exchange.getOut().setBody(gson.toJson(JMXOperation.builder().type("EXEC").mbean("osgi.core:type=bundleState,version=1.7,framework=org.apache.felix.framework,uuid=00991b2e-69ac-44f2-aa68-9efe89f7aa35").operation("listBundles()").arguments(null).build()));
                    }
                })
//                .setBody(constant(new Gson().toJson(JMXOperation.builder().type("EXEC").mbean("osgi.core:type=bundleState,version=1.7,framework=org.apache.felix.framework,uuid=00991b2e-69ac-44f2-aa68-9efe89f7aa35").operation("listBundles()").build())))
                .log("${headers}")
                .log("${body}")
                .to("undertow:http://localhost:8181/hawtio/jolokia/")
                .log("JMX operation: ${body}");

        from("direct:jmxOperationImpl")
                .log("${body}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeader("Authorization", "Basic "+Base64.getEncoder().encodeToString(("admin:admin").getBytes(StandardCharsets.UTF_8)));
                        JMXOperation jmxOperation = new ObjectMapper().readValue(exchange.getIn().getBody(String.class), JMXOperation.class);
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        exchange.getOut().setBody(gson.toJson(jmxOperation));
                        System.out.println(exchange.getOut().getBody());
                    }
                })
                .to("undertow:http://localhost:8181/hawtio/jolokia/");
    }
}
