package com.example.wequest.wequest.utils;

import android.app.Activity;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


public class ToolTipHintShower {

    public static MaterialTapTargetPrompt.Builder builder(Activity context, int target, int primaryText, int secondaryText) {
        return new MaterialTapTargetPrompt.Builder(context)
                .setTarget(target)
                .setPrimaryText(primaryText)
                .setSecondaryText(secondaryText);
    }
}
