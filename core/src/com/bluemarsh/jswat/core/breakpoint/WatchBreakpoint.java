/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

/**
 * A WatchBreakpoint stops each time a particular field is accessed
 * or modified by the debuggee.
 *
 * @author  Nathan Fiedler
 */
public interface WatchBreakpoint extends Breakpoint {

    /** Name of 'fieldName' property. */
    String PROP_FIELDNAME = "fieldName";
    /** Name of 'stopOnAccess' property. */
    String PROP_STOPONACCESS = "stopOnAccess";
    /** Name of 'stopOnModify' property. */
    String PROP_STOPONMODIFY = "stopOnModify";

    /**
     * Returns the name of the field being watched.
     *
     * @return  expression referring to the watched field.
     */
    String getFieldName();

    /**
     * Returns the stop-on-access status.
     *
     * @return  true if stopping when field is accessed.
     */
    boolean getStopOnAccess();

    /**
     * Returns the stop-on-modify status.
     *
     * @return  true if stopping when field is modified.
     */
    boolean getStopOnModify();

    /**
     * Sets the expression referring to the field to watch.
     *
     * @param  name   expression for watched field.
     * @throws  MalformedMemberNameException
     *          if field name is invalid.
     */
    void setFieldName(String name) throws MalformedMemberNameException;

    /**
     * Sets the stop-on-access status.
     *
     * @param  stop  true to stop when field is accessed.
     */
    void setStopOnAccess(boolean stop);

    /**
     * Sets the stop-on-modify status.
     *
     * @param  stop  true to stop when field is modified.
     */
    void setStopOnModify(boolean stop);
}
