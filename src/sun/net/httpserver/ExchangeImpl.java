/*
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
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

package sun.net.httpserver;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.*;
import java.util.logging.Logger;
import java.text.*;
import com.sun.net.httpserver.*;

class ExchangeImpl {

    Headers reqHdrs, rspHdrs;
    Request req;
    String method;
    boolean writefinished;
    URI uri;
    HttpConnection connection;
    long reqContentLen;
    long rspContentLen;
    /* raw streams which access the socket directly */
    InputStream ris;
    OutputStream ros;
    Thread thread;
    /* close the underlying connection when this exchange finished */
    boolean close;
    boolean closed;
    boolean http10 = false;

    /* for formatting the Date: header */
    private static final String pattern = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final TimeZone gmtTZ = TimeZone.getTimeZone("GMT");
    private static final ThreadLocal<DateFormat> dateFormat =
         new ThreadLocal<DateFormat>() {
             @Override protected DateFormat initialValue() {
                 DateFormat df = new SimpleDateFormat(pattern, Locale.US);
                 df.setTimeZone(gmtTZ);
                 return df;
         }
     };

    private static final String HEAD = "HEAD";

    /* streams which take care of the HTTP protocol framing
     * and are passed up to higher layers
     */
    InputStream uis;
    OutputStream uos;
    LeftOverInputStream uis_orig; // uis may have be a user supplied wrapper
    PlaceholderOutputStream uos_orig;

    boolean sentHeaders; /* true after response headers sent */
    Map<String,Object> attributes;
    int rcode = -1;
    HttpPrincipal principal;
    ServerImpl server;

    ExchangeImpl (
        String m, URI u, Request req, long len, HttpConnection connection
    ) throws IOException {
        this.req = req;
        this.reqHdrs = req.headers();
        this.rspHdrs = new Headers();
        this.method = m;
        this.uri = u;
        this.connection = connection;
        this.reqContentLen = len;
        /* ros only used for headers, body written directly to stream */
        this.ros = req.outputStream();
        this.ris = req.inputStream();
        server = getServerImpl();
        server.startExchange();
    }

    public Headers getRequestHeaders () {
        return new UnmodifiableHeaders (reqHdrs);
    }

    public Headers getResponseHeaders () {
        return rspHdrs;
    }

    public URI getRequestURI () {
        return uri;
    }

    public String getRequestMethod (){
        return method;
    }

    public HttpContextImpl getHttpContext (){
        return connection.getHttpContext();
    }

    private boolean isHeadRequest() {
        return HEAD.equals(getRequestMethod());
    }

    public void close () {
        if (closed) {
            return;
        }
        closed = true;

        /* close the underlying connection if,
         * a) the streams not set up yet, no response can be sent, or
         * b) if the wrapper output stream is not set up, or
         * c) if the close of the input/outpu stream fails
         */
        try {
            if (uis_orig == null || uos == null) {
                connection.close();
                return;
            }
            if (!uos_orig.isWrapped()) {
                connection.close();
                return;
            }
            if (!uis_orig.isClosed()) {
                uis_orig.close();
            }
            uos.close();
        } catch (IOException e) {
            connection.close();
        }
    }

    public InputStream getRequestBody () {
        if (uis != null) {
            return uis;
        }
        if (reqContentLen == -1L) {
            uis_orig = new ChunkedInputStream (this, ris);
            uis = uis_orig;
        } else {
            uis_orig = new FixedLengthInputStream (this, ris, reqContentLen);
            uis = uis_orig;
        }
        return uis;
    }

    LeftOverInputStream getOriginalInputStream () {
        return uis_orig;
    }

    public int getResponseCode () {
        return rcode;
    }

    public OutputStream getResponseBody () {
        /* TODO. Change spec to remove restriction below. Filters
         * cannot work with this restriction
         *
         * if (!sentHeaders) {
         *    throw new IllegalStateException ("headers not sent");
         * }
         */
        if (uos == null) {
            uos_orig = new PlaceholderOutputStream (null);
            uos = uos_orig;
        }
        return uos;
    }


    /* returns the place holder stream, which is the stream
     * returned from the 1st call to getResponseBody()
     * The "real" ouputstream is then placed inside this
     */
    PlaceholderOutputStream getPlaceholderResponseBody () {
        getResponseBody();
        return uos_orig;
    }

    public void sendResponseHeaders (int rCode, long contentLen)
    throws IOException
    {
        if (sentHeaders) {
            throw new IOException ("headers already sent");
        }
        this.rcode = rCode;
        String statusLine = "HTTP/1.1 "+rCode+Code.msg(rCode)+"\r\n";
        OutputStream tmpout = new BufferedOutputStream (ros);
        PlaceholderOutputStream o = getPlaceholderResponseBody();
        tmpout.write (bytes(statusLine, 0), 0, statusLine.length());
        boolean noContentToSend = false; // assume there is content
        rspHdrs.set ("Date", dateFormat.get().format (new Date()));

        /* check for response type that is not allowed to send a body */

        if ((rCode>=100 && rCode <200) /* informational */
            ||(rCode == 204)           /* no content */
            ||(rCode == 304))          /* not modified */
        {
            if (contentLen != -1) {
                Logger logger = server.getLogger();
                String msg = "sendResponseHeaders: rCode = "+ rCode
                    + ": forcing contentLen = -1";
                logger.warning (msg);
            }
            contentLen = -1;
        }

        if (isHeadRequest()) {
            /* HEAD requests should not set a content length by passing it
             * through this API, but should instead manually set the required
             * headers.*/
            if (contentLen >= 0) {
                final Logger logger = server.getLogger();
                String msg =
                    "sendResponseHeaders: being invoked with a content length for a HEAD request";
                logger.warning (msg);
            }
            noContentToSend = true;
            contentLen = 0;
        } else { /* not a HEAD request */
            if (contentLen == 0) {
                if (http10) {
                    o.setWrappedStream (new UndefLengthOutputStream (this, ros));
                    close = true;
                } else {
                    rspHdrs.set ("Transfer-encoding", "chunked");
                    o.setWrappedStream (new ChunkedOutputStream (this, ros));
                }
            } else {
                if (contentLen == -1) {
                    noContentToSend = true;
                    contentLen = 0;
                }
                rspHdrs.set("Content-length", Long.toString(contentLen));
                o.setWrappedStream (new FixedLengthOutputStream (this, ros, contentLen));
            }
        }
        write (rspHdrs, tmpout);
        this.rspContentLen = contentLen;
        tmpout.flush() ;
        tmpout = null;
        sentHeaders = true;
        if (noContentToSend) {
            WriteFinishedEvent e = new WriteFinishedEvent (this);
            server.addEvent (e);
            closed = true;
        }
        server.logReply (rCode, req.requestLine(), null);
    }

    void write (Headers map, OutputStream os) throws IOException {
        Set<Map.Entry<String,List<String>>> entries = map.entrySet();
        for (Map.Entry<String,List<String>> entry : entries) {
            String key = entry.getKey();
            byte[] buf;
            List<String> values = entry.getValue();
            for (String val : values) {
                int i = key.length();
                buf = bytes (key, 2);
                buf[i++] = ':';
                buf[i++] = ' ';
                os.write (buf, 0, i);
                buf = bytes (val, 2);
                i = val.length();
                buf[i++] = '\r';
                buf[i++] = '\n';
                os.write (buf, 0, i);
            }
        }
        os.write ('\r');
        os.write ('\n');
    }

    private byte[] rspbuf = new byte [128]; // used by bytes()

    /**
     * convert string to byte[], using rspbuf
     * Make sure that at least "extra" bytes are free at end
     * of rspbuf. Reallocate rspbuf if not big enough.
     * caller must check return value to see if rspbuf moved
     */
    private byte[] bytes (String s, int extra) {
        int slen = s.length();
        if (slen+extra > rspbuf.length) {
            int diff = slen + extra - rspbuf.length;
            rspbuf = new byte [2* (rspbuf.length + diff)];
        }
        char c[] = s.toCharArray();
        for (int i=0; i<c.length; i++) {
            rspbuf[i] = (byte)c[i];
        }
        return rspbuf;
    }

    public InetSocketAddress getRemoteAddress (){
        Socket s = connection.getChannel().socket();
        InetAddress ia = s.getInetAddress();
        int port = s.getPort();
        return new InetSocketAddress (ia, port);
    }

    public InetSocketAddress getLocalAddress (){
        Socket s = connection.getChannel().socket();
        InetAddress ia = s.getLocalAddress();
        int port = s.getLocalPort();
        return new InetSocketAddress (ia, port);
    }

    public String getProtocol (){
        String reqline = req.requestLine();
        int index = reqline.lastIndexOf (' ');
        return reqline.substring (index+1);
    }

    public SSLSession getSSLSession () {
        SSLEngine e = connection.getSSLEngine();
        if (e == null) {
            return null;
        }
        return e.getSession();
    }

    public Object getAttribute (String name) {
        if (name == null) {
            throw new NullPointerException ("null name parameter");
        }
        if (attributes == null) {
            attributes = getHttpContext().getAttributes();
        }
        return attributes.get (name);
    }

    public void setAttribute (String name, Object value) {
        if (name == null) {
            throw new NullPointerException ("null name parameter");
        }
        if (attributes == null) {
            attributes = getHttpContext().getAttributes();
        }
        attributes.put (name, value);
    }

    public void setStreams (InputStream i, OutputStream o) {
        assert uis != null;
        if (i != null) {
            uis = i;
        }
        if (o != null) {
            uos = o;
        }
    }

    /**
     * PP
     */
    HttpConnection getConnection () {
        return connection;
    }

    ServerImpl getServerImpl () {
        return getHttpContext().getServerImpl();
    }

    public HttpPrincipal getPrincipal () {
        return principal;
    }

    void setPrincipal (HttpPrincipal principal) {
        this.principal = principal;
    }

    static ExchangeImpl get (HttpExchange t) {
        if (t instanceof HttpExchangeImpl) {
            return ((HttpExchangeImpl)t).getExchangeImpl();
        } else {
            assert t instanceof HttpsExchangeImpl;
            return ((HttpsExchangeImpl)t).getExchangeImpl();
        }
    }
}

/**
 * An OutputStream which wraps another stream
 * which is supplied either at creation time, or sometime later.
 * If a caller/user tries to write to this stream before
 * the wrapped stream has been provided, then an IOException will
 * be thrown.
 */
class PlaceholderOutputStream extends java.io.OutputStream {

    OutputStream wrapped;

    PlaceholderOutputStream (OutputStream os) {
        wrapped = os;
    }

    void setWrappedStream (OutputStream os) {
        wrapped = os;
    }

    boolean isWrapped () {
        return wrapped != null;
    }

    private void checkWrap () throws IOException {
        if (wrapped == null) {
            throw new IOException ("response headers not sent yet");
        }
    }

    public void write(int b) throws IOException {
        checkWrap();
        wrapped.write (b);
    }

    public void write(byte b[]) throws IOException {
        checkWrap();
        wrapped.write (b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        checkWrap();
        wrapped.write (b, off, len);
    }

    public void flush() throws IOException {
        checkWrap();
        wrapped.flush();
    }

    public void close() throws IOException {
        checkWrap();
        wrapped.close();
    }
}