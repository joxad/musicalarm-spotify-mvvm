package com.startogamu.zikobot.ftu;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;

import com.joxad.easydatabinding.bottomsheet.DialogBottomSheetBase;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.databinding.AccountLinkFragmentBinding;

/**
 * Generated by generator-android-template
 */
public class AccountLinkFragment extends DialogBottomSheetBase<AccountLinkFragmentBinding, AccountLinkFragmentVM> {


    public static AccountLinkFragment newInstance() {
        Bundle args = new Bundle();
        AccountLinkFragment fragment = new AccountLinkFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int data() {
        return BR.accountLinkFragmentVM;
    }

    @Override
    public int layoutResources() {
        return R.layout.account_link_fragment;
    }

    @Override
    public AccountLinkFragmentVM baseFragmentVM(AccountLinkFragmentBinding binding) {
        return new AccountLinkFragmentVM(this, binding);
    }


    /**
     * Use this part if you want to put the dialog fullscreen
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
            BottomSheetBehavior<FrameLayout> bot = BottomSheetBehavior.from(bottomSheet);
            bot.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return dialog;
    }

}
