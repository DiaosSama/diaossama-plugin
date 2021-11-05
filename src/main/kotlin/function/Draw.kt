package work.diaossama.qqbot.plugin.function

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import work.diaossama.qqbot.plugin.PluginMain
import work.diaossama.qqbot.plugin.util.DrawUtil

suspend fun PluginMain.Draw() {
    subscribeGroupMessages {
        case("抽签") {
            val drawUtil = DrawUtil(sender.id)
            reply(At(sender as Member) + drawUtil.draw())
        }

        case("解签") {
            val drawUtil = DrawUtil(sender.id)
            reply(At(sender as Member) + drawUtil.dealDraw())
        }
    }
}