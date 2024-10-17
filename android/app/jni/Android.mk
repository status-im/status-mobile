LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := status-logs
LOCAL_SRC_FILES := Log.c

LOCAL_CPPFLAGS  := -std=c++11 -fexceptions -I$(LOCAL_PATH)/include
# Fix for : ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
LOCAL_CFLAGS := '-Wno-error=implicit-function-declaration'
LOCAL_LDFLAGS := -llog

include $(BUILD_SHARED_LIBRARY)
