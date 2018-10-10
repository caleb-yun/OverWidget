package com.cogentworks.overwidget;

import com.google.gson.Gson;

/**
 * Created by cyun on 8/9/17.
 */

public class Profile {
    public String BattleTag;
    public String AvatarURL;
    public String Platform;
    public String Region;

    public String Prestige;
    public String Level;
    public String LevelImgURL;
    public String PrestigeImgURL;

    public String CompRank;
    public String Tier;

    public String gamesWon;
    private String Hero;
    private String HeroTime;

    private int updateInterval = 0;
    private String errorMsg;
    private String theme;

    public int Id;

    public Profile() {}

    public Profile(String battleTag, String platform, String region) {
        this.BattleTag = battleTag;
        this.Platform = platform;
        this.Region = region;
    }

    public void setUser(String avatarURL) {
        this.AvatarURL = avatarURL;
    }

    public void setId() {
        Id = BattleTag.hashCode();
    }

    public void setLevel(String level, String prestige, String rankImageURL) {
        this.Level = level;
        this.Prestige = prestige;
        this.LevelImgURL = rankImageURL;
    }

    public void setRank(String compRank, String tier) {
        this.CompRank = compRank;
        this.Tier = tier;
    }

    public void setHero(String hero, String heroTime) {
        this.Hero = hero;
        this.HeroTime = heroTime;
    }

    public String getErrorMsg() {
        if (errorMsg != null)
            return errorMsg;
        else
            return "Error";
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(String updateInterval) {
        int hours = Integer.parseInt(updateInterval.replaceAll("[\\D]", ""));
        this.updateInterval = hours * 60 * 60 * 1000;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
