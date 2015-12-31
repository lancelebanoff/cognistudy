package com.cognitutor.cognistudyapp;

/**
 * Created by Kevin on 12/30/2015.
 */
import java.util.*;

public class QS_Achievements {

    public static final Map<String, AchievementGroup> AchievementGroupTypeToAchievementGroup;
    static {
        Map<String, AchievementGroup> groupsMap = new HashMap<String, AchievementGroup>();

        //Challenges won player group
        String type = Constants.AchievementGroupType.CHALLENGES_WON_PLAYER;
        groupsMap.put(type, new AchievementGroup(type,
                "Win xxx challenges",
                new int[][]{{10,15},{25,20},{50,25},{100,30},{500,50}}));

        //Challenges won computer group
        //TODO: Finish entering these
        AchievementGroupTypeToAchievementGroup = Collections.unmodifiableMap(groupsMap);
    }
}
class AchievementGroup {
    String achievementGroupType;
    String textTemplate;
    HashMap<Integer, Integer> numGainedToCoins;
    public AchievementGroup(String type, String template, int[][] num_award) {
        achievementGroupType = type;
        textTemplate = template;
        numGainedToCoins = new HashMap<Integer, Integer>();
        putIntoMap(num_award);
    }
    public String getImageFilename(int numToGain) throws Exception {
        if(!numGainedToCoins.containsKey(numToGain))
            throw new Exception("Error: invalid numToGain");
        return "images/" + achievementGroupType.toLowerCase() + "_" + String.valueOf(numToGain) + ".png";
    }
    public String getDisplayText(int numToGain) throws Exception {
        if(!numGainedToCoins.containsKey(numToGain))
            throw new Exception("Error: invalid numToGain");
        return textTemplate.replace("xxx", String.valueOf(numToGain));
    }
    private void putIntoMap(int[][] num_award) {
        for(int i=0; i<num_award.length; i++)
            numGainedToCoins.put(num_award[i][0], num_award[i][1]);
    }
}
