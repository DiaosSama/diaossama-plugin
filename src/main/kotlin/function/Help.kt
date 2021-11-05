package work.diaossama.qqbot.plugin.function

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import work.diaossama.qqbot.plugin.PluginMain

/*
 帮助功能
 */

suspend fun PluginMain.Help() {
    // 群聊帮助
    subscribeGroupMessages {
        atBot {
            reply (
                At(sender as Member) + "命令指南\n" +
                    "1. /bililivestart: 启动b站直播间监听\n" +
                    "2. /bililivestop: 关闭b站直播间监听\n" +
                    "3. /bililiveadd <房间号>: 增加b站直播间监听\n" +
                    "4. /bililiverm <房间号>: 删除b站直播间监听\n" +
                    "5. 抽签: 就是抽签\n" +
                    "6. 解签: 就是解签\n" +
                    "7. 来张涩图: 懂的都懂\n"
            )
        }
    }

    // 好友私聊帮助
    subscribeFriendMessages {
        case("帮助") {
            reply("命令指南\n" +
                    "1. /bililive add <直播间ID>\n" +
                    "2. /bililive rm <直播间ID>\n")
        }
    }
}