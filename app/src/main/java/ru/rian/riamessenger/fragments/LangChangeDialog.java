package ru.rian.riamessenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioGroup;

import lombok.Setter;
import ru.rian.riamessenger.R;
import ru.rian.riamessenger.utils.LocaleHelper;

/**
 * Created by Roman on 09.04.2015.
 */

public class LangChangeDialog extends DialogFragment {

    @Setter
    RadioGroup.OnCheckedChangeListener radioGroupOnCheckedChangeListener;

    public static DialogFragment showDialog(Context context, RadioGroup.OnCheckedChangeListener radioGroupOnCheckedChangeListener) {
        LangChangeDialog fragment = new LangChangeDialog();
        fragment.setRadioGroupOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
        fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), LangChangeDialog.class.getSimpleName());
        return fragment;
    }

    public static String getStringResourceByName(int resId, Context context) {
        String resourceName = context.getResources().getResourceName(resId);
        resourceName = resourceName.substring(resourceName.indexOf('/')+1);
        return resourceName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lang_change_dialog, null);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.lang_change_radio_group);
        int id = -1;
        String langStr = LocaleHelper.getLanguage(getContext());
        if (langStr.equals(getStringResourceByName(R.string.ru, getContext()))) {
            id = R.id.ru;
        } else if (langStr.equals(getStringResourceByName(R.string.en, getContext()))) {
            id = R.id.en;
        } else if (langStr.equals(getStringResourceByName(R.string.es, getContext()))) {
            id = R.id.es;
        } else if (langStr.equals(getStringResourceByName(R.string.ar, getContext()))) {
            id = R.id.ar;
        }
        if (id == -1) {
            id = R.id.ru;
        }
        radioGroup.check(id);
        radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
        return view;
    }


}