LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := JniParserPage
LOCAL_SRC_FILES := WeekPageParser.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := JniParserLastPage
LOCAL_SRC_FILES := LastPageParser.cpp
include $(BUILD_SHARED_LIBRARY)