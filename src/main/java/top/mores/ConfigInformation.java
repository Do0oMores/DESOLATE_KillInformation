package top.mores;

public class ConfigInformation {

    public boolean getONLY_SAME_WORLD() {
        return KillInformation.getInstance().getConfig().getBoolean("只给相同世界的玩家发送信息");
    }
}
