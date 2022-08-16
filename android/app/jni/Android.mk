LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := status-logs
LOCAL_SRC_FILES := Log.c

LOCAL_CPPFLAGS  := -std=c++11 -fexceptions -I$(LOCAL_PATH)/include
LOCAL_LDFLAGS := -llog

include $(BUILD_SHARED_LIBRARY)
