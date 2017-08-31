package com.cogentworks.overwidget;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cyun on 8/9/17.
 */

public class Profile {
    public void SetUser(String battleTag, String avatarURL) throws MalformedURLException {
        this.BattleTag = battleTag;
        this.AvatarURL = avatarURL;
    }

    public void SetLevel(String level, String prestige, String rankImageURL) throws MalformedURLException {
        this.Level = level;
        this.Prestige = prestige;
        this.RankImageURL = rankImageURL;
    }

    public void SetRank(String compRank, String tier) {
        this.CompRank = compRank;
        this.Tier = tier;
    }

    public String BattleTag = "Error";
    public String AvatarURL;

    public String Prestige = "Error";
    public String Level = "Error";
    public String RankImageURL;

    public String CompRank = "Error";
    public String Tier = "Error";
}
