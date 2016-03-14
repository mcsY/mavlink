package com.stratus.activity;

import com.stratus.R;

import android.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;

public class ConnectDialog extends DialogFragment
{
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.connect, null));
		builder.setTitle("Connect to ArduCopter");
		return builder.create();
	}

}