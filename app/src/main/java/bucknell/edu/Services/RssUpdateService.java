package bucknell.edu.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RssUpdateService extends Service {
    public RssUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
