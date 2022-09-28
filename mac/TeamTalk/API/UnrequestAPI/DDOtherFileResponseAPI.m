//
//  DDOtherFileResponse.m
//  Duoduo
//
//  Created by 独嘉 on 14-5-12.
//  Copyright (c) 2014年 zuoye. All rights reserved.
//

#import "DDOtherFileResponseAPI.h"
#import "FileTransferEntity.h"
@implementation DDOtherFileResponseAPI

/**
 *  数据包中的serviceID
 *
 *  @return serviceID
 */
- (int)responseServiceID
{
    return MODULE_ID_FILETRANSFER;
}

/**
 *  数据包中的commandID
 *
 *  @return commandID
 */
- (int)responseCommandID
{
    return CMD_FILE_RESPONSE;
}

/**
 *  解析数据包
 *
 *  @return 解析数据包的block
 */
- (UnrequestAPIAnalysis)unrequestAnalysis
{
    UnrequestAPIAnalysis analysis = (id)^(NSData* data)
    {
        DataInputStream* bodyData = [DataInputStream dataInputStreamWithData:data];
        NSString *fromUserId = [bodyData readUTF];
        NSString *toUserId = [bodyData readUTF];
        NSString *fileName = [bodyData readUTF];
        uint32_t acceptFlag = [bodyData readInt];
        NSString *listenIp = [bodyData readUTF];
        uint16_t listenPort = [bodyData readShort];
        DDLog(@"handleFileResponse, %@->%@, fileName=%@, acceptFlag=%d, "
              "listenIp=%@, listenPort=%d", fromUserId, toUserId, fileName,
              acceptFlag, listenIp, listenPort);
        
        FileTransferEntity* entity = [[FileTransferEntity alloc] init];
        entity.fromUserId = fromUserId;
        entity.toUserId = toUserId;
        entity.fileName = fileName;
        entity.acceptFlag = acceptFlag;
        entity.listenIp = listenIp;
        entity.listenPort = listenPort;
        return entity;

    };
    return analysis;
}

@end
