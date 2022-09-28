/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：Client.cpp
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年01月20日
*   描    述：
*
================================================================*/
#include "Client.h"
#include "HttpClient.h"
#include "Common.h"
#include "json/json.h"
#include "md5.h"


CClient::CClient(const string& strName, const string& strPass, const string strDomain):
m_strName(strName),
m_strLoginDomain(strDomain),
m_nLastGetUser(0)
{
    m_client_conn = NULL;
	char md5[33];
    md5[32] = '\0';
    MD5_Calculate(strPass.c_str(), strPass.size(), md5);    // 
    m_strPass = md5;
    m_bLogined = false;
}

CClient::~CClient()
{

}

static void TimerCallback(void* callback_data, uint8_t msg, uint32_t handle, void* pParam)
{
    CClient *client = (CClient *)callback_data;
    if (client && client->isLogin()) {
        uint64_t cur_time = get_tick_count();
        if(client->getConn())
            client->getConn()->OnTimer(cur_time);
    }
}

void CClient::onError(uint32_t nSeqNo, uint32_t nCmd,const string& strMsg)
{
    g_imlog.Error("get error:%d, msg:%s", nCmd, strMsg.c_str());
}

void CClient::connect()
{
    CHttpClient httpClient;
    string strUrl = m_strLoginDomain + "/msg_server";   // 连接login服务器
    string strResp;
    CURLcode nRet = httpClient.Get(strUrl, strResp);
    if(nRet != CURLE_OK)
    {
        printf("login falied. access url:%s error\n", strUrl.c_str());
        PROMPTION;
        return;
    }
    printf("CClient::connect %s\n", strResp.c_str());
    Json::Reader reader;
    Json::Value value;
    if(!reader.parse(strResp, value))
    {
        printf("login falied. parse response error:%s\n", strResp.c_str());
        PROMPTION;
        return;
    }
    string strPriorIp, strBackupIp;
    uint16_t nPort;
    try {
        uint32_t nRet = value["code"].asUInt();
        if(nRet != 0)
        {
            string strMsg = value["msg"].asString();
            printf("login falied. errorMsg:%s\n", strMsg.c_str());
            PROMPTION;
            return;
        }
        strPriorIp = value["priorIP"].asString();
        strBackupIp = value["backupIp"].asString();
        nPort = (uint16_t)atoi(value["port"].asString().c_str());
    } 
    catch (std::runtime_error msg) {
        printf("login falied. get json error:%s\n", strResp.c_str());
        PROMPTION;
        return;
    }
    
    m_client_conn = new ClientConn(this);   // 用来连接msg_server
    m_nHandle = m_client_conn->connect(strPriorIp.c_str(), nPort, m_strName, m_strPass);
    if(m_nHandle != INVALID_SOCKET)
    {
        printf("netlib_register_timer TimerCallback 1000ms\n");
        netlib_register_timer(TimerCallback, this, 1000);
    }
    else
    {
        printf("invalid socket handle\n");
    }
}

void CClient::onConnect()
{
    printf("CClient::onConnect \n");
    login(m_strName, m_strPass);
}


void CClient::close()
{
    if(m_client_conn) {
        m_client_conn->Close();
        m_client_conn = NULL;
    }
}

void CClient::onClose()
{
    printf("%s  onClose\n", __FUNCTION__);  
    if(m_client_conn) {
        m_client_conn->Close();
        m_client_conn = NULL;
    }
}

uint32_t CClient::registerUser(const string &strName, const string &strNick)
{
	return m_client_conn->registerUser(strName, strNick);
}
uint32_t CClient::login(const string& strName, const string& strPass)
{
    return m_client_conn->login(strName, strPass);
}

void CClient::onLogin(uint32_t nSeqNo, uint32_t nResultCode, string& strMsg, IM::BaseDefine::UserInfo* pUser)
{
    if(nResultCode != 0)
    {
        printf("login failed.errorCode=%u, msg=%s\n",nResultCode, strMsg.c_str());
        return;
    }
    if(pUser)
    {
        m_cSelfInfo = *pUser;
        m_bLogined = true;
        printf("%s %s login success\n", __FUNCTION__, pUser->user_real_name().c_str());      // 提示登录成功
    }
    else
    {
        printf("pUser is null\n");
    }
}

uint32_t CClient::getChangedUser()
{
    uint32_t nUserId = m_cSelfInfo.user_id();
    return m_client_conn->getUser(nUserId, m_nLastGetUser);
}

