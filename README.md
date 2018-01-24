# TakePhotoDemo
Android三方库--TakePhoto

参考:

https://www.jianshu.com/p/9528a0d29f29

TakePhoto Github地址:[https://github.com/crazycodeboy/TakePhoto](https://link.jianshu.com?t=https://github.com/crazycodeboy/TakePhoto)

# gradle 打多渠道包

**第一种：**选择菜单栏里的build，打开后选择Generate Signed Apk就会看到这样的一个界面： 

当然第一次进入这个界面,Key store path是空的，这个时候选择create new一个新的就可以了，进入这个界面:

![这里写图片描述](http://img.blog.csdn.net/20170315094719787?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvenh5dWRpYQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

在这里填写一下，最上面选个保存路径，不要乱填，后面打包时候会用到，以后直接拿来用就可以了，不至于每次都重新new一个。完了点击ok就可以了。完了返回上个界面，点击next，进入下面这个页面：

看最上面APK Destination Folder这个路径是打包生成apk的路径，一般都会选app\build\outputs\apk，第一次可能没有apk这个目录，在工程目录中的build下的目录生成一个apk文件夹就可以了。我们打包后的apk就在这里了。点击Finish，之后就完成打包了，可以在对应目录中看到这样：

这里是选择了打好几个包，你这里如果选release，则打出的是release.apk，如果刚刚build Type里选择的是debug，则打出的是debug.apk 这里就结束了。

第二种，用命令方式打包： 
打开Project Structrue，或者点击鼠标右键打开open moudle setting也能进入下面这个界面：

第一次进来，是没有release的，这个是我加的，显示的是config，选中，改为release或者debug，可以同时添加这两种，右侧，把对应刚刚签名填的对应信息输入，点击ok就可以了。完了后，我们再次打开，这次选中BuildTypes，在Signing Config这里选release，如果是debug就选debug好了。这时候app下的build.gradle里会显示这样的配置：

这里我选了一个release，如果你选择两种的话，debug也会相应的显示出来。结束配置，开始敲命令了： 

从下面这张图，通过BUILD SUCCESSFUL可以看出我们打包成功了，这里打了6个apk包，用时1分过点。

**最后我们看看多渠道打包(利用友盟实现多渠道打包)：**

**渠道的概念：** 
就是channel，例如百度，小米，360，安卓市场等等，每一个都是一个渠道，而每一个对应这个渠道的apk文件叫渠道包。

**经常听到某某某说要打几百个渠道包，为什么要提供如此多的渠道包？** 
应用在请求网络时携带渠道信息，方便后台统计，因此我们在安装包里添加不同的标识，去区别。

**友盟多渠道打包：**

其实我们刚刚实现的就是，只是要加入相应的配置，才能完成，看清单文件…. 
在Manifest.xml下加这样两个配置：

```
//友盟统计要用到的
<meta-data
            android:name="UMENG_APPKEY"
            android:value=" 58c79e572ae85b5073000edf" />

 //多渠道打包配置       
 <meta-data
            android:name="UMENG_CHANNEL"
            android:value="${UMENG_CHANNEL_VALUE}" />
```

下面那个value的这串“${UMENG_CHANNEL_VALUE}”一定要在build.gradle下指定一个值，不然Mainfest.xml这个就编译不过去，为了这点纠结了好一会，加入这样一行代码，在defaultConfig下：

```
 // 默认是umeng的渠道
        manifestPlaceholders = [UMENG_CHANNEL_VALUE: "umeng"]12
```

在build.gradle的根目录android下，加入这样的配置：

```
  // 友盟多渠道打包
    productFlavors {
        wandoujia {}
        baidu {}
        c360 {}
        uc {}
        xiaomi {}
        tencent {}
        taobao {}
        ........
    }

    productFlavors.all { flavor ->
        flavor.manifestPlaceholders = [UMENG_CHANNEL_VALUE: name]
    }
```

最后是整个build.gradle代码，看看就明白了：

```java
apply plugin: 'com.android.application'

android {
    signingConfigs {
        release {
            keyAlias 'takephoto'
            keyPassword '123456'
            storeFile file('../takephoto.jks')
            storePassword '123456'
        }
    }
    compileSdkVersion 26
    buildToolsVersion "26.0.2"
    defaultConfig {
        applicationId "sina.com.takephotodemo"
        minSdkVersion 17
        targetSdkVersion 26
        versionCode 1
        versionName "1.0.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
    }
    productFlavors {

        wandoujia {}
        baidu {}
        c360 {}
        uc {}
        xiaomi {}
        tencent {}
        taobao {}

        productFlavors.all { flavor ->
            flavor.manifestPlaceholders = [UMENG_CHANNEL_VALUE: name]
        }
    }
    //给apk添加对应的版本号：
    applicationVariants.all { variant ->
        variant.outputs.each { output ->
            def outputFile = output.outputFile
            if (outputFile != null && outputFile.name.endsWith('.apk')) {
                def fileName = outputFile.name.replace(".apk", "-${defaultConfig.versionName}.apk")
                output.outputFile = new File("C:\\Users\\Administrator\\Desktop\\apk\\takephoto\\${defaultConfig.versionName}", fileName)
            }
        }
    }
}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:26.0.0-alpha1'
    compile project(':takephoto_library')
}


```

配置好后，用上面两种常规打包方法，就会把productFlavors下的所有渠道包都打完。

**美团多渠道打包：** 
原理：把一个Android应用包当做zip文件包进行解压，然后发现签名生成的目录下(META_INF)添加一个空文件，不需要重新签名。利用这个机制，该文件名就是渠道名，这种方式不需要重新签名步骤，非常高效。

**优点：**打包速度非常快，几百个包一分钟就能打完。 
**缺点：**google如果变换了打包规则，当在META-INF时添加文件需要重新打包时，这种方式就不适合了。还有就是不安全，可以通过工具被修改渠道包。

具体用法参考：[美团打包原理及用法](http://blog.csdn.net/myliuyx/article/details/52171145)

**360多渠道打包：** 
原理：apk文件的本质是zip文件，利用zip文件特性，可以添加comment(摘要)”的数据结构特点，在文件的末尾写入任意的数据，而不用重新解压zip文件，就可以将渠道信息写入摘要区。

**优点：** 打包速度快，相对于美图打包来说，提高了修改渠道名的门槛，对应有加密方式，渠道名不容易被修改。 
**缺点：**仍然存在不安全风险，会被修改渠道包。