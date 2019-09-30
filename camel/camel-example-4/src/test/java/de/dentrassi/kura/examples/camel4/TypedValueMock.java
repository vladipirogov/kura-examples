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

package de.dentrassi.kura.examples.camel4;

public class TypedValueMock {
    private Object value;

    public TypedValueMock(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}