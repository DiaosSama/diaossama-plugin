package work.diaossama.qqbot.plugin.function

import net.mamoe.mirai.event.subscribeGroupMessages
import work.diaossama.qqbot.plugin.PluginMain
import work.diaossama.qqbot.plugin.data.PluginData

/*
 复读姬功能
 */

suspend fun PluginMain.Repeater() {
    subscribeGroupMessages {
        case("复读姬模式") {
            if(group.id in PluginData.repeatGroupList) {
                reply("复读姬在复读了，别点")
            }
            else {
                PluginData.repeatGroupList.add(group.id)
                reply("群 ${group.name} 复读姬启动")
            }
            //MyData.reload()
        }

        case("关闭复读姬") {
            if(group.id in PluginData.repeatGroupList) {
                PluginData.repeatGroupList.remove(group.id)
                reply("复读姬倒了（哭腔）")
            }
            else {
                reply("复读姬对你翻了个白眼并表示她并没有启动过")
            }

        }

        // 复读处理
        always {
            if (group.id in PluginData.repeatGroupList) {
                reply(message)
            }
        }
    }
}