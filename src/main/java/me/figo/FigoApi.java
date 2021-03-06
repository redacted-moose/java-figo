/*
 * The MIT License
 *
 * Copyright 2015 figo GmbH.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.figo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import me.figo.internal.FigoRequest;
import me.figo.internal.FigoTrustManager;
import me.figo.internal.GsonAdapter;

/**
 * @author halber
 */
public class FigoApi {
    private final RequestQueue requestQueue;
    private final String apiEndpoint;
    private final String authorization;
    private int timeout;

    public FigoApi(String apiEndpoint, String authorization, int timeout, RequestQueue requestQueue) {
        this.apiEndpoint = apiEndpoint;
        this.authorization = authorization;
        this.timeout = timeout;
        this.requestQueue = requestQueue;
    }

    /**
     * Helper method for making a OAuth2-compliant API call
     *  @param <T>           Type of expected response
     * @param clazz
     * @param path          path on the server to call
     * @param data          Payload of the request
     * @param method        the HTTP verb to use
     * @param clazz         Class of expected response
     * @param listener
     * @param errorListener @return the parsed result of the request
     */
    public <T> FigoRequest<T> queryApi(String path, Object data, int method, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        FigoRequest<T> request = new FigoRequest<T>(method, apiEndpoint, path, authorization, data, clazz, listener, errorListener);
        requestQueue.add(request);
        return request;
    }

    /**
     * Method to configure TrustManager.
     *
     * @param connection
     */
    protected void setupTrustManager(HttpURLConnection connection) throws IOException {
        if (connection instanceof HttpsURLConnection) {
            // Setup and install the trust manager
            try {
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new FigoTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("Connection setup failed", e);
            } catch (KeyManagementException e) {
                throw new IOException("Connection setup failed", e);
            }
        }
    }

    /**
     * Method to process the response.
     *
     * @param <T>
     * @param connection
     * @param typeOfT
     * @return
     */
    protected <T> T processResponse(HttpURLConnection connection, Type typeOfT) throws IOException, FigoError {
        // process response
        int code = connection.getResponseCode();
        if (code >= 200 && code < 300) {
            return handleResponse(connection.getInputStream(), typeOfT);
        } else if (code == 400) {
            throw new FigoError((FigoError.ErrorResponse) handleResponse(connection.getErrorStream(), FigoError.ErrorResponse.class));
        } else if (code == 401) {
            throw new FigoError("access_denied", "Access Denied");
        } else {
            // return decode(connection.getErrorStream(), resultType);
            throw new FigoError("internal_server_error", "We are very sorry, but something went wrong");
        }
    }

    /**
     * Handle the response of a request by decoding its JSON payload
     *
     * @param stream  Stream containing the JSON data
     * @param typeOfT Type of the data to be expected
     * @return Decoded data
     */
    protected <T> T handleResponse(InputStream stream, Type typeOfT) {
        // check whether decoding is actual requested
        if (typeOfT == null)
            return null;

        // read stream body
        Scanner s = new Scanner(stream, "UTF-8");
        s.useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";
        s.close();

        // decode JSON payload
        return createGson().fromJson(body, typeOfT);
    }

    /**
     * Instantiate the GSON class. Meant to be overridden in order to provide custom Gson settings.
     *
     * @return GSON instance
     */
    protected Gson createGson() {
        return GsonAdapter.createGson();
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * The timeout used for queries.
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout used for queries
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
