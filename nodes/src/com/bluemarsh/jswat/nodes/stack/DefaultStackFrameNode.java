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
 * are Copyright (C) 2005-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultStackFrameNode.java 29 2008-06-30 00:41:09Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.stack;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.nodes.Nodes;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.bluemarsh.jswat.nodes.ShowSourceAction;
import com.bluemarsh.jswat.nodes.ShowSourceCookie;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.awt.Image;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a strack frame in the node tree.
 *
 * @author  Nathan Fiedler
 */
public class DefaultStackFrameNode extends StackFrameNode
        implements ShowSourceCookie, SessionListener {
    /** The stack frame index for the frame we represent (zero-based). */
    private int frameIndex;
    /** The class and method of the frame location. */
    private String methodName;
    /** The location of the stack frame (source and line). */
    private String locationName;
    /** The byte code index of the frame. */
    private long codeIndex;

    /**
     * Constructs a FrameNode to represent the given StackFrame.
     *
     * @param  index  index of the stack frame (zero-based).
     * @param  frame  the stack frame.
     */
    public DefaultStackFrameNode(int index, StackFrame frame) {
        super();
        frameIndex = index;
        Location loc = frame.location();
        codeIndex = loc.codeIndex();
        Method method = loc.method();
        String cname = method.declaringType().name();
        cname = Names.getShortClassName(cname);
        int line = loc.lineNumber();
        if (line >= 0) {
            methodName = cname + '.' + method.name() + ':' + line;
        } else {
            methodName = cname + '.' + method.name();
        }

        if (method.isNative()) {
            locationName = NbBundle.getMessage(
                    DefaultStackFrameNode.class, "LBL_StackView_frame_native");
        } else {
            try {
                locationName = loc.sourceName();
            } catch (AbsentInformationException aie) {
                locationName = NbBundle.getMessage(
                        DefaultStackFrameNode.class, "ERR_StackView_frame_no_info");
            }
        }

        // Should be safe to assume we are associated with the current session.
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        if (dc.getThread() != null) {
            getLookupContent().add(this);
            // Need to listen for when the session resumes, after which this
            // frame will be invalid and we cannot show the source anymore.
            session.addSessionListener(WeakListeners.create(
                    SessionListener.class, this, session));
        }
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    private Node.Property createProperty(String key, String value) {
        String name = NbBundle.getMessage(
                DefaultStackFrameNode.class, "CTL_StackView_Column_Name_" + key);
        String desc = NbBundle.getMessage(
                DefaultStackFrameNode.class, "CTL_StackView_Column_Desc_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_LOCATION, methodName));
        set.put(createProperty(PROP_SOURCE, locationName));
        set.put(createProperty(PROP_CODEINDEX, String.valueOf(codeIndex)));
        return sheet;
    }

    public void disconnected(SessionEvent sevt) {
    }

    @Override
    public String getDisplayName() {
        return methodName;
    }

    @Override
    public String getHtmlDisplayName() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        if (frameIndex == dc.getFrame()) {
            return Nodes.toHTML(methodName, true, false, null);
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        String url;
        if (frameIndex == dc.getFrame()) {
            url = NbBundle.getMessage(DefaultStackFrameNode.class,
                    "IMG_CurrentStackFrameNode");
        } else {
            url = NbBundle.getMessage(DefaultStackFrameNode.class,
                    "IMG_StackFrameNode");
        }
        return ImageUtilities.loadImage(url);
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    protected Action[] getNodeActions() {
        return new Action[] {
            SystemAction.get(SetCurrentAction.class),
            SystemAction.get(ShowSourceAction.class),
            SystemAction.get(PopFramesAction.class),
            SystemAction.get(SetBreakpointAction.class),
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(SetCurrentAction.class);
    }

    public void opened(Session session) {
    }

    public void resuming(SessionEvent sevt) {
        getLookupContent().remove(this);
    }

    public void showSource() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference tr = dc.getThread();
        try {
            StackFrame frame = tr.frame(frameIndex);
            Location location = frame.location();
            PathManager pm = PathProvider.getPathManager(session);
            FileObject fobj = pm.findSource(location);
            if (fobj != null) {
                String url = fobj.getURL().toString();
                EditorSupport es = EditorSupport.getDefault();
                int line = location.lineNumber();
                es.showSource(url, line);
            } else {
                ReferenceType rt = location.declaringType();
                String msg = NbBundle.getMessage(DefaultStackFrameNode.class,
                        "ERR_Source_Missing", rt.name());
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault().notify(fsie);
        } catch (IncompatibleThreadStateException itse) {
            ErrorManager.getDefault().notify(itse);
        } catch (IndexOutOfBoundsException ioobe) {
            ErrorManager.getDefault().notify(ioobe);
        }
    }

    public void suspended(SessionEvent sevt) {
    }
}