package work.diaossama.qqbot.plugin.util

// TODO: 直播间通知增加直播间链接
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import work.diaossama.qqbot.plugin.data.BiliRoomInfo
import work.diaossama.qqbot.plugin.data.BililiveEvent
import work.diaossama.qqbot.plugin.data.PluginData
import work.diaossama.qqbot.plugin.data.Tuple
import java.util.logging.LogManager
import kotlin.coroutines.CoroutineContext

// 轮询间隔，ms
const val LIVE_FETCH_DELAY_MILLS = 60000L
// 直播间状态 0:下播 1:开播 2:轮播
const val OFFLINE = "0"
const val ONLINE = "1"
const val CAROUSEL = "2"

class BililiveListener(val context: CoroutineContext) {
    // 消息传递管道
    // https://www.kotlincn.net/docs/reference/coroutines/channels.html
    var liveChannel = Channel<BililiveEvent>()
    // 创建日志记录器
    @ConsoleExperimentalApi
    val logger = MiraiConsole.createLogger("BiliLiveListener")
    // 监听列表
    private var liveNotifyList = initRoom()

    // 启动直播监听
    suspend fun start() {
        while (true) {
            PluginData.bililiveEnableList.forEach {
                CoroutineScope(context).launch {
                    getLiveStatus(it.Id, it.Idtype)
                }
            }
            delay(LIVE_FETCH_DELAY_MILLS)
        }
    }

    // 监听函数主逻辑
    private suspend fun getLiveStatus(Id: Long, Idtype: Int) {
        var rooms = liveNotifyList[Tuple(Idtype, Id)]
        var new_room: BiliLiveUtil
        if (rooms != null) {
            for (room in rooms) {
                new_room = BiliLiveUtil(room.key.toString())
                // 直播状态改变
                if (!new_room.online.equals(room.value.status)) {
                    // 开播
                    if (new_room.online.equals(ONLINE)) {
                        val message = "您关注的房间已开播!\n" +
                                "房间号: " + room.key + "\n" +
                                "标题: " + new_room.title + "\n" +
                                "主播: " + new_room.name
                        // 发送通知信息至通知管道
                        liveChannel.send(BililiveEvent(Id, Idtype, message))
                        room.value.title = new_room.title
                        room.value.name = new_room.name
                        room.value.status = ONLINE
                    }
                    // 下播
                    else if (new_room.online.equals(OFFLINE)) {
                        val message = "您关注的房间已下播!\n" +
                                "房间号: " + room.key + "\n" +
                                "标题: " + new_room.title + "\n" +
                                "主播: " + new_room.name
                        // 发送通知信息至通知管道
                        liveChannel.send(BililiveEvent(Id, Idtype, message))
                        room.value.title = new_room.title
                        room.value.name = new_room.name
                        room.value.status = OFFLINE
                    }
                    // 轮播
                    else if (new_room.online.equals(CAROUSEL)) {
                        val message = "您关注的房间开启轮播!\n" +
                                "房间号: " + room.key + "\n" +
                                "标题: " + new_room.title + "\n" +
                                "主播: " + new_room.name
                        // 发送通知信息至通知管道
                        liveChannel.send(BililiveEvent(Id, Idtype, message))
                        room.value.title = new_room.title
                        room.value.name = new_room.name
                        room.value.status = CAROUSEL
                    }
                    // 更新数据库信息
                    var db = DBUtil()
                    db.update(
                        "update bili_live set title=" + room.value.title +
                                ",name=" + room.value.name +
                                " where room_id=" + room.key
                    )
                    db.close()
                }
                // 直播标题发生改变
                if (!new_room.title.equals(room.value.title)) {
                    /*
                    logger.error(room.value.title[0].toString())
                    logger.error(room.value.title[0].toInt().toString())
                    logger.error("直播原标题: " + room.value.title)
                    logger.error("直播新标题: " + new_room.title)
                    logger.error(new_room.title.equals(room.value.title.trim()).toString())
                     */
                    val message = "直播间标题变更!\n" +
                            "房间号: " + room.key + "\n" +
                            "标题: " + new_room.title + "\n" +
                            "主播: " + new_room.name
                    // 发送通知信息至通知管道
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                    room.value.title = new_room.title
                    room.value.name = new_room.name
                    println("update bili_live set title=\"" + room.value.title +
                            "\"" + ",name=\"" + room.value.name + "\"" +
                            " where room_id=" + room.key)
                    // 更新数据库信息
                    var db = DBUtil()
                    // sqlite 需要转义双引号，用单引号包括即可
                    db.update(
                        "update bili_live set title='" + room.value.title +
                                "'" + ",name='" + room.value.name + "'" +
                                " where room_id=" + room.key
                    )
                    db.close()
                }
            }
        }

    }

    /*
    从数据库初始化需要监听的直播间信息
    */
    private fun initRoom(): HashMap<Tuple, HashMap<Long, BiliRoomInfo>> {
        val db = DBUtil()
        val userResult = db.query("select * from bili_live_user;")
        val outter_hashmap = HashMap<Tuple, HashMap<Long, BiliRoomInfo>>()

        while (userResult.next()) {
            // QQ号码或群号
            val Id = userResult.getLong("user_num")
            // 用户类型
            val Idtype = userResult.getInt("user_type")
            // 订阅的房间号
            val room_id = userResult.getLong("room_id")
            val tuple = Tuple(Idtype, Id)
            var roomList: HashMap<Long, BiliRoomInfo>

            // 如果列表中已存在该用户
            if (tuple in outter_hashmap.keys) {
                roomList = outter_hashmap[tuple]!!
                val roomResult = db.query("select * from bili_live where room_id=$room_id")
                while (roomResult.next()) {
                    val title = roomResult.getString("title")
                    val uid = roomResult.getLong("uid")
                    val name = roomResult.getString("name")
                    roomList[room_id] = BiliRoomInfo(room_id, uid, title, name, OFFLINE)
                }
            } else {
                roomList = HashMap()
                val roomResult = db.query("select * from bili_live where room_id=$room_id")
                while (roomResult.next()) {
                    val title = roomResult.getString("title")
                    val uid = roomResult.getLong("uid")
                    val name = roomResult.getString("name")
                    roomList[room_id] = BiliRoomInfo(room_id, uid, title, name, OFFLINE)
                    outter_hashmap[tuple] = roomList
                }
            }
        }
        db.close()
        logger.info { outter_hashmap.toString() }
        return outter_hashmap
    }

    /*
    增加需要监听的直播间
     */
    @ConsoleExperimentalApi
    suspend fun addRoom(Id: Long, Idtype: Int, room_id: Long) {
        val info = BiliLiveUtil(room_id.toString())
        val tuple = Tuple(Idtype, Id)
        var roomList: HashMap<Long, BiliRoomInfo>
        // 判断直播间信息获取情况
        if (info.isStatus) {
            // 判断该用户是否为新用户
            if (tuple in liveNotifyList.keys) {
                roomList = liveNotifyList[tuple]!!
                println(roomList)
                // 判断直播间是否重复添加
                if (room_id in roomList.keys) {
                    val message = "直播间已在监听列表中\n" +
                            "房间号: " + room_id + "\n" +
                            "标题: " + info.title + "\n" +
                            "主播: " + info.name
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                }
                else {
                    roomList[room_id] = BiliRoomInfo(room_id, java.lang.Long.valueOf(info.uid), info.title, info.name, info.online)
                    // 写入数据库
                    // TODO: 判断bili_live表中有无其他用户添加的相同直播间信息
                    val db = DBUtil()
                    val res = db.query("select * from bili_live where room_id=$room_id")
                    val res1: Boolean
                    // 如果bili_live表中有其他用户添加的相同直播间信息
                    if (res.next()) {
                        logger.info { "bili_live表中有其他用户添加的相同直播间信息room_id=$room_id" }
                        res1 = true
                    }
                    else {
                        logger.info { "bili_live表中不存在其他用户添加的相同直播间信息room_id=$room_id" }
                        res1 = db.insert("insert into bili_live (room_id, title, uid, name) values (" +
                                room_id + ", " +
                                info.title + "," +
                                java.lang.Long.valueOf(info.uid) + "," +
                                info.name + ")"
                        )
                    }
                    val res2 = db.insert("insert into bili_live_user (user_num, user_type, room_id) values (" +
                            Id + "," +
                            Idtype + "," +
                            room_id + ")"
                    )
                    db.close()
                    // 发送反馈消息
                    if (res1 && res2) {
                        val message = "直播间监听添加成功\n" +
                                "房间号: " + room_id + "\n" +
                                "标题: " + info.title + "\n" +
                                "主播: " + info.name
                        liveChannel.send(BililiveEvent(Id, Idtype, message))
                    }
                    else {
                        val message = "直播间监听添加失败 ProblemID: 1"
                        liveChannel.send(BililiveEvent(Id, Idtype, message))
                    }
                }
            } else {
                roomList = HashMap<Long, BiliRoomInfo>()
                roomList[room_id] = BiliRoomInfo(room_id, java.lang.Long.valueOf(info.uid), info.title, info.name, info.online)
                liveNotifyList[tuple] = roomList
                // 写入数据库
                val db = DBUtil()
                var res1 = db.insert("insert into bili_live (room_id, title, uid, name) values (" +
                        room_id + ", " +
                        info.title + "," +
                        java.lang.Long.valueOf(info.uid) + "," +
                        info.name + ")"
                )
                var res2 = db.insert("insert into bili_live_user (user_num, user_type, room_id) values (" +
                        Id + "," +
                        Idtype + "," +
                        room_id + ")"
                )
                db.close()
                // 发送反馈消息
                if (res1 && res2) {
                    val message = "直播间监听添加成功\n" +
                            "房间号: " + room_id + "\n" +
                            "标题: " + info.title + "\n" +
                            "主播: " + info.name
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                }
                else {
                    val message = "直播间监听添加失败 ProblemID: 1"
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                }
            }
        }
    }

    /*
    删除监听列表中的直播间
     */
    suspend fun removeRoom(Id: Long, Idtype: Int, room_id: Long) {
        val tuple = Tuple(Idtype, Id)
        // 判断监听列表中是否有该用户
        if (tuple in liveNotifyList.keys) {
            var roomList = liveNotifyList[tuple]!!
            // 判断监听列表中是否存在该房间
            if(roomList.containsKey(room_id)) {
                val rm_room = roomList.remove(room_id)
                val db = DBUtil()
                // TODO: bili_live 表冗余问题，如何判断bili_live表项的删除条件
                val res = db.delete("delete from bili_live_user where user_num=$Id " +
                        "and user_type=$Idtype " +
                        "and room_id=$room_id"
                )
                // 判断bili_live_user中是否有其他用户监听该直播间
                // 如果没有则可以删除bili_live表中的房间信息
                if (!db.query("select * from bili_live_user where room_id=$room_id").next()) {
                    db.delete("delete from bili_live where room_id=$room_id")
                }
                db.close()
                // 判断删除是否成功
                if (res) {
                    val message = "直播间监听删除成功\n" +
                            "房间号: " + room_id + "\n" +
                            "标题: " + (rm_room?.title ?: "null") + "\n" +
                            "主播: " + (rm_room?.name ?: "null")
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                }
                else {
                    val message = "直播间监听删除失败"
                    liveChannel.send(BililiveEvent(Id, Idtype, message))
                }
            }
            else {
                val message = "监听列表中不存在该房间"
                liveChannel.send(BililiveEvent(Id, Idtype, message))
            }
        }
        else {
            val message = "您还未添加过任何直播间监听"
            liveChannel.send(BililiveEvent(Id, Idtype, message))
        }
    }

    suspend fun startBiliLiveListener(Id: Long, Idtype: Int) {
        val user = Tuple(Idtype, Id)
        val message: String
        message = if (user in PluginData.bililiveEnableList) {
            "B站直播间监听已启动"
        } else {
            PluginData.bililiveEnableList.add(user)
            "启动B站直播间监听"
        }
        liveChannel.send(BililiveEvent(Id, Idtype, message))
    }

    suspend fun stopBiliLiveListener(Id: Long, Idtype: Int) {
        val user = Tuple(Idtype, Id)
        val message: String
        message = if (user in PluginData.bililiveEnableList) {
            PluginData.bililiveEnableList.remove(user)
            "关闭B站直播监听"
        } else {
            "B站直播间监听未开启"
        }
        liveChannel.send(BililiveEvent(Id, Idtype, message))
    }

    // TODO: 查询直播间监听列表
}