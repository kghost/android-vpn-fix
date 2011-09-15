package info.kghost.android.openvpn;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class StopVpnService extends Service {
	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(StopVpnService.class.getName(), "Stopping VPN.");
		try {
			Class<?> vpnManagerClass = Class
					.forName("android.net.vpn.VpnManager");
			Object vpnManager = vpnManagerClass.getConstructor(Context.class)
					.newInstance(this);
			Object bindResult = vpnManagerClass.getMethod("bindVpnService",
					ServiceConnection.class).invoke(vpnManager,
					new ServiceConnection() {
						public void onServiceConnected(ComponentName className,
								IBinder service) {
							try {
								Object stub = Class
										.forName(
												"android.net.vpn.IVpnService$Stub")
										.getMethod("asInterface", IBinder.class)
										.invoke(null, service);
								Class.forName("android.net.vpn.IVpnService")
										.getMethod("disconnect").invoke(stub);
							} catch (Exception e) {
								Log.e(StopVpnService.class.getName(),
										"Invoke service failed", e);
							} finally {
								StopVpnService.this.unbindService(this);
							}
						}

						public void onServiceDisconnected(
								ComponentName className) {
							StopVpnService.this.stopSelf();
						}
					});
			if (!(bindResult instanceof Boolean)) {
				this.stopSelf();
			}
		} catch (Exception e) {
			Log.e(StopVpnService.class.getName(), "Bind service failed", e);
			this.stopSelf();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
