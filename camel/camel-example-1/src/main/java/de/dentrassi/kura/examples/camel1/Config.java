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
package de.dentrassi.kura.examples.camel1;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Meta type information for {@link CamelExampleConfigurableComponent}
 * <p>
 * <strong>Note: </strong> The id must be the full qualified name of the assigned component.
 * </p>
 */
@ObjectClassDefinition(id="de.dentrassi.kura.examples.camel1.CamelExampleConfigurableComponent", name="Camel Example #1", description="This is the Camel example #1 component")
@interface Config {
}
