# Beta Gradle插件使用说明

[![](https://jitpack.io/v/joyrun/BetaUploader.svg)](https://jitpack.io/#joyrun/BetaUploader)


```
打包上传apk到七牛云
```

在根目录下的build.gralde文件的depandencies（buildscript部分）中添加：

```
buildscript {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.joyrun:BetaUploader:1.3.2'
    }
}
```

在module的build.gradle文件的顶部添加：
```
apply plugin: 'com.beta.plugin.betauploader'



beta {
    desc = '我是描述'
    title = "测试版"


    qiniuAccessKey = "<七牛云 AccessKey>"
    qiniuSecretKey = "<七牛云 SecretKey>"
    qiniuBucketName = "<七牛云 bucket名称>"
    qiniuUploadFileName = "android-" + System.currentTimeMillis()+".apk"
    qiniuBaseUrl = "<七牛云 bucket对于的外链地址>"

    debugOn = true

    webhook = "<上传成功后调用的接口>"
}




```
除了appId和appKey之外，还可以设置其他属性，属性列表如下：

| 属性 | 值  | 说明 |
| --- | --- | --- |
| title | String | 版本名称，默认以`<projectname>-<version name><version code> `命名|
| desc | String | 版本描述，默认为空 |
| enable | Boolean | 插件开关，默认为true|
| autoUpload | Boolean | 是否自动上传，默认为false |
| debugOn |Boolean | debug模式是否上传， 默认为false|

