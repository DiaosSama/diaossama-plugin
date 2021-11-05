package work.diaossama.qqbot.plugin.util;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class DrawUtil {
    private final Long qqId;
    private Long timestamp;
    private final Calendar calendar;
    private int drawNum;

    public DrawUtil(Long qqId) {
        this.qqId = qqId;

        // 获取SQLite中保存的用户信息
        this.getUserInfo();

        // 初始化格式化时间
        Date date = new Date(timestamp);
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
    }

    private void getUserInfo() {
        DBUtil db = new DBUtil();
        ResultSet res = db.query("select * from user where qqid=" + qqId);
        try {
            res.next();
            this.timestamp = res.getLong("timestamp");
            this.drawNum = res.getInt("draw_num");
        }
        catch (Exception e) {
            e.printStackTrace();
            this.drawNum = 0;
            this.timestamp = Calendar.getInstance().getTimeInMillis() - 86400000;
            db.insert("insert into user (qqid, draw_num, timestamp) values (" + qqId + ", 0, " + this.timestamp + ")");
        }
        finally {
            db.close();
        }
    }

    // 判断距上次更新是否为新的一天
    public boolean isNewDay() {
        Calendar nowcalendar = Calendar.getInstance();
        // long nowtime = nowcalendar.getTimeInMillis();
        int nowyear = nowcalendar.get(Calendar.YEAR);
        int nowmonth = nowcalendar.get(Calendar.MONTH);
        int nowday = nowcalendar.get(Calendar.DATE);
        int lastyear = this.calendar.get(Calendar.YEAR);
        int lastmonth = this.calendar.get(Calendar.MONTH);
        int lastday = this.calendar.get(Calendar.DATE);

        if (nowday > lastday) {
            return true;
        }
        else if (nowmonth > lastmonth) {
            return true;
        }
        else return nowyear > lastyear;
    }

    // 抽签
    public String draw() {
        // DrawUtil drawUtil = new DrawUtil();
        String resp;
        DBUtil db = new DBUtil();
        // 判断是否为新的一天
        if (isNewDay()) {
            long nowtime = Calendar.getInstance().getTimeInMillis();
            Random r = new Random(nowtime);
            for (int i = 0; i < (qqId % 1000); i++) {
                r.nextInt();
            }
            int draw = r.nextInt(384) + 1;
            db.update("update user set draw_num=" + draw + ",timestamp=" + nowtime + " where qqid=" + qqId);

            try {
                ResultSet res = db.query("select draw from draw where id=" + draw);
                res.next();
                resp = "签位: " + draw + "\n" + res.getString("draw") + "\n" + "解签请发送\"解签\"";
            }
            catch (Exception e) {
                e.printStackTrace();
                resp = "抽签出现错误，请重试或联系管理员。";
            }
        }
        else {
            try {
                ResultSet res = db.query("select draw from draw where id=" + drawNum);
                res.next();
                resp = "签位: " + drawNum + "\n" + res.getString("draw") + "\n" + "解签请发送\"解签\"";
            }
            catch (Exception e) {
                e.printStackTrace();
                resp = "抽签出现错误，请重试或联系管理员。";
            }
            resp = "您今天已经抽过签了。\n" + resp;
        }
        db.close();
        return resp;
    }

    // 解签
    public String dealDraw() {
        String resp;
        if (isNewDay()) {
            resp = "您今天还未抽签\n请发送\"抽签\"进行抽签";
        }
        else {
            DBUtil db = new DBUtil();
            try {
                ResultSet res = db.query("select deal_draw from draw where id=" + drawNum);
                res.next();
                resp = "解签: " + drawNum + "\n" + res.getString("deal_draw");
            }
            catch (Exception e) {
                e.printStackTrace();
                resp = "解签出现错误，请重试或联系管理员。";
            }
            db.close();
        }
        return resp;
    }
}
