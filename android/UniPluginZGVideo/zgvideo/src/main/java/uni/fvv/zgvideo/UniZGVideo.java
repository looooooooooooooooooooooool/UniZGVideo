package uni.fvv.zgvideo;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import fvv.sdk.FvvZGVideoSDK.FvvZGVideo;
import fvv.sdk.FvvZGVideoSDK.MessageHandle;

public class UniZGVideo extends WXModule {

    private FvvZGVideo fvvZGVideo;
    private JSCallback mJSCallback;

    @JSMethod(uiThread = true)
    public void test(){
        Toast.makeText(mWXSDKInstance.getContext(),"test",Toast.LENGTH_SHORT).show();
    }

    @JSMethod(uiThread = true)  //初始化
    public void init(JSONObject options, final JSCallback jsCallback){
        mJSCallback = jsCallback;
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null,(Object[])null); // android上下文. 不能为null
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        }

        Boolean testEnv = SetValue(options,"testEnv",true);

        fvvZGVideo = new FvvZGVideo(application, testEnv, new MessageHandle() {
            @Override
            public void Callback(String s, Object o) {
                try{
                    jsCallback.invokeAndKeepAlive(SetCallback(s,JSONObject.parse(o.toString())));
                }catch (Exception e){
                    jsCallback.invokeAndKeepAlive(SetCallback(s,o.toString()));
                }

            }
        });

        String appId = SetValue(options,"appId","");
        String appSign = SetValue(options,"appSign","");

        fvvZGVideo.init(appId,appSign);
    }

    @JSMethod(uiThread = true)
    public void unInit(){
        fvvZGVideo.unInit();
    }

    //------------------------------------用户-----------------------------------
    @JSMethod(uiThread = true)  //设置用户信息
    public void setUser(JSONObject options){
        String userId = SetValue(options,"userId","");
        String userName = SetValue(options,"userName","");
        fvvZGVideo.setUser(userId,userName);
    }

    @JSMethod(uiThread = true)  //登录房间
    public void loginRoom(JSONObject options){
        String roomId = SetValue(options,"roomId","");
        String role = SetValue(options,"role","");   //AUDIENCE
        fvvZGVideo.loginRoom(roomId,role);
    }

    @JSMethod(uiThread = true)  //退出房间
    public void logoutRoom(){
        fvvZGVideo.logoutRoom();
    }


    @JSMethod(uiThread = true)  //邀请观众连麦
    public void inviteJoinLive(String userId){
        if(userId == null || userId == ""){
            mJSCallback.invokeAndKeepAlive(SetCallback("inviteJoinLive",SetCallbackMessage(false,"please input userId")));
            return;
        }
        fvvZGVideo.inviteJoinLive(userId);
    }

    //-------------------------------------预览界面--------------------------------------
    @JSMethod(uiThread = true) //设置窗口尺寸
    public void setViewConfig(JSONObject options){
        String level = SetValue(options,"level","");
        int width = SetValue(options,"width",576);
        int height = SetValue(options,"height",1024);
        int fps = SetValue(options,"fps",30);
        int bitrate = SetValue(options,"bitrate",(width * height * 5));

        fvvZGVideo.setViewConfig(level,width,height,fps,bitrate);
    }


    @JSMethod(uiThread = true) //开始预览
    public void startPreview(JSONObject options){
        String view = SetValue(options,"view","");
        if(view == ""){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPreview",SetCallbackMessage(false,"please input view name")));
            return;
        }
        String mode = SetValue(options,"mode","SCALETOFILL");
        Boolean zoom = SetValue(options,"zoom",true);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setPreviewMode(mode);
        fvvZGVideo.startPreview(UniZGVideoMap.getComponentView(view),zoom,channelIndex);
    }

    @JSMethod(uiThread = true) // 停止预览
    public void stopPreview(){
        fvvZGVideo.stopPreview();
    }


    //------------------------------------推流录制-----------------------------------
    @JSMethod(uiThread = true) // 开始推流
    public void startPublishing(JSONObject options){
        String streamId = SetValue(options,"streamId","");
        if(streamId == "" || streamId == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPublishing",SetCallbackMessage(false,"please input streamId")));
            return;
        }
        String title = SetValue(options,"title","");
        String flag = SetValue(options,"flag","");
        String extraInfo = SetValue(options,"extraInfo","");
        String channelIndex = SetValue(options,"channelIndex","");
        if(channelIndex == "" || channelIndex == null){
            fvvZGVideo.startPublishing(streamId,title,flag,extraInfo);
        }else{
            fvvZGVideo.startPublishing2(streamId,title,flag,channelIndex);
        }
    }

    @JSMethod(uiThread = true) // 停止推流
    public void stopPublishing(){
        fvvZGVideo.stopPublishing();
    }


    @JSMethod(uiThread = true) // 开始拉流
    public void startPlayingStream(JSONObject options){
        String streamId = SetValue(options,"streamId","");
        if(streamId == "" || streamId == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPlayingStream",SetCallbackMessage(false,"please input streamId")));
            return;
        }
        String view = SetValue(options,"view","");
        if(view == ""){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPlayingStream",SetCallbackMessage(false,"please input view name")));
            return;
        }
        fvvZGVideo.startPlayingStream(streamId,UniZGVideoMap.getComponentView(view));
        String mode = SetValue(options,"mode","SCALETOFILL");
        fvvZGVideo.setPlayingViewMode(streamId,mode);
    }

    @JSMethod(uiThread = true) // 更新拉流视图
    public void updatePlayView(JSONObject options){
        String streamId = SetValue(options,"streamId","");
        if(streamId == "" || streamId == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPlayingStream",SetCallbackMessage(false,"please input streamId")));
            return;
        }
        String view = SetValue(options,"view","");
        if(view == ""){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPlayingStream",SetCallbackMessage(false,"please input view name")));
            return;
        }
        fvvZGVideo.updatePlayView(streamId,UniZGVideoMap.getComponentView(view));
        String mode = SetValue(options,"mode","SCALETOFILL");
        fvvZGVideo.setPlayingViewMode(streamId,mode);
    }

    @JSMethod(uiThread = true) // 停止拉流
    public void stopPlayingStream(JSONObject options){
        String streamId = SetValue(options,"streamId","");
        if(streamId == "" || streamId == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("startPlayingStream",SetCallbackMessage(false,"please input streamId")));
            return;
        }
        fvvZGVideo.stopPlayingStream(streamId);
    }


    @JSMethod(uiThread = true) // 开始录制
    public void startRecord(JSONObject options){
        String storagePath = SetValue(options,"storagePath","");
        if(storagePath == "" || storagePath == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("startRecord",SetCallbackMessage(false,"please input storagePath")));
            return;
        }
        if(!CheckPath("startRecord",storagePath)){
            return;
        }
        String channelIndex = SetValue(options,"channelIndex","");
        String recordType = SetValue(options,"recordType","");
        String recordFormat = SetValue(options,"recordFormat","");
        fvvZGVideo.startRecord(channelIndex,recordType,storagePath,recordFormat);
    }

    @JSMethod(uiThread = true) // 停止录制
    public void stopRecord(String channelIndex){
        String channel = channelIndex == "" || channelIndex == null?"":channelIndex;
        fvvZGVideo.stopRecord(channel);
    }

    @JSMethod(uiThread = true) // 截图
    public void takePicture(JSONObject options){
        String storagePath = SetValue(options,"storagePath","");
        if(storagePath == "" || storagePath == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("takePicture",SetCallbackMessage(false,"please input storagePath")));
            return;
        }
        if(!CheckPath("takePicture",storagePath)){
            return;
        }
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.takePreviewSnapshot(storagePath,channelIndex);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @JSMethod(uiThread = true) // 裁剪视频
    public void clipVideo(JSONObject options){
        String storagePath = SetValue(options,"input","");
        if(storagePath == "" || storagePath == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("clipVideo",SetCallbackMessage(false,"please input video input path")));
            return;
        }
        String outputPath = SetValue(options,"output","");
        if(storagePath == "" || storagePath == null){
            mJSCallback.invokeAndKeepAlive(SetCallback("clipVideo",SetCallbackMessage(false,"please input video output path")));
            return;
        }
        if(!CheckPath("clipVideo",outputPath)){
            return;
        }

        int clipPoint = SetValue(options,"point",0);
        int clipDuration = SetValue(options,"duration",0);

        UniVideoClip uniVideoClip = new UniVideoClip();
        boolean check = uniVideoClip.clipVideo(storagePath,outputPath,clipPoint * 1000000,clipDuration * 1000000);
        if(check){
            mJSCallback.invokeAndKeepAlive(SetCallback("clipVideo",SetCallbackMessage(true,outputPath)));
        }else{
            mJSCallback.invokeAndKeepAlive(SetCallback("clipVideo",SetCallbackMessage(false,"please check point or duration error")));
        }
    }

    //-------------------------------------IM--------------------------------------
    @JSMethod(uiThread = true) // 发送房间消息
    public void sendRoomMessage(JSONObject options){
        String type = SetValue(options,"type","TEXT");
        String category = SetValue(options,"category","CHAT");
        String content = SetValue(options,"content","");
        fvvZGVideo.sendRoomMessage(type,category,content);
    }

    //------------------------------------美颜滤镜-----------------------------------
    @JSMethod(uiThread = true) // 开启美颜
    public void enableBeautify(){
        fvvZGVideo.enableBeautify();
    }

    @JSMethod(uiThread = true) // 关闭美颜
    public void disableBeautify(){
        fvvZGVideo.disableBeautify();
    }

    @JSMethod(uiThread = true) // 设置美颜美白的亮度修正参数,取值范围[0,1]，默认值是 0.5,参数值越大亮度越暗即参数值越小越白。
    public void setWhitenFactor(float value){
        fvvZGVideo.setWhitenFactor(value);
    }

    @JSMethod(uiThread = true) // 设置美颜磨皮的采样步长，取值范围[1,16]，默认值是 4.0；参数值越大，磨皮力度越大，皮肤肉眼效果更好
    public void setPolishStep(float value){
        fvvZGVideo.setPolishStep(value);
    }

    @JSMethod(uiThread = true) // 设置锐化参数，取值范围[0,2]，默认值是 0.2；参数值越大锐化越强，图片越清晰
    public void setPolishFactor(float value){
        fvvZGVideo.setPolishFactor(value);
    }

    @JSMethod(uiThread = true) // 设置美颜采样颜色阈值，取值范围[0,16]，默认值是 4.0；参数值越小，画面稍微有点儿朦胧
    public void setSharpenFactor(float value){
        fvvZGVideo.setSharpenFactor(value);
    }

    @JSMethod(uiThread = true) // 设置滤镜
    public void setFilter(String value){
        fvvZGVideo.setFilter(value);
    }

    @JSMethod(uiThread = true) // 设置水印
    public void setWaterMark(JSONObject options){
        String path = SetValue(options,"path","");
        int x = SetValue(options,"x",0);
        int y = SetValue(options,"y",0);
        int w = SetValue(options,"w",0);
        int h = SetValue(options,"h",0);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setWaterMarkImagePath(path,x,y,w,h,channelIndex);
    }


    //------------------------------------音量相关-----------------------------------
    @JSMethod(uiThread = true) // 麦克风开关
    public void enableMic(JSONObject options){
        Boolean bool = SetValue(options,"enable",true);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.enableMic(bool,channelIndex);
    }

    @JSMethod(uiThread = true) // 混音开关
    public void enableAux(Boolean bool){
        fvvZGVideo.enableAux(bool == null?true:bool);
    }

    @JSMethod(uiThread = true) // 混音音量
    public void setAuxVolume(int volume){
        fvvZGVideo.setAuxVolume(volume);
    }

    @JSMethod(uiThread = true) // 音量降噪开关
    public void enableNoiseSuppress(Boolean bool){
        fvvZGVideo.enableNoiseSuppress(bool == null?true:bool);
    }

    @JSMethod(uiThread = true) // 音量降噪模式
    public void setNoiseSuppressMode(String mode){
        fvvZGVideo.setNoiseSuppressMode(mode == null?"":mode);
    }

    @JSMethod(uiThread = true) // 回声消除
    public void enableAEC(Boolean bool){
        fvvZGVideo.enableAEC(bool == null?true:bool);
    }

    @JSMethod(uiThread = true) // 回声消除模式
    public void setAECMode(String mode){
        fvvZGVideo.setAECMode(mode == null?"":mode);
    }

    @JSMethod(uiThread = true) // 变声
    public void setVoiceChangerParam(String mode){
        fvvZGVideo.setVoiceChangerParam(mode == null?"":mode);
    }

    //------------------------------------镜头相关-----------------------------------

    @JSMethod(uiThread = true) // 切换摄像头
    public void setFrontCam(JSONObject options){
        Boolean bool = SetValue(options,"enable",true);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setFrontCam(bool,channelIndex);
    }

    @JSMethod(uiThread = true) // 设置app朝向
    public void setAppOrientation(JSONObject options){
        int orientation = SetValue(options,"orientation",0);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setAppOrientation(orientation,channelIndex);
    }

    @JSMethod(uiThread = true) // 摄像头开关
    public void enableCamera(JSONObject options){
        Boolean bool = SetValue(options,"enable",true);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.enableCamera(bool,channelIndex);
    }

    @JSMethod(uiThread = true) // 设置镜像
    public void setVideoMirrorMode(JSONObject options){
        String mode = SetValue(options,"mode","");
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setVideoMirrorMode(mode,channelIndex);
    }

    @JSMethod(uiThread = false) // 获取镜头最大变焦倍数
    public float getCamMaxZoomFactor(String channel){
        return fvvZGVideo.getCamMaxZoomFactor(channel == null?"":channel);
    }

    @JSMethod(uiThread = true) // 设置镜像变焦倍数
    public void setCamZoomFactor(JSONObject options){
        float mode = SetValue(options,"zoom",0);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.setCamZoomFactor(mode,channelIndex);
    }

    @JSMethod(uiThread = true) // 闪光灯开关
    public void enableTorch(JSONObject options){
        Boolean bool = SetValue(options,"enable",true);
        String channelIndex = SetValue(options,"channelIndex","");
        fvvZGVideo.enableTorch(bool,channelIndex);
    }
    //------------------------------------其他杂项-----------------------------------

    @JSMethod(uiThread = true) //硬件编码开关
    public void requireHardwareEncoder(Boolean bool){
        fvvZGVideo.requireHardwareEncoder(bool == null?true:bool);
    }

    @JSMethod(uiThread = true) //硬件解码开关
    public void requireHardwareDecoder(Boolean bool){
        fvvZGVideo.requireHardwareDecoder(bool == null?true:bool);
    }



    boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    Boolean CheckPath(String type,String storagePath){
        String path = storagePath.substring(0,storagePath.lastIndexOf("/"));
        if(!isFolderExists(path)){
            mJSCallback.invokeAndKeepAlive(SetCallback(type,SetCallbackMessage(false,"storagePath no exists")));
            return false;
        }
        return true;
    }

    JSONObject SetCallbackMessage(Boolean state ,Object o){
        JSONObject data = new JSONObject();
        data.put("state",state);
        data.put("data",o);
        return data;
    }

    JSONObject SetCallback(String type ,Object o){
        JSONObject data = new JSONObject();
        data.put("type",type);
        data.put("data",o);
        return data;
    }

    int SetValue(JSONObject object,String key,int defaultValue){
        return object != null && object.containsKey(key)?object.getInteger(key):defaultValue;
    }
    float SetValue(JSONObject object,String key,float defaultValue){
        return object != null && object.containsKey(key)?object.getInteger(key):defaultValue;
    }
    String SetValue(JSONObject object,String key,String defaultValue){
        return object != null && object.containsKey(key)?object.getString(key):defaultValue;
    }
    Boolean SetValue(JSONObject object,String key,Boolean defaultValue){
        return object != null && object.containsKey(key)?object.getBoolean(key):defaultValue;
    }


}
