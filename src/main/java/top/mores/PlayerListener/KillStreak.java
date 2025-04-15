package top.mores.PlayerListener;

class KillStreak {
    private int killCount;
    private long lastKillTime;

    public KillStreak(int killCount, long lastKillTime) {
        this.killCount = killCount;
        this.lastKillTime = lastKillTime;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementKills() {
        killCount++;
    }

    public void resetKills() {
        killCount = 1;
    }

    public long getLastKillTime() {
        return lastKillTime;
    }

    public void setLastKillTime(long lastKillTime) {
        this.lastKillTime = lastKillTime;
    }
}
