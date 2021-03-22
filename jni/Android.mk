LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := JniParser
LOCAL_SRC_FILES := PageParser.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := JniLastPageParser
LOCAL_SRC_FILES := LastPageParser.cpp
include $(BUILD_SHARED_LIBRARY)
