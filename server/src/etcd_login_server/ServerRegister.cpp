/*
 * ServerRegister.cpp
 *
 *  Created on: 2020-11-09
 *      Author: 0voice Darren
 */


#include "ServerRegister.h"
#include "public_define.h"
#include "HttpClient.h"     // http客户端
#include "json/json.h"      // json文件
#include "cetcd.h"


using namespace IM::BaseDefine;


static LoginServerRegInfo g_reg_info;

// 开源项目地址：https://github.com/shafreeck/cetcd

void server_register_timer_callback(void* callback_data, uint8_t msg, uint32_t handle, void* pParam)
{
     cetcd_client cli;
    cetcd_response *resp;
    cetcd_array addrs;

    cetcd_array_init(&addrs, 3);
    cetcd_array_append(&addrs, (void *)g_reg_info.reg_center_addr.c_str());

    cetcd_client_init(&cli, &addrs);

    string strKey = g_reg_info.service_dir + "/" + g_reg_info.service_id;
    Json::Value root;
    root["ip"] = g_reg_info.host_ip;
    root["client_port"] = g_reg_info.client_port;
	root["msg_port"] = g_reg_info.msg_port;
    string strValue= root.toStyledString();
    
    int ttl = (g_reg_info.ttl + 1000)/1000;
    resp = cetcd_set(&cli, strKey.c_str(),  strValue.c_str(), ttl);
    if(resp->err) {
        printf("cetcd_set error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
        log("cetcd_set error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
        cetcd_response_release(resp);
        cetcd_array_destroy(&addrs);
        cetcd_client_destroy(&cli);
        return;
    }
    cetcd_response_print(resp);
    
    cetcd_response_release(resp);

    cetcd_array_destroy(&addrs);
    cetcd_client_destroy(&cli);
}


int init_server_register(const LoginServerRegInfo *reg_info)
{
    if(reg_info->ttl < 1000)
    {
        return -1;
    }

    g_reg_info.reg_center_addr    = reg_info->reg_center_addr;
    g_reg_info.service_id       = reg_info->service_id;
    g_reg_info.service_dir      = reg_info->service_dir;
    g_reg_info.host_ip          = reg_info->host_ip;
    g_reg_info.ttl              = reg_info->ttl;
    g_reg_info.client_port      = reg_info->client_port;
    g_reg_info.http_port        = reg_info->http_port;
    g_reg_info.msg_port         = reg_info->msg_port;
    // 第一步，检测service dir是否存在，如果不存在则创建

    cetcd_client cli;
    cetcd_response *resp;
    cetcd_array addrs;

    cetcd_array_init(&addrs, 3);
    cetcd_array_append(&addrs, (void *)g_reg_info.reg_center_addr.c_str());

    cetcd_client_init(&cli, &addrs);

    resp = cetcd_mkdir(&cli, g_reg_info.service_dir.c_str(), 0);
    if(resp->err) {
        printf("error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
    }
    cetcd_response_print(resp);

    string strKey = g_reg_info.service_dir + "/" + g_reg_info.service_id;
    Json::Value root;
    root["ip"] = g_reg_info.host_ip;
    root["client_port"] = g_reg_info.client_port;
	root["msg_port"] = g_reg_info.msg_port;
    string strValue= root.toStyledString();
    
    int ttl = (g_reg_info.ttl + 1000)/1000;
    resp = cetcd_set(&cli, strKey.c_str(),  strValue.c_str(), ttl);
    if(resp->err) {
        printf("cetcd_set error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
        log("cetcd_set error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
        cetcd_response_release(resp);
        cetcd_array_destroy(&addrs);
        cetcd_client_destroy(&cli);
        return -1;
    }
    cetcd_response_print(resp);
    
    cetcd_response_release(resp);

    cetcd_array_destroy(&addrs);
    cetcd_client_destroy(&cli);
    

	netlib_register_timer(server_register_timer_callback, NULL, g_reg_info.ttl);
}

