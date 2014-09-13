package bucknell.edu.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RssUpdateService extends Service {
    public RssUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("Services started", "OnCreate");
        stopSelf();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.i("Services started", "OnStartCommand");
        stopSelf();
        return START_NOT_STICKY;
    }

}
