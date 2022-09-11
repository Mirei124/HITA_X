package com.stupidtree.hitax.ui.timetable.manager

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.stupidtree.hitax.R
import com.stupidtree.hitax.data.model.timetable.Timetable
import com.stupidtree.hitax.databinding.ActivityTimetableManagerItemAddBinding
import com.stupidtree.hitax.databinding.ActivityTimetableManagerItemBinding
import com.stupidtree.style.base.BaseCheckableListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.hitax.utils.TimeTools


@SuppressLint("ParcelCreator")
class TimetableListAdapter(context: Context, mBeans: MutableList<Timetable>) :
    BaseCheckableListAdapter<Timetable, RecyclerView.ViewHolder>(context, mBeans) {

    enum class SOURCE { EAS, NEW }

    interface OnAddClickListener {
        fun onAddClick(source: SOURCE)
    }

    var mOnAddClickListener: OnAddClickListener? = null
    fun setOnAddClickListener(lis: OnAddClickListener) {
        mOnAddClickListener = lis
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return if (viewType == TYPE_ITEM) ActivityTimetableManagerItemBinding.inflate(
            mInflater,
            parent,
            false
        )
        else ActivityTimetableManagerItemAddBinding.inflate(mInflater, parent, false)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mBeans.size) TYPE_ADD
        else TYPE_ITEM
    }

    override fun createViewHolder(
        viewBinding: ViewBinding,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            CHolder(viewBinding as ActivityTimetableManagerItemBinding)
        } else {
            AHolder(viewBinding as ActivityTimetableManagerItemAddBinding)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: RecyclerView.ViewHolder, data: Timetable?, position: Int) {
        super.bindHolder(holder, data, position)
        if (holder is CHolder) {
            holder.binding.title.text = data?.name
            holder.binding.subtitle.text = TimeTools.printDate(data?.startTime?.time)
            data?.startTime?.time?.let {
                holder.binding.icon.setImageResource(
                    when (TimeTools.getSeason(it)) {
                        TimeTools.SEASON.SPRING -> R.drawable.season_spring
                        TimeTools.SEASON.SUMMER -> R.drawable.season_summer
                        TimeTools.SEASON.AUTUMN -> R.drawable.season_autumn
                        else -> R.drawable.season_winter
                    }
                )
                holder.binding.icon.setBackgroundResource(
                    when (TimeTools.getSeason(it)) {
                        TimeTools.SEASON.SPRING -> R.drawable.element_round_spring
                        TimeTools.SEASON.SUMMER -> R.drawable.element_round_summer
                        TimeTools.SEASON.AUTUMN -> R.drawable.element_round_autumn
                        else -> R.drawable.element_round_winter
                    }
                )
            }
        } else if (holder is AHolder) {
            holder.binding.icon.setOnClickListener {
                mOnAddClickListener?.onAddClick(SOURCE.NEW)
            }
            holder.binding.eas.setOnClickListener {
                mOnAddClickListener?.onAddClick(SOURCE.EAS)
            }
        }


    }

    fun notifyDatasetChanged(list: List<Timetable>) {
        mBeans.clear()
        mBeans.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    inner class CHolder(itemView: ActivityTimetableManagerItemBinding) :
        BaseViewHolder<ActivityTimetableManagerItemBinding>(itemView), CheckableViewHolder {
        override fun showCheckBox() {
            binding.check.visibility = View.VISIBLE
        }

        override fun hideCheckBox() {
            binding.check.visibility = View.GONE
        }

        override fun toggleCheck() {
            binding.check.toggle()
        }

        override fun setChecked(boolean: Boolean) {
            binding.check.isChecked = boolean
        }

        override fun setInternalOnLongClickListener(listener: View.OnLongClickListener) {
            binding.card.setOnLongClickListener(listener)
        }

        override fun setInternalOnClickListener(listener: View.OnClickListener) {
            binding.card.setOnClickListener(listener)
        }

        override fun setInternalOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
            binding.check.setOnCheckedChangeListener(listener)
        }


    }

    inner class AHolder(viewBinding: ActivityTimetableManagerItemAddBinding) :
        BaseViewHolder<ActivityTimetableManagerItemAddBinding>(viewBinding)

    companion object {
        const val TYPE_ADD = 301
        const val TYPE_ITEM = 300
    }
}
