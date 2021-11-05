package work.diaossama.qqbot.plugin

import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import work.diaossama.qqbot.plugin.data.PluginData
import work.diaossama.qqbot.plugin.function.*
import work.diaossama.qqbot.plugin.util.DBUtil

// TODO: 执行命令 log
// TODO: 内建权限
// TODO: 控制台使用AnsiMessageBuilder增加log颜色
// TODO: 依赖检查

const val REPO_ADDRESS = ""

object PluginMain:KotlinPlugin(
    JvmPluginDescription(
        "work.diaossama.diaossama-plugin",
        "1.1.0",
        "DiaosSama-Plugin"
    )
) {
    // 权限注册
    val PERMISSION_BILILIVE by lazy {
        PermissionService.INSTANCE.register(permissionId("bililive"), "控制B站直播间监听权限")
    }

    override fun onEnable() {
        logger.info { "感谢使用 DiaosSama 闲来无事写的插件，插件仓库：$REPO_ADDRESS" }
        // PluginData.reload()
        // DBUtil.test()

        launch {
            // 复读姬功能
            Repeater()
            // 帮助功能
            Help()
            // 抽签功能
            Draw()
            // 直播监听功能
            BililiveHandler()
            // 涩图抽奖(x
            HsoHandler()
            PluginData.reload()
        }
    }
}
