#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_blackAndWhite(JNIEnv *env, jclass clazz,
                                                                    jintArray pixels, jint width,
                                                                    jint height) {
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
//    jsize len = (*env).GetArrayLength(pixels);
//    for(int i=0;i<len;i++){
//        pixels_[i]/=2;
//    }
   unsigned char *colors = (unsigned char *) pixels_;
    int pixelCount = width * height *4; //char
    int i=0;
    while(i<pixelCount){
        unsigned char average = (colors[i]+ colors[i+1] + colors[i+2])/3; //RGB
        colors[i] = average;
        colors[i+1] = average;
        colors[i+2] = average;
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);
//printf("Je suis");

}