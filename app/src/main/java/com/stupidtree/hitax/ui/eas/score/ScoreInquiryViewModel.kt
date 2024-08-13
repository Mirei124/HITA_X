package com.stupidtree.hitax.ui.eas.score

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.stupidtree.component.data.DataState
import com.stupidtree.component.data.MTransformations
import com.stupidtree.component.data.Trigger
import com.stupidtree.hitax.data.model.eas.CourseScoreItem
import com.stupidtree.hitax.data.model.eas.TermItem
import com.stupidtree.hitax.data.repository.EASRepository
import com.stupidtree.hitax.data.source.web.service.EASService
import com.stupidtree.hitax.ui.eas.EASViewModel

class ScoreInquiryViewModel(application: Application) : EASViewModel(application) {
    /**
     * 仓库区
     */
    private val easRepository = EASRepository.getInstance(application)

    /**
     * LiveData区
     */
    private val pageController = MutableLiveData<Trigger>()

    val termsLiveData : LiveData<DataState<List<TermItem>>> = pageController.switchMap {
            return@switchMap easRepository.getAllTerms()
        }

    val selectedTermLiveData: MutableLiveData<TermItem> = MutableLiveData()
    val selectedTestTypeLiveData: MutableLiveData<EASService.TestType> = MutableLiveData()

    val scoresLiveData: LiveData<DataState<List<CourseScoreItem>>> =
        MTransformations.switchMap(selectedTermLiveData, selectedTestTypeLiveData) {
            return@switchMap easRepository.getPersonalScores(it.first, it.second)
        }

    /**
     * 方法区
     */
    fun startRefresh() {
        pageController.value = Trigger.actioning
    }


}