package org.askue.invoker;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jdbc.JdbcComponent;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.crypto.CryptoService;
import org.eclipse.scada.utils.str.StringReplacer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.postgresql.ds.PGPoolingDataSource;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static org.eclipse.kura.camel.component.Configuration.asInt;
import static org.eclipse.kura.camel.component.Configuration.asString;

@Designate(ocd = Config.class)
@Component(enabled = true, immediate = true)
public class App implements ConfigurableComponent{

    private DefaultCamelContext camelContext;
    private ServiceRegistration registration;
    private CryptoService cryptoService;

    @Reference
    public void setCryptoService(final CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }


    @Activate
    public void activate(final BundleContext context, final Map<String, ?> properties) throws Exception {
        if(this.camelContext == null) {
            this.camelContext = new OsgiDefaultCamelContext(context);
            registerJdbcComponent(camelContext, properties);
            this.camelContext.addRoutes(new Routes(asString(properties, "input"), asString(properties, "output")));
            this.camelContext.start();
        }

        final Dictionary<String, String> dictionary = new Hashtable<>();
        dictionary.put("camel.context.id", asString(properties, "contextId"));
        this.registration = context.registerService(CamelContext.class, this.camelContext, dictionary);
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (this.registration != null) {
            this.registration.unregister();
            this.registration = null;
        }
        if (this.camelContext != null) {
            this.camelContext.stop();
            this.camelContext = null;
        }
    }

    @Modified
    public void modified(BundleContext context, final Map<String, ?> properties) throws Exception {
        deactivate();
        activate(context, properties);
    }

    public void registerJdbcComponent(CamelContext camelContext, final Map<String, ?> properties) {
        final String databaseName = asString(properties, "databaseName");
        int port = asInt(properties, "port", 5432);
        final String host = StringReplacer.replace(asString(properties, "host", "localhost"), System.getenv());
        final String user = asString(properties, "user");
        String password = asString(properties, "password");

        // decode the password
        if (password != null) {
            try {
                password = String.valueOf(this.cryptoService.decryptAes(password.toCharArray()));
            } catch (final Exception e) {
            }
        }

        final JdbcComponent jdbc = new JdbcComponent(this.camelContext);
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(host);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(databaseName);

        jdbc.setDataSource(dataSource);
        camelContext.addComponent("jdbc", jdbc);
    }
}
