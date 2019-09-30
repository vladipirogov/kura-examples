/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package de.dentrassi.kura.examples.generator1;

import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.CloudSubscriber;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.data.DataService;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generator component, generating a sawtooth
 * <p>
 * <strong>Note:</strong> The component must be marked as
 * {@code immediate = true} and {@code configurationPolicy = REQUIRE}.
 * </p>
 *
 * @author Jens Reimann
 */
@Designate(ocd = Config.class)
@Component(immediate = true, configurationPolicy = REQUIRE)
public class GeneratorComponent implements ConfigurableComponent, CloudSubscriberListener {

    private static final Logger logger = LoggerFactory.getLogger(GeneratorComponent.class);

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> job;
    private Config config;
    private CloudSubscriber cloudSubscriber;

    @Reference(bind = "bindCloudService", unbind = "unbindCloudService")
    private CloudService cloudService;

    private CloudClient cloudClient;
    private ToDoubleFunction<Instant> function;

    public void setCloudSubscriber(CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber = cloudSubscriber;
        this.cloudSubscriber.registerCloudSubscriberListener(GeneratorComponent.this);
    }

    public void unsetCloudSubscriber(CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber.unregisterCloudSubscriberListener(GeneratorComponent.this);
        this.cloudSubscriber = null;
    }

    //@Reference(bind = "setDataService", unbind = "unsetDataService")
    private DataService dataService;

    public void bindCloudService(final CloudService cloudService) throws KuraException {
        logger.info("Set cloud service: {}", cloudService);
        System.out.println("Set cloud service");

        this.cloudService = cloudService;
        if (cloudService != null) {
            this.cloudClient = cloudService.newCloudClient("generator-1");
            System.out.println("Set cloud client");
        }
    }

    public void unbindCloudService(final CloudService cloudService) {
        logger.info("Unset cloud service: {}", cloudService);
        System.out.println("UNSet cloud service");

        if (this.cloudClient != null) {
            this.cloudClient.release();
            this.cloudClient = null;
        }

        this.cloudService = null;
    }

    public void setDataService(DataService dataService) {
        dataService = dataService;
    }

    public void unsetDataService(DataService dataService) {
        dataService = null;
    }

    @Activate
    public void activate(final Config config) {
        dumpProperties("activate", config);
        setConfig(config);
    }

    @Modified
    public void modified(final Config config) {
        dumpProperties("modified", config);
        setConfig(config);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivate");
        stop();
    }

    private void tick() {

        //logger.info("Ticking ...");
        //System.out.println("Ticking ...");

        //final CloudClient cloudClient = this.cloudClient;

//        try {
//            if (dataService != null) {
//                String topic = "your/topic";
//                String payload = "Hello!";
//                cloudClient.publish(topic, payload.getBytes(), 0, false, 2);
//                System.out.println("Published " + payload);
//            }
//        } catch (final Exception e) {
//            logger.warn("Failed to publish", e);
//        }
        try {
            if (cloudClient != null) {
                cloudClient.publish("data", makePayload(), 1, false);
            }
        } catch (final Exception e) {
            logger.warn("Failed to publish", e);
        }

    }

    protected KuraPayload makePayload() {
        final KuraPayload payload = new KuraPayload();
        payload.addMetric("value", this.function.applyAsDouble(Instant.now()));
        return payload;
    }

    private void setConfig(final Config config) {

        if (!config.enabled()) {
            logger.info("Component is not enabled");
            stop();
            return;
        }

        if (this.config != null && this.config.publishRate() != config.publishRate()) {
            logger.info("Period time changes, restarting scheduler");
            stop();
        }

        this.function = now -> sawtooth(now, config);

        if (this.executor != null) {
            logger.info("Already running");
            return;
        }

        logger.info("Starting scheduler");

        this.executor = Executors.newScheduledThreadPool(1);
        this.job = this.executor.scheduleWithFixedDelay(this::tick, config.publishRate(), config.publishRate(),
                TimeUnit.MILLISECONDS);
    }

    private static double sawtooth(final Instant now, final Config config) {
        final double diff = config.maxValue() - config.minValue();
        final double v = diff / config.period() * now.toEpochMilli();
        return v % diff + config.minValue();
    }

    private void stop() {
        logger.info("Stopping scheduler");

        if (this.job != null) {
            this.job.cancel(false);
            this.job = null;
        }
        if (this.executor == null) {
            this.executor.shutdown();
            this.executor = null;
        }
    }

    private void dumpProperties(final String operation, final Config config) {
        if (logger.isInfoEnabled()) {
            logger.info("=========== {} ===========", operation);
            logger.info("\t'enabled' = '{}'", config.enabled());
            logger.info("\t'minValue' = '{}'", config.minValue());
            logger.info("\t'maxValue' = '{}'", config.maxValue());
            logger.info("\t'period' = '{}'", config.period());
            logger.info("=========== {} ===========", operation);
        }
    }

    @Override
    public void onMessageArrived(KuraMessage kuraMessage) {
        System.out.println(kuraMessage.getPayload().toString());
    }
}
