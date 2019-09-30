package org.askue.invoker;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.kura.type.StringValue;
import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Routes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(Routes.class);
    private String input;
    private String output;

    public Routes(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void configure() throws Exception {

        from(String.format("seda:%s", this.input))
//                .process(msg -> {
//                    logger.info("Processing normal file: " + ((WireEnvelope)msg.getIn().getBody()).getRecords().get(0).getProperties());
//                    logger.info("Processing normal file: " + msg.getIn().getHeaders());
//                    logger.info("Processing Meassage: {}", msg.getIn().getBody(String.class));
//                })
                .setBody(constant("select descript from parameters where id = 5")) //
                .to("jdbc:component") //
                .process(msg -> {
                    JsonElement element = new Gson().toJsonTree(msg.getIn().getBody());
                    Map<String, TypedValue<?>> dictionary = Collections.singletonMap("records", new StringValue(element.toString()));
                    List<WireRecord> records = Collections.singletonList(new WireRecord(dictionary));
                    msg.getIn().setBody(new WireEnvelope("records", records));
                })
                .to(String.format("seda:%s", this.output));

    }
}
