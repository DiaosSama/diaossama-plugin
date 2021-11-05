package work.diaossama.qqbot.plugin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BiliLiveUtil {
    // 直播间信息接口
    private final String liveURL = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=";
    // 用户个人信息接口
    private final String userURL = "https://api.bilibili.com/x/space/acc/info?mid=";
    // 直播间ID
    private String roomId;
    // 直播间标题
    private String title;
    // 直播间UP主uid
    private String uid;
    // 直播间UP主名字
    private String name;
    // 直播间状态
    // 0:下播 1:开播 2:轮播
    private String online;
    // 获取信息状态
    private boolean status;

    public BiliLiveUtil(String room_id) {
        this.roomId = room_id;
        initRoom();
        initName();
        // System.out.println(roomId + " " + title + " " + uid + " " + name);
    }

    private void initName() {
        try {
            URL url = new URL(this.userURL + this.uid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String res = bufferedReader.readLine();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(res, JsonNode.class);
            JsonNode dataNode = jsonNode.get("data");
            this.name = dataNode.get("name").toString();
            this.status = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            this.name = "用户名获取失败";
            this.status = false;
        }
    }

    private void initRoom() {
        try {
            URL url = new URL(this.liveURL + this.roomId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String res = bufferedReader.readLine();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(res, JsonNode.class);
            JsonNode dataNode = jsonNode.get("data");
            this.title = dataNode.get("title").toString();
            this.uid = dataNode.get("uid").toString();
            this.online = dataNode.get("live_status").toString();
            this.status = true;
        } catch (Exception e)
        {
            e.printStackTrace();
            this.title = "直播间标题获取失败";
            this.uid = "直播间UP uid获取失败";
            this.online = "-1";
            this.status = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return uid;
    }

    public boolean isStatus() {
        return status;
    }

    public String getOnline() {
        return online;
    }
}

