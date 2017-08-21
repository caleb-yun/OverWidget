package com.cogentworks.overwidget;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cyun on 8/9/17.
 */

public class Profile {
    public void SetUser(String battleTag, String avatarURL) throws MalformedURLException {
        this.BattleTag = battleTag;
        this.AvatarURL = new URL(avatarURL);
    }

    public void SetLevel(String level, String prestige, String rankImageURL) throws MalformedURLException {
        this.Level = level;
        this.Prestige = prestige;
        this.RankImageURL = new URL(rankImageURL);
    }

    public void SetRank(String compRank, String tier) {
        this.CompRank = compRank;
        this.Tier = tier;
    }

    public String BattleTag = "Error";
    public URL AvatarURL;

    public String Prestige = "Error";
    public String Level = "Error";
    public URL RankImageURL;

    public String CompRank = "Error";
    public String Tier = "Error";
}
