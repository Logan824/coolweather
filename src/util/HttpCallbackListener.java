package util;

public interface HttpCallbackListener {
	
	
	//若数据从服务器上加载成功
	void onFinish(String response);
	
	//数据从服务器上加载失败
	void onError(Exception e);
}
