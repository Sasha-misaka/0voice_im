//
//  DDMessagesReviewContentModule.h
//  Duoduo
//
//  Created by 独嘉 on 14-5-6.
//  Copyright (c) 2014年 zuoye. All rights reserved.
//

#import <Foundation/Foundation.h>
@class MessageEntity;
@interface DDMessagesReviewContentModule : NSObject
+ (NSAttributedString*)getAttributedStringFromShowMessage:(MessageEntity*)message;
@end
