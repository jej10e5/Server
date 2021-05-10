#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;

float resize(Mat img_src, Mat &img_resize, int resize_width) {


    float scale = resize_width / (float) img_src.cols;

    if (img_src.cols > resize_width) {

        int new_height = cvRound(img_src.rows * scale);

        resize(img_src, img_resize, Size(resize_width, new_height));

    } else {

        img_resize = img_src;

    }

    return scale;

}

double real_facesize_x;
double real_facesize_y;
double real_facesize_width;
double real_facesize_height;

double faceX;
double faceY;
double faceWidth;
double faceHeight;

double currentFaceSize;
double previousSize;

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_server_MainActivity_loadCascade(JNIEnv *env, jobject thiz,
                                                 jstring cascade_file_name) {
    // TODO: implement loadCascade()
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_server_MainActivity_detect(JNIEnv *env, jobject thiz,
                                            jlong cascade_classifier_face,
                                            jlong cascade_classifier_eye, jlong mat_addr_input,
                                            jlong mat_addr_result) {
    // TODO: implement detect()
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_server_MainActivity_detect4(JNIEnv *env, jobject thiz,
                                             jlong cascade_classifier_face,
                                             jlong cascade_classifier_eye, jlong mat_addr_input,
                                             jlong mat_addr_result) {
    // TODO: implement detect4()
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_server_MainActivity_detect5(JNIEnv *env, jobject thiz,
                                             jlong cascade_classifier_face,
                                             jlong cascade_classifier_eye, jlong mat_addr_input,
                                             jlong mat_addr_result) {
    // TODO: implement detect5()
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_server_MainActivity_faceWidth(JNIEnv *env, jobject thiz,
                                               jlong cascade_classifier_face,
                                               jlong cascade_classifier_eye, jlong mat_addr_input,
                                               jlong mat_addr_result) {
    // TODO: implement faceWidth()
}extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_server_MainActivity_faceHeight(JNIEnv *env, jobject thiz,
                                                jlong cascade_classifier_face,
                                                jlong cascade_classifier_eye, jlong mat_addr_input,
                                                jlong mat_addr_result) {
    // TODO: implement faceHeight()
}