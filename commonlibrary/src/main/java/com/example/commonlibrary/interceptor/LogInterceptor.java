package com.example.commonlibrary.interceptor;

import androidx.annotation.Nullable;

import com.example.commonlibrary.net.OkHttpGlobalHandler;
import com.example.commonlibrary.utils.CommonLogger;
import com.example.commonlibrary.utils.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * Created by COOTEK on 2017/7/31.
 */
@Singleton
public class LogInterceptor implements Interceptor {

    public enum Level {
        NONE,       //涓嶆墦鍗發og
        BASIC,      //鍙墦鍗?璇锋眰棣栬 鍜?鍝嶅簲棣栬
        HEADERS,    //鎵撳嵃璇锋眰鍜屽搷搴旂殑鎵€鏈?Header
        BODY        //鎵€鏈夋暟鎹叏閮ㄦ墦鍗?    }


    private OkHttpGlobalHandler handler;
    private Level printLevel;
    private java.util.logging.Level colorLevel;
    private static final Charset UTF8 = Charset.forName("UTF-8");


    @Inject
    public LogInterceptor(@Nullable OkHttpGlobalHandler okHttpGlobalHandler, @Nullable Level level) {
        handler = okHttpGlobalHandler;
        if (level != null) {
            printLevel = level;
        } else {
            printLevel = Level.BODY;
        }
        colorLevel = java.util.logging.Level.INFO;
        logger=Logger.getLogger("JUN");
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (printLevel == Level.NONE) {
            return chain.proceed(request);
        }

        //璇锋眰鏃ュ織鎷︽埅
        logForRequest(request, chain.connection());

        //鎵ц璇锋眰锛岃绠楄姹傛椂闂?        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        if (handler != null) {
//            handler.onResultResponse()
        }
        //鍝嶅簲鏃ュ織鎷︽埅
        return logForResponse(response, tookMs, chain);
    }


    private void logForRequest(Request request, Connection connection) throws IOException {
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;

        try {
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
            log(requestStartMessage);

            if (logHeaders) {
                if (hasRequestBody) {
                    // Request body headers are only present when installed as a network interceptor. Force
                    // them to be included (when available) so there values are known.
                    if (requestBody.contentType() != null) {
                        log("\tContent-Type: " + requestBody.contentType());
                    }
                    if (requestBody.contentLength() != -1) {
                        log("\tContent-Length: " + requestBody.contentLength());
                    }
                }
                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    String name = headers.name(i);
                    // Skip headers from the request body as they are explicitly logged above.
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        log("\t" + name + ": " + headers.value(i));
                    }
                }

                log(" ");
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody.contentType())) {
                        bodyToString(request);
                    } else {
                        log("\tbody: maybe [binary body], omitted!");
                    }
                }
            }
        } catch (Exception e) {
            CommonLogger.printStackTrace(e);
        } finally {
            log("--> END " + request.method());
        }
    }

    private Logger logger;

    private void log(String message) {
//        logger.log(colorLevel, message);
        CommonLogger.e(message);
    }

    private Response logForResponse(Response response, long tookMs, Chain chain) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);

        try {
            log("<-- " + clone.code() + ' ' + clone.message() + ' ' + clone.request().url() + " (" + tookMs + "ms锛?);
            if (logHeaders) {
                Headers headers = clone.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    log("\t" + headers.name(i) + ": " + headers.value(i));
                }
                log(" ");
                if (logBody && HttpHeaders.hasBody(clone)) {
                    if (responseBody == null) return response;

                    if (isPlaintext(responseBody.contentType())) {
                        byte[] bytes = IOUtils.toByteArray(responseBody.byteStream());
                        MediaType contentType = responseBody.contentType();
                        String body = new String(bytes, getCharset(contentType));
                        log("\tbody:" + body);
                        responseBody = ResponseBody.create(responseBody.contentType(), bytes);

                        Response newResponse = response.newBuilder().body(responseBody).build();
                        if (handler != null) {
                            handler.onResultResponse(body, chain, newResponse);
                        }
                        return newResponse;
                    } else {
                        log("\tbody: maybe [binary body], omitted!");
                    }
                }
            }
        } catch (Exception e) {
            CommonLogger.printStackTrace(e);
        } finally {
            log("<-- END HTTP");
        }
        if (handler != null) {
            handler.onResultResponse(null, chain, response);
        }
        return response;
    }


    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset() : UTF8;
        if (charset == null) charset = UTF8;
        return charset;
    }


    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            if (subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html")) //
                return true;
        }
        return false;
    }

    private void bodyToString(Request request) {
        try {
            Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
            if (body == null) return;
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            Charset charset = getCharset(body.contentType());
            log("\tbody:" + buffer.readString(charset));
        } catch (Exception e) {
            CommonLogger.printStackTrace(e);
        }
    }
}

