package util;

public interface HttpCallbackListener {
	
	
	//�����ݴӷ������ϼ��سɹ�
	void onFinish(String response);
	
	//���ݴӷ������ϼ���ʧ��
	void onError(Exception e);
}
