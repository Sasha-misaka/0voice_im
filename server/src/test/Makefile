#基本
CC = g++
CFLAGS=-Wall -Wno-deprecated -g -std=c++11
LDFLAGS= -lbase -lpthread -lprotobuf-lite -lslog -lcurl
ifeq ($(shell uname), Darwin)
#CFLAGS := $(CFLAGS) -Os -framework Cocoa
LDFLAGS := $(LDFLAGS) -Os -framework Cocoa
endif
LN=/bin/ln -s 
AR=ar
CP=/bin/cp
RM=-/bin/rm -rf
ARCH=PC

#链接库名
LIB_NAME=
#链接库版本
LIB_VER=1.0.0

# 二进制目标
BIN=test

#源文件目录
SrcDir= .
#头文件目录
IncDir= . ../base ../base/jsoncpp ../base/pb/protocol ../base/pb
#连接库目录
LibDir= ../base/ ./ ../base/pb/lib/linux/ ../base/slog/lib/
#LibDir=
SRCS=$(foreach dir,$(SrcDir),$(wildcard $(dir)/*.cpp))
INCS=$(foreach dir,$(IncDir),$(addprefix -I,$(dir)))
LINKS=$(foreach dir,$(LibDir),$(addprefix -L,$(dir)))
CFLAGS := $(CFLAGS) $(INCS)
LDFLAGS:= $(LINKS) $(LDFLAGS)

OBJS = $(SRCS:%.cpp=%.o)

all:$(BIN)
#$(BIN):$(OBJS)
$(BIN):$(OBJS)
	$(CC) -o $(BIN) $(OBJS) $(LDFLAGS)
	@echo " OK!\tComplie $@ "

%.o:%.cpp
	@echo "[$(ARCH)] \t\tCompileing $@..."
	@$(CC) $(CFLAGS) -c $< -o $@

clean:
	@echo "[$(ARCH)] \tCleaning files..."
	@$(RM) $(OBJS) $(BIN)
