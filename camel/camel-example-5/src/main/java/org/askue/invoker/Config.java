/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.askue.invoker;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static org.osgi.service.metatype.annotations.AttributeType.PASSWORD;

/**
 * Meta type information for {@link App}
 * <p>
 * <strong>Note: </strong> The id must be the full qualified name of the assigned component. This is a requirement by
 * Kura.
 * </p>
 */
@ObjectClassDefinition(id = "org.askue.invoker.App", name = "Camel Example #5", description = "This is the Camel example #5 component")
@interface Config {

    @AttributeDefinition(name = "Context ID", description = "Context ID")
    String contextId() default "camel.example.5";

    @AttributeDefinition(name = "Input", description = "Input")
    String input() default "input1";

    @AttributeDefinition(name = "Input", description = "Input")
    String output() default "output";

    @AttributeDefinition(name = "Database name")
    public String databaseName() default "deeplearn";

    @AttributeDefinition(name = "Host", description = "The name or IP address of the database host. This value may use environment variables by using the syntax \"${VAR}\"")
    public String host() default "192.168.1.101";

    @AttributeDefinition(name = "Port")
    public int port() default 5432;

    @AttributeDefinition(name = "User name")
    public String user() default "postgres";

    @AttributeDefinition(name = "Password", type = PASSWORD)
    public String password() default "postgres";
}