void CClient::onGetChangedUser(uint32_t nSeqNo, const list<IM::BaseDefine::UserInfo> &lsUser)
{
    
    for(auto it=lsUser.begin(); it!=lsUser.end(); ++it)
    {
        IM::BaseDefine::UserInfo* pUserInfo = new IM::BaseDefine::UserInfo();
        *pUserInfo = *it;
        uint32_t nUserId = pUserInfo->user_id();
        string strNick = pUserInfo->user_nick_name();
        if(it->status() != 3)
        {
            auto it1 = m_mapId2UserInfo.find(nUserId);
            if(it1 == m_mapId2UserInfo.end())
            {
                m_mapId2UserInfo.insert(pair<uint32_t, IM::BaseDefine::UserInfo*>(nUserId, pUserInfo));
            }
            else
            {
                delete it1->second;
                m_mapId2UserInfo[nUserId] = pUserInfo;
            }
            
            auto it2 = m_mapNick2UserInfo.find(strNick);
            if(it2 == m_mapNick2UserInfo.end())
            {
                m_mapNick2UserInfo.insert(pair<string, IM::BaseDefine::UserInfo*>(strNick, pUserInfo));
            }
            else
            {
                delete it1->second;
                m_mapNick2UserInfo[strNick] = pUserInfo;
            }
        }
    }
}

uint32_t CClient::getUserInfo(list<uint32_t>& lsUserId)
{
    uint32_t nUserId = m_cSelfInfo.user_id();
    return m_client_conn->getUserInfo(nUserId, lsUserId);
}

void CClient::onGetUserInfo(uint32_t nSeqNo, const list<IM::BaseDefine::UserInfo> &lsUser)
{
    printf("%s  onGetUserInfo\n", __FUNCTION__);  

    for(auto iter = lsUser.begin(); iter != lsUser.end() ;iter++)
    {
        printf("name:%s nick:%s\n", iter->user_real_name().c_str(), iter->user_nick_name().c_str());

    }
}

uint32_t CClient::sendMsg(uint32_t nToId, IM::BaseDefine::MsgType nType, const string &strMsg)
{
    uint32_t nFromId = m_cSelfInfo.user_id();
    return m_client_conn->sendMessage(nFromId, nToId, nType, strMsg);
}

void CClient::onSendMsg(uint32_t nSeqNo, uint32_t nSendId, uint32_t nRecvId, IM::BaseDefine::SessionType nType, uint32_t nMsgId)
{
    printf("send msg succes. seqNo:%u, msgId:%u\n", nSeqNo, nMsgId);
}


uint32_t CClient::getUnreadMsgCnt()
{
    uint32_t nUserId = m_cSelfInfo.user_id();
    return m_client_conn->getUnreadMsgCnt(nUserId);
}

void CClient::onGetUnreadMsgCnt(uint32_t nSeqNo, uint32_t nUserId, uint32_t nTotalCnt, const list<IM::BaseDefine::UnreadInfo>& lsUnreadCnt)
{
    printf("%s  nSeqNo:%d, nUserId:%d, nTotalCnt:%d\n", __FUNCTION__, nSeqNo, nUserId, nTotalCnt);  
}

uint32_t CClient::getRecentSession()
{
    uint32_t nUserId = m_cSelfInfo.user_id();
    return m_client_conn->getRecentSession(nUserId, m_nLastGetSession);
}

void CClient::onGetRecentSession(uint32_t nSeqNo, uint32_t nUserId, const list<IM::BaseDefine::ContactSessionInfo> &lsSession)
{
    printf("%s nSeqNo:%u, nUserId:%u, Sessions:%d\n", __FUNCTION__, nSeqNo, nUserId, (int)lsSession.size());
}

uint32_t CClient::getMsgList(IM::BaseDefine::SessionType nType, uint32_t nPeerId, uint32_t nMsgId, uint32_t nMsgCnt)
{
    uint32_t nUserId = m_cSelfInfo.user_id();
    return m_client_conn->getMsgList(nUserId, nType, nPeerId, nMsgId, nMsgCnt);
}

void CClient::onGetMsgList(uint32_t nSeqNo, uint32_t nUserId, uint32_t nPeerId, IM::BaseDefine::SessionType nType, uint32_t nMsgId, uint32_t nMsgCnt, const list<IM::BaseDefine::MsgInfo> &lsMsg)
{
    printf("%s nSeqNo:%u, nUserId:%u, nPeerId:%u, nMsgId:%d, nMsgType:%d, nMsgCnt:%d\n", __FUNCTION__, nSeqNo, nUserId, nPeerId, nMsgId, nType, nMsgCnt);
}

void CClient::onRecvMsg(uint32_t nSeqNo, uint32_t nFromId, uint32_t nToId, uint32_t nMsgId, uint32_t nCreateTime, IM::BaseDefine::MsgType nMsgType, const string &strMsgData)
{
    printf("%s nSeqNo:%u, nFromId:%u, nToId:%u, nMsgId:%d, nMsgType:%d, Msg:%s\n", __FUNCTION__, nSeqNo, nFromId, nToId, nMsgId, nMsgType, strMsgData.c_str());
}
