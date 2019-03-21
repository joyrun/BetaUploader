package com.beta.plugin

import com.UpYun
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.text.SimpleDateFormat


public class BetaPlugin implements Plugin<Project> {
    private Project project = null;

    // URL for uploading apk file
    private static final String APK_UPLOAD_URL = "https://api.bugly.qq.com/beta/apiv1/exp?app_key=";
    private static
    final String APK_UPLOAD_URL2 = "https://api.bugly.qq.com/beta2/apiv1/exp?app_key=";


    @Override
    void apply(Project project) {
        this.project = project
        // 接收外部参数
        project.extensions.create("beta", BetaExtension)

        // 取得外部参数
        if (project.android.hasProperty("applicationVariants")) { // For android application.
            project.android.applicationVariants.all { variant ->
                String variantName = variant.name.capitalize()

                // Check for execution
                if (false == project.beta.enable) {
                    project.logger.error("JoyrunBetaPlugin: beta gradle enable is false, if you want to auto upload apk file, you should set the execute = true")
                    return
                }

                // Create task.
                Task betaTask = createUploadTask(variant)

                // Check autoUpload
                if (!project.beta.autoUpload) {
                    // dependsOn task
                    betaTask.dependsOn project.tasks["assemble${variantName}"]
                } else {
                    // autoUpload after assemble
                    project.tasks["assemble${variantName}"].doLast {
                        // if debug model and debugOn = false no execute upload
                        if (variantName.contains("Debug") && !project.beta.debugOn) {
                            println("JoyrunBetaPlugin: the option debugOn is closed, if you want to upload apk file on debug model, you can set debugOn = true to open it")
                            return
                        }

                        if (variantName.contains("Release")) {
                            println("JoyrunBetaPlugin: the option autoUpload is opened, it will auto upload the release to the bugly platform")
                        }
                        uploadApk(generateUploadInfo(variant))

                    }
                }
            }
        }
    }

    /**
     * generate upload info
     * @param variant
     * @return
     */
    public UploadInfo generateUploadInfo(Object variant) {
//        def manifestFile = variant.outputs.processManifest.manifestOutputFile[0]
//        println("-> Manifest: " + manifestFile)
//        println("VersionCode: " + variant.getVersionCode() + " VersionName: " + variant.getVersionName())

        UploadInfo uploadInfo = new UploadInfo()
        uploadInfo.project = project
        uploadInfo.versionCode = variant.getVersionCode()
        uploadInfo.versionName = variant.getVersionName()
        if (project.beta.title == null) {
            uploadInfo.title = project.getName() + "-" + variant.getVersionName() + variant.getVersionCode()
        } else {
            uploadInfo.title = project.beta.title
        }
        if (project.beta.desc == null) {
            uploadInfo.description = ""
        } else {
            uploadInfo.description = project.beta.desc
        }
        if (project.beta.extra == null) {
            uploadInfo.extra = ""
        } else {
            uploadInfo.extra = project.beta.extra
        }


        if (project.beta.apkFile != null) {
            uploadInfo.sourceFile = project.beta.apkFile
            println("JoyrunBetaPlugin: you has set the custom apkFile")
            println("JoyrunBetaPlugin: your apk absolutepath :" + project.beta.apkFile)
        } else {
            File apkFile = variant.outputs[0].outputFile
            uploadInfo.sourceFile = apkFile.getAbsolutePath()
            println("JoyrunBetaPlugin: the apkFile is default set to build file")
            println("JoyrunBetaPlugin: your apk absolutepath :" + apkFile.getAbsolutePath())
        }

        return uploadInfo
    }

    /**
     * 创建上传任务
     *
     * @param variant 编译参数
     * @return
     */
    private Task createUploadTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task uploadTask = project.tasks.create("upload${variantName}BetaApkFile") {
            doLast {
                // if debug model and debugOn = false no execute upload
                if (variantName.contains("Debug") && !project.beta.debugOn) {
                    println("JoyrunBetaPlugin: the option debugOn is closed, if you want to upload apk file on debug model, you can set debugOn = true to open it")
                    return
                }
                uploadApk(generateUploadInfo(variant))
            }
        }
        println("JoyrunBetaPlugin:create upload${variantName}BetaApkFile task")
        return uploadTask
    }

    /**
     *  上传apk
     * @param uploadInfo
     * @return
     */
    public boolean uploadApk(UploadInfo uploadInfo) {

        try {
            if (!post(uploadInfo.sourceFile, uploadInfo)) {
                project.logger.error("JoyrunBetaPlugin: Failed to upload!")
                return false
            } else {
                println("JoyrunBetaPlugin: upload apk success !!!")
                return true
            }
        } catch (Exception e) {
            e.printStackTrace()
            throw new RuntimeException(e)
        }
    }

    /**
     * 上传apk
     * @param filePath 文件路径
     * @param uploadInfo 更新信息
     * @return
     */
    public static boolean post(String filePath, UploadInfo uploadInfo) {
        // 上传到七牛云
//        String accessKey = uploadInfo.project.beta.qiniuAccessKey
//        String secretKey = uploadInfo.project.beta.qiniuSecretKey
//        String bucketName = uploadInfo.project.beta.qiniuBucketName
//        String uploadFileName = uploadInfo.project.beta.qiniuUploadFileName

        UpYun upyun = new UpYun(uploadInfo.project.beta.upyunBucketName, uploadInfo.project.beta.upyunUserName, uploadInfo.project.beta.upyunPassword);
        upyun.setTimeout(60);
        upyun.setApiDomain(UpYun.ED_AUTO);
        String versionName = URLEncoder.encode(uploadInfo.versionName, "utf-8")

        String path = uploadInfo.project.beta.upyunPath + "/" + uploadInfo.project.beta.uploadName + "_" + versionName + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".apk";
        boolean result = upyun.writeFile(path, new File(filePath), true);
        uploadInfo.downloadUrl = uploadInfo.project.beta.upyunBaseUrl + "/" + path;
//        boolean result = upyun.mkDir(uploadInfo.project.beta.upyunPath, true);

//        UploadManager uploadManager = new UploadManager();
//        Auth auth = Auth.create(accessKey, secretKey);
//        String token = auth.uploadToken(bucketName);
//        Response response = uploadManager.put(filePath, uploadFileName, token);
//        uploadInfo.downloadUrl = uploadInfo.project.beta.qiniuBaseUrl + "/" + uploadFileName
        println "uploadFileName: " + path
//        println "upload result: " + response.bodyString()
        println "apk download url: " + uploadInfo.downloadUrl

        if (result) {
            // 调用webhook，告知已经更新版本
            // versionName、versionCode、title、description、extra、

            String title = URLEncoder.encode(uploadInfo.title, "utf-8")
            String description = URLEncoder.encode(uploadInfo.description, "utf-8")
//            String versionName = URLEncoder.encode(uploadInfo.versionName,"utf-8")
            String versionCode = URLEncoder.encode(uploadInfo.versionCode, "utf-8")
            String extra = URLEncoder.encode(uploadInfo.extra, "utf-8")
            String downloadUrlUTF8 = URLEncoder.encode(uploadInfo.downloadUrl, "utf-8")

            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            String url = String.format("%s?title=%s&description=%s&versionName=%s&versionCode=%s&extra=%s&downloadUrl=%s&platform=1", uploadInfo.project.beta.webhook, title, description, versionName, versionCode, extra, downloadUrlUTF8)
            Request.Builder builder = new Request.Builder().url(url).get();
            println "webhook info: " + uploadInfo.toString()
            println "webhook start: " + url
            okhttp3.Response response2 = okHttpClient.newCall(builder.build()).execute();
            println "webhook end: statusCode = " + response2.code()
            println "webhook end: " + response2.body().string()
            return response2.code() == 200;
        } else {
            println "apk upload error"
            return false
        }
    }


    public static class UploadInfo {
        public Project project;
        public String sourceFile = null
        public String title = null
        public String description = null
        public String downloadUrl = null

        public String versionName = null
        public String versionCode = null
        public String extra = null

        @Override
        public String toString() {
            return "UploadInfo{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode='" + versionCode + '\'' +
                    ", extra='" + extra + '\'' +
                    '}';
        }
    }


}
