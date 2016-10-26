package kaiery.csrs.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kaiery.csrs.R;
import kaiery.csrs.beans.ModuleBean;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-26.
 * ------------------------------
 */

public class Holder_Module_Header extends RecyclerView.ViewHolder  {

    private TextView header;
    private View line;

    public Holder_Module_Header(View itemView) {
        super(itemView);
        header = (TextView) itemView.findViewById(R.id.tv_header);
        line = itemView.findViewById(R.id.line);
    }


    public void bindHolder(ModuleBean model){
        header.setText(model.getHeader());
    }

    public void hideTopLine(int position){
        if(position==0) line.setVisibility(View.GONE);
    }
}
