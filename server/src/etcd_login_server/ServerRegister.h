/*
 * ServerRegister.h
 *
 *  Created on: 2020-11-09
 *      Author: 0voice Darren
 */

#ifndef SERVER_REGISTER_H_
#define SERVER_REGISTER_H_

#include "imconn.h"

typedef struct
{
    string reg_center_addr;
    string service_id;  // 作为key
    string service_dir;
    string host_ip;
    int ttl;
    int client_port;
    int http_port;
    int msg_port;  
}LoginServerRegInfo;

/*
* ip 注册中心的ip地址
* port 注册中心的端口
* key 对应的key
* interval超时间隔
*/
//int init_server_register(string &ip, int port, string &key, uint64_t interval);
int init_server_register(const LoginServerRegInfo *reg_info);

#endif /* SERVER_REGISTER_H_ */
