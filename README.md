# BlackShadow
接入shadow需要大量的二次开发工作，其实一般小型项目其实并不想关心太多的逻辑和管理，只想开袋即食，奈何Shadow也并没有提供这方面的能力，所有开发者接入都需要二次开发才可以使用，所以花了点时间在Shadow的基础上包装了一层，几乎不需要任何二次开发，即可通过几个简单的接口使用与管理Shadow，屏蔽了Shadow所有的技术细节。

## 相关
博客文章：
[腾讯Shadow浅析及应用及BlackShadow](https://blog.niunaijun.top/index.php/archives/blackshadow.html)

Tencent Shadow：
https://github.com/Tencent/Shadow

## 基于Shadow的技术方案
BlackShadow使用的是非动态方案
- 支持同时最多10个插件运行，分别都是各自单独的进程。
- install与launch都有boolean返回值，可反馈出插件是否安装/启动成功。
- 支持插件内打开/查询/卸载/安装，其他插件。

## 未实现
- Activity栈的管理，目前统一打开standard Activity
- 多个插件共用一个进程

## 如何使用？
建议直接clone本项目查看项目结构。

### 1. clone Shadow
nnjun仓库与Tencent仓库没有技术性差异。
```
git clone https://github.com/Tencent/Shadow.git
或者
git clone https://github.com/nnjun/Shadow.git (建议使用这个)
```

### 2. 编译本地仓库
拉下仓库后，进入Shadow目录，将Shadow发布到本地maven仓库
```
./gradlew publish
```

### 3. 修改项目Shadow版本
修改Shadow版本为本地的版本，如果是拉取nnjun仓库则不需要改

https://github.com/CodingGay/BlackShadow/blob/main/build.gradle#L3


## BlackShadow使用方法
在Application#attachBaseContext中初始化
```java
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BlackShadow.get().init(this);
    }
```

安装与启动
```java
    InstallResult installResult = BlackShadow.get().installPlugin("plugin-key", new File(pluginAPk));
    if (installResult.isSuccess()) {
        Intent intent = new Intent();
        intent.xxxxxxxxxxxxx
        BlackShadow.get().launchPlugin("plugin-key", intent);
    }
```

其余接口
```java
    // 仅启动application
    public boolean callApplication(String pluginKey)

    // 获取所有已安装的plugin
    public List<InstalledPlugin> getInstalledPlugins()

    // 获取某个已安装的plugin
    public InstalledPlugin getInstalledPlugin(String pluginKey)；

    // 卸载某个plugin
    public void uninstallPlugin(String pluginKey)

    // 停止某个plugin
    public void stopPlugin(String pluginKey)

    // 停止所有plugin
    public void stopAllPlugin()

    // 获取正在运行的plugin
    public List<RunningPlugin> getRunningPlugins()
```

## 插件包名与宿主包名不相同的需求
由于Shadow内核要求，plugin与宿主的包名必须一致，否则会出现问题，然而我方产品可能会存在不同的渠道包不同的包名，但是插件没有必要分开很多份，所以BlackShadow是支持插件与宿主不同的包名，处理的方法是在install时如果不一样，BlackShaodw会自动将插件的包名改成与宿主相同，不需要额外开发，直接进行install即可，BlackShadow会自动处理该问题。

假如你也有这个需求，则需要自行修改Shaodw内核，或者直接使用nnjun仓库

https://github.com/nnjun/Shadow/commit/32636d2759bae1d1f241c8f43ffb769ff2ce5ef5

### 不是修改了包名了吗？为什么还需要修改内核？
因为Shadow的包名基准是由Shadow编译时生成的com.tencent.shadow.core.manifest_parser.PluginManifest文件来确定，BlackShadow只会修改Manifest中的包名，并不会修改PluginManifest.class内的硬编码包名，所以需要修改编译插件，否则无法运行。

如果你没有以上的场景，那么请无视上面这一段内容，直接使用即可。
