package cn.anysou.as_floatlibrary.infowindows;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.anysou.as_floatlibrary.R;

public class InfoRecycleViewAdapter extends RecyclerView.Adapter<InfoRecycleViewAdapter.MyHolder> {

    private List mList;//数据源
    public InfoRecycleViewAdapter(List list) {
        mList = list;
        mList.add(0,"");  //前面插入一个空值，方便在信息框里显示
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public MyHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_content);
        }
    }

    ////创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出
    @NonNull
    @Override
    public InfoRecycleViewAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //将我们自定义的item布局R.layout.info_item 转换为 View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.info_item, parent, false);
        //将view传递给我们自定义的 MyHolder
        MyHolder holder = new MyHolder(view);
        //返回这个MyHolder实体
        return holder;
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolder中
    @Override
    public void onBindViewHolder(@NonNull InfoRecycleViewAdapter.MyHolder holder, int position) {
        String oneRecord=(String)mList.get(position);
        if(holder.textView==null)
            return;
        if(oneRecord!=null) {
            oneRecord = oneRecord.replace("：",":");
            if (oneRecord.startsWith("error:")) {
                holder.textView.setText(oneRecord.replace("error:",""));
                holder.textView.setTextColor(Color.parseColor("#FF0000"));  //红色
                holder.textView.getPaint().setFakeBoldText(true);
            }else if (oneRecord.startsWith("ok:")) {
                holder.textView.setText(oneRecord.replace("ok:",""));
                holder.textView.setTextColor(Color.parseColor("#458B00"));   //绿色
                holder.textView.getPaint().setFakeBoldText(true);
            } else if (oneRecord.startsWith("run:")) {
                holder.textView.setText(oneRecord.replace("run:",""));
                holder.textView.setTextColor(Color.parseColor("#0000CD"));  //蓝色
                holder.textView.getPaint().setFakeBoldText(true);
            }else {
                holder.textView.setText(oneRecord);
                holder.textView.setTextColor(Color.parseColor("#000000"));  //黑色
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    //  添加数据
    public void addData(String msg) {
        //在list中添加数据，并通知条目加入一条
        mList.add(1, msg);   //默任都是插入到第2位；index=1
        //添加动画
        notifyItemInserted(1);
    }
    public void addData(int position,String msg){
        mList.add(position,msg);
        notifyItemInserted(position);
    }
    //  删除数据
    public void removeData(int position) {
        mList.remove(position);
        //删除动画
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
    // 修改数据
    public void changeDate(String msg){
        mList.set(1,msg);
        notifyItemChanged(1);
    }
    public void changeDate(int position,String msg){
        mList.set(position,msg);
        notifyItemChanged(position);
    }

}
