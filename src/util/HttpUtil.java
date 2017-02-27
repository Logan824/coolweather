package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpUtil {
	
	public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
		
		new Thread(new Runnable() {
			//请求网络属于耗时操作，开启线程来完成
			@Override
			public void run() {

				//与服务器建立连接
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection)url.openConnection();
					//设置请求方法必须要大写
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					//构建一个字符串构造器，来存放从服务器返回的响应数据
					StringBuilder response = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					if (listener != null) {
						//回调onFinsih()方法
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (listener != null) {
						//回调onError方法
						listener.onError(e);
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
				
			}
		}).start();
		
	}

}
