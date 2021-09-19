package giles.ledcontroller.services

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import giles.bluetooth.BluetoothSerial
import giles.ledcontroller.AppData
import giles.ledcontroller.R
import giles.ledcontroller.activities.MainActivity


/**
 * A foreground service which sends pattern frame data over bluetooth to the connected device
 */
class DisplayService: Service(){
    private val channelId = "DisplayServiceChannel"
    private lateinit var displayThread: Thread

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Get pattern from intent extra
        val pattern = AppData.currentDisplay.currentPattern

        //Make notification
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Display Service is Running")
            .setContentText("Displaying " + pattern.name)
            .setSmallIcon(R.drawable.ic_chevron_right_gray_32dp)
            .setContentIntent(pendingIntent)
            .build()

        //Send service to foreground
        startForeground(1, notification)

        displayThread = Thread {
            //Generate frame matrix
            val frameMatrix = pattern.generateFrameMatrix(AppData.currentDisplay.numLights)

            //Continuously send frame data while connected to BT
            while(!Thread.currentThread().isInterrupted && AppData.currentDisplay.bluetooth.connectionState ==
                BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED) {
                //Iterate over frames
                for (frame in frameMatrix) {
                    //Wait until ready for next frame
                    try {
                        AppData.currentDisplay.frameSemaphore.acquire()
                    } catch (e: InterruptedException){
                        stopForeground(true)
                        stopSelf()
                        return@Thread
                    }

                    //Iterate over lights in frame to build byte array
                    val array = ByteArray(frame.size * 3)
                    var i = 0
                    for (color in frame) {
                        array[i++] = (Color.red(color) * AppData.currentDisplay.brightness).toInt().toByte()
                        array[i++] = (Color.green(color) * AppData.currentDisplay.brightness).toInt().toByte()
                        array[i++] = (Color.blue(color) * AppData.currentDisplay.brightness).toInt().toByte()
                    }
                    //Check to make sure connection is still established and this thread is not interrupted
                    if(AppData.currentDisplay.bluetooth.connectionState !=
                        BluetoothSerial.BluetoothConnectionState.STATE_CONNECTED ||
                        Thread.currentThread().isInterrupted){
                        stopForeground(true)
                        stopSelf()
                        return@Thread
                    }

                    //Send array over BT serial connection
                    AppData.currentDisplay.bluetooth.write(array)
                }
            }
            stopForeground(true)
            stopSelf()
            return@Thread
        }
        displayThread.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            channelId,
            "Display Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        displayThread.interrupt()
    }
}

