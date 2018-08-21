package io.yugoal.meizi.net;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 网络拦截器
 *
 * @author yugoal
 * @date 2018/08/23/017
 */

public class NetInterceptor implements Interceptor {
    public static final String TAG = "NetInterceptor";
    private String tag;
    /**
     * 是否显示返回数据
     */
    private boolean showResponse;

    public NetInterceptor(String tag, boolean showResponse) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        this.showResponse = showResponse;
        this.tag = tag;
    }

    public NetInterceptor(String tag) {
        this(tag, true);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request().newBuilder()
//                .addHeader("version", String.valueOf(AndroidUtil.getLocalVersion()))
                .build();
        logForRequest(request);
        return chain.proceed(request);
    }

    private Response logForResponse(Response response) {
        try {
            //===>response log
            Log.e(tag, "========response'log=======");
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            Log.e(tag, "url : " + clone.request().url());
            Log.e(tag, "code : " + clone.code());
            Log.e(tag, "protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message())) {
                Log.e(tag, "message : " + clone.message());
            }
            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        Log.e(tag, "responseBody's contentType : " + mediaType.toString());
                        if (isText(mediaType)) {
                            String resp = body.string();
                            Log.e(tag, "responseBody's  : " + resp);
                            JSONObject jsonObject = new JSONObject(resp);
                            String code = jsonObject.optString("code");
                            if ("no_login".equals(code)) {
                                //ToastUtil.showToast("请退出程序，重新启动！");
                            }
                            Log.e(tag, "responseBody's content : " + resp);
                            body = ResponseBody.create(mediaType, resp);
                            return response.newBuilder().body(body).build();
                        } else {
                            Log.e(tag, "responseBody's content : " + " maybe [file part] , too large too print , ignored!");
                        }
                    }
                }
            }
            Log.e(tag, "========response'log=======end");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private void logForRequest(Request request) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();
            Log.e(tag, "========request'log=======");
            Log.e(tag, "method : " + request.method());
            Log.e(tag, "url : " + url);
            if (headers != null && headers.size() > 0) {
                Log.e(tag, "headers : " + headers.get("version"));
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    Log.e(tag, "requestBody's contentType : " + mediaType.toString());
                    Log.e(tag, "requestBody's contentType : " + requestBody.toString());
                    if (isText(mediaType)) {
                        Log.e(tag, "requestBody's content : " + bodyToString(request));
                    } else {
                        Log.e(tag, "requestBody's content : " + " maybe [file part] , too large too print , ignored!");
                    }
                }
            }
            Log.e(tag, "========request'log=======end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && "text".equals(mediaType.type())) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if ("json".equals(mediaType.subtype()) ||
                    "xml".equals(mediaType.subtype()) ||
                    "html".equals(mediaType.subtype()) ||
                    "webviewhtml".equals(mediaType.subtype())
                    ) {
                return true;
            }
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }


}
