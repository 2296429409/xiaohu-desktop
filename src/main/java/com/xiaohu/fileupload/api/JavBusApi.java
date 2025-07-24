package com.xiaohu.fileupload.api;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaohu.fileupload.TrustAllCerts;
import com.xiaohu.fileupload.pojo.MovieVo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaxh
 * @date 2024/11/7
 */
public class JavBusApi {

    /**
     * 创建OkHttpClient
     * @return
     */
    public static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .proxy(
                        new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress("127.0.0.1", 7890)))
                .connectTimeout(1000L, TimeUnit.SECONDS)
                .readTimeout(1000L, TimeUnit.SECONDS)
                .writeTimeout(1000L, TimeUnit.SECONDS)
                .sslSocketFactory(TrustAllCerts.createSSLSocketFactory())
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())
                .build();
    }

    /**
     * 查询详情信息
     * @param id
     * @return
     */
    public static MovieVo getVideoInfo(String id) {
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url("https://javbus-api-from-ovnrain-tau.vercel.app/api/movies/" + id)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            if (!jsonObject.containsKey("id")) {
                return null;
            }
            return jsonObject.toJavaObject(MovieVo.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 名称模糊查询获取视频id
     * @param keyword
     * @return
     */
    public static String getVideoLike(String keyword) {
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url("https://javbus-api-from-ovnrain-tau.vercel.app/api/movies/search?keyword=" + keyword)
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("User-Agent", "PostmanRuntime-ApipostRuntime/1.1.0")
                .addHeader("Connection", "keep-alive")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "javbus-api-from-ovnrain-tau.vercel.app")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            JSONArray movies = jsonObject.getJSONArray("movies");
            if (movies.isEmpty()) {
                return null;
            }
            return movies.getJSONObject(0).getString("id");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 名称模糊查询获取视频详情信息
     * @param keyword
     * @return
     */
    public static MovieVo getVideoInfoByName(String keyword) {
        String videoId = getVideoLike(keyword);
        if (videoId != null){
            return getVideoInfo(videoId);
        }
        return null;
    }

    public static void main(String[] args) {
        String videoId = getVideoLike("VEC705");
        System.out.println(videoId);
        MovieVo video = getVideoInfo("VEC-705");
        System.out.println(video);
    }
}
