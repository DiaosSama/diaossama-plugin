import kotlin.random.Random

fun main() {
    val src = "test trim()"
    val str = "\"【怪猎Rise】爽够我就去做视频\""
    println(str)
    println(str.trim())
    println(src.equals(str.trim()))
}