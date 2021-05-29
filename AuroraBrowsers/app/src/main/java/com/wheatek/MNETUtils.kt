package com.wheatek.shy.mylibrary

import android.content.Context
import com.wheatek.HTTPSTrustManager
import com.wheatek.SystemInfoUtils
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.concurrent.thread

val url = "https://ee7ddfec-e9f9-408f-ac4b-d7c16adb25df.bspapp.com/http/device"

interface Callb {
    fun response(response: String, isSuccess: Boolean)
}

class MNETUtils {
    companion object {
        fun postPhoneInfotest(context: Context, callb: Callb? = null) {
            val json = JSONObject()
            json.put("action", "register")
            json.put("imei", SystemInfoUtils.getDeviceID(context))
            json.put("os", SystemInfoUtils.getSdkVersion())
            json.put("version", SystemInfoUtils.getAppVersion(context).versionName)
            println(json)
            thread {
                try {
                    postPhoneInfo(context, json.toString(), callb)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    var message = JSONObject()
                    message.put("error", e.toString())
                    callb?.response(message.toString(), false)
                }
            }
        }

        val charsetName = "utf-8"
        fun postPhoneInfo(context: Context, jsonstr: String, callb: Callb? = null) {
            if (url.startsWith("https")) return postPhoneInfohttps(context, jsonstr, callb)
            val conn: URLConnection? =
                URL(url).openConnection()
            if (conn is HttpURLConnection) {
                doconnect(conn, context, jsonstr, callb)
            }

        }

        private fun doconnect(
            conn: HttpURLConnection,
            context: Context,
            jsonstr: String,
            callb: Callb?
        ) {
            try {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//设置不要缓存
                conn.setInstanceFollowRedirects(true);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();
                //POST请求
                var out: PrintWriter =
                    PrintWriter(OutputStreamWriter(conn.getOutputStream(), charsetName))
                out.print(jsonstr)
                out.flush();
                //读取响应
                var reader = BufferedReader(InputStreamReader(conn.getInputStream()));
                var response = ""
                var lines: String
                lines = reader.readLine()
                while (true) {
                    lines = String(lines.toByteArray(), Charset.forName(charsetName));
                    response += lines
                    var c = reader?.readLine();
                    if (c == null) break
                    lines = c;
                }
                reader.close();
                // 断开连接
                conn.disconnect();
                var isSuccess = false;
                try {
                    isSuccess = JSONObject(response).getBoolean("success")
                } catch (e: java.lang.Exception) {
                }
                callb?.response(response, isSuccess)
            } catch (e: Throwable) {
                e.printStackTrace()
                var message = JSONObject()
                message.put("error", e.toString())
                callb?.response(message.toString(), false)
            }
        }

        private fun postPhoneInfohttps(context: Context, jsonstr: String, callb: Callb?) {
            val conn: URLConnection? =
                URL(url).openConnection()
            if (conn is HttpsURLConnection) {
                HTTPSTrustManager.allowAllSSL();
//                conn.setSSLSocketFactory(getSSLContext(context)?.getSocketFactory());
                doconnect(conn, context, jsonstr, callb)
            }
        }

        fun getSSLContext(inputContext: Context): SSLContext? {
            var context: SSLContext? = null;
            try {
                var cf: CertificateFactory = CertificateFactory.getInstance("X.509");
                var ini: InputStream = inputContext.getAssets().open("root.crt");
                var ca: Certificate = cf.generateCertificate(ini);
                var keystore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, null);
                keystore.setCertificateEntry("ca", ca);
                var tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm();
                var tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keystore);
                // Create an SSLContext that uses our TrustManager
                context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
            } catch (e: Exception) {
                e.printStackTrace();
            }
            return context;
        }
    }


}
