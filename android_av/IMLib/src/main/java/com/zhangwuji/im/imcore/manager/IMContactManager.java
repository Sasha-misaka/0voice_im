package com.zhangwuji.im.imcore.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhangwuji.im.DB.DBInterface;
import com.zhangwuji.im.DB.entity.Department;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.imcore.event.UserInfoEvent;
import com.zhangwuji.im.protobuf.helper.ProtoBuf2JavaBean;
import com.zhangwuji.im.protobuf.IMBaseDefine;
import com.zhangwuji.im.protobuf.IMBuddy;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.server.utils.json.JsonMananger;
import com.zhangwuji.im.utils.IMUIHelper;
import com.zhangwuji.im.utils.Logger;
import com.zhangwuji.im.utils.pinyin.PinYin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.greenrobot.eventbus.EventBus;

/**
 * 负责用户信息的请求
 * 为回话页面以及联系人页面提供服务
 * 联系人信息管理reqGetAllUsers
 */
public class IMContactManager extends IMManager {
    private Logger logger = Logger.getLogger(IMContactManager.class);

    // 单例
    private static IMContactManager inst = new IMContactManager();
    public static IMContactManager instance() { return inst; }
    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();

     // 自身状态字段
    private boolean  userDataReady = false;
    private Map<Integer,User> userFriendMap = new ConcurrentHashMap<>();
    private Map<Integer,Department> departmentMap = new ConcurrentHashMap<>();
    private IMAction imAction;

    @Override
    public void doOnStart() {
//        imAction=new IMAction(ctx);
    }

    /**
     * 登陆成功触发
     * auto自动登陆
     * */
    public void onNormalLoginOk(){
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 加载本地DB的状态
     * 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public void onLocalLoginOk(){
        logger.d("contact#loadAllUserInfo");
        List<Department> deptlist = dbInterface.loadAllDept();
        logger.d("contact#loadAllDept dbsuccess");

        List<User> userlist = dbInterface.loadAllUsers();
        logger.d("contact#loadAllUserInfo dbsuccess");

        for(User userInfo:userlist){
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userFriendMap.put(userInfo.getPeerId(),userInfo);
        }

        for(Department deptInfo:deptlist){
            PinYin.getPinYin(deptInfo.getDepartName(), deptInfo.getPinyinElement());
            departmentMap.put(deptInfo.getDepartId(),deptInfo);
        }

        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk(){
     //   reqGetAllUsers(0);
    }

    @Override
    public void reset() {
        userDataReady = false;
        userFriendMap.clear();
    }


    /**
     * @param event
     */
    public void triggerEvent(UserInfoEvent event) {
        //先更新自身的状态
        switch (event){
            case USER_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    /**-----------------------事件驱动---end---------*/
    //每次登录只拉取一次，有特需指定时，再进行拉取
    public void reqGetAllUsers(int lastUpdateTime) {
        logger.i("contact#reqGetAllUsers");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMAllUserReq imAllUserReq  = IMBuddy.IMAllUserReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(imAllUserReq, sid, cid);
    }

    /**
     * yingmu change id from string to int
     * @param imAllUserRsp
     *
     * 1.请求所有用户的信息,总的版本号version
     * 2.匹配总的版本号，返回可能存在变更的
     * 3.选取存在变更的，请求用户详细信息
     * 4.更新DB，保存globalVersion 以及用户的信息
     */
    public void onRepAllUsers(IMBuddy.IMAllUserRsp imAllUserRsp) {
        logger.i("contact#onRepAllUsers");
        int userId = imAllUserRsp.getUserId();
        int lastTime = imAllUserRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count =  imAllUserRsp.getUserListCount();
        logger.i("contact#user cnt:%d", count);
        if(count <=0){
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if(userId != loginId){
            logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return ;
        }

        List<IMBaseDefine.UserInfo> changeList =  imAllUserRsp.getUserListList();
        ArrayList<User> needDb = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:changeList){
            User entity =  ProtoBuf2JavaBean.getUserEntity(userInfo);
            userFriendMap.put(entity.getPeerId(),entity);
            needDb.add(entity);
        }

        dbInterface.batchInsertOrUpdateUser(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    public User findContact(int buddyId) {
        return findContactReq(buddyId,2);
    }
    public User findContact(int buddyId,int flag) {
       return findContactReq(buddyId,flag);
    }
    public User findContactReq(int buddyId,int flag) {
	    if(buddyId==IMLoginManager.instance().getLoginId())
        {
            return IMLoginManager.instance().getLoginInfo();
        }
        if(buddyId > 0){
	       User user=userFriendMap.get(buddyId);//如果常量里没有好友列表。尝试从db里面查找
           if(user==null)
           {
               user=dbInterface.getByLoginId(buddyId);
           }
           if(user==null)
           {
               ArrayList<Integer> userId = new ArrayList<>();
               userId.add(buddyId);
               reqGetDetaillUsers(userId, flag);
           }
           return user;
        }
        return null;
    }

    /**
     * 重新加载好友列表到本地。用于类似新加好友等地方
     */
    public void reLoadFriendsList()
    {
        List<Department> deptlist = dbInterface.loadAllDept();
        logger.d("contact#loadAllDept dbsuccess");

        List<User> userlist = dbInterface.loadAllUsers();
        logger.d("contact#loadAllUserInfo dbsuccess");
        for(User userInfo:userlist){
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userFriendMap.put(userInfo.getPeerId(),userInfo);
        }
        for(Department deptInfo:deptlist){
            PinYin.getPinYin(deptInfo.getDepartName(), deptInfo.getPinyinElement());
            departmentMap.put(deptInfo.getDepartId(),deptInfo);
        }
    }

    /**
     * 请求用户详细信息
     * @param userIds
     */
    public void reqGetDetaillUsers(ArrayList<Integer> userIds, final int flag){
        logger.i("contact#contact#reqGetDetaillUsers");
        if(null == userIds || userIds.size() <=0){
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq  =  IMBuddy.IMUsersInfoReq.newBuilder()
                .setUserId(loginId)
                .addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }

    /**
     * 获取用户详细的信息
     * @param imUsersInfoRsp
     */
    public void  onRepDetailUsers(IMBuddy.IMUsersInfoRsp imUsersInfoRsp){
        int loginId = imUsersInfoRsp.getUserId();
        boolean needEvent = false;
        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp.getUserInfoListList();

        ArrayList<User>  dbNeed = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:userInfoList) {
            User userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            int userId = userEntity.getPeerId();
            if (userFriendMap.containsKey(userId) && userFriendMap.get(userId).equals(userEntity)) {
                //没有必要通知更新
            } else {
                needEvent = true;
                userFriendMap.put(userEntity.getPeerId(), userEntity);
                dbNeed.add(userEntity);
                if (userInfo.getUserId() == loginId) {
                    IMLoginManager.instance().setLoginInfo(userEntity);
                }
            }
        }
        // 负责userMap
        dbInterface.batchInsertOrUpdateUser(dbNeed);

        // 判断有没有必要进行推送
        if(needEvent){
            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        }
    }

    public void reqGetDetaillUsersCallbck(ArrayList<Integer> userIds,final int flag, final BaseAction.ResultCallback<String> callback){
        if(userIds==null || userIds.size()<=0)return;

        String uids="";
        for(int s:userIds)
        {
            uids+=s+",";
        }
        if(uids!="")
        {
            uids=uids.substring(0,uids.length()-1);
        }

        imAction.getUserInfo(uids,new BaseAction.ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                ArrayList<User> needDb = new ArrayList<>();
                try {
                    JSONObject object = JSONObject.parseObject(s);
                    if (object.getIntValue("code") == 200) {

                        JSONObject data = object.getJSONObject("data");
                        //*******解析列表************
                        JSONArray alllist = data.getJSONArray("userinfo");
                        for (int i = 0; i < alllist.size(); i++) {
                            User tempUser = JsonMananger.parseUser(alllist.getJSONObject(i));
                            tempUser.setStatus(flag);
                            needDb.add(tempUser);
                            if(flag==1)
                            {
                                userFriendMap.put(tempUser.getPeerId(),tempUser);
                            }
                        }
                        if (needDb.size() > 0) {
                            dbInterface.batchInsertOrUpdateUser(needDb);
                        }

                    }
                }catch (Exception ee){}
                callback.onSuccess(s);
            }
            @Override
            public void onError(String errString) { callback.onFail(errString);}
        });
    }
    public Department findDepartment(int deptId){
         Department entity = departmentMap.get(deptId);
         return entity;
    }


    public  List<Department>  getDepartmentSortedList(){
        // todo eric efficiency
        List<Department> departmentList = new ArrayList<>(departmentMap.values());
        Collections.sort(departmentList, new Comparator<Department>() {
            @Override
            public int compare(Department entity1, Department entity2) {

                if (entity1.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity1.getDepartName(), entity1.getPinyinElement());
                }
                if (entity2.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity2.getDepartName(), entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);

            }
        });
        return departmentList;
    }

    public  List<User> getContactSortedList() {
        // todo eric efficiency
        List<User> contactList = new ArrayList<>(userFriendMap.values());
        Collections.sort(contactList, new Comparator<User>(){
            @Override
            public int compare(User entity1, User entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if(entity1.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity1.getMainName(),entity1.getPinyinElement());
                    }
                    if(entity2.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }

    // 通讯录中的部门显示 需要根据优先级
    public List<User> getDepartmentTabSortedList() {
        // todo eric efficiency
        List<User> contactList = new ArrayList<>(userFriendMap.values());
        Collections.sort(contactList, new Comparator<User>() {
            @Override
            public int compare(User entity1, User entity2) {
                Department dept1 = departmentMap.get(entity1.getDepartmentId());
                Department dept2 = departmentMap.get(entity2.getDepartmentId());

                if (entity1.getDepartmentId() == entity2.getDepartmentId()) {
                    // start compare
                    if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                        return -1;
                    } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                        // todo eric guess: latter is > 0
                        return 1;
                    } else {
                        if(entity1.getPinyinElement().pinyin==null)
                        {
                            PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                        }
                        if(entity2.getPinyinElement().pinyin==null)
                        {
                            PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                        }
                        return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                    }
                    // end compare
                } else {
                    if(dept1!=null && dept2!=null && dept1.getDepartName()!=null && dept2.getDepartName()!=null)
                    {
                        return dept1.getDepartName().compareToIgnoreCase(dept2.getDepartName());
                    }
                    else
                        return 1;

                }
            }
        });
        return contactList;
    }


    // 确实要将对比的抽离出来 Collections
    public  List<User> getSearchContactList(String key){
       List<User> searchList = new ArrayList<>();
       for(Map.Entry<Integer,User> entry:userFriendMap.entrySet()){
           User user = entry.getValue();
           if (IMUIHelper.handleContactSearch(key, user)) {
               searchList.add(user);
           }
       }
       return searchList;
    }

    public List<Department> getSearchDepartList(String key) {
        List<Department> searchList = new ArrayList<>();
        for(Map.Entry<Integer,Department> entry:departmentMap.entrySet()){
            Department dept = entry.getValue();
            if (IMUIHelper.handleDepartmentSearch(key, dept)) {
                searchList.add(dept);
            }
        }
        return searchList;
    }

    /**------------------------部门相关的协议 start------------------------------*/

    // 更新的方式与userInfo一直，根据时间点
    public void reqGetDepartment(int lastUpdateTime){
        logger.i("contact#reqGetDepartment");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMDepartmentReq imDepartmentReq  = IMBuddy.IMDepartmentReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(imDepartmentReq,sid,cid);
    }

    public void onRepDepartment(IMBuddy.IMDepartmentRsp imDepartmentRsp){
        logger.i("contact#onRepDepartment");
        int userId = imDepartmentRsp.getUserId();
        int lastTime = imDepartmentRsp.getLatestUpdateTime();

        int count =  imDepartmentRsp.getDeptListCount();
        logger.i("contact#department cnt:%d", count);
        // 如果用户找不到depart 那么部门显示未知
        if(count <=0){
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if(userId != loginId){
            logger.e("[fatal error] userId not equels loginId ,cause by onRepDepartment");
            return ;
        }
        List<IMBaseDefine.DepartInfo> changeList =  imDepartmentRsp.getDeptListList();
        ArrayList<Department> needDb = new ArrayList<>();

        for(IMBaseDefine.DepartInfo  departInfo:changeList){
            Department entity =  ProtoBuf2JavaBean.getDepartEntity(departInfo);
            departmentMap.put(entity.getDepartId(),entity);
            needDb.add(entity);
        }
        // 部门信息更新
        dbInterface.batchInsertOrUpdateDepart(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    /**-----------------------实体 get set 定义-----------------------------------*/

    public Map<Integer, User> getUserMap() {
        return userFriendMap;
    }

    public Map<Integer, Department> getDepartmentMap() {
        return departmentMap;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }

}
