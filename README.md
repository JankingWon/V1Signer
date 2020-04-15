## 说明

本项目包含两个`Module`

* `JarSigner`
* `ApkSigner`

使用的均是V1签名，可适用所有Android版本。

## 效果

<img src="README.assets/image-20200415221027516.png" width="369"></img>

## JarSigner

### 说明

* 本`Module`的关于签名的源码全部来源于 https://github.com/frohoff/jdk8u-jdk ，用于给`Android Apk`签
  1. 如果是使用`java`开发，只需要复制 `com.sun.jarsigner` 和 `sun.security.tools.jarsigner` 两个`package`到你的项目中即可
  2. 如果是使用`android`开发，则需要手动导入本项目中的所有源代码

### 使用

1. 直接使用打包好的`jarsigner.jar`包([链接](https://github.com/JankingWon/V1Signer/releases))，在命令行中输入

   ```bash
   //linux
   $jarsigner [-verbose] -keystore "~/Desktop/test.jks"  -storepass 123456  -keyPass 123456 -signedjar "~/Desktop/test.apk" "~/Desktop/unsigned.apk" test
   //windows
   $java -jar .\jarsigner.jar [-verbose] -keystore "C:\\test.jks"  -storepass 123456  -keyPass 123456 -signedjar "C:\\test.apk" "C:\\unsigned.apk" test
   ```

2. 使用源码，调用`sun.security.tools.jarsigner.Main#main(String[] args)`方法，参数同上

### 注意

如果是在`Android`设备上适用，要使用`BKS-V1`类型的秘钥，一般用`AndroidStudio`生成的`jks`秘钥是`JKS`类型，可以使用 [KeyStore Explorer](https://keystore-explorer.org/) 查看和转换签名类型

## ApkSigner

### 说明

* 本`Module`的关于签名的源码来源于`Android`源码和`jdk1.8.0_241`，使用`IntelliJ Idea`反编译获得，用于给`Android Apk`签名
  1. 如果是使用`java`开发，只需要复制 `SignApk.java`([链接](https://github.com/JankingWon/V1Signer/blob/master/apksigner/src/main/java/com/android/signapk/SignApk.java))文件到你的项目中即可
  2. 如果是使用`android`开发，则需要手动导入本项目中的所有源代码


### 使用

1. 直接使用打包好的`apksigner.jar`包([链接](https://github.com/JankingWon/V1Signer/releases))，在命令行中输入

   ```bash
   //linux
   $signapk [-w] publickey.x509[.pem] privatekey.pk8 input.jar output.jar
   //windows
   $java -jar apksigner.jar [-w] publickey.x509[.pem] privatekey.pk8 input.jar output.jar
   ```

2. 使用源码，调用`SignApk#Main(String[] args)`方法，参数同上


