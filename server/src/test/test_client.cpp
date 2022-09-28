/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：test_client.cpp
 *   创 建 者：Zhang Yuanhao
  *   创建日期: 2014年12月30日
 *   修 改 者：Darren Liao
 *   邮    箱： 326873713@qq.com
 *   修改日期：2022年6月29日
 *   描    述：测试即时通讯项目
 *
 ================================================================*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <vector>
#include <iostream>
#include "ClientConn.h"
#include "netlib.h"
#include "TokenValidator.h"
#include "Thread.h"
#include "IM.BaseDefine.pb.h"
#include "IM.Buddy.pb.h"
#include "Common.h"
#include "Client.h"
using namespace std;

#define MAX_LINE_LEN 1024
string g_login_domain = "http://1.15.184.62:8080";
string g_cmd_string[10];
int g_cmd_num;
CClient *g_pClient = NULL;

typedef struct
{
    char **str; // the PChar of string array
    int num;    // the number of string
} IString;

int Split(const char *src, const char *delim, IString *istr) // split buf
{
    int i = 0;
    char *str = NULL, *p = NULL;

    (*istr).num = 1;
    str = (char *)calloc(strlen(src) + 1, sizeof(char));
    if (str == NULL)
        return 0;
    (*istr).str = (char **)calloc(1, sizeof(char *));
    if ((*istr).str == NULL)
        return 0;
    strcpy(str, src);

    p = strtok(str, delim);
    (*istr).str[0] = (char *)calloc(strlen(p) + 1, sizeof(char));
    if ((*istr).str[0] == NULL)
        return 0;
    strcpy((*istr).str[0], p);
    for (i = 1; p = strtok(NULL, delim); i++)
    {
        (*istr).num++;
        (*istr).str = (char **)realloc((*istr).str, (i + 1) * sizeof(char *));
        if ((*istr).str == NULL)
            return 0;
        (*istr).str[i] = (char *)calloc(strlen(p) + 1, sizeof(char));
        if ((*istr).str[0] == NULL)
            return 0;
        strcpy((*istr).str[i], p);
    }
    free(str);
    str = p = NULL;

    return 1;
}
void split_cmd(char *buf)
{
    int len = strlen(buf);
    string element;

    g_cmd_num = 0;
    for (int i = 0; i < len; i++)
    {
        if (buf[i] == ' ' || buf[i] == '\t')
        {
            if (!element.empty())
            {
                g_cmd_string[g_cmd_num++] = element;
                element.clear();
            }
        }
        else
        {
            element += buf[i];
        }
    }

    // put the last one
    if (!element.empty())
    {
        g_cmd_string[g_cmd_num++] = element;
    }
}

void print_help()
{
    printf("Usage:\n");
    printf("login user_name user_pass\n");
    printf("getuserinfo id1,id2,id3\n");
    printf("send toId msg\n");
    printf("recentsess\n");
    printf("unreadcnt\n");
    printf("close\n");
    printf("quit\n");
}

void doLogin(const string &strName, const string &strPass)
{
    try
    {
        g_pClient = new CClient(strName, strPass, g_login_domain); // 只能对应一个客户端
    }
    catch (...)
    {
        printf("get error while alloc memory\n");
        PROMPTION;
        return;
    }
    g_pClient->connect();
}
void exec_cmd()
{
    if (g_cmd_num == 0)
    {
        return;
    }

    if (g_pClient && !g_pClient->isLogin() && g_cmd_string[0] != "quit") // 连接超时的时候需要重新登录
    {
        printf("wait the login result\n");
        return;
    }

    if ((g_cmd_string[0] != "login" && g_cmd_string[0] != "quit") && (NULL == g_pClient || !g_pClient->isLogin()))
    {
        printf("please login.\n");
        printf("like:login darren 123456\n");
        printf("like:login king 123456\n");
        printf("like:login mark 123456\n");
        return;
    }

    if (g_cmd_string[0] == "login")
    {
        if (NULL != g_pClient)
        {
            g_pClient->close();
            delete g_pClient;
            g_pClient = NULL;
        }
        if (g_cmd_num == 3)
        {
            doLogin(g_cmd_string[1], g_cmd_string[2]); // login king 123456
        }
        else
        {
            print_help();
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "send") == 0)
    {
        if (g_cmd_num == 4)
        {
            g_pClient->sendMsg(atoi(g_cmd_string[1].c_str()), IM::BaseDefine::MsgType(atoi(g_cmd_string[2].c_str())), g_cmd_string[3]);
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "register") == 0)
    {
        if (g_cmd_num == 3)
        {
            g_pClient->registerUser(g_cmd_string[1].c_str(), g_cmd_string[2].c_str());
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "getuserinfo") == 0)
    {
        if (g_cmd_num == 2)
        {
            list<uint32_t> lsUserId; // 用户id列表用
            IString istr;
            if (Split(g_cmd_string[1].c_str(), ",", &istr)) // 用户id列表用,隔开
            {
                for (int i = 0; i < istr.num; i++)
                {
                    printf("%d\n", atoi(istr.str[i]));
                    lsUserId.push_back(atoi(istr.str[i]));
                }
                // when you don't ues it,you must to free memory.
                for (int i = 0; i < istr.num; i++)
                    free(istr.str[i]);
                free(istr.str);
                g_pClient->getUserInfo(lsUserId);
            }
            else
            {
                printf("memory allocation failure!\n");
            }
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "recentsess") == 0)
    {
        if (g_cmd_num == 1)
        {
            g_pClient->getRecentSession();
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "unreadcnt") == 0)
    {
        if (g_cmd_num == 1)
        {
            g_pClient->getUnreadMsgCnt();
        }
    }
    else if (strcmp(g_cmd_string[0].c_str(), "close") == 0)
    {
        g_pClient->close();
    }
    else if (strcmp(g_cmd_string[0].c_str(), "quit") == 0)
    {
        exit(0);
    }
    else if (strcmp(g_cmd_string[0].c_str(), "list") == 0)
    {
        printf("+---------------------+\n");
        printf("|        用户名        |\n");
        printf("+---------------------+\n");
        CMapNick2User_t mapUser = g_pClient->getNick2UserMap();
        auto it = mapUser.begin();
        for (; it != mapUser.end(); ++it)
        {
            uint32_t nLen = 21 - it->first.length();
            printf("|");
            for (uint32_t i = 0; i < nLen / 2; ++it)
            {
                printf(" ");
            }
            printf("%s", it->first.c_str());
            for (uint32_t i = 0; i < nLen / 2; ++it)
            {
                printf(" ");
            }
            printf("|\n");
            printf("+---------------------+\n");
        }
    }
    else
    {
        print_help();
    }
}

class CmdThread : public CThread
{
public:
    void OnThreadRun()
    {
        while (true)
        {
            fprintf(stderr, "%s", PROMPT); // print to error will not buffer the printed message

            if (fgets(m_buf, MAX_LINE_LEN - 1, stdin) == NULL)
            {
                fprintf(stderr, "fgets failed: %d\n", errno);
                continue;
            }

            m_buf[strlen(m_buf) - 1] = '\0'; // remove newline character

            split_cmd(m_buf); // 分隔命令

            exec_cmd();
        }
    }

private:
    char m_buf[MAX_LINE_LEN];
};

CmdThread g_cmd_thread;

int main(int argc, char *argv[])
{
    //    play("message.wav");
    g_cmd_thread.StartThread(); // 起了一个新线程

    signal(SIGPIPE, SIG_IGN);

    int ret = netlib_init();

    if (ret == NETLIB_ERROR)
        return ret;

    netlib_eventloop(); // 客户端 netlib只负责网络数据的收发

    return 0;
}
