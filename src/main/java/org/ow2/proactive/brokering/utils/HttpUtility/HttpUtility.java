package org.ow2.proactive.brokering.utils.HttpUtility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;

public class HttpUtility {
    private static final String DFLT_CHARSET = "ISO-8859-1";

    private HttpUtility() {
    }

    public static String encodeUrl(String url) {
        try {
            return (new URLCodec()).encode(url, DFLT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new CLIException(CLIException.REASON_OTHER, e);
        }
    }

    public static void setInsecureAccess(HttpClient client)
            throws KeyManagementException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException {

        SSLSocketFactory socketFactory = new SSLSocketFactory(
                new RelaxedTrustStrategy(),
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme https = new Scheme("https", 443, socketFactory);
        client.getConnectionManager().getSchemeRegistry().register(https);
    }

    public static String encode(String unescaped) {
        try {
            return URLEncoder.encode(unescaped, DFLT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new CLIException(CLIException.REASON_OTHER, e);
        }
    }

    public static HttpClient threadSafeClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new PoolingClientConnectionManager(
                mgr.getSchemeRegistry()), params);
        return client;
    }

    private static class RelaxedTrustStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            return true;
        }
    }
}
