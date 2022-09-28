/*
 * ServerDiscovery.cpp
 *
 *  Created on: 2021-09-09
 *      Author: 0voice Darren
 */

#include <vector>
#include "LoginServerDiscovery.h"
#include "public_define.h"
#include "HttpClient.h" // http客户端
#include "LoginServConn.h"
#include "json/json.h" // json文件
#include "cetcd.h"

using namespace IM::BaseDefine;

static LoginServerRegisterCenter g_reg_center;
uint32_t login_server_count = 0;
std::vector<serv_info_t> g_login_server_vector;
// 在原来的代码基础上改动极大，所以这里检测到服务后，后续不再更新服务地址。
bool g_got_login_server = false;
string g_msg_ip1;
string g_msg_ip2;
int g_msg_port;
int g_msg_max_conn;
// 开源项目地址：https://github.com/shafreeck/cetcd
int login_server_response_parse_callback(void *userdata, cetcd_response_node *node, int is_pre_node)
{
    printf("callback: %s\n", is_pre_node ? "pre_node" : "node");
    printf("1 Node TTL: %lu\n", node->ttl);
    printf("1 Node ModifiedIndex: %lu\n", node->modified_index);
    printf("1 Node CreatedIndex: %lu\n", node->created_index);
    printf("2 Node Key: %s\n", node->key);
    printf("2 Node Value: %s\n", node->value);
    printf("2 Node Dir: %d\n", node->dir);
    printf("\n");

    if (node->dir == 0)
    {
        Json::Reader reader;
        Json::Value value;

        if (!reader.parse(node->value, value))
        {
            printf("json parse failed, node->value=%s ", node->value);
            return 0;
        }

        serv_info_t server;
        server.server_ip = value["ip"].asString();
        int server_port = value["msg_port"].asInt();
        server.server_port = server_port;
        printf("loginserver -> ip_addr:%s, msg_port:%d\n", server.server_ip.c_str(), server.server_port);
        if (!g_got_login_server)
        {
            // 在原来的代码基础上改动极大，所以这里检测到服务后，后续不再更新服务地址。
            // 这里只有没有发现loginserver的情况下才去获取
            g_login_server_vector.push_back(server);
        }
    }
    // 检测是否在连接里面
    // bool found = false;
    // for(int i = 0; i < g_login_server_vector.size(); i++) {
    //     if((g_login_server_vector[i].server_ip ==  server.server_ip)
    //         && (g_login_server_vector[i].server_port ==  server.server_ip) ) {
    //         found = true;
    //         break;
    //     }
    // }
    // // 如果没有找到则新增加连接
    // if(!found) {
    //     g_login_server_vector.push(server);
    // }
    return 0;
}
void login_server_discovery_timer_callback(void *callback_data, uint8_t msg, uint32_t handle, void *pParam)
{

    cetcd_client cli;
    cetcd_response *resp;
    cetcd_array addrs;

    cetcd_array_init(&addrs, 3);
    cetcd_array_append(&addrs, (void *)g_reg_center.reg_center_addr.c_str());

    cetcd_client_init(&cli, &addrs);

    resp = cetcd_lsdir(&cli, g_reg_center.service_dir.c_str(), 1, 1);
    if (resp->err)
    {
        printf("error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
    }
    cetcd_response_print(resp);
    printf("\n---------- cetcd_response_parse ---------------------------------\n");
    cetcd_response_parse(resp, login_server_response_parse_callback, NULL);
    // login_server_response_parse_callback end 
    int server_count = g_login_server_vector.size();
    if (!g_got_login_server && server_count > 0)
    {
        serv_info_t *server_list = new serv_info_t[server_count];
        for (uint32_t i = 0; i < server_count; i++)
        {
            server_list[i].server_ip = g_login_server_vector[i].server_ip;
            server_list[i].server_port = g_login_server_vector[i].server_port;
        }
        printf("login_server_discovery_timer_callback init_login_serv_conn into\n");
        init_login_serv_conn(server_list, server_count, g_msg_ip1.c_str(), g_msg_ip2.c_str(), g_msg_port, g_msg_max_conn);
        g_got_login_server = true;
    }

    cetcd_response_release(resp);

    cetcd_array_destroy(&addrs);
    cetcd_client_destroy(&cli);
}
// msg_ip1  msg_ip2 连接上loginserver要发送给 loginserver
int login_server_discovery_init(const LoginServerRegisterCenter *reg_center, string msg_ip1,
                                string msg_ip2, int msg_port, int msg_max_conn)
{
    g_reg_center.reg_center_addr = reg_center->reg_center_addr;
    g_reg_center.service_dir = reg_center->service_dir;
    g_reg_center.host_ip = reg_center->host_ip;
    g_reg_center.ttl = reg_center->ttl;
    g_msg_ip1 = msg_ip1;
    g_msg_ip2 = msg_ip2;
    g_msg_port = msg_port;
    g_msg_max_conn = msg_max_conn;
    // 第一步，检测service dir是否存在，如果不存在则创建

    cetcd_client cli;
    cetcd_response *resp;
    cetcd_array addrs;

    printf("etcd addr: %s\n", g_reg_center.reg_center_addr.c_str());

    cetcd_array_init(&addrs, 3);
    cetcd_array_append(&addrs, (void *)g_reg_center.reg_center_addr.c_str());

    cetcd_client_init(&cli, &addrs);

    resp = cetcd_lsdir(&cli, g_reg_center.service_dir.c_str(), 1, 1);
    if (resp->err)
    {
        printf("error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
    }
    cetcd_response_print(resp);
    printf("\n---------- cetcd_response_parse ---------------------------------\n");
    //1.解析 node节点 
    cetcd_response_parse(resp, login_server_response_parse_callback, NULL);
    //2 .检测连接，然后创建msgserver -> loginserver
    int server_count = g_login_server_vector.size();
    if (server_count > 0)
    {
        serv_info_t *server_list = new serv_info_t[server_count];
        for (uint32_t i = 0; i < server_count; i++)
        {
            server_list[i].server_ip = g_login_server_vector[i].server_ip;
            server_list[i].server_port = g_login_server_vector[i].server_port;
        }
        printf("login_server_discovery_init init_login_serv_conn into\n");
        init_login_serv_conn(server_list, server_count, g_msg_ip1.c_str(), g_msg_ip2.c_str(), g_msg_port, g_msg_max_conn);
        g_got_login_server = true;
    }

    cetcd_response_release(resp);

    cetcd_array_destroy(&addrs);
    cetcd_client_destroy(&cli);

    netlib_register_timer(login_server_discovery_timer_callback, NULL, g_reg_center.ttl);
}
