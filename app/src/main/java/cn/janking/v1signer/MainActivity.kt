package cn.janking.v1signer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.signapk.SignApk
import jarsigner.sun.security.tools.jarsigner.Main
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    /**
     * 选择未签名文件的request code
     */
    private val codePickUnsignedApk = 1

    /**
     * 请求权限的request code
     */
    private val codeRequestStoragePermission = 2

    /**
     * 读写权限组
     */
    private val permissionGroupStorage = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * jarSigner需要的的签名文件
     */
    private val keyJarSigner = "cert.jks"

    /**
     * apkSigner需要的签名文件
     */
    private val keyApkSignerPrivate = "platform.pk8"
    private val keyApkSignerPublic = "platform.x509.pem"

    /**
     * 用来复制签名的Runnable
     */
    private val runnableCopySignFile = Runnable {
        try {
            FileUtils.copyFileToFile(assets.open(keyJarSigner), filesDir.absolutePath + File.separator + keyJarSigner)
            FileUtils.copyFileToFile(assets.open(keyApkSignerPrivate), filesDir.absolutePath + File.separator + keyApkSignerPrivate)
            FileUtils.copyFileToFile(assets.open(keyApkSignerPublic), filesDir.absolutePath + File.separator + keyApkSignerPublic)
        } catch (e: IOException) {
            log("复制签名出错！" + e.message)
            e.printStackTrace()
        }
        if (!hasInit) {
            log("复制签名完成！")
        }
        hasInit = true
    }
    /**
     * 是否把签名复制完成
     */
    private var hasInit: Boolean = false

    /**
     * 未签名文件
     */
    private var unsignedApkFile: File? = null

    /**
     * 签名后APK保存文件夹
     */
    lateinit var outPath: String

    /**
     * 签名后APK完整路径
     */
    lateinit var outFile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //设置可以滚动
        console.run { console.movementMethod = ScrollingMovementMethod() }
        //6.0以下直接复制签名文件
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Thread(runnableCopySignFile).start()
        }
        //优先使用外置存储中本项目的file文件夹，其次是内部file文件夹
        outPath = getExternalFilesDir("V1Signer")?.absolutePath
                ?: filesDir.absolutePath + File.separator + "V1Signer"
        outFile = outPath + File.separator + "out.apk"
    }

    /**
     * 检查权限
     */
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionGroupStorage, codeRequestStoragePermission)
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
                    requestPermissions(permissionGroupStorage, codeRequestStoragePermission)
                }
                return
            }
        }
        //表示已经授权
        Thread(runnableCopySignFile).start()
    }

    /**
     * 判断权限是否被允许
     */
    private fun isGranted(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(applicationContext, permission)
    }


    /**
     * 在屏幕上输出运行日志
     */
    @SuppressLint("SetTextI18n")
    private fun log(msg: String) {
        runOnUiThread {
            console.text = "${console.text}\n${msg}"
        }
    }

    /**
     * 选择签名文件
     */
    fun pickUnsignedApkFile(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, codePickUnsignedApk)
    }

    /**
     * 选择文件返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == codePickUnsignedApk) {
            data?.data?.let {
                unsignedApkFile = UriUtils.uri2File(application, it)?.apply {
                    pickUnsignedApkFile.text = absolutePath
                    log("成功加载文件(${name})")
                }
            }
        }
    }


    /**
     * 执行ApkSigner
     */
    fun startApkSigner(view: View) {
        log("[ApkSigner]")
        performSign {
            //签名
            SignApk.main(
                    arrayOf(
                            filesDir.absolutePath + File.separator + keyApkSignerPublic,
                            filesDir.absolutePath + File.separator + keyApkSignerPrivate,
                            it,
                            FileUtils.getExistFile(outFile).absolutePath
                    )
            )
        }
    }

    /**
     * 执行JarSigner
     */
    fun startJarSigner(view: View) {
        log("[JarSigner]")
        performSign {
            //签名
            Main.main(
                    arrayOf(
                            "-verbose",
                            "-keystore", filesDir.absolutePath + File.separator + keyJarSigner,
                            "-storepass", "123456",
                            "-keyPass", "123456",
                            "-signedjar",
                            FileUtils.getExistFile(outFile).absolutePath,
                            it,
                            "test"
                    )
            )
        }
    }


    /**
     * 执行签名
     */
    private fun performSign(signAction: (String)-> Unit){
        return unsignedApkFile?.let {
            if (!it.exists()) {
                log("待签名apk不存在！")
            } else if (FileUtils.getFileExtension(it) != "apk" &&
                    FileUtils.getFileExtension(it) != "jar" &&
                    FileUtils.getFileExtension(it) != "zip") {
                log("待签名文件必须是(.apk|.jar|.zip)！")
            }
            log("开始签名...")
            Thread(Runnable {
                try {
                    //签名
                    signAction(it.absolutePath)
                } catch (e: java.lang.Exception) {
                    log("签名出错...(${e.message})")
                    e.printStackTrace()
                    return@Runnable
                }
                log("签名完成！($outFile)")
            }).start()
        } ?: log("请选择待签名apk文件！")
    }
}
