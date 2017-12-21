package com.video.newqu.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/21.
 */

public class NetMessageInfo {

    /**
     * add_time : 1513158550
     * collect_times : 0
     * comment_count : 35
     * comment_list : [{"add_time":"1513134365","comment":"颜值爆表","id":"3869","logo":"http://app.nq6.com/Upload/Picture/2017-10-30/59f6cd0069ac4.jpg","nickname":"小苒","status":"0","to_nickname":"","to_user_id":"0","user_id":"1071429","video_id":"8818"},{"add_time":"1513112594","comment":"老家哪儿的��","id":"3880","logo":"http://app.nq6.com/Upload/Picture/2017-11-06/5a000f71a3f84.jpg","nickname":"甜心萝莉酱","status":"0","to_nickname":"","to_user_id":"0","user_id":"1072835","video_id":"8818"}]
     * comment_times : 35
     * cover : http://video.nq6.com/user-dir/2ijxsrpD6h.jpg
     * desp : 高清饭拍-女团 #美女# #女神# #性感# #热舞# #韩国女团#
     * download_permiss : 0
     * grade : 5
     * id : 8818
     * is_private : 0
     * logo : http://app.nq6.com/Upload/Picture/2017-11-30/5a1faa798841e.jpg
     * nickname : ৡৢﺴﻬ幻ৢ夢ﺴﻬৡ
     * path : http://video.nq6.com/user-dir/2ijxsrpD6h.mp4
     * play_times : 1794
     * share_times : 0
     * status : 1
     * type : 2
     * user_id : 1076521
     * video_height : 0
     * video_id : 8818
     * video_width : 0
     */

    private String add_time;
    private String collect_times;
    private String comment_count;
    private String comment_times;
    private String cover;
    private String desp;
    private String download_permiss;
    private String grade;
    private String id;
    private String is_private;
    private String logo;
    private String nickname;
    private String path;
    private int play_times;
    private int share_times;
    private String status;
    private String type;
    private String user_id;
    private String video_height;
    private String video_id;
    private String video_width;
    /**
     * add_time : 1513134365
     * comment : 颜值爆表
     * id : 3869
     * logo : http://app.nq6.com/Upload/Picture/2017-10-30/59f6cd0069ac4.jpg
     * nickname : 小苒
     * status : 0
     * to_nickname :
     * to_user_id : 0
     * user_id : 1071429
     * video_id : 8818
     */

    private List<CommentListBean> comment_list;

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public String getCollect_times() {
        return collect_times;
    }

    public void setCollect_times(String collect_times) {
        this.collect_times = collect_times;
    }

    public String getComment_count() {
        return comment_count;
    }

    public void setComment_count(String comment_count) {
        this.comment_count = comment_count;
    }

    public String getComment_times() {
        return comment_times;
    }

    public void setComment_times(String comment_times) {
        this.comment_times = comment_times;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }

    public String getDownload_permiss() {
        return download_permiss;
    }

    public void setDownload_permiss(String download_permiss) {
        this.download_permiss = download_permiss;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIs_private() {
        return is_private;
    }

    public void setIs_private(String is_private) {
        this.is_private = is_private;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPlay_times() {
        return play_times;
    }

    public void setPlay_times(int play_times) {
        this.play_times = play_times;
    }

    public int getShare_times() {
        return share_times;
    }

    public void setShare_times(int share_times) {
        this.share_times = share_times;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getVideo_height() {
        return video_height;
    }

    public void setVideo_height(String video_height) {
        this.video_height = video_height;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getVideo_width() {
        return video_width;
    }

    public void setVideo_width(String video_width) {
        this.video_width = video_width;
    }

    public List<CommentListBean> getComment_list() {
        return comment_list;
    }

    public void setComment_list(List<CommentListBean> comment_list) {
        this.comment_list = comment_list;
    }

    public static class CommentListBean {
        private String add_time;
        private String comment;
        private String id;
        private String logo;
        private String nickname;
        private String status;
        private String to_nickname;
        private String to_user_id;
        private String user_id;
        private String video_id;

        public String getAdd_time() {
            return add_time;
        }

        public void setAdd_time(String add_time) {
            this.add_time = add_time;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTo_nickname() {
            return to_nickname;
        }

        public void setTo_nickname(String to_nickname) {
            this.to_nickname = to_nickname;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public void setTo_user_id(String to_user_id) {
            this.to_user_id = to_user_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getVideo_id() {
            return video_id;
        }

        public void setVideo_id(String video_id) {
            this.video_id = video_id;
        }
    }
}
