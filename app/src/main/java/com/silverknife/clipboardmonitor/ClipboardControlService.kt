package com.silverknife.clipboardmonitor

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class ClipboardControlService : Service() {
    private val topic: String = "ClipboardSync"
    private val qos: Int = 2
    private lateinit var clientID: String
    private lateinit var persistence: MemoryPersistence
    private lateinit var mqttClient: MqttClient
    private lateinit var coonOpts: MqttConnectOptions
    private val pushCallback = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            Log.e("mqtt", "连接丢失:${cause.toString()}")
            mqttClient.connect(coonOpts)
            mqttClient.subscribe(topic)
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            Log.d("mqtt", "收到消息主题:${topic}")
            Log.d("mqtt", "收到消息Qos:${message?.qos}")
            Log.d("mqtt", "收到消息内容:${String(message?.payload!!)}")
            val pushModel = Gson().fromJson(String(message.payload!!), PushModel::class.java)
            handleContent(pushModel)
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.e("mqtt", "${token?.isComplete}")
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "服务启动")
        clientID = DeviceUtils.getMacAddress()
        persistence = MemoryPersistence()
        try {
            mqttClient = MqttClient(Constants.BROKER, clientID, persistence)
            coonOpts = MqttConnectOptions()
            coonOpts.userName = "op6t"
            coonOpts.password = "L123456".toCharArray()
            coonOpts.isCleanSession = true
            mqttClient.setCallback(pushCallback)
            mqttClient.connect(coonOpts)
            mqttClient.subscribe(topic)
        } catch (me: MqttException) {
            Log.e("reason ", me.getReasonCode().toString())
            Log.e("msg ", me.message.toString())
            Log.e("loc ", me.getLocalizedMessage().toString())
            Log.e("cause ", me.cause.toString())
            Log.e("excep ", me.toString())
            me.printStackTrace();
        }
    }

    fun handleContent(pushModel: PushModel){
        when(pushModel.type){
            0->{

            }
            1->{
                val uri = Uri.parse(pushModel.content)
                val intent = Intent(Intent.ACTION_VIEW,uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    /**
     * 注销监听，避免内存泄漏。
     */
    override fun onDestroy() {
        super.onDestroy()
        mqttClient.disconnect()
        mqttClient.close()
    }
}