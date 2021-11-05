package work.diaossama.qqbot.plugin.function

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import work.diaossama.qqbot.plugin.PluginMain
import work.diaossama.qqbot.plugin.data.BililiveEvent
import work.diaossama.qqbot.plugin.data.FRIEND
import work.diaossama.qqbot.plugin.data.GROUP
import work.diaossama.qqbot.plugin.util.BililiveListener
import kotlin.coroutines.CoroutineContext

suspend fun PluginMain.BililiveHandler() {
    val listener = BililiveListener(this.coroutineContext)


    subscribeGroupMessages {
        // 启动b站直播监听
        // TODO: 添加命令权限检查
        startsWith("/bililivestart") {
            logger.info { "接收到/bililivestart" }
            listener.startBiliLiveListener(group.id, GROUP)
        }
        // 停止b站直播监听
        startsWith("/bililivestop") {
            logger.info { "接收到/bililivestop" }
            listener.stopBiliLiveListener(group.id, GROUP)
        }
        // 增加b站监听房间号
        startsWith("/bililiveadd") {
            logger.info { "接收到/bililiveadd" }
            var reMess = message.toString()
            reMess = reMess.substring(reMess.indexOfFirst { it == ' ' }+1)
            var room_id:Long = java.lang.Long.valueOf(reMess)
            listener.addRoom(group.id, GROUP, room_id)
        }
        // 删除b站监听房间号
        startsWith("/bililiverm") {
            logger.info { "接收到/bililiverm" }
            var reMess = message.toString()
            reMess = reMess.substring(reMess.indexOfFirst { it == ' ' } + 1)
            var room_id: Long = java.lang.Long.valueOf(reMess)
            listener.removeRoom(group.id, GROUP, room_id)
        }
    }

    subscribeFriendMessages {
        // 启动b站直播监听
        startsWith("/bililivestart") {
            listener.startBiliLiveListener(sender.id, FRIEND)
        }
        // 停止b站直播监听
        startsWith("/bililivestop") {
            listener.stopBiliLiveListener(sender.id, FRIEND)
        }
        // 增加b站监听房间号
        startsWith("/bililiveadd") {
            var reMess = message.toString()
            reMess = reMess.substring(reMess.indexOfFirst { it == ' ' }+1)
            var room_id:Long = java.lang.Long.valueOf(reMess)
            listener.addRoom(sender.id, FRIEND, room_id)
        }
        // 删除b站监听房间号
        startsWith("/bililiverm") {
            var reMess = message.toString()
            reMess = reMess.substring(reMess.indexOfFirst { it == ' ' } + 1)
            var room_id: Long = java.lang.Long.valueOf(reMess)
            listener.removeRoom(sender.id, FRIEND, room_id)
        }
    }

    // 监听
    launch {
        for (event in listener.liveChannel) {
            bililiveNotify(event, this.coroutineContext)
        }
    }

    launch {
        listener.start()
    }
}

// 直播通知发送函数
// TODO: 多BOT实例分隔
suspend fun bililiveNotify(event: BililiveEvent, context: CoroutineContext) {
    Bot.forEachInstance {
        CoroutineScope(context).launch {
            if (event.Idtype.equals(FRIEND)) {
                println(event)
                it.getFriend(event.Id).sendMessage(event.Message)
            }
            else if (event.Idtype.equals(GROUP)) {
                println(event)
                it.getGroup(event.Id).sendMessage(event.Message)
            }
        }
    }
}