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
Java_com_example_phoroeditorapplication2_MainActivity_blackAndWhite(JNIEnv *env, jclass clazz,
                                                                    jintArray pixels, jint width,
                                                                    jint height) {
    jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
    unsigned char *colors = (unsigned char *) pixels_;
    for(int y = 0;y<height;y++){
        for(int x = 0;x<width;x++){
            for(int c = 0; c< 4;c++){
                int yDif = y - height/2;
                int xDif = x - width/2;
                int rotation = 100*(3.14/180);

                double radian = sqrt(pow(xDif,2)+pow(yDif,2));
                double cal = atan2(yDif,xDif) + rotation*(((height/2)-radian)/(height/2));

                int rotationY = (height/2) + (int)(sin(cal)*radian);
                int rotationX = (width/2) + (int)(cos(cal)*radian);

                if(rotationY > height-1){
                    rotationY = height-1;
                } else if(rotationY < 0){
                    rotationY = 0;
                }
                if(rotationX > width-1){
                    rotationX = width-1;
                }else
                if(rotationX < 0){
                    rotationX = 0;
                }
                //colors[(y*width+x)*4+c] = colors[(rotationY*width+rotationX)*4+c];
                pixels_[rotationY*width*4+rotationX*4+c] = pixels_[y*width*4+x*4+c];
            }
        }
    }
    (*env).ReleaseIntArrayElements(pixels,pixels_,0);

}

//Java_com_example_phoroeditorapplication2_MainActivity_blackAndWhite(JNIEnv *env, jclass clazz,
//        jintArray pixels, jint width,
//        jint height) {
//jint *pixels_ = (*env).GetIntArrayElements(pixels,NULL);
////    jsize len = (*env).GetArrayLength(pixels);
////    for(int i=0;i<len;i++){
////        pixels_[i]/=2;
////    }
//unsigned char *colors = (unsigned char *) pixels_;
//int pixelCount = width * height *4; //char
//int i=0;
//while(i<pixelCount){
//unsigned char average = (colors[i]+ colors[i+1] + colors[i+2])/3; //RGB
////average = 20;
//colors[i] = truncateBW(average);//truncate(colors[i]*0.5);
//colors[i+1] = truncateBW(average);//truncate(colors[i+1]*0.5);
//colors[i+2] = truncateBW(average);//truncate(colors[i+2]*0.5);
//i+=4;
//}
//(*env).ReleaseIntArrayElements(pixels,pixels_,0);
//
//}