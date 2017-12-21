
package com.video.newqu.ui.contract;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.video.newqu.base.BaseContract;

/**
 * @time 2017/8/6 17:13
 * @des 获取上传文件的临时Token
 */
public interface STSContract {

    interface View extends BaseContract.BaseView {
        void getOSSFederationToken(OSSFederationToken object);
    }

    interface Presenter<T> extends BaseContract.BasePresenter<T> {
        void getFederationToken(String stsServer);
    }
}
