# DiaosSama-Plugin

## 介绍

本项目是原 [Simple_QQBot](https://github.com/DiaosSama/DiaosSama_QQBOT) 项目的 mirai 插件化版本（mirai-plugin）

（其实年初就写好了，但是一直没有上传，今天整理的文件的时候想起来了，故上传

## 部署

1. 编译
（这一步可跳过，可以直接使用 release 中打包好的 jar 文件）
使用 IDEA 打开本项目，并初始化
在 IDEA 中打开 `build.gradle` 文件，并找到 ShadowJar

```groovy
shadowJar {
    // 生成包的命名规则： baseName-version-classifier.jar
    manifest {
        attributes(
                'Main-Class': 'work.diaossama.qqbot.plugin.PluginMain'//入口点
        )
    }

    // 将 build.gradle 打入到 jar 中, 方便查看依赖包版本
    from("./"){
        include 'build.gradle'
    }
}
```

点击 ShadowJar 左侧的 run 按钮（绿色小三角），build 完成之后在 `build/libs` 中找到编译好的 jar 文件

2. 将插件放入 mirai-terminal
将编译好的 jar 文件复制到 mirai-console 中的 `plugins` 文件夹

3. 准备 sqlite 文件
使用 `config` 目录下的 `database.sql` 生成 sqlite 文件，并将生成的 sqlite 文件复制到 mirai-console 的 `data/DiaosSama-Plugin/` 文件夹中（没有该目录则新建一个）

4. 配置 PluginData.yml
编辑 `config` 目录下的 `PluginData.yml`

```yaml
repeatGroupList: []
bililiveEnableList: 
  - Idtype: 1
    Id: 1234567890  # 这一行填入允许发送B站监听指令的QQ号码
  - Idtype: 1
    Id: 9876543210  # 想要多个QQ号码同时发送指令则多添加一相
```

然后将 `PluginData.yml` 文件复制到 `data/DiaosSama-Plugin` 目录下即可

## 功能

- [x] Base64 编码/解码
- [x] 无聊的抽签（使用诸葛神签的签位）
- [x] B站直播间监听（可动态添加/删除房间号）
- [x] 涩图抽奖（不是
- [ ] 图片上传至图床并返回链接
- [ ] 远程主机执行特定命令

## 使用

1. Base64 编码/解码

   目前Base64编解码在群聊中执行（有空再改回个人聊天窗口）

   编码命令以`/b64e`开头，如

   ```
   /b64e 测试base64编码
   ```

   解码命令以`/b64d` 开头

2. 无聊的抽签

   该功能在群聊中执行，在任意群聊输入“抽签”即可进行抽签，一天可抽一次，每天0点刷新。抽签后输入“解签”即可解签

3. B站直播间监听

   该功能目前只面向单一个体（一个群聊或一个好友，在`setting.yml`中配置），只有管理员（在`setting.yml`中配置）私聊可以添加或删除需要监听的房间

   - `/bililivestart`：启动B站直播间监听
   - `/bililivestop`：关闭B站直播间监听
   - `/bililiveadd`：增加监听的B站直播间号码
   - `/bililiverm`：删除监听的B站直播间号码
   
4. 涩图抽奖（不是

   该功能使用了V2EX老哥 [随机二次元图片接口](https://www.v2ex.com/t/727134) 的一个随机二次元图片接口，先谢谢这位老哥（有空再自己开发一个，一定不鸽
   
   本质是借用了 i.pixiv.cat 做的反向代理
   
   相关命令如下：
   
   - `PC涩图`：抽取宽屏分辨率的涩图
   - `手机涩图`：抽取竖屏分辨率的涩图
   
   机器人会回复图片以及对应的镜像链接，图片源文件会放置在机器人所在目录的`./Picture`文件夹下（记得定时清理一下