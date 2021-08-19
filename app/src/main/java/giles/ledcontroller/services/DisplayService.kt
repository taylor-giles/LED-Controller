package giles.ledcontroller.services

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import giles.ledcontroller.AppData
import giles.ledcontroller.MILLIS_BETWEEN_FRAMES
import giles.ledcontroller.Pattern
import giles.ledcontroller.R
import giles.ledcontroller.activities.MainActivity

/**
 * A foreground service which sends pattern frame data over bluetooth to the connected device
 */
class DisplayService: Service(){
    val channelId = "ForegroundServiceChannel"
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Get pattern from intent extra
        val pattern = intent.getSerializableExtra(getString(R.string.EXTRA_PATTERN)) as Pattern

        //Generate frame matrix
        val frameMatrix = pattern.generateFrameMatrix(AppData.display.numLights)

        //Make notification
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Displaying " + pattern.name)
            .setContentIntent(pendingIntent)
            .build()

        //Send service to foreground
        startForeground(1, notification)

        //Create thread to display pattern until BT connection is broken
        val thread = Thread {
            while(AppData.display.connection!!.isConnected()) {
                //Iterate over frames
                for (frame in frameMatrix) {
                    //Iterate over lights in frame
                    for (color in frame) {
                        //Send brightness-adjusted R, G, B components of color via BT Serial
                        AppData.display.connection!!.sendOneByte((Color.red(color) * AppData.display.brightness).toInt().toByte())
                        AppData.display.connection!!.sendOneByte((Color.green(color) * AppData.display.brightness).toInt().toByte())
                        AppData.display.connection!!.sendOneByte((Color.blue(color) * AppData.display.brightness).toInt().toByte())
                    }

//                    //Wait
//                    try {
//                        Thread.sleep(MILLIS_BETWEEN_FRAMES.toLong())
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
                    if (!AppData.display.connection!!.isConnected()) {
                        stopSelf()
                    }
                }
            }
            stopSelf()
        }
        thread.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }
}