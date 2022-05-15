#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

int truncate(int val){
    if(val < 0){
        return 0;
    }
    if(val > 255){
        return 255;
    }
    return val;
}
int truncateBW(int val){
    if(val < 128){
        return 0;
    }
    return 255;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_grayscale(JNIEnv *env, jclass clazz,
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
//average = 20;
        colors[i] = truncate(average);//truncate(colors[i]*0.5);
        colors[i+1] = truncate(average);//truncate(colors[i+1]*0.5);
        colors[i+2] = truncate(average);//truncate(colors[i+2]*0.5);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_sepia(JNIEnv *env, jclass clazz,
                                                            jintArray pixels, jint width,
                                                            jint height) {
    // TODO: implement sepia()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    int pixelCount = width * height *4; //char
    int i=0;
    while(i<pixelCount){
        unsigned char average = (colors[i]+ colors[i+1] + colors[i+2])/3; //RGB
        //average = 20;
        unsigned char sepiaRed = truncate(0.393*colors[i] + 0.769*colors[i+1] + 0.189*colors[i+2]);
        unsigned char sepiaGreen = truncate(0.349*colors[i] + 0.686*colors[i+1] + 0.168*colors[i+2]);
        unsigned char sepiaBlue = truncate(0.272*colors[i] + 0.534*colors[i+1] + 0.131*colors[i+2]);
        colors[i] = sepiaBlue;//truncate(average);//truncate(colors[i]*0.5);
        colors[i+1] = sepiaGreen;// truncate(average);//truncate(colors[i+1]*0.5);
        colors[i+2] = sepiaRed;//truncate(average);//truncate(colors[i+2]*0.5);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_ripple(JNIEnv *env, jclass clazz,
                                                             jintArray pixels, jint width,
                                                             jint height) {
    // TODO: implement ripple()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    for(int y = 0;y<height;y++){
        for(int x = 0;x<width;x++){
            for(int c = 0;c<3;c++){
                int rippleX = x + (rand()%width);
                int rippleY = y + (rand()%height);
                colors[(y*width+x)*4+c] = colors[(rippleY*width+rippleX)*4+c];
            }
        }
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_pastels(JNIEnv *env, jclass clazz,
                                                              jintArray pixels, jint width,
                                                              jint height) {
    // TODO: implement pastels()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    int pixelCount = width * height *4; //char
    int i=0;
    while(i<pixelCount){
        colors[i] = truncate(colors[i]/2+127);
        colors[i+1] = truncate(colors[i+1]/2+127);
        colors[i+2] = truncate(colors[i+2]/2+127);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_pixelate(JNIEnv *env, jclass clazz,
                                                               jintArray pixels, jint width,
                                                               jint height) {
    // TODO: implement pixelate()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    int adjustment = 15;
    for(int y=0; y<height; y++) {
        for(int x=0; x<width; x++) {
            for(int c=0; c<4; c++) {
                int xPixel = x-x%adjustment;
                int yPixel = y-y%adjustment;
                colors[(y*width+x)*4+c] = colors[(yPixel*width+xPixel)*4+c];
            }
        }
    }

    (*env).ReleaseIntArrayElements(pixels, pixels_, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_invert(JNIEnv *env, jclass clazz,
                                                             jintArray pixels, jint width,
                                                             jint height) {
    // TODO: implement invert()
    int pixelCount = width * height *4; //char
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *)pixels_;
    int i=0;
    while(i<pixelCount){
        colors[i] = truncate(255-colors[i]);
        colors[i+1] = truncateBW(255-colors[i+1]);
        colors[i+2] = truncateBW(255-colors[i+2]);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels, pixels_, 0);


}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_bw(JNIEnv *env, jclass clazz,
                                                         jintArray pixels, jint width,
                                                         jint height) {
    // TODO: implement bw() aka black and white filter
    int pixelCount = width * height *4; //char
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *)pixels_;
    int i=0;
    while(i<pixelCount){
        unsigned char average = (colors[i]+ colors[i+1] + colors[i+2])/3; //RGB
        colors[i] = truncateBW(average);
        colors[i+1] = truncateBW(average);
        colors[i+2] = truncateBW(average);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_brightness_1p(JNIEnv *env, jclass clazz,
                                                                    jintArray pixels, jint width,
                                                                    jint height,int brightness) {
    // TODO: implement brightness_p()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    int pixelCount = width * height *4; //char
    int i=0;
    while(i<pixelCount){
        colors[i] = truncate(colors[i]+brightness);
        colors[i+1] = truncate(colors[i+1]+brightness);
        colors[i+2] = truncate(colors[i+2]+brightness);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_phoroeditorapplication2_MainActivity_contrast_1p(JNIEnv *env, jclass clazz,
                                                                  jintArray pixels, jint width,
                                                                  jint height, jint contrast) {
    // TODO: implement contrast_p()
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    auto *colors = (unsigned char *) pixels_;
    int pixelCount = width * height *4; //char
    int i=0;
    // (259 * (contrast + 255)) / (255 * (259 – contrast))
    float factor = (float)(259 * (contrast + 255)) / (float)(255 * (259 -contrast));

    while(i<pixelCount){
        colors[i] = truncate(factor*(colors[i]-128)+128);
        colors[i+1] = truncate(factor*(colors[i+1]-128)+128);
        colors[i+2] = truncate(factor*(colors[i+2]-128)+128);
        i+=4;
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);

}