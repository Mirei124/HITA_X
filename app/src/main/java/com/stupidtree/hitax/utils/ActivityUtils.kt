package com.stupidtree.hitax.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.stupidtree.hita.theta.ThetaActivity
import com.stupidtree.hitax.R
import com.stupidtree.hitax.data.repository.EASRepository
import com.stupidtree.hitax.data.source.preference.EasPreferenceSource
import com.stupidtree.hitax.ui.about.ActivityAbout
import com.stupidtree.style.base.BaseActivity
import com.stupidtree.hitax.ui.eas.login.PopUpLoginEAS
import com.stupidtree.hitax.ui.myprofile.MyProfileActivity
import com.stupidtree.hitax.ui.profile.ProfileActivity
import com.stupidtree.hitax.ui.search.SearchActivity
import com.stupidtree.hitax.ui.subject.SubjectActivity
import com.stupidtree.hitax.ui.teacher.ActivityTeacherOfficial
import com.stupidtree.hitax.ui.timetable.detail.TimetableDetailActivity
import com.stupidtree.hitax.ui.timetable.manager.TimetableManagerActivity
import com.stupidtree.hitax.ui.welcome.WelcomeActivity
import com.stupidtree.stupiduser.data.model.CheckUpdateResult
import com.stupidtree.stupiduser.data.repository.LocalUserRepository
import com.stupidtree.style.widgets.PopUpText
import com.stupidtree.style.widgets.PopUpUpdate


object ActivityUtils {

    fun startOfficialTeacherActivity(from: Context, id: String, url: String, name: String) {
        val i = Intent(from, ActivityTeacherOfficial::class.java)
        i.putExtra("id", id)
        i.putExtra("url", url)
        i.putExtra("name", name)
        from.startActivity(i)
    }


    enum class SearchType { TEACHER, CLASS,ARTICLE,USER }

    fun searchFor(from: Context, text: String?, type: SearchType) {
        if (text.isNullOrBlank()) return
        val i = Intent(from, SearchActivity::class.java)
        i.putExtra("keyword", text)
        i.putExtra("type", type.name)
        from.startActivity(i)

    }

    fun startSearchActivity(from: Context) {
        val i = Intent(from, SearchActivity::class.java)
        from.startActivity(i)
    }

    fun startSearchActivity(from: Activity,transition: View) {
        val i = Intent(from, SearchActivity::class.java)
        transition.transitionName = "search"
        val ao = ActivityOptionsCompat.makeSceneTransitionAnimation(from,transition,"search")
        from.startActivity(i,ao.toBundle())
    }
    fun startMyProfileActivity(from: Context) {
        val i = Intent(from, MyProfileActivity::class.java)
        from.startActivity(i)
    }

    fun startWelcomeActivity(from: Context) {
        val i = Intent(from, WelcomeActivity::class.java)
        from.startActivity(i)
    }

    /**
     * 进行教务认证，或直接跳转
     * @param directTo 若存在已登录token，则直接跳转到activity。传null表示忽略
     * @param lock 是否锁定窗口（=true时，若cancel则连带宿主一起销毁）
     * @param onResponseListener 认证监听
     */
    fun <T : Activity> showEasVerifyWindow(
        from: Context,
        directTo: Class<T>? = null,
        lock: Boolean = false,
        onResponseListener: PopUpLoginEAS.OnResponseListener
    ) {
        if (from is BaseActivity<*, *>) {
            if (EASRepository.getInstance((from as AppCompatActivity).application).getEasToken()
                    .isLogin()
            ) {
                directTo?.let {
                    val i = Intent(from, directTo)
                    from.startActivity(i)
                    return
                }
            }
            val window = PopUpLoginEAS()
            window.lock = lock
            window.onResponseListener = onResponseListener
            window.show(from.supportFragmentManager, "verify")
        }
    }


    fun <T : Activity> startActivity(from: Context, activity: Class<T>) {
        val i = Intent(from, activity)
        from.startActivity(i)
    }

    fun startTimetableManager(from: Context) {
        val i = Intent(from, TimetableManagerActivity::class.java)
        from.startActivity(i)
    }


    fun startSubjectActivity(from: Context, id: String) {
        val i = Intent(from, SubjectActivity::class.java)
        i.putExtra("subjectId", id)
        from.startActivity(i)
    }

    fun startTimetableDetailActivity(from: Context, id: String) {
        val i = Intent(from, TimetableDetailActivity::class.java)
        i.putExtra("id", id)
        from.startActivity(i)
    }

    fun startThetaActivity(from: Context) {
        if(LocalUserRepository.getInstance(from).getLoggedInUser().isValid()){
            val i = Intent(from, ThetaActivity::class.java)
            from.startActivity(i)
        }else{
            startWelcomeActivity(from)
        }

    }

    fun startProfileActivity(from: Context, userId: String?, imageView: ImageView?=null) {
        val i = Intent(from, ProfileActivity::class.java)
        i.putExtra("id", userId)
        imageView?.let {
            val op = ActivityOptionsCompat.makeSceneTransitionAnimation(from as Activity,it,"useravatar")
            from.startActivity(i,op.toBundle())
        }?:run {
            from.startActivity(i)
        }
    }

    fun showUpdateNotificationForce(cr:CheckUpdateResult,activity: BaseActivity<*,*>){
        PopUpText().setText("版本：${cr.latestVersionName}\n更新内容：${cr.updateLog}\n" + "是否前往下载？")
            .setTitle(R.string.new_version_available)
            .setOnConfirmListener(object : PopUpText.OnConfirmListener {

                override fun OnConfirm() {
                    val uri: Uri = Uri.parse(cr.latestUrl);
                    val intent = Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent)
                }
            }).show(activity.supportFragmentManager, "update")
    }

    fun showUpdateNotification(cr:CheckUpdateResult,activity: BaseActivity<*,*>){
       val preference: SharedPreferences =
            activity.application.getSharedPreferences("update_skip", Context.MODE_PRIVATE)
        if(preference.getBoolean(cr.latestVersionCode.toString(),false)) return
        PopUpUpdate().setText("版本：${cr.latestVersionName}\n更新内容：${cr.updateLog}\n" + "是否前往下载？")
            .setTitle(R.string.new_version_available)
            .setOnActionListener(object : PopUpUpdate.OnActionListener {
                override fun onConfirm() {
                    val uri: Uri = Uri.parse(cr.latestUrl);
                    val intent = Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent)
                }

                override fun onCancel() {

                }

                override fun onSkip() {
                    preference.edit().putBoolean(cr.latestVersionCode.toString(),true).apply()
                }
            }).show(activity.supportFragmentManager, "update")
    }
}