package receiver;

import service.AutoUpdateService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent i = new Intent(context, AutoUpdateService.class);
		//开启服务，只是再次去启动AutoUpdateService
		context.startService(i);
	}

}
