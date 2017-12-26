package com.example.discover.ui.Video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.discover.R;
import com.example.discover.adapter.VideoRecyclerAdapter;
import com.example.discover.app.Constant;
import com.example.discover.base.BaseFragment;
import com.example.discover.bean.EyeBean;
import com.example.discover.databinding.FragmentVideoBinding;
import com.example.discover.http.RequestListener;
import com.example.discover.http.cahe.ACache;
import com.example.discover.model.VideoModel;
import com.example.discover.utils.DebugUtil;
import com.example.discover.utils.DensityUtil;
import com.example.zmenu.FloatButton;
import com.example.zmenu.PUtils;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

import cn.jzvd.JZVideoPlayer;
import rx.Subscription;

/**
 * Created by Administrator on 2017/12/9 0009.
 */

public class VideoFragment extends BaseFragment<FragmentVideoBinding> {

    private VideoRecyclerAdapter mVideoAdapter;
    LinearLayoutManager mLayoutManager;
    private int start = 5; //前几个数据可能没有video，所以从5开始
    private int num = 10;
    private int mPage = 1;
    private boolean isPrepare = false;
    private boolean isFirst = true;
    private EyeBean mEyeBean;
    private ACache mCache;
    private List<FloatButton> floatButtons;//XMenu的悬浮按钮
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCache = ACache.get(getContext());
        floatButtons = PUtils.getInstance().getViewList(); //获取悬浮按钮
        initRecyclerView();

        //准备就绪
        isPrepare = true;
        //解决懒加载首页不显示，手动loadData()
        loadData();
    }

    @Override
    public void loadData() {
        if (!isPrepare || !isVisibile || !isFirst) {
            return;
        }
        //先从缓存读取数据，如果没有在请求
        mEyeBean = (EyeBean) mCache.getAsObject(Constant.EYE_VIDEO);
        if (mEyeBean != null ) {
            //getAchecaData();
            setAdapter(mEyeBean);
           // DebugUtil.debug("test12", mEyeBean.getItemList().get(0).getType());
        }else {
            loadVideo();
        }
        //避免重复加载
        isFirst = false;
    }

    public void loadVideo() {
        VideoModel.showVideo(start, num, new RequestListener() {
            @Override
            public void onSuccess(Object object) {
                EyeBean eyeBean = (EyeBean) object;
                if (mPage == 1) {
                    if(eyeBean != null && eyeBean.getItemList() != null&& eyeBean.getItemList().size() > 0) {

                        bindingView.srlVideo.setRefreshing(false);
                        DebugUtil.debug("test1", eyeBean.getItemList().get(0).getData().getPlayInfo().size() + "");
                        setAdapter(eyeBean);
                        //缓存5小时
                        mCache.remove(Constant.EYE_VIDEO);
                        mCache.put(Constant.EYE_VIDEO, eyeBean, 18000);
                    }
                } else {
                    if(eyeBean != null && eyeBean.getItemList() != null&& eyeBean.getItemList().size() > 0) {
                        mVideoAdapter.updateStateLoad(false);
                        mVideoAdapter.addAll(eyeBean.getItemList());
                        mVideoAdapter.notifyDataSetChanged();
                    } else {
                        //数据刷新到底了
                       // bindingView.rvVideo.setNoMore(true);
                    }
                }
            }

            @Override
            public void onFailed() {
                //DebugUtil.toast(getActivity(), "failed");
            }

            @Override
            public void addSubscription(Subscription subscription) {
                addMySubscription(subscription);
            }
        });

    }
    @Override
    public int setContentView() {
        return R.layout.fragment_video;
    }


    public void initRecyclerView() {
        mVideoAdapter = new VideoRecyclerAdapter(getContext());
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bindingView.rvVideo.setLayoutManager(mLayoutManager);
        bindingView.srlVideo.setColorSchemeResources(R.color.background5, R.color.background2, R.color.background4);
        //上拉刷新
        bindingView.srlVideo.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                JZVideoPlayer.goOnPlayOnPause();
                bindingView.srlVideo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPage = 1;
                        start = 5;
                        loadVideo();
                    }
                }, 1000);
            }
        });
        //设置recycler滚动监听
        bindingView.rvVideo.setOnScrollListener(new RecyclerView.OnScrollListener() {
            //下拉加载更多
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mLayoutManager.getItemCount();
                DebugUtil.debug("position1", lastVisibleItem + "  " + totalItemCount);
                if (lastVisibleItem == totalItemCount - 1 && !mVideoAdapter.isLoading()) {
                    mVideoAdapter.updateStateLoad(true);
                    mPage++;
                    start += num;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DebugUtil.debug("startpage", mPage + "" + start);
                            loadVideo();
                        }
                    }, 500);

                }
            }

            //recyclerview滑动时ZMemu自动隐藏
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == XRecyclerView.SCROLL_STATE_IDLE) {
                    for (int i = 0; i < floatButtons.size(); i++) {
                        floatButtons.get(i).show();
                    }
                } else {
                    for (int i = 0; i < floatButtons.size(); i++) {
                        floatButtons.get(i).hide();
                    }
                }
            }
        });
    }
    public void setAdapter(EyeBean eyeBean) {
        stopLoading();
        mVideoAdapter.clear();
        mVideoAdapter.addAll(eyeBean.getItemList());
        bindingView.rvVideo.setAdapter(mVideoAdapter);
    }

    //取缓存
    public void getAchecaData() {
        mEyeBean = (EyeBean) mCache.getAsObject(Constant.EYE_VIDEO);
        //如果是第一次打开再加载
        if (isFirst) {
            setAdapter(mEyeBean);
            isFirst = false;
        }
    }

    @Override
    public void onPause() {
        JZVideoPlayer.releaseAllVideos();
        super.onPause();
    }

}