package com.video.newqu.holder;

import android.app.Activity;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.VideoListComentAdapter;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.PlayCountInfo;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.comadapter.entity.MultiItemEntity;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.contants.NetContants;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.listener.OnPostPlayStateListener;
import com.video.newqu.listener.TopicClickListener;
import com.video.newqu.listener.VideoOnItemClickListener;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.PostPlayStateHanderUtils;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.TextViewTopicSpan;
import com.video.newqu.util.TimeUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.layout.VideoGroupRelativeLayout;
import com.video.newqu.view.layout.WrapContentGrideView;
import com.video.newqu.view.widget.GlideCircleTransform;
import com.volokh.danylo.visibility_utils.items.ListItem;
import com.xinqu.videoplayer.XinQuVideoPlayer;
import com.xinqu.videoplayer.XinQuVideoPlayerStandard;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/5/26 10:12
 * 配合滑动监听器而使用的Item,专注于数据绑定和处理不影响界面刷新的点赞，关注，播放统计等业务
 */

public class ComendVideoItem implements ListItem, MultiItemEntity {

    private static final String TAG = ComendVideoItem.class.getSimpleName();
    private final Rect mCurrentViewRect = new Rect();
    private final int status;//界面目标类型  1：我的作品 2：我喜欢的作品 3：用户中心
    private final TopicClickListener topicClickListener;
    private  ItemCallback mItemCallback;
    private VideoOnItemClickListener onItemClickListener;
    private BaseViewHolder helper;
    private int position;
    private Activity context;
    private FollowVideoList.DataBean.ListsBean item;
    private ScaleAnimation followScaleAnimation;//点赞动画
    private boolean isPrice=false;
    private boolean isPostPlayState=false;//是否已经上传播放次数
    private int itemType;

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    /**
     * 管理滚动条目对象
     * @param callback
     * @param onItemClickListener
     * @param context
     */
    public ComendVideoItem(ItemCallback callback, VideoOnItemClickListener onItemClickListener, TopicClickListener topicClickListener, Activity context, int status, int itemType) {
        this.mItemCallback = callback;
        this.context=context;
        this.onItemClickListener=onItemClickListener;
        this.topicClickListener=topicClickListener;
        this.status=status;
        this.itemType=itemType;
    }


    /**
     * 数据绑定
     * @param holder
     * @param videoListInfo
     * @param position
     */
    public void onBindViewHolder(BaseViewHolder holder, FollowVideoList.DataBean.ListsBean videoListInfo, int position, ScaleAnimation  followScaleAnimation) {
        this.helper=holder;
        this.item=videoListInfo;
        this.position=position;
        this.followScaleAnimation=followScaleAnimation;
        bindData();
    }

