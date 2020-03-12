package cn.janking.jarsigner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val CODE_OPEN_UNSIGNED_APK = 1
    private val CODE_REQUEST_STORAGE_PERMISSION = 2
    private val GROUP_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var hasInit: Boolean = false
    private var unsignedApkFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        requestPermissions(GROUP_STORAGE, CODE_REQUEST_STORAGE_PERMISSION)
    }

    /**
     * 选择签名文件
     */
    fun pickUnsignedApkFile(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, CODE_OPEN_UNSIGNED_APK)
    }

    /**
     * 选择文件返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_OPEN_UNSIGNED_APK) {
            data?.data?.let {
                unsignedApkFile = UriUtils.uri2File(application, it)?.apply {
                    pickUnsignedApkFile.text = absolutePath
                }
                log("准备就绪...")
            }
        }
    }

    /**
     * 执行签名
     */
    fun startSign(view: View) {
        unsignedApkFile?.let {
            if (!it.exists()) {
                log("待签名apk不存在！")
                return
            } else if (FileUtils.getFileExtension(it) != "apk" &&
                    FileUtils.getFileExtension(it) != "jar" &&
                    FileUtils.getFileExtension(it) != "zip") {
                log("待签名文件必须是(.apk|.jar|.zip)！")
                return
            }
            log("开始签名...")
            val outPath: String = Environment.getExternalStorageDirectory().absolutePath + File.separator + "JarSigner"
            val outFile: String = outPath + File.separator + "out.apk"
            Thread(Runnable {
                try {
                    //签名
                    sun.security.tools.jarsigner.Main.main(
                            arrayOf(
                                    "-verbose",
                                    "-keystore", filesDir.absolutePath + File.separator + "test.jks",
                                    "-storepass", "123456",
                                    "-keyPass", "123456",
                                    "-signedjar",
                                    FileUtils.getExistFile(outFile).absolutePath,
                                    it.absolutePath,
                                    "test"
                            )
                    )
                } catch (e: java.lang.Exception) {
                    log("签名出错...(${e.message})")
                    e.printStackTrace()
                    return@Runnable
                }
                log("签名完成！($outFile)")
            }).start()

        } ?: log("请选择待签名apk文件！")

    }

    /**
     * 打开文件夹
     */
//    private fun openAssignFolder(path: String) {
//        val file = File(path)
//        if (!file.exists()) {
//            return
//        }
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.setDataAndType(UriUtils.file2Uri(application, file), "file/*")
//        try {
//            startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            e.printStackTrace()
//        }
//    }

    /**
     * 输出运行日志
     */
    private fun log(msg: String) {
        runOnUiThread {
            console.text = console.text.toString() + "\n" + msg
        }
    }

    /**
     * 请求权限返回
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (permission in permissions) {
            if (!isGranted(permission)) {
                if (!shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder(this)
                            .setTitle("获取权限失败")
                            .setMessage("请手动允许本应用获取存储权限")
                            .setPositiveButton("确定") { dialog, _ ->
                                dialog.dismiss()
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = fromParts("package", packageName, null)
                                intent.data = uri
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    finish()
                                }
                            }.show()
                } else {
                    requestPermissions(GROUP_STORAGE, CODE_REQUEST_STORAGE_PERMISSION)
                }
                return
            }
        }
        //表示已经授权
        try {
            FileUtils.copyFileToFile(assets.open("test.jks"), filesDir.absolutePath + File.separator + "test.jks")
        } catch (e: IOException) {
            log("复制签名出错！" + e.message)
            e.printStackTrace()
            return
        }
        if (!hasInit) {
            log("复制签名完成！")
        }
        hasInit = true
    }

    private fun isGranted(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(applicationContext, permission)
    }
}
