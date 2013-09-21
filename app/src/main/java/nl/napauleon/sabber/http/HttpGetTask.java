package nl.napauleon.sabber.http;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.security.*;
import java.io.IOException;
import java.io.InputStream;

public class HttpGetTask extends GetTask {

    public HttpGetTask(HttpCallback callback) {
		super(callback);
	}

    @Override
    protected String doInBackground(String... strings) {
        InputStream content = null;
        HttpResponse response = null;
        String request = strings[0];
        try {
            response = executeHttpRequest(request);
            content = response.getEntity().getContent();
            if (isCancelled()) {
                return null;
            }
            if(response.getStatusLine().getStatusCode() == 200 && content != null) {
                return (inputStreamToString(content)).toString();
            } else {
            	Log.w(TAG, "no response for request " + request);
            }
        } catch (ConnectTimeoutException e) {
            Log.w(TAG, "Connection timed out for uri " + request);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Http error occured", e);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Connection to ")) {
                Log.w(TAG, "Failed to connect to " + request);
            } else {
                Log.e(TAG, "IO exception occured", e);
            }
        }
        return null;
    }

    private HttpResponse executeHttpRequest(String string) throws IOException {
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 8000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 10000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 9095));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParameters, registry);

            return new DefaultHttpClient(ccm, httpParameters).execute(new HttpGet(string));
        } catch (Exception e) {
            return new DefaultHttpClient(httpParameters).execute(new HttpGet(string));
        }
    }

//    private HttpResponse executeSslHttpRequest(String string) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
//        // Load CAs from an InputStream
//// (could be from a resource or ByteArrayInputStream or ...)
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//// From https://www.washington.edu/itconnect/security/ca/load-der.crt
//        InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
//        Certificate ca;
//        try {
//            ca = cf.generateCertificate(caInput);
//            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
//        } finally {
//            caInput.close();
//        }
//
//// Create a KeyStore containing our trusted CAs
//        String keyStoreType = KeyStore.getDefaultType();
//        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("ca", ca);
//
//// Create a TrustManager that trusts the CAs in our KeyStore
//        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//        tmf.init(keyStore);
//
//// Create an SSLContext that uses our TrustManager
//        SSLContext context = SSLContext.getInstance("TLS");
//        context.init(null, tmf.getTrustManagers(), null);
//
//// Tell the URLConnection to use a SocketFactory from our SSLContext
//        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
//        HttpsURLConnection urlConnection =
//                (HttpsURLConnection)url.openConnection();
//        urlConnection.setSSLSocketFactory(context.getSocketFactory());
//        InputStream in = urlConnection.getInputStream();
//
//        HttpParams httpParameters = new BasicHttpParams();
//        httpParameters.s
//        response = new DefaultHttpClient(httpParameters).execute(new HttpGet(string));
//
//    }
}
