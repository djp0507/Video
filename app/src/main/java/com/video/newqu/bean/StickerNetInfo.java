package com.video.newqu.bean;

import com.video.newqu.comadapter.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/11.
 * 贴纸Info
 */

public class StickerNetInfo implements Serializable{

    /**
     * code : 1
     * data : [{"id":"7270","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-11/59b64640e10e7.jpg","desp":null,"type_id":"157","sort":"500","add_time":"1505117760","add_date":"20170911","down_num":null},{"id":"7271","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1d6b93a.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177117","add_date":"20170912","down_num":null},{"id":"7272","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1d94270.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177117","add_date":"20170912","down_num":null},{"id":"7273","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1db710e.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177117","add_date":"20170912","down_num":null},{"id":"7274","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1ddf207.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177117","add_date":"20170912","down_num":null},{"id":"7275","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1e079ff.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177118","add_date":"20170912","down_num":null},{"id":"7276","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1e27c4d.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177118","add_date":"20170912","down_num":null},{"id":"7277","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1e3e357.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177118","add_date":"20170912","down_num":null},{"id":"7278","title":null,"src":"http://sc.wk2.com/upload/157/2017-09-12/59b72e1e5a7d4.png","desp":null,"type_id":"157","sort":"500","add_time":"1505177118","add_date":"20170912","down_num":null}]
     */

    private int code;

    /**
     * id : 7270
     * title : null
     * src : http://sc.wk2.com/upload/157/2017-09-11/59b64640e10e7.jpg
     * desp : null
     * type_id : 157
     * sort : 500
     * add_time : 1505117760
     * add_date : 20170911
     * down_num : null
     */

    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean implements Serializable, MultiItemEntity {

        private String id;
        private String title;
        private String src;
        private String desp;
        private String type_id;
        private String sort;
        private String add_time;
        private String add_date;
        private String down_num;
        private boolean isSelector;
        private boolean isDownloading;
        private int itemType;

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        private String cover;



        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getDesp() {
            return desp;
        }

        public void setDesp(String desp) {
            this.desp = desp;
        }

        public String getType_id() {
            return type_id;
        }

        public void setType_id(String type_id) {
            this.type_id = type_id;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public String getAdd_time() {
            return add_time;
        }

        public void setAdd_time(String add_time) {
            this.add_time = add_time;
        }

        public String getAdd_date() {
            return add_date;
        }

        public void setAdd_date(String add_date) {
            this.add_date = add_date;
        }

        public String getDown_num() {
            return down_num;
        }

        public void setDown_num(String down_num) {
            this.down_num = down_num;
        }

        public boolean isSelector() {
            return isSelector;
        }

        public void setSelector(boolean selector) {
            isSelector = selector;
        }

        public boolean isDownloading() {
            return isDownloading;
        }

        public void setDownloading(boolean downloading) {
            isDownloading = downloading;
        }
    }
}
