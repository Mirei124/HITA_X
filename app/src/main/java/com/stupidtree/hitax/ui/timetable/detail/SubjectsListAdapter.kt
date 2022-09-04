package com.stupidtree.hitax.ui.timetable.detail

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.stupidtree.hitax.R
import com.stupidtree.hitax.data.model.timetable.TermSubject
import com.stupidtree.hitax.databinding.DynamicSubjectListTitleBinding
import com.stupidtree.hitax.databinding.DynamicSubjectsItemBinding
import com.stupidtree.style.base.BaseCheckableListAdapter
import com.stupidtree.style.base.BaseViewHolder
import com.stupidtree.hitax.ui.timetable.detail.SubjectsListAdapter.SubjectViewHolder

@SuppressLint("ParcelCreator")
class SubjectsListAdapter(
    context: Context,
    subjects: MutableList<TermSubject>,
    val viewModel: TimetableDetailViewModel,
    private val lifecycleOwner: LifecycleOwner
) :
    BaseCheckableListAdapter<TermSubject, SubjectViewHolder>(context, subjects) {

    interface OnColorClickListener{
        fun onColorClick(data:TermSubject)
    }

    var onColorClickListener:OnColorClickListener? = null

    override fun getItemViewType(position: Int): Int {
        if (position == mBeans.size) return FOOT
        return if (mBeans[position].type == TermSubject.TYPE.TAG) TITLE else NORMAL
    }

    override fun getItemCount(): Int {
        return mBeans.size
    }

    override fun getViewBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            NORMAL -> DynamicSubjectsItemBinding.inflate(mInflater, parent, false)
            else -> DynamicSubjectListTitleBinding.inflate(mInflater, parent, false)
        }
    }


    class SubjectViewHolder(binding: ViewBinding) :
        BaseViewHolder<ViewBinding>(viewBinding = binding), CheckableViewHolder {

        override fun showCheckBox() {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).check.visibility = View.VISIBLE
            }
        }

        override fun hideCheckBox() {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).check.visibility = View.GONE
            }
        }

        override fun toggleCheck() {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).check.toggle()
            }
        }

        override fun setChecked(boolean: Boolean) {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).check.isChecked = boolean
            }
        }

        override fun setInternalOnLongClickListener(listener: View.OnLongClickListener) {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).item.setOnLongClickListener(listener)
            }
        }

        override fun setInternalOnClickListener(listener: View.OnClickListener) {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).item.setOnClickListener(listener)
            }
        }

        override fun setInternalOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
            if (binding is DynamicSubjectsItemBinding) {
                (binding as DynamicSubjectsItemBinding).check.setOnCheckedChangeListener(listener)
            }
        }

    }

    companion object {
        private const val NORMAL = 967
        private const val TITLE = 971
        private const val FOOT = 608
    }


    override fun bindHolder(
        holder: SubjectViewHolder,
        data: TermSubject?,
        position: Int
    ) {
        super.bindHolder(holder, data, position)
        if (holder.binding is DynamicSubjectListTitleBinding) {
            val titleBinding = holder.binding as DynamicSubjectListTitleBinding
            titleBinding.name.text = mBeans[position].name
        } else if (holder.binding is DynamicSubjectsItemBinding) {
            val binding = holder.binding as DynamicSubjectsItemBinding
            binding.name.text = data?.name
            if (data?.color != null) {
                binding.icon.setColorFilter(data.color)
            } else {
                binding.icon.clearColorFilter()
            }
            data?.id?.let { str ->
                viewModel.getSubjectProgress(str).observe(lifecycleOwner) {
                    binding.progress.progress = (it.first / (it.second.toFloat()) * 100).toInt()
                }
            }
//            data?.let {
//                binding.progress.progressTintList = ColorStateList.valueOf(it.color)
//
//                binding.progress.progressBackgroundTintMode = PorterDuff.Mode.SRC_IN
//                binding.progress.progressBackgroundTintList = ColorStateList.valueOf(ColorTools.changeAlpha(it.color, 0.2f))
//            }


            binding.icon.setOnClickListener {
                data?.let { it1 -> onColorClickListener?.onColorClick(it1) }
            }
            val t =
                if (TextUtils.isEmpty(data?.school)) mContext.getString(R.string.unknown_department) else data?.school
            binding.label.text = t
            if (isEditMode) {
                binding.icon.visibility = View.GONE
            } else {
                binding.icon.visibility = View.VISIBLE
            }
        }
    }

    override fun createViewHolder(viewBinding: ViewBinding, viewType: Int): SubjectViewHolder {
        return SubjectViewHolder(viewBinding)
    }
}