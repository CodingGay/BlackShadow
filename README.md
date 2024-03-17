# BlackShadow
接入shadow需要大量的二次开发工作，其实一般小型项目其实并不想关心太多的逻辑和管理，只想开袋即食，奈何Shadow也并没有提供这方面的能力，所有开发者接入都需要二次开发才可以使用，包括本次我自己使用也是，所以花了点时间在Shadow的基础上包装了一层，几乎不需要任何二次开发，即可通过几个简单的接口使用与管理Shadow，屏蔽了Shadow所有的技术细节。

## 依赖安装
```
git clone https://github.com/Tencent/Shadow.git
或者
git clone https://github.com/nnjun/Shadow.git (建议使用这个)
```

拉下仓库后，进入仓库目录，将Shadow发布到本地maven仓库。
```
./gradlew publish
```


## 使用方法
在Application#attachBaseContext中初始化
```
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BlackShadow.get().init(this);
    }
```

安装与启动
```
InstallResult installResult = BlackShadow.get().installPlugin("plugin-key", new File(pluginAPk));
if (installResult.isSuccess()) {
    Intent intent = new Intent();
    intent.xxxxxxxxxxxxx
    BlackShadow.get().launchPlugin("plugin-key", intent);
}
```

其余接口
```
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

## 基于Shadow的技术方案
BlackShadow使用的是非动态方案，支持同时最多10个插件运行，分别都是各自单独的进程。install与launch都有boolean返回值，可反馈出插件是否安装/启动成功。

## 插件包名与宿主包名不相同的需求
由于Shadow内核要求，plugin与宿主的包名必须一致，否则会出现问题，然而我方产品可能会存在不同的渠道包不同的包名，但是插件没有必要分开很多份，所以BlackShadow是支持插件与宿主不同的包名，处理的方法是在install时如果不一样，BlackShaodw会自动将插件的包名改成与宿主相同，不需要额外开发，直接进行install即可，BlackShadow会自动处理该问题。

假如你也有这个需求，则需要自行修改Shaodw内核，将这个检测去除，或者安装我这个内核。其余内容都是与官方保持一致。

https://github.com/nnjun/Shadow/commit/4f769afdd4e86814fa09d1ef9b19d6ea68f175fd

### 不是修改了包名了吗？为什么还需要去除检测？
因为Shadow的包名基准是由Shadow编译时生成的com.tencent.shadow.core.manifest_parser.PluginManifest文件来确定，BlackShadow只会修改Manifest中的包名，并不会修改PluginManifest.class内的硬编码包名，所以需要去除检测，否则无法运行。

如果你没有以上的场景，那么请无视上面这一段内容，直接使用即可。