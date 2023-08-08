/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This file is part of OpenVK Legacy.
 *
 * OpenVK Legacy is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

// Java/C++ standard implementations headers
#include <jni.h>
#include <stdio.h>

// Android implementations headers
#include <android/log.h>

// FFmpeg implementation headers (using LGPLv3.0 model)
#include <libavutil/imgutils.h>
#include <libavformat/avformat.h>

/*for Android logs*/
#define LOG_TAG "OVK-MP"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char version[7] = "0.0.1";
char *gFileName;	      //file name of the video

AVFormatContext *gFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

jobject generateTrackInfo(JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo
            (JNIEnv *env, jobject instance, jstring filename, jint type) {
        return NULL;
    }
}

jobject generateTrackInfo(
        JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type
) {
    jclass track_class;
    if(type == AVMEDIA_TYPE_VIDEO) {
        // Load OvkVideoTrack class
        track_class = env->FindClass(
                "uk/openvk/android/legacy/utils/media/OvkVideoTrack"
        );

        // Load OvkVideoTrack class methods
        jmethodID video_track_init = env->GetMethodID(
                track_class, "<init>", "()V"
        );
        jfieldID codec_name_field = env->GetFieldID(
                track_class, "codec_name", "Ljava/lang/String;"
        );
        jfieldID frame_size_field = env->GetFieldID(track_class, "frame_size", "[I");
        jfieldID bitrate_field = env->GetFieldID(
                track_class, "bitrate", "Ljava/lang/Integer;"
        );
        jfieldID frame_rate_field = env->GetFieldID(
                track_class, "frame_rate", "Ljava/lang/Float;"
        );

        // Load OvkVideoTrack values form fields (class variables)
        env->SetObjectField(track_class, codec_name_field, env->NewStringUTF(pCodec->name));
        jintArray array = (jintArray) env->GetObjectField(track_class, frame_size_field);
        jint *frame_size = env->GetIntArrayElements(array, 0);
        frame_size[0] = pCodecCtx->width;
        frame_size[1] = pCodecCtx->height;
        env->ReleaseIntArrayElements(array, frame_size, 0);
        env->SetIntField(track_class, bitrate_field, pCodecCtx->bit_rate);
        env->SetFloatField(track_class, frame_rate_field, pStream->avg_frame_rate.num);
    } else {
        // Load OvkAudioTrack class
        track_class = env->FindClass(
                "uk/openvk/android/legacy/utils/media/OvkAudioTrack"
        );
        // Load OvkVideoTrack class methods
        jmethodID audio_track_init = env->GetMethodID(
                track_class, "<init>", "()V"
        );
        jfieldID codec_name_field = env->GetFieldID(
                track_class, "codec_name", "Ljava/lang/String;"
        );
        jfieldID bitrate_field = env->GetFieldID(
                track_class, "bitrate", "Ljava/lang/Integer;"
        );
        jfieldID sample_rate_field = env->GetFieldID(
                track_class, "frame_rate", "Ljava/lang/Integer;"
        );
        jfieldID channels_field = env->GetFieldID(
                track_class, "channels", "Ljava/lang/Integer;"
        );

        // Load OvkAudioTrack values form fields (class variables)
        env->SetObjectField(track_class, codec_name_field, env->NewStringUTF(pCodec->name));
        env->SetIntField(track_class, bitrate_field, pCodecCtx->bit_rate);
        env->SetIntField(track_class, channels_field, pCodecCtx->channels);
    }
    return track_class;
};