package com.demo.smartqr.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.smartqr.R
import com.demo.smartqr.bean.SetBean
import kotlinx.android.synthetic.main.item_set.view.*

class SetAdapter(private val context: Context,private val click:(index:Int)->Unit):RecyclerView.Adapter<SetAdapter.SetView>() {
    private val list= arrayListOf<SetBean>()
    init {
        list.add(SetBean(R.drawable.contact,"Contact us"))
        list.add(SetBean(R.drawable.agree,"Privacy agreement"))
        list.add(SetBean(R.drawable.update,"Update"))
        list.add(SetBean(R.drawable.share,"Share"))
    }

    inner class SetView(view:View):RecyclerView.ViewHolder(view){
        init {
            view.setOnClickListener { click.invoke(layoutPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetView {
        return SetView(LayoutInflater.from(context).inflate(R.layout.item_set,parent,false))
    }

    override fun onBindViewHolder(holder: SetView, position: Int) {
        with(holder.itemView){
            val setBean = list[position]
            tv_title.text=setBean.title
            iv_icon.setImageResource(setBean.icon)
        }
    }

    override fun getItemCount(): Int = list.size
}