package work.diaossama.qqbot.plugin.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("PluginData") {
    // 存放开启复读的群组列表
    var repeatGroupList: MutableList<Long> by value()
    // 直播监听开关
    var bililiveEnableList: MutableList<Tuple> by value()
}

// 标识常量
const val GROUP = 0
const val FRIEND = 1

data class BililiveEvent(
    // 群号或QQ号码
    val Id: Long,
    // 标识群组或好友
    val Idtype: Int,
    // 通知内容
    val Message: String
)

@Serializable
data class Tuple(val Idtype: Int, val Id: Long) {
    override fun equals(other: Any?): Boolean {
        return if (other is Tuple) {
            ((other.Idtype == Idtype) && (other.Id == Id))
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = Idtype
        result = 31 * result + Id.hashCode()
        return result
    }
}

data class BiliRoomInfo(
    // 直播间房间号
    var room_id: Long,
    // 主播uid
    var uid: Long,
    // 房间标题
    var title: String,
    // 主播昵称
    var name: String,
    // 房间状态 0为下播，1为开播，2为轮播
    var status: String
)