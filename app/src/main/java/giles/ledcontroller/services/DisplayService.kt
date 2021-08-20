package giles.ledcontroller.services

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import giles.bluetooth.BluetoothSerial
import giles.ledcontroller.AppData
import giles.ledcontroller.Pattern
import giles.ledcontroller.R
import giles.ledcontroller.activities.MainActivity
import kotlin.concurrent.withLock

/**
 * A foreground service which sends pattern frame data over bluetooth to the connected device
 */
class DisplayService: Service(){
    private val channelId = "DisplayServiceChannel"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Get pattern from intent extra
        val pattern = intent.getSerializableExtra(getString(R.string.EXTRA_PATTERN)) as Pattern

        //Make notification
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Displaying " + pattern.name)
            .setContentIntent(pendingIntent)
            .build()

        //Send service to foreground
        startForeground(1, notification)

        val thread = Thread {
            //Generate frame matrix
            val frameMatrix = pattern.generateFrameMatrix(AppData.display.numLights)
            while(AppData.display.bluetooth.connectionState == BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED) {
                //Iterate over frames
                for (frame in frameMatrix) {
                    //Wait until ready for next frame
                    try {
                        AppData.display.frameSemaphore.acquire()
                    } catch (e: InterruptedException){
                        stopForeground(true)
                        stopSelf()
                        return@Thread
                    }

                    //Iterate over lights in frame
                    for (color in frame) {
                        //Make a byte array
                        val array = byteArrayOf(
                            (Color.red(color) * AppData.display.brightness).toInt().toByte(),
                            (Color.green(color) * AppData.display.brightness).toInt().toByte(),
                            (Color.blue(color) * AppData.display.brightness).toInt().toByte()
                        )
                        //Send array over BT serial connection
                        AppData.display.bluetooth.write(array)
                    }
                    if(AppData.display.bluetooth.connectionState != BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED){
                        stopForeground(true)
                        stopSelf()
                        return@Thread
                    }
                }
            }
            stopForeground(true)
            stopSelf()
            return@Thread
        }
        thread.start()

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Pattern Display Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }
}