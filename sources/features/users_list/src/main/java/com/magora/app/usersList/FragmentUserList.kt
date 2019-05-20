package com.magora.app.usersList

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.SharedElementCallback
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.magora.app.usersList.di.UsersModuleLoader
import com.magora.core.BaseFragment
import com.magora.core.HasTransition
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */

class FragmentUserList : BaseFragment(), HasTransition {
    private val viewModel: VmUsersList by viewModel()
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ContentLoadingProgressBar

    private var sharedElement: View? = null
    private var sharedPosition = 0

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        UsersModuleLoader.load()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_user_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh = view.findViewById(R.id.swipe_refresh_layout)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.recycler_view)

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                recyclerView.findViewHolderForAdapterPosition(sharedPosition)?.also {
                    if (it.itemView != null) {
                        sharedElements[names.first()] = it.itemView.findViewById(R.id.avatarImageView)
                        sharedElements[names[1]] = it.itemView
                    }
                }
            }
        })
        postponeEnterTransition()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = AdapterUserList(viewModel.contentData) { user, position ->
            val holder = recyclerView.findViewHolderForAdapterPosition(position)
            if (holder?.itemView != null) {
                sharedElement = holder.itemView.findViewById(R.id.avatarImageView)
                sharedPosition = position
                (exitTransition as TransitionSet).excludeTarget(holder.itemView, true)
                viewModel.onUserClick(user)
            }
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }

        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                recyclerView.removeOnLayoutChangeListener(this)
                startPostponedEnterTransition()
            }
        })
        exitTransition = TransitionSet().apply {
            addTransition(Fade())
            duration = 220
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userListLiveData.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is UsersResult.ContentProgress -> showUserListProgress(it.inProgress)
                is UsersResult.PullToRefreshProgress -> showPtrProgress(it.inProgress)
                is UsersResult.Error -> showError("Error: ${it.error}")
            }
        })
    }

    private fun showUserListProgress(isVisible: Boolean) {
        if (isVisible) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun showPtrProgress(refreshing: Boolean) {
        swipeRefresh.isRefreshing = refreshing
    }

    private fun showError(message: String) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun getSharedElements(): List<Pair<String, View>> = sharedElement?.let {
        listOf(
            it.transitionName to it,
            (it.parent as ViewGroup).transitionName to it.parent as ViewGroup
        )
    } ?: listOf()

    companion object {
        fun newInstance() = FragmentUserList()
    }
}