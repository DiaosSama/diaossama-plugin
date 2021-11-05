package work.diaossama.qqbot.plugin.function

import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.event.subscribeGroupMessages
import work.diaossama.qqbot.plugin.PluginMain
import work.diaossama.qqbot.plugin.util.PicUtil
import java.io.File
import kotlin.random.Random

// 发涩图（不是
// 使用v2ex老哥提供的接口（有空再自己重写随机接口
// https://www.v2ex.com/t/727134
suspend fun PluginMain.HsoHandler() {
    subscribeGroupMessages {
        case("来张涩图") {
            val picNum = Random.nextInt(2)  // 0代表手机分辨率，1为PC分辨率
            var url = ""
            if (picNum == 0) {
                url = "https://open.pixivic.net/wallpaper/mobile/random?size=large&domain=https://i.pixiv.cat&webp=0&detail=1"

            }
            else {
                url = "https://open.pixivic.net/wallpaper/pc/random?size=large&domain=https://i.pixiv.cat&webp=0&detail=1"
            }
            val urlpath = PicUtil.getLocation(url)
            if (urlpath == "") {
                quoteReply("获取涩图失败，联系DiaosSama查看日志")
            }
            else {
                val filename = PicUtil.downloadPic(urlpath)
                File(filename).sendAsImage()
                quoteReply("图片URL: $urlpath\n如果有链接无图说明被鹅吞了QAQ")
            }
        }
    }
}