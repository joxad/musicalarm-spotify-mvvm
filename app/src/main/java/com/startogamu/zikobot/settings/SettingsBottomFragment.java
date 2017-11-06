package com.startogamu.zikobot.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;

import com.joxad.easydatabinding.bottomsheet.DialogBottomSheetBase;
import com.joxad.zikobot.data.db.model.ZikoTrack;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.Constants;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.databinding.SettingsBottomFragmentBinding;

/**
 * Generated by generator-android-template
 */
public class SettingsBottomFragment extends DialogBottomSheetBase<SettingsBottomFragmentBinding, SettingsBottomFragmentVM> {


    public static SettingsBottomFragment newInstance(ZikoTrack track) {
        Bundle args = new Bundle();
        args.putLong(Constants.Extra.INSTANCE.getTRACK_ID(), track.getId());
        SettingsBottomFragment fragment = new SettingsBottomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int data() {
        return BR.settingsBottomFragmentVM;
    }

    @Override
    public int layoutResources() {
        return R.layout.settings_bottom_fragment;
    }

    @Override
    public SettingsBottomFragmentVM baseFragmentVM(SettingsBottomFragmentBinding binding) {
        return new SettingsBottomFragmentVM(this, binding);
    }


    /**
     * Use this part if you want to put the dialog fullscreen
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
            BottomSheetBehavior<FrameLayout> bot = BottomSheetBehavior.from(bottomSheet);
            bot.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheet.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        });
        return dialog;
    }

}
