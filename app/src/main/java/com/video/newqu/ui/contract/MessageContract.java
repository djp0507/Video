
package com.video.newqu.ui.contract;

import com.video.newqu.base.BaseContract;
import com.video.newqu.bean.MessageInfo;
import com.video.newqu.bean.NotifactionMessageInfo;

import java.util.List;


/**
 * @time 2017/5/23 10:50
 * @des 用户消息
 */
public interface MessageContract {

    interface View extends BaseContract.BaseView {
        void showMessageInfo(List<NotifactionMessageInfo> data);
        void getMessageError(String data);
        void getMessageEmpty(String data);
    }

    interface Presenter<T> extends BaseContract.BasePresenter<T> {
        void getMessageList(String userID, String page, String pageSize);
    }
}
