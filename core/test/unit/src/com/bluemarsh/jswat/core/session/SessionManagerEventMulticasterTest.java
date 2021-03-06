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
 * are Copyright (C) 2004-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.session;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the SessionManagerEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class SessionManagerEventMulticasterTest {

    @Test
    public void testMulticaster() {
        SessionManagerEventMulticaster smem = new SessionManagerEventMulticaster();
        Assert.assertNotNull(smem);

        // nothing should happen
        smem.add(null);
        smem.remove(null);

        TestListener l1 = new TestListener();
        smem.add(l1);

        Assert.assertEquals(0, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(0, l1.setCurrent);

        TestListener l2 = new TestListener();
        smem.add(l2);
        smem.sessionAdded(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(0, l1.setCurrent);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(0, l2.setCurrent);

        smem.remove(l2);
        smem.sessionRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(0, l1.setCurrent);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(0, l2.setCurrent);

        smem.add(l2);
        smem.sessionSetCurrent(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(1, l1.setCurrent);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(1, l2.setCurrent);

        smem.sessionRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(2, l1.removed);
        Assert.assertEquals(1, l1.setCurrent);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(1, l2.removed);
        Assert.assertEquals(1, l2.setCurrent);
    }

    private static class TestListener implements SessionManagerListener {

        public int added;
        public int removed;
        public int setCurrent;

        @Override
        public void sessionAdded(SessionManagerEvent e) {
            added++;
        }

        @Override
        public void sessionRemoved(SessionManagerEvent e) {
            removed++;
        }

        @Override
        public void sessionSetCurrent(SessionManagerEvent e) {
            setCurrent++;
        }
    }
}
