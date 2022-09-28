#include "../cetcd.h"
int cetcd_set_response_parse_callback (void *userdata, cetcd_response_node *node, int is_pre_node)
{
    printf("callback: %s\n", is_pre_node ?"pre_node":"node");
    printf("1 Node TTL: %lu\n", node->ttl);
    printf("1 Node ModifiedIndex: %lu\n", node->modified_index);
    printf("1 Node CreatedIndex: %lu\n", node->created_index);
    printf("2 Node Key: %s\n", node->key);
    printf("2 Node Value: %s\n", node->value);
    printf("1 Node Dir: %d\n", node->dir);
    printf("\n");
}

int main(int argc, char *argv[]) {
    cetcd_client cli;
    cetcd_response *resp;
    cetcd_array addrs;

    cetcd_array_init(&addrs, 3);
    cetcd_array_append(&addrs, "http://127.0.0.1:2379");

    cetcd_client_init(&cli, &addrs);
    // key - value
    resp = cetcd_set(&cli, "/login_servers_bk/2", "darren 2", 200);  // 时间
    if(resp->err) {
        printf("error :%d, %s (%s)\n", resp->err->ecode, resp->err->message, resp->err->cause);
    }
    cetcd_response_print(resp);
    printf("\n---------- cetcd_response_parse ---------------------------------\n");
    cetcd_response_parse(resp, cetcd_set_response_parse_callback, NULL);
    cetcd_response_release(resp);

    cetcd_array_destroy(&addrs);
    cetcd_client_destroy(&cli);
    return 0;
}
