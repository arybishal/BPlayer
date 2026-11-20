#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include <cstdlib>
#include <cstdint>
#include <vector>
#include <algorithm>
#include <string>

extern "C" {
#include "layer3.h"
#include "soxr.h"
}

#define LOG_TAG "BPlayerShineMp3"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int pickTargetSampleRate(int inRate) {
    if (inRate <= 48000) {
        if (shine_find_samplerate_index(inRate) >= 0) {
            return inRate;
        }
    }
    // High-res downsampling target: 44.1k or 48k depending on base multiple
    if (inRate % 44100 == 0) {
        return 44100;
    }
    return 48000;
}

static int pickValidBitrate(int targetBitrate, int sampleRate) {
    int sIndex = shine_find_samplerate_index(sampleRate);
    if (sIndex < 0) return 192;
    int version = shine_mpeg_version(sIndex);
    if (shine_find_bitrate_index(targetBitrate, version) >= 0) {
        return targetBitrate;
    }
    // Fallback search closest valid bitrate
    static const int validBitrates[] = {320, 256, 224, 192, 160, 128, 96, 64};
    for (int b : validBitrates) {
        if (shine_find_bitrate_index(b, version) >= 0 && b <= targetBitrate) {
            return b;
        }
    }
    return 128;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lastwave_app_data_download_Mp3Encoder_nativeEncodePcmFile(
    JNIEnv* env,
    jclass /*clazz*/,
    jstring jPcmPath,
    jstring jMp3Path,
    jint inSampleRate,
    jint inChannels,
    jint targetBitrateKbps
) {
    if (jPcmPath == nullptr || jMp3Path == nullptr) {
        LOGE("Null file paths provided");
        return JNI_FALSE;
    }
    if (inChannels != 1 && inChannels != 2) {
        LOGE("Unsupported channel count: %d (only mono or stereo supported)", inChannels);
        return JNI_FALSE;
    }

    const char* pcmPath = env->GetStringUTFChars(jPcmPath, nullptr);
    const char* mp3Path = env->GetStringUTFChars(jMp3Path, nullptr);

    FILE* pcmFile = fopen(pcmPath, "rb");
    if (!pcmFile) {
        LOGE("Failed to open input PCM file: %s", pcmPath);
        env->ReleaseStringUTFChars(jPcmPath, pcmPath);
        env->ReleaseStringUTFChars(jMp3Path, mp3Path);
        return JNI_FALSE;
    }

    FILE* mp3File = fopen(mp3Path, "wb");
    if (!mp3File) {
        LOGE("Failed to open output MP3 file: %s", mp3Path);
        fclose(pcmFile);
        env->ReleaseStringUTFChars(jPcmPath, pcmPath);
        env->ReleaseStringUTFChars(jMp3Path, mp3Path);
        return JNI_FALSE;
    }

    int targetRate = pickTargetSampleRate(inSampleRate);
    int bitr = pickValidBitrate(targetBitrateKbps, targetRate);

    LOGI("Encoding PCM (%s) to MP3 (%s): inRate=%d, targetRate=%d, channels=%d, bitrate=%dkbps",
         pcmPath, mp3Path, inSampleRate, targetRate, inChannels, bitr);

    shine_config_t config;
    shine_set_config_mpeg_defaults(&config.mpeg);
    config.wave.samplerate = targetRate;
    config.wave.channels = (inChannels == 1) ? PCM_MONO : PCM_STEREO;
    config.mpeg.bitr = bitr;
    config.mpeg.mode = (inChannels == 1) ? MONO : STEREO;

    if (shine_check_config(config.wave.samplerate, config.mpeg.bitr) < 0) {
        LOGE("Unsupported Shine MP3 configuration: rate=%d, bitr=%d", targetRate, bitr);
        fclose(pcmFile);
        fclose(mp3File);
        env->ReleaseStringUTFChars(jPcmPath, pcmPath);
        env->ReleaseStringUTFChars(jMp3Path, mp3Path);
        return JNI_FALSE;
    }

    shine_t s = shine_initialise(&config);
    if (!s) {
        LOGE("shine_initialise failed");
        fclose(pcmFile);
        fclose(mp3File);
        env->ReleaseStringUTFChars(jPcmPath, pcmPath);
        env->ReleaseStringUTFChars(jMp3Path, mp3Path);
        return JNI_FALSE;
    }

    const size_t channelsCount = static_cast<size_t>(inChannels);
    const int samplesPerPass = shine_samples_per_pass(s);
    const size_t targetChunkSamples = static_cast<size_t>(samplesPerPass) * channelsCount;

    soxr_t resampler = nullptr;
    if (inSampleRate != targetRate) {
        soxr_error_t sErr = nullptr;
        const soxr_io_spec_t ioSpec = soxr_io_spec(SOXR_INT16_I, SOXR_INT16_I);
        const soxr_quality_spec_t qSpec = soxr_quality_spec(SOXR_HQ, 0);
        const soxr_runtime_spec_t rSpec = soxr_runtime_spec(1);
        resampler = soxr_create(
            static_cast<double>(inSampleRate),
            static_cast<double>(targetRate),
            static_cast<unsigned>(inChannels),
            &sErr,
            &ioSpec,
            &qSpec,
            &rSpec
        );
        if (sErr != nullptr || resampler == nullptr) {
            LOGE("Failed to create soxr resampler: %s", sErr ? sErr : "unknown");
        }
    }

    std::vector<int16_t> inBuffer(4096 * channelsCount);
    std::vector<int16_t> passBuffer(targetChunkSamples);
    std::vector<int16_t> pendingSamples;
    pendingSamples.reserve(targetChunkSamples * 4);

    bool ok = true;
    while (!feof(pcmFile)) {
        size_t samplesRead = fread(inBuffer.data(), sizeof(int16_t), inBuffer.size(), pcmFile);
        if (samplesRead == 0) break;

        size_t inFrames = samplesRead / channelsCount;
        if (resampler != nullptr) {
            size_t maxOutFrames = static_cast<size_t>(static_cast<double>(inFrames) * (static_cast<double>(targetRate) / inSampleRate) + 256.0);
            std::vector<int16_t> resampled(maxOutFrames * channelsCount);
            size_t idone = 0;
            size_t odone = 0;
            soxr_error_t err = soxr_process(resampler, inBuffer.data(), inFrames, &idone,
                                            resampled.data(), maxOutFrames, &odone);
            if (err != nullptr) {
                LOGE("soxr_process error: %s", err);
                ok = false;
                break;
            }
            pendingSamples.insert(pendingSamples.end(), resampled.begin(), resampled.begin() + static_cast<ptrdiff_t>(odone * channelsCount));
        } else {
            pendingSamples.insert(pendingSamples.end(), inBuffer.begin(), inBuffer.begin() + static_cast<ptrdiff_t>(samplesRead));
        }

        while (pendingSamples.size() >= targetChunkSamples) {
            std::copy(pendingSamples.begin(), pendingSamples.begin() + static_cast<ptrdiff_t>(targetChunkSamples), passBuffer.begin());
            pendingSamples.erase(pendingSamples.begin(), pendingSamples.begin() + static_cast<ptrdiff_t>(targetChunkSamples));

            int written = 0;
            unsigned char* mp3Data = shine_encode_buffer_interleaved(s, passBuffer.data(), &written);
            if (written > 0 && mp3Data != nullptr) {
                if (fwrite(mp3Data, 1, static_cast<size_t>(written), mp3File) != static_cast<size_t>(written)) {
                    LOGE("fwrite error writing MP3 data");
                    ok = false;
                    break;
                }
            }
        }
        if (!ok) break;
    }

    // Flush resampler if active
    if (ok && resampler != nullptr) {
        size_t odone = 0;
        std::vector<int16_t> tail(4096 * channelsCount);
        soxr_process(resampler, nullptr, 0, nullptr, tail.data(), 4096, &odone);
        if (odone > 0) {
            pendingSamples.insert(pendingSamples.end(), tail.begin(), tail.begin() + static_cast<ptrdiff_t>(odone * channelsCount));
        }
    }

    // Pad and encode any remaining pending samples
    if (ok && !pendingSamples.empty()) {
        std::fill(passBuffer.begin(), passBuffer.end(), 0);
        std::copy(pendingSamples.begin(), pendingSamples.end(), passBuffer.begin());
        int written = 0;
        unsigned char* mp3Data = shine_encode_buffer_interleaved(s, passBuffer.data(), &written);
        if (written > 0 && mp3Data != nullptr) {
            fwrite(mp3Data, 1, static_cast<size_t>(written), mp3File);
        }
    }

    // Flush encoder bit reservoir
    if (ok) {
        int written = 0;
        unsigned char* flushed = shine_flush(s, &written);
        if (written > 0 && flushed != nullptr) {
            fwrite(flushed, 1, static_cast<size_t>(written), mp3File);
        }
    }

    if (resampler != nullptr) {
        soxr_delete(resampler);
    }
    shine_close(s);

    fflush(mp3File);
    long finalSize = ftell(mp3File);
    fclose(pcmFile);
    fclose(mp3File);

    env->ReleaseStringUTFChars(jPcmPath, pcmPath);
    env->ReleaseStringUTFChars(jMp3Path, mp3Path);

    LOGI("Finished MP3 encoding: ok=%d, finalSize=%ld bytes", ok ? 1 : 0, finalSize);
    return (ok && finalSize > 128) ? JNI_TRUE : JNI_FALSE;
}
