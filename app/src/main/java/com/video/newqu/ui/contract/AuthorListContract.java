
package com.video.newqu.ui.contract;

import com.video.newqu.base.BaseContract;
import com.video.newqu.bean.FollowVideoList;


/**
 * @time 2017/5/23 10:50
 * @des 用户编辑自己的资料上传
 */
public interface AuthorListContract {

    interface View extends BaseContract.BaseView {
        void showUpLoadVideoList(FollowVideoList data);
        void showReportVideoResult(String data);

    }

    interface Presenter<T> extends BaseContract.BasePresenter<T> {
        void getUpLoadVideoList(String userID, String fansID, String page, String pageSize);
        void onReportVideo(String userID, String videoID);
    }
}
