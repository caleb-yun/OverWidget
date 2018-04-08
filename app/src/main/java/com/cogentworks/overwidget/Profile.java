package com.cogentworks.overwidget;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cyun on 8/9/17.
 */

public class Profile {
    public String BattleTag;
    public String AvatarURL;

    public String Prestige = "Error";
    public String Level = "Error";
    public String RankImageURL;

    public String CompRank = "Error";
    public String Tier;

    public String Hero;
    public String HeroTime;

    public int updateInterval = 1000*60*60;

    private String errorMsg;

    public Profile() {
        BattleTag = "";
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void SetUser(String battleTag, String avatarURL) {
        this.BattleTag = battleTag;
        this.AvatarURL = avatarURL;
    }

    public void SetLevel(String level, String prestige, String rankImageURL) {
        this.Level = level;
        this.Prestige = prestige;
        this.RankImageURL = rankImageURL;
    }

    public void SetRank(String compRank, String tier) {
        this.CompRank = compRank;
        this.Tier = tier;
    }

    public void SetHero(String hero, String heroTime) {
        this.Hero = hero;
        this.HeroTime = heroTime;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
