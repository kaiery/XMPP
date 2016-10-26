package kaiery.csrs.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kaiery.csrs.R;
import kaiery.csrs.beans.ModuleBean;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-26.
 * ------------------------------
 */

public class Holder_Module_Button extends RecyclerView.ViewHolder  {

    private TextView title;
    private ImageView icon;

    public Holder_Module_Button(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.tv_title);
        icon = (ImageView) itemView.findViewById(R.id.iv_icon);
    }


    public void bindHolder(ModuleBean model){
        title.setText(model.getTitle());
        icon.setImageResource(R.mipmap.ic_launcher);
    }
}
