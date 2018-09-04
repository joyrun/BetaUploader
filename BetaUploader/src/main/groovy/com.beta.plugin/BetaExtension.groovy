package com.beta.plugin

/**
 * Beta extension params
 * @author wenjiewu
 */
public class BetaExtension {

    public String title = null // 标题
    public String desc = null // 版本描述
    public int secret = 1 // 公开范围（1：所有人，2：密码，4管理员，5QQ群，6白名单）

    // 【插件配置】
    public String apkFile = null // 指定上传的apk文件
    public Boolean enable = true // 插件开关
    public Boolean autoUpload = false // 是否自动上传
    public Boolean debugOn = false // debug模式是否上传



    public String qiniuAccessKey = null
    public String qiniuSecretKey = null
    public String qiniuBucketName = null
    public String qiniuUploadFileName = null
    public String qiniuBaseUrl = null

    public String upyunPath;
    public String upyunBucketName;
    public String upyunUserName;
    public String upyunPassword;
    public String upyunBaseUrl = null
    public String uploadName = null

    public String webhook = null
    public String extra = null
}