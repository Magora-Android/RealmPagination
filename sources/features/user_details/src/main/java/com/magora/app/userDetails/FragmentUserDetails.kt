package com.magora.app.userDetails

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.SharedElementCallback
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.transition.ArcMotion
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.magora.app.model.User
import com.magora.app.userDetails.di.UserDetailsModuleLoader
import com.magora.core.BaseFragment
import com.magora.core.HasTransition
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Developed by Magora Team (magora-systems.com)
 * 2019
 */
private const val KEY_USER_ID = "KEY_USER_ID"

class UserDetailsFragment : BaseFragment(), HasTransition {
    private val viewModel by viewModel<VmUserDetails>()
    private lateinit var fragmentRoot: ViewGroup
    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        UserDetailsModuleLoader.load()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_user_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        fragmentRoot = view.findViewById(R.id.fragment_root)
        userAvatar = view.findViewById(R.id.image_user_avatar)
        userName = view.findViewById(R.id.text_user_name)
        sharedElementEnterTransition = TransitionSet().apply {
            setPathMotion(ArcMotion())
            addTransition(AutoTransition())

            interpolator = FastOutSlowInInterpolator()
            duration = 280
        }

        (enterTransition as? Transition)?.excludeTarget(userName, true)

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                userAvatar.transitionName = names.first()
                fragmentRoot.transitionName = names[1]
                sharedElements[names.first()] = userAvatar
                sharedElements[names[1]] = fragmentRoot
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.also {
            viewModel.getUser(it.getInt(KEY_USER_ID))
        }

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer(::onUserLoaded))
    }

    private fun onUserLoaded(user: User) {
        Glide.with(this)
            .load(user.avatarUrl)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

            })
            .into(userAvatar)

        userName.text = user.login
    }

    override fun getSharedElements(): List<Pair<String, View>> = listOf()

    companion object {

        fun newInstance(userId: Int) = UserDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_USER_ID, userId)
            }
        }
    }
}


