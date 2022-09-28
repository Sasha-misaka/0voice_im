/*
 * ServerDiscovery.h
 *
 *  Created on: 2021-09-09
 *      Author: 0voice Darren
 */

#ifndef _LOGIN_SERVER_DISCOVERY_H_
#define _LOGIN_SERVER_DISCOVERY_H_

#include "imconn.h"

typedef struct
{
    string reg_center_addr;
    string service_dir;
    string host_ip;
    int ttl;
}LoginServerRegisterCenter;


typedef struct
{
    string ip_addr;
    int client_port;
    int http_port;
    int msg_port;  
}LoginServerRegInfoItem;

int login_server_discovery_init(const LoginServerRegisterCenter *reg_center, 
        string msg_ip1, string msg_ip2, int msg_port, int msg_max_conn);
#endif /* _LOGIN_SERVER_DISCOVERY_H_ */

