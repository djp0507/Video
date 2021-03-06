package com.video.newqu.bean;

/**
 * QQ/微信用户资料
 */
public class UserDataInfo {

    private String nickname;
    private String city;
    private String figureurl_qq_2;
    private String gender;
    private String province;
    private String openid;
    private String iemil;
    private String loginType;
    private String imageBG;

    public UserDataInfo(){
        super();
    }
    public UserDataInfo(String nickname, String city, String figureurl_qq_2, String gender, String province, String openid, String iemil, String loginType, String imageBG) {
        this.nickname = nickname;
        this.city = city;
        this.figureurl_qq_2 = figureurl_qq_2;
        this.gender = gender;
        this.province = province;
        this.openid = openid;
        this.iemil = iemil;
        this.loginType = loginType;
        this.imageBG = imageBG;
    }

    public String getImageBG() {
        return imageBG;
    }

    public void setImageBG(String imageBG) {
        this.imageBG = imageBG;
    }


    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFigureurl_qq_2() {
        return figureurl_qq_2;
    }

    public void setFigureurl_qq_2(String figureurl_qq_2) {
        this.figureurl_qq_2 = figureurl_qq_2;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getIemil() {
        return iemil;
    }

    public void setIemil(String iemil) {
        this.iemil = iemil;
    }

    @Override
    public String toString() {
        return "UserDataInfo{" +
                "nickname='" + nickname + '\'' +
                ", city='" + city + '\'' +
                ", figureurl_qq_2='" + figureurl_qq_2 + '\'' +
                ", gender='" + gender + '\'' +
                ", province='" + province + '\'' +
                ", openid='" + openid + '\'' +
                ", iemil='" + iemil + '\'' +
                '}';
    }
}
