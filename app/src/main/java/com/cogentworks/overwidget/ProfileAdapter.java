package com.cogentworks.overwidget;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {

    public ProfileAdapter(Context context, ArrayList<Profile> profiles) {
        super(context, 0, profiles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Profile profile = getItem(position);

        if (profile == null)
            return null;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Context context = convertView.getContext();

        // Lookup view for data population
        TextView name = convertView.findViewById(R.id.name);
        TextView tag = convertView.findViewById(R.id.tag);
        TextView level = convertView.findViewById(R.id.level);
        TextView gamesWon = convertView.findViewById(R.id.games_won);
        TextView info = convertView.findViewById(R.id.info);
        ImageView comprank = convertView.findViewById(R.id.appwidget_comprank);
        ImageView tier = convertView.findViewById(R.id.appwidget_tier);

        boolean isDark = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_DARK_THEME, false);

        // Populate the data into the template view using the data object
        String[] parts = profile.BattleTag.split("#");
        String username = parts[0];
        name.setText(username);
        if (parts.length > 1) {
            tag.setText("#" + parts[1]);
        }

        if(Integer.parseInt(profile.gamesWon) > 0)
            gamesWon.setText(profile.gamesWon + " games won");

        level.setText("Lvl " + (Integer.parseInt(profile.Prestige)*100 + Integer.parseInt(profile.Level)));
        if (!profile.RankImgURL.equals("")) {
            //tier.setImageResource(context.getResources().getIdentifier(profile.Tier, "drawable", context.getPackageName()));
            Glide.with(parent)
                    .load(profile.RankImgURL)
                    .into(tier);
            if (isDark)
                comprank.setImageBitmap(WidgetUtils.BuildTextBmp(profile.CompRank, "Dark", context));
            else
                comprank.setImageBitmap(WidgetUtils.BuildTextBmp(profile.CompRank, "Light", context));
        } else {
            convertView.findViewById(R.id.skill_layout).setVisibility(View.GONE);
        }

        String platform = profile.Platform;
        if (!(profile.Region.equals("") || profile.Region.equals("any")))
            platform += " " + profile.Region;

        info.setText(platform);
        convertView.setTag(profile.BattleTag);
        try {
            Glide.with(parent)
                    .load(profile.AvatarURL)
                    .into((ImageView) convertView.findViewById(R.id.avatar));
        } catch (Exception e) {
            Glide.with(parent)
                    .load("https://us.battle.net/forums/static/images/avatars/overwatch/avatar-overwatch-default.png")
                    .into((ImageView) convertView.findViewById(R.id.avatar));
        }

        // Return the completed view to render on screen
        return convertView;
    }

}