    /**
     * 绑定数据
     */
    private void bindData() {
        if(null==item) return;
        final VideoGroupRelativeLayout re_video_group=helper.getView(R.id.re_video_group);
        final XinQuVideoPlayerStandard video_player=helper.getView(R.id.video_player);
        final RelativeLayout re_item_video = helper.getView(R.id.re_item_video);
        //布局的全局宽高变化监听器
        ViewTreeObserver viewTreeObserver = video_player.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                re_item_video.getLayoutParams().height=video_player.getHeight();
                re_video_group.getLayoutParams().height=video_player.getHeight();
                video_player.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        int videoType=0;
        if(TextUtils.isEmpty(item.getType())){
            if(!TextUtils.isEmpty(item.getVideo_width())){
                int videoWidth = Integer.parseInt(item.getVideo_width());
                int videoHeight = Integer.parseInt(item.getVideo_height());
                if(videoWidth==videoHeight){
                    videoType=3;
                }else if(videoWidth>videoHeight){
                    videoType=1;
                }else if(videoWidth<videoHeight){
                    videoType=2;
                }
            }
        }else{
            videoType=Integer.parseInt(item.getType());
        }
        Utils.setVideoRatio(videoType,video_player,re_item_video);
        re_item_video.invalidate();

        re_video_group.setIsPrice(0==item.getIs_interest()?false:true);
        re_video_group.setImageVisibility();
        //设置双击监听
        re_video_group.setOnDoubleClickListener(new VideoGroupRelativeLayout.OnDoubleClickListener() {
            @Override
            public void onDoubleClick() {
                Log.d(TAG, "onDoubleClick: ");
                if(!TextUtils.equals("1",item.getStatus())){
                    String status = item.getStatus();
                    String message="收藏失败";
                    if(TextUtils.equals("0",status)){
                        message="暂时无法收藏，此视频正在审核中..";
                    }else if(TextUtils.equals("2",status)){
                        message="收藏失败，此视频审核未通过!";
                    }else if(TextUtils.equals("3",status)){
                        message="收藏失败，此视频已被原作者删除!";
                    }
                    ToastUtils.showErrorToast(context,null,null,message);
                    return;
                }
                onPrice(helper,item);
            }

            @Override
            public void onClick() {

            }
        });


        helper.setImageResource(R.id.iv_item_follow_icon,0==item.getIs_interest()?R.drawable.iv_follow_selector:R.drawable.iv_icon_follow_true);//设置是否已点赞
        helper.setVisible(R.id.tv_item_type, false);
        helper.setVisible(R.id.re_follow,0==item.getIs_follow()?true:false);

        //我的作品和用户中心隐藏关注按钮
        helper.setVisible(R.id.re_follow,1==status||3==status?false:true);

        //设置视频类型
        if(0==item.getIs_follow()){
            helper.setText(R.id.tv_item_add,"关注");
        }

        String add_time = item.getAdd_time()+"000";

        //设置标题，收藏、分享、评论数量
        helper.setText(R.id.tv_item_follow_count,item.getCollect_times())
                .setText(R.id.tv_item_coment_count,item.getComment_count())
                .setText(R.id.tv_item_share_count,item.getShare_times())
                .setText(R.id.tv_item_user_name,item.getNickname())
                .setText(R.id.tv_item_time, TimeUtils.getTilmNow(Long.parseLong(add_time)))
                .setText(R.id.tv_item_play_count,TextUtils.isEmpty(item.getPlay_times())?"0次播放":item.getPlay_times()+"次播放");

        //设置视频介绍，需要单独处理
        try {
            String decode = URLDecoder.decode(TextUtils.isEmpty(item.getDesp())?"这家伙很懒..没有关于此视频的详情介绍":item.getDesp(), "UTF-8");
            TextView video_item_list_title=helper.getView(R.id.video_item_list_title);
            //设置视频介绍，需要单独处理
            SpannableString weiBoContent = TextViewTopicSpan.getTopicStyleContent(decode, CommonUtils.getColor(R.color.app_text_style),video_item_list_title,topicClickListener,null);
            video_item_list_title.setText(weiBoContent);
            video_item_list_title.setOnClickListener(new OnItemChildViewClickListener(position,item));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //作者封面
        Glide.with(context)
                .load(TextUtils.isEmpty(item.getLogo())?R.drawable.iv_mine:item.getLogo())
                .error(R.drawable.iv_mine)
                .placeholder(R.drawable.iv_mine)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                .centerCrop()//中心点缩放
                .skipMemoryCache(true)//跳过内存缓存
                .transform(new GlideCircleTransform(context))
                .into((ImageView) helper.getView(R.id.iv_item_user_icon));


        //封面
        Glide.with(context)
                .load(TextUtils.isEmpty(item.getCover())?R.drawable.iv_empty_bg_error:item.getCover())
                .crossFade()//渐变
                .thumbnail(0.1f)
                .error(R.drawable.iv_empty_bg_error)
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                .centerCrop()//中心点缩放
                .skipMemoryCache(true)//跳过内存缓存
                .into(video_player.thumbImageView);

        WrapContentGrideView grid_view = helper.getView(R.id.grid_view);
        //设置评论列表适配器
        List<FollowVideoList.DataBean.ListsBean.CommentListBean> itemComment_list = item.getComment_list();
        if(null!=itemComment_list&&itemComment_list.size()>0){
            helper.setVisible(R.id.ll_coment_list,true);
            grid_view.setHaveScrollbar(false);
            VideoListComentAdapter videoListComentAdapter=new VideoListComentAdapter(context,itemComment_list,topicClickListener);
            grid_view.setAdapter(videoListComentAdapter);
            grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemClickListener.onItemChildComent(position,item,false);
                }
            });
        }else{
            helper.setVisible(R.id.ll_coment_list,false);
        }

        //关注
        helper.setOnClickListener(R.id.re_follow,new OnItemChildViewClickListener(position,item));
        //查看用户主页
        helper.setOnClickListener(R.id.iv_item_user_icon,new OnItemChildViewClickListener(position,item));
        //更多评论
        helper.setOnClickListener(R.id.ll_more_coment,new OnItemChildViewClickListener(position,item));
        //打开菜单
        helper.setOnClickListener(R.id.iv_item_menu,new OnItemChildViewClickListener(position,item));
        //点赞,评论，分享
        helper.setOnClickListener(R.id.ll_price,new OnItemChildViewClickListener(position,item));
        helper.setOnClickListener(R.id.ll_coment,new OnItemChildViewClickListener(position,item));
        helper.setOnClickListener(R.id.ll_share,new OnItemChildViewClickListener(position,item));

        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });


        //设置播放器路径信息
        String proxyUrl=item.getPath();
        //设置播放器路径信息
        HttpProxyCacheServer proxy = VideoApplication.getProxy();
        if(null!=proxy){
            proxyUrl= proxy.getProxyUrl(item.getPath());
        }
        video_player.setUp(proxyUrl, XinQuVideoPlayer.SCREEN_WINDOW_LIST,
                ConfigSet.getInstance().isPalyerLoop(),item.getDesp());

        //统计播放
        video_player.setOnPlayerCallBackListener(new XinQuVideoPlayer.OnPlayerCallBackListener() {
            @Override
            public void callBack() {
                onPlayerCount(helper.itemView,item);
            }
        });



        //播放完成调用
        video_player.setOnPlayCompletionListener(new XinQuVideoPlayer.OnPlayCompletionListener() {
            @Override
            public void onCompletion() {

                if(!isPostPlayState){
                    if(null!=video_player){

                        PostPlayStateHanderUtils.postVideoPlayState(item.getVideo_id(), (int)video_player.getDuration(), 1, new OnPostPlayStateListener() {
                            //统计播放成功
                            @Override
                            public void onPostPlayStateComple(String newPlayCount) {
                                Log.d(TAG, "onPostPlayStateComple: 播放完成，统计完成");
                                isPostPlayState=true;
                                TextView tv_item_play_count=helper.getView(R.id.tv_item_play_count);
                                if(null!=tv_item_play_count){
                                    tv_item_play_count.setText(newPlayCount+"次播放");
                                }
                            }
                            //统计播放失败
                            @Override
                            public void onPostPlayStateError() {
                                Log.d(TAG, "onPostPlayStateComple: 播放完成，统计失败");
                                isPostPlayState=false;
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    /**
     * 点击事件的分发
     */
    private class OnItemChildViewClickListener implements View.OnClickListener {

        private final int position;
        private final FollowVideoList.DataBean.ListsBean data;

        public OnItemChildViewClickListener(int position, FollowVideoList.DataBean.ListsBean item) {
            this.position=position;
            this.data=item;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //查看用户主页
                case R.id.iv_item_user_icon:
                    onItemClickListener.onItemVisitOtherHome(position,data);
                    break;
                //更多评论/播放视频
                case R.id.ll_more_coment:
                    onItemClickListener.onItemComent(position,data,false);
                    break;
                //菜单
                case R.id.iv_item_menu:
                    onItemClickListener.onItemMenu(position,data);
                    break;
                //点赞
                case R.id.ll_price:
                    onPrice(helper,data);
                    break;
                //评论
                case R.id.ll_coment:
                    onItemClickListener.onItemComent(position,data,true);
                    break;
                //分享
                case R.id.ll_share:
                    onItemClickListener.onItemShare(position,data);
                    break;
                //关注
                case R.id.re_follow:
                    onFollowUser(helper,data);
                    break;
                //说明文字
                case R.id.video_item_list_title:
                    onItemClickListener.onItemComent(position,data,false);
                    break;

            }
        }
    }



    /**
     * 统计播放次数
     * @param itemView
     * @param item
     */
    private void onPlayerCount(View itemView, FollowVideoList.DataBean.ListsBean item) {

        if(!Utils.isCheckNetwork(context)){
            return;
        }
        final TextView tv_item_play_count = (TextView) itemView.findViewById(R.id.tv_item_play_count);
        Map<String,String> params=new HashMap<>();
        if(null==item){
            return;
        }
        params.put("video_id", item.getVideo_id());
        params.put("imeil", VideoApplication.mUuid);
        params.put("user_id", VideoApplication.getLoginUserID());

        HttpCoreEngin.get(context).rxpost(NetContants.BASE_VIDEO_HOST + "play_record", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                if(TextUtils.isEmpty(data)){
                    return;
                }
                try {
                    JSONObject jsonObject=new JSONObject(data);
                    if(jsonObject.length()>0&&1==jsonObject.getInt("code")&& TextUtils.equals(Constant.PLAY_COUNT_SUCCESS,jsonObject.getString("msg"))){
                        PlayCountInfo playCountInfo = new Gson().fromJson(data, PlayCountInfo.class);
                        PlayCountInfo.DataBean.InfoBean info = playCountInfo.getData().getInfo();
                        tv_item_play_count.setText(info.getPlaty_times()+"次播放");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * 对用户关注，为不影响界面播放，在这里进行
     * @param helper
     * @param data
     */

    private void onFollowUser(final BaseViewHolder helper, FollowVideoList.DataBean.ListsBean data) {

        if(!Utils.isCheckNetwork()){
            ToastUtils.showNetWorkTips(context,"网络设置","没有网络连接");
            return;
        }

        //未登录
        if(null== VideoApplication.getInstance().getUserData()){
            onItemClickListener.onItemFollow(0,null);
        //已登录
        }else{

            if(TextUtils.equals(VideoApplication.getLoginUserID(),data.getUser_id())){
                ToastUtils.showErrorToast(context,null,null,"自己无法关注自己");
                return;
            }
            Map<String,String> params=new HashMap<>();
            params.put("user_id",data.getUser_id());
            params.put("fans_user_id",VideoApplication.getLoginUserID());
            HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "follow", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
                @Override
                public void call(String data) {
                    if(TextUtils.isEmpty(data)){
                        return;
                    }
                    boolean isFollow=false;
                    try {
                        JSONObject jsonObject=new JSONObject(data);
                        if(jsonObject.length()>0&&1==jsonObject.getInt("code")){
                            //关注成功
                            if(TextUtils.equals(Constant.FOLLOW_SUCCESS,jsonObject.getString("msg"))){
                                isFollow=true;
                            }else if(TextUtils.equals(Constant.FOLLOW_UNSUCCESS,jsonObject.getString("msg"))){
                                isFollow=false;
                            }
                            helper.setVisible(R.id.re_follow,isFollow?false:true);
                            VideoApplication.isFolloUser=true;
                            ToastUtils.showFinlishToast(context,null,null,jsonObject.getString("msg"));
                        }else{
                            ToastUtils.showErrorToast(context,null,null,jsonObject.getString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 对视频收藏，为不影响界面播放，在这里进行
     * @param helper
     * @param data
     */
    private void onPrice(final BaseViewHolder helper, FollowVideoList.DataBean.ListsBean data) {

        Logger.d(TAG,"data.getVideo_id()="+data.getVideo_id());
        if(!Utils.isCheckNetwork()){
            ToastUtils.showNetWorkTips(context,"网络设置","没有网络连接");
            return;
        }

        if(2==status){
            //如果是收藏界面，将点赞事件交给外界处理
            onItemClickListener.onItemPrice(position,data);
        //未登录
        }else if(null== VideoApplication.getInstance().getUserData()){
            onItemClickListener.onItemPrice(0,null);
        }else{

            if(!TextUtils.equals("0",data.getIs_private())){
                ToastUtils.showErrorToast(context,null,null,"私密视频无法收藏，请先更改隐私权限");
                return;
            }

            if(!TextUtils.equals("1",data.getStatus())){
                String status = data.getStatus();
                String message="收藏失败";
                if(TextUtils.equals("0",status)){
                    message="暂时无法收藏，此视频正在审核中..";
                }else if(TextUtils.equals("2",status)){
                    message="收藏失败，此视频审核未通过!";
                }
                ToastUtils.showErrorToast(context,null,null,message);
                return;
            }

            //正在点赞
            if(isPrice){
                return;
            }

            isPrice=true;
            Log.d(TAG, "onPrice: ");
            Map<String,String> params=new HashMap<>();
            params.put("user_id", VideoApplication.getLoginUserID());
            params.put("video_id",data.getVideo_id());

            HttpCoreEngin.get(context).rxpost(NetContants.BASE_VIDEO_HOST + "collect", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
                @Override
                public void call(String result) {
                    isPrice=false;
                    if(TextUtils.isEmpty(result)){
                        return;
                    }
                    try {

                        JSONObject jsonObject=new JSONObject(result);
                        if(jsonObject.length()>0&&1==jsonObject.getInt("code")){
                            //收藏成功
                            String  comentCount= new JSONObject(jsonObject.getString("data")).getString("collect_times");

                            VideoGroupRelativeLayout re_video_group = helper.getView(R.id.re_video_group);
                            ImageView iv_item_follow_icon = helper.getView(R.id.iv_item_follow_icon);

                            if(TextUtils.equals(Constant.PRICE_SUCCESS,jsonObject.getString("msg"))){
                                helper.setImageResource(R.id.iv_item_follow_icon,R.drawable.iv_icon_follow_true);
                                re_video_group.startPriceAnimation();
                                iv_item_follow_icon.startAnimation(followScaleAnimation);
                                re_video_group.setIsPrice(true);
                                //取消收藏成功onAnimationFinlish
                            }else if(TextUtils.equals(Constant.PRICE_UNSUCCESS,jsonObject.getString("msg"))){
                                helper.setImageResource(R.id.iv_item_follow_icon,R.drawable.iv_follow_selector);
                                re_video_group.setIsPrice(false);
                            }
                            helper.setText(R.id.tv_item_follow_count,comentCount);

                            if(TextUtils.equals("com.video.newqu.ui.activity.MainActivity", SystemUtils.getTopActivity())){
                                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_MAIN_UPDATA_MINE_WORKS_FOLLOW));
                                //标记刷新
                            }else{
                                VideoApplication.isWorksChange=true;
                            }
                        }else{
                            ToastUtils.showErrorToast(context,null,null,"点赞失败");
                        }
                    } catch (JSONException e) {
                        ToastUtils.showErrorToast(context,null,null,"点赞失败");
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 内部接口回调
     */
    public interface ItemCallback {
        void onDeactivate(View currentView, int position);
        void onActiveViewChangedActive(View newActiveView, int newActiveViewPosition);
    }


    /**
     * 计算VIew的显示高度
     * @param view
     *计算可见性百分比的视图。*注意：可见性不一定要依赖于全景视图的可见性。可以通过计算任何内部视图的可见性来计算。
     */
    @Override
    public int getVisibilityPercents(View view) {
        int percents = 100;
        if(null==view){
            return percents;
        }
        view.getLocalVisibleRect(mCurrentViewRect);
        int height = view.getHeight();

        if(viewIsPartiallyHiddenTop()){
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if(viewIsPartiallyHiddenBottom(height)){
            percents = mCurrentViewRect.bottom * 100 / height;
        }
        return percents;
    }

    /**
     * 当前捆绑的滑动中的View
     * @param newActiveView
     * @param newActiveViewPosition
     */
    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {
        Log.d(TAG, "setActive: newActiveViewPosition="+newActiveViewPosition);
        if(null==newActiveView){
            return;
        }
        mItemCallback.onActiveViewChangedActive(newActiveView, newActiveViewPosition);
        //是否在WIFI网络下自动播放
        if(ConfigSet.getInstance().isWifiAuthPlayer()&&1==Utils.getNetworkType()){
            XinQuVideoPlayerStandard video_player = (XinQuVideoPlayerStandard) newActiveView.findViewById(R.id.video_player);
            if(null==video_player){
                return;
            }
            video_player.startVideo();
        }
    }


    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }

    /**
     * 当前解释滑动捆绑的Item
     * @param currentView
     * @param position
     */
    @Override
    public void deactivate(View currentView, int position) {
        Log.d(TAG, "deactivate: 切换了，原来的"+position);
        //统计播放，未播放完成
        XinQuVideoPlayerStandard video_player = (XinQuVideoPlayerStandard) currentView.findViewById(R.id.video_player);
        final TextView tv_item_play_count = (TextView) currentView.findViewById(R.id.tv_item_play_count);
        if(null!=video_player&&!isPostPlayState){
            Log.d(TAG, "deactivate: 切换了，原来的,未统计"+position);
            PostPlayStateHanderUtils.postVideoPlayState(item.getVideo_id(), (int)video_player.getCurrentPositionWhenPlaying(), 0, new OnPostPlayStateListener() {
                @Override
                public void onPostPlayStateComple(String newPlayCount) {
                    Log.d(TAG, "onPostPlayStateComple: 统计完成");
                    isPostPlayState=true;
                    if(null!=tv_item_play_count){
                        tv_item_play_count.setText(newPlayCount+"次播放");
                    }
                }

                @Override
                public void onPostPlayStateError() {
                    Log.d(TAG, "onPostPlayStateComple: 统计失败");
                    isPostPlayState=false;
                }
            });
        }

        mItemCallback.onDeactivate(currentView,position);
    }
}
