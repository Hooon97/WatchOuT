package com.ssafy.homealone

import io.flutter.embedding.android.FlutterActivity
import android.os.Bundle
import io.flutter.plugin.common.MethodChannel
import android.media.AudioManager
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender
import android.content.ActivityNotFoundException
import android.telephony.emergency.EmergencyNumber
import android.provider.Settings
import android.provider.Settings.Secure
import android.provider.Settings.System
import android.app.PendingIntent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.FragmentActivity
import android.util.Log
import android.telephony.SmsManager


class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.ssafy.homealone/channel"
    lateinit var mAudioManager: AudioManager
    lateinit var smsManager: SmsManager
    lateinit var s: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GeneratedPluginRegistrant.registerWith(FlutterEngine(this))
        MethodChannel(flutterEngine?.getDartExecutor()?.getBinaryMessenger()!!, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "sosSoundSetting") {
                s = sosSoundSetting()
                result.success(s)
            } else if (call.method == "openEmergencySetting") {
                try {
                    openEmergencySetting()
                    result.success("opened")
                } catch (e: ActivityNotFoundException) {
                    Log.e(CHANNEL, e.message ?:"EmptyMsg")
                    result.error("UNAVAILABLE", "SOS 설정을 열 수 없습니다.", null)
                }
            } else if (call.method == "sendTextMessage") {
                val recipients : List<String>? = call.argument("recipients")
                val message : String? = call.argument("message")
                try {
                    smsManager = this.getSystemService(SmsManager::class.java)
                    val parts : ArrayList<String>? = smsManager.divideMessage(message);
                    for (recipient in recipients!!) {
                        smsManager.sendMultipartTextMessage(recipient, null, parts!!, null, null)
                    }
                    result.success("sent")
                } catch (e: Exception) {
                    Log.e(CHANNEL, e.message ?:"EmptyMsg")
                    result.error("UNAVAILABLE", "문자 전송에 실패했습니다.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun openEmergencySetting() {
//        val activity = startActivity(Intent(Settings.ACTION_SETTINGS))
        val intent = Intent()
        intent.component = ComponentName("com.sec.android.app.safetyassurance", "com.sec.android.app.safetyassurance.settings.SafetyAssuranceSetting")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = startActivity(intent)
    }

    private fun sosSoundSetting(): String {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager;
        if (mAudioManager.isBluetoothA2dpOn()) {
            mAudioManager.stopBluetoothSco();
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.setSpeakerphoneOn(true);
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            return "bluetooth"
        }
        if(mAudioManager.isWiredHeadsetOn()){
            mAudioManager.setWiredHeadsetOn(false);
            mAudioManager.setSpeakerphoneOn(true);
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            return "wired headset on"
        }
        return "success"
    }
}
