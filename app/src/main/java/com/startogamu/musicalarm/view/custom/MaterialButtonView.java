package com.startogamu.musicalarm.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.startogamu.musicalarm.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by josh on 18/03/16.
 */
public class MaterialButtonView extends MaterialRippleLayout {

    @Bind(R.id.tv_button)
    TextView tvButton;

    public MaterialButtonView(Context context) {
        super(context);
        init(null);
    }

    public MaterialButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.view_material_button, this);
        ButterKnife.bind(this);
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs, R.styleable.MaterialButtonView, 0, 0);
            tvButton.setText(a.getString(R.styleable.MaterialButtonView_mbv_text));
            tvButton.setTextColor(a.getColor(R.styleable.MaterialButtonView_mbv_text_color,
                    ContextCompat.getColor(getContext(), R.color.colorAccent)));

        }

    }


}
