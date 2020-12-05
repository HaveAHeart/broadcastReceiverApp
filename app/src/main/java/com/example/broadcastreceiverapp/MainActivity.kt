package com.example.broadcastreceiverapp

import android.content.*
import android.net.ConnectivityManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

private const val MSG_REGISTER = 2
private const val MSG_UNREGISTER = 3
private const val MSG_RETVAL = 4

class MainActivity : AppCompatActivity() {
    private var serviceMessenger: Messenger? = null
    //private lateinit var receiver: BroadcastReceiver
    private lateinit var tv: TextView
    private val filter = IntentFilter("picDownloaded")
    private var isBound = false

    inner class activityHandler: Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                MSG_RETVAL -> {
                    tv.text = msg.data.getString("address")
                }
                else -> super.handleMessage(msg)
            }
        }
    }
    private var activityMessenger: Messenger?  = Messenger(activityHandler())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(compName: ComponentName, binder: IBinder) {
            serviceMessenger = Messenger(binder)
            try {
                val retBundle = Bundle()
                retBundle.putString("urlText", "https://smartminds.ru/wp-content/uploads/2019/12/foto-2-kartinka-s-pozhelaniem-prekrasnogo-nastroeniya-na-ves-den.jpg")
                
                val msgStartDownload = Message.obtain(null, MSG_REGISTER).apply {
                    data = retBundle
                    replyTo = activityMessenger
                }
                serviceMessenger!!.send(msgStartDownload)

            } catch (e: RemoteException) {
                //disconnection logic is in service, no need to worry
            }
        }
        override fun onServiceDisconnected(compName: ComponentName) {
            serviceMessenger = null
        }
    }

    private fun doBindService() {
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Toast.makeText(this, "BINDED", Toast.LENGTH_SHORT).show()
        isBound = true
    }

    private fun doUnbindService() {
        if (isBound) {
            try {
                val msg = Message.obtain(null, MSG_UNREGISTER)
                msg.replyTo = activityMessenger
                serviceMessenger!!.send(msg)
            } catch (e: RemoteException) {

            }
            unbindService(connection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = textView

        //val receiver = PicDownloadBroadcastReceiver()
        //registerReceiver(receiver, filter)

        intent = Intent()
        intent.component = ComponentName("com.example.serviceapp", "com.example.serviceapp.DownloaderService")

        doBindService()

        
    }

    override fun onStart() {
        super.onStart()
        //registerReceiver(receiver, filter)
        Log.d("receiverCUSTOM", "registered")
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(receiver)
        Log.d("receiverCUSTOM", "UNregistered")
    }

    inner class PicDownloadBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val path = intent.getStringExtra(Intent.EXTRA_TEXT)
            tv.text = path
        }
    }
}