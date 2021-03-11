/*
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.net.ssl;

/**
 * This is the base interface for JSSE trust managers.
 * <P>
 * <code>TrustManager</code>s are responsible for managing the trust material
 * that is used when making trust decisions, and for deciding whether
 * credentials presented by a peer should be accepted.
 * <P>
 * <code>TrustManager</code>s are created by either
 * using a <code>TrustManagerFactory</code>,
 * or by implementing one of the <code>TrustManager</code> subclasses.
 *
 * @see TrustManagerFactory
 * @since 1.4
 */
public interface TrustManager {
}
