package me.figo.internal;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import me.figo.FigoError;

/**
 * Created by Zane on 9/26/2015.
 */
public class FigoRequest<T> extends Request<T> {
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private final Object data;

    public FigoRequest(int method, String apiEndpoint, String path, String authorization, Object data, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, apiEndpoint + path, errorListener);
        this.clazz = clazz;
        this.listener = listener;
        this.headers = new HashMap<String, String>();
        this.headers.put("Authorization", authorization);
        this.headers.put("Accept", "application/json");
        this.headers.put("Content-Type", "application/json");
        this.data = data;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(this.data != null) {
            String encodedData = GsonAdapter.getInstance().toJson(this.data);

            return encodedData.getBytes(Charset.forName("UTF-8"));
        }

        return null;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return this.headers;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            int code = response.statusCode;
            if(code >= 200 && code < 300) {
                return Response.success(handleResponse(response, clazz),
                        HttpHeaderParser.parseCacheHeaders(response));
            } else if (code == 400) {
                return Response.error(new FigoError((FigoError.ErrorResponse) handleResponse(response,
                       FigoError.ErrorResponse.class)));
            } else if (code == 401) {
                return Response.error(new FigoError("access_denied", "Access Denied"));
            } else {
                return Response.error(new FigoError("internal_server_error", "We are very sorry, but something went wrong"));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    private <C> C handleResponse(NetworkResponse response, Class<C> clazz) throws UnsupportedEncodingException {
        String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        return GsonAdapter.getInstance().fromJson(json, clazz);
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}
