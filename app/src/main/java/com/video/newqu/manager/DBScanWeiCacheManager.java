package com.video.newqu.manager;

import android.content.Context;
import android.util.Log;
import com.video.newqu.bean.WeiXinVideo;
import com.video.newqu.bean.WeiXinVideoDao;
import com.video.newqu.dao.DBBaseDao;
import org.greenrobot.greendao.query.QueryBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/7/10 8:48
 * 扫描的微信信息记录
 */
public class DBScanWeiCacheManager extends DBBaseDao<WeiXinVideo> {

    public DBScanWeiCacheManager(Context context) {
        super(context);
    }

    /**
     * 通过ID查询对象
     * @param id
     * @return
     */
    private WeiXinVideo loadById(long id){

        return daoSession.getWeiXinVideoDao().load(id);
    }

    /**
     * 获取某个对象的主键ID
     * @param weiChactVideoInfo
     * @return
     */
    private long getID(WeiXinVideo weiChactVideoInfo){
        return daoSession.getWeiXinVideoDao().getKey(weiChactVideoInfo);
    }

    /**
     * 通过videoID获取UploadVideoInfo对象
     * @return
     */
    private List<WeiXinVideo> getVideoInfoByName(String ID){
        QueryBuilder queryBuilder =  daoSession.getWeiXinVideoDao().queryBuilder();
        queryBuilder.where(WeiXinVideoDao.Properties.ID.eq(ID));
        int size = queryBuilder.list().size();
        if (size > 0){
            return queryBuilder.list();
        }else{
            return null;
        }
    }

    /**
     * 通过文件MD5 Key 获取所有ID
     * @return
     */
    private List<String> getIdByName(String id){
        List<WeiXinVideo> students = getVideoInfoByName(id);
        List<String> ids = new ArrayList<String>();
        int size = students.size();
        if (size > 0){
            for (int i = 0;i < size;i++){
                ids.add(students.get(i).getID()+"");
            }
            return ids;
        }else{
            return null;
        }
    }



    /**
     * 根据ID进行数据库的删除操作
     * @param id
     */
    public void deleteById(long  id){
        Log.d(TAG, "deleteById: 删除元素");
        daoSession.getWeiXinVideoDao().deleteByKey(id);
    }

    /**
     * 根据ID同步删除数据库操作
     * @param ids
     */
    private void deleteByIds(List<Long> ids){

        daoSession.getWeiXinVideoDao().deleteByKeyInTx(ids);
    }

    /**
     * 根据对象插入一条消息
     * @param weiChactVideoInfo
     */
    public boolean insertNewUploadVideoInfo(WeiXinVideo weiChactVideoInfo) {

        WeiXinVideoDao weiXinVideoDao = daoSession.getWeiXinVideoDao();
        WeiXinVideo unique = weiXinVideoDao.queryBuilder().where(WeiXinVideoDao.Properties.FileName.eq(weiChactVideoInfo.getFileName())).unique();
        if(null==unique){
            Log.d(TAG, "insertNewUploadVideoInfo: 插入一条扫描记录，weiChactVideoInfo.getId()="+weiChactVideoInfo.getID());
            weiXinVideoDao.insertInTx(weiChactVideoInfo);
            return true;
        }else{
            Log.d(TAG, "insertNewUploadVideoInfo: 扫描记录已存在");
           return false;
        }
    }


    /**
     * 获取所有上传列表
     * @return
     */

    public synchronized List<WeiXinVideo> getUploadVideoList(){
        return daoSession.getWeiXinVideoDao().queryBuilder().orderAsc(WeiXinVideoDao.Properties.FileName).list();
    }

    /**
     * 根据对象删除一上传记录
     *
     * @param weiChactVideoInfo
     */
    public void deleteUploadVideoInfo(WeiXinVideo weiChactVideoInfo) {
        daoSession.getWeiXinVideoDao().delete(weiChactVideoInfo);
    }


    /**
     * 更新一条消息
     * @param weiChactVideoInfo
     */
    public synchronized void updateUploadVideoInfo(WeiXinVideo weiChactVideoInfo) {
        Log.d(TAG,"更新="+weiChactVideoInfo.getID());
        daoSession.getWeiXinVideoDao().update(weiChactVideoInfo);
    }

    /**
     * 删除所有消息记录
     */
    public void deteleAllUploadList() {
        daoSession.getWeiXinVideoDao().deleteAll();
    }

    /**
     * 分页加载数据
     * @param page 页数
     * @param count 一页中的条数 加载第几页中的多少条数据
     */
    public List<WeiXinVideo> queryUploadListOfPage(int page, int count) {
        return daoSession.getWeiXinVideoDao().queryBuilder().offset((page-1)*count).limit(count).list();
    }

}
