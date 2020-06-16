package fvv.sdk.FvvZGVideoSDK;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zego.zegoavkit2.audioaux.ZegoAudioAux;
import com.zego.zegoavkit2.audioprocessing.ZegoAudioProcessing;
import com.zego.zegoavkit2.camera.ZegoCamera;
import com.zego.zegoavkit2.camera.ZegoCameraExposureMode;
import com.zego.zegoavkit2.camera.ZegoCameraFocusMode;
import com.zego.zegoavkit2.mediarecorder.IZegoMediaRecordCallback;
import com.zego.zegoavkit2.mediarecorder.ZegoMediaRecordChannelIndex;
import com.zego.zegoavkit2.mediarecorder.ZegoMediaRecordFormat;
import com.zego.zegoavkit2.mediarecorder.ZegoMediaRecordType;
import com.zego.zegoavkit2.mediarecorder.ZegoMediaRecorder;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoInitSDKCompletionCallback;
import com.zego.zegoliveroom.callback.IZegoLivePlayerCallback;
import com.zego.zegoliveroom.callback.IZegoLoginCompletionCallback;
import com.zego.zegoliveroom.callback.IZegoResponseCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.callback.IZegoSnapshotCompletionCallback2;
import com.zego.zegoliveroom.callback.im.IZegoIMCallback;
import com.zego.zegoliveroom.callback.im.IZegoRoomMessageCallback;
import com.zego.zegoliveroom.constants.ZegoAvConfig;
import com.zego.zegoliveroom.constants.ZegoBeauty;
import com.zego.zegoliveroom.entity.ZegoBigRoomMessage;
import com.zego.zegoliveroom.entity.ZegoPlayStreamQuality;
import com.zego.zegoliveroom.entity.ZegoRoomMessage;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;
import com.zego.zegoliveroom.entity.ZegoUserState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FvvZGVideo {

    private ZegoLiveRoom mZegoLiveRoom;
    private ZegoMediaRecorder mZegoMediaRecorder;
    private ZegoAudioAux mZegoAudioAux;
    private MessageHandle mMessageHandle;
    public Beautify mBeautify;


    //视频缩放相关变量
    private float mMaxZoomValue = 0;
    private float mCurrentZoomValue = 0;
    private float mCurrentDistance;
    private float mLastDistance = -1;
    //-------------------------------------初始化--------------------------------------

    public FvvZGVideo(final Application application,boolean testEnv,MessageHandle messageHandle){
        mMessageHandle = messageHandle;
        mZegoLiveRoom.setSDKContext(new ZegoLiveRoom.SDKContextEx() {

            @Override
            public long getLogFileSize() {
                return 0;  // 单个日志文件的大小，必须在 [5M, 100M] 之间；当返回 0 时，表示关闭写日志功能，不推荐关闭日志。
            }

            @Override
            public String getSubLogFolder() {
                return null;
            }

            @Override
            public String getSoFullPath() {
                return null; // return null 表示使用默认方式加载 libzegoliveroom.so
                // 此处可以返回 so 的绝对路径，用来指定从这个位置加载 libzegoliveroom.so，确保应用具备存取此路径的权限
            }

            @Override
            public String getLogPath() {
                return null; //  return null 表示日志文件会存储到默认位置，如果返回非空，则将日志文件存储到该路径下，注意应用必须具备存取该目录的权限
            }

            @Override
            public Application getAppContext() {
                return application; // android上下文. 不能为null
            }
        });
        ZegoLiveRoom.setTestEnv(testEnv);
        ZegoLiveRoom.setVerbose(testEnv);
    }

    //初始化
    public void init(String appID,String appSign){
        mZegoLiveRoom = new ZegoLiveRoom();
        mZegoMediaRecorder = new ZegoMediaRecorder();
        mZegoAudioAux = new ZegoAudioAux();
        mBeautify = new Beautify();
        // 初始化sdk, appID与appSign 开发者如果还没有申请, 可通过 <a>https://console.zego.im/acount/login</a> 申请 AppID
        // AppID 和 AppSign 由 ZEGO 分配给各 App。其中，为了安全考虑，建议将 AppSign 存储在 App 的业务后台，需要使用时从后台获取
        // 如果不需要再继续使用 SDK 可调用 g_ZegoApi.unInitSDK() 释放SDK

        mZegoLiveRoom.initSDK(ZegoUtil.parseAppIDFromString(appID), ZegoUtil.parseSignKeyFromString(appSign), new IZegoInitSDKCompletionCallback() {
            @Override
            public void onInitSDK(int errorCode) {
                // errorCode 非0 代表初始化sdk失败
                // 具体错误码说明请查看<a> https://doc.zego.im/CN/308.html </a>
                setMethodCallback("init",errorCode == 0,errorCode);
                if(errorCode == 0){

                    //-------------------------设置房间回调------------------------
                    mZegoLiveRoom.setZegoRoomCallback(new IZegoRoomCallback() {
                        @Override
                        public void onKickOut(int i, String s, String s1) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","KickOut");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            jsonObject.put("String1",s1);
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onDisconnect(int i, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","Disconnect");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onReconnect(int i, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","Reconnect");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onTempBroken(int i, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","TempBroken");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onStreamUpdated(int i, ZegoStreamInfo[] zegoStreamInfos, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","StreamUpdated");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            jsonObject.put("ZegoStreamInfo", JSON.toJSON(zegoStreamInfos));
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onStreamExtraInfoUpdated(ZegoStreamInfo[] zegoStreamInfos, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","StreamExtraInfoUpdated");
                            jsonObject.put("String",s);
                            jsonObject.put("ZegoStreamInfo",JSON.toJSON(zegoStreamInfos));
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onRecvCustomCommand(String s, String s1, String s2, String s3) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","RecvCustomCommand");
                            jsonObject.put("String",s);
                            jsonObject.put("String1",s1);
                            jsonObject.put("String2",s2);
                            jsonObject.put("String3",s3);
                            try {
                                mMessageHandle.Callback("room",jsonObject);
                            }catch (Exception e){

                            }
                        }
                    });

                    //-------------------------设置拉流回调------------------------
                    mZegoLiveRoom.setZegoLivePlayerCallback(new IZegoLivePlayerCallback(){

                        @Override
                        public void onPlayStateUpdate(int i, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","PlayStateUpdate");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            try {
                                mMessageHandle.Callback("player",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onPlayQualityUpdate(String s, ZegoPlayStreamQuality zegoPlayStreamQuality) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","PlayQualityUpdate");
                            jsonObject.put("ZegoPlayStreamQuality",zegoPlayStreamQuality);
                            try {
                                mMessageHandle.Callback("player",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onInviteJoinLiveRequest(int i, String s, String s1, String s2) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","InviteJoinLiveRequest");
                            jsonObject.put("code",i);
                            jsonObject.put("String",s);
                            jsonObject.put("String1",s1);
                            jsonObject.put("String2",s2);
                            try {
                                mMessageHandle.Callback("player",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onRecvEndJoinLiveCommand(String s, String s1, String s2) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","RecvEndJoinLiveCommand");
                            jsonObject.put("String",s);
                            jsonObject.put("String1",s1);
                            jsonObject.put("String2",s2);
                            try {
                                mMessageHandle.Callback("player",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onVideoSizeChangedTo(String s, int i, int i1) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","VideoSizeChangedTo");
                            jsonObject.put("String",s);
                            jsonObject.put("i",i);
                            jsonObject.put("i1",i1);
                            try {
                                mMessageHandle.Callback("player",jsonObject);
                            }catch (Exception e){

                            }
                        }
                    });
                    //-------------------------设置IM回调------------------------
                    mZegoLiveRoom.setZegoIMCallback(new IZegoIMCallback() {
                        @Override
                        public void onUserUpdate(ZegoUserState[] zegoUserStates, int i) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","UserUpdate");
                            jsonObject.put("ZegoUserState",zegoUserStates);
                            jsonObject.put("code",i);
                            try {
                                mMessageHandle.Callback("im",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onRecvRoomMessage(String s, ZegoRoomMessage[] zegoRoomMessages) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","RecvRoomMessage");
                            jsonObject.put("ZegoRoomMessage",zegoRoomMessages);
                            jsonObject.put("roomId",s);
                            try {
                                mMessageHandle.Callback("im",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onUpdateOnlineCount(String s, int i) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","UpdateOnlineCount");
                            jsonObject.put("String",s);
                            jsonObject.put("code",i);
                            try {
                                mMessageHandle.Callback("im",jsonObject);
                            }catch (Exception e){

                            }
                        }

                        @Override
                        public void onRecvBigRoomMessage(String s, ZegoBigRoomMessage[] zegoBigRoomMessages) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","RecvBigRoomMessage");
                            jsonObject.put("String",s);
                            jsonObject.put("ZegoBigRoomMessage",zegoBigRoomMessages);
                            try {
                                mMessageHandle.Callback("im",jsonObject);
                            }catch (Exception e){

                            }
                        }
                    });
                    //-------------------------设置录制回调------------------------
                    mZegoMediaRecorder.setZegoMediaRecordCallback(new IZegoMediaRecordCallback() {
                        @Override
                        public void onRecordStatusUpdate(ZegoMediaRecordChannelIndex zegoMediaRecordChannelIndex, String s, long l, long l1) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","RecordStatusUpdate");
                            jsonObject.put("path",s);
                            jsonObject.put("ZegoMediaRecordChannelIndex",zegoMediaRecordChannelIndex);
                            jsonObject.put("duration",l);
                            jsonObject.put("fileSize",l1);
                            try {
                                mMessageHandle.Callback("record",jsonObject);
                            }catch (Exception e){
                                mMessageHandle.Callback("record","record exeption");
                            }
                        }

                        @Override
                        public void onMediaRecord(int i, ZegoMediaRecordChannelIndex zegoMediaRecordChannelIndex, String s) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type","MediaRecord");
                            jsonObject.put("path",s);
                            jsonObject.put("ZegoMediaRecordChannelIndex",zegoMediaRecordChannelIndex);
                            jsonObject.put("code",i);
                            try {
                                mMessageHandle.Callback("record",jsonObject);
                            }catch (Exception e){
                                mMessageHandle.Callback("record","record exeption");
                            }
                        }
                    });
                }
            }
        });
    }

    //释放
    public void unInit(){
        setMethodCallback("unInitSDK", mZegoLiveRoom.unInitSDK(),0);
    }

    //设置页面参数
    public void setViewConfig(String level,int width,int height,int fps,int bitrate){
        int mLevel = 2;
        switch (level.toUpperCase()){
            case "VERYLOW":
                mLevel = 0;
                break;
            case "LOW":
                mLevel = 1;
                break;
            case "HIGH":
                mLevel = 3;
                break;
            case "VERYHIGH":
                mLevel = 4;
                break;
            case "SUPERHIGH":
                mLevel = 5;
                break;
        }
        ZegoAvConfig zegoAvConfig = new ZegoAvConfig(mLevel);
        zegoAvConfig.setVideoEncodeResolution(width,height);
        zegoAvConfig.setVideoCaptureResolution(width,height);
        zegoAvConfig.setVideoFPS(fps);
        zegoAvConfig.setVideoBitrate(bitrate);
        mZegoLiveRoom.setAVConfig(zegoAvConfig);
    }

    //-------------------------------------用户--------------------------------------

    //设置用户信息
    public void setUser(String userId,String userName){
        setMethodCallback("setUser", ZegoLiveRoom.setUser(userId,userName),0);
    }

    //登录房间
    public void loginRoom(String roomId,String role){
        int mRole = 1;
        if(role.toUpperCase().equals("AUDIENCE")){
            mRole = 2;
        }
        Boolean ret = mZegoLiveRoom.loginRoom(roomId, mRole, new IZegoLoginCompletionCallback() {
            @Override
            public void onLoginCompletion(int i, ZegoStreamInfo[] zegoStreamInfos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ZegoStreamInfo",JSON.toJSON(zegoStreamInfos));
                jsonObject.put("code",i);
                mMessageHandle.Callback("loginRoom",jsonObject);
            }
        });
        if(!ret){
            setMethodCallback("loginRoom", false,0);
        }
    }

    //邀请观众连麦
    public void inviteJoinLive(String userId){
        mZegoLiveRoom.inviteJoinLive(userId, new IZegoResponseCallback() {
            @Override
            public void onResponse(int i, String s, String s1) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",i);
                jsonObject.put("s",s);
                jsonObject.put("s1",s1);
                mMessageHandle.Callback("inviteResponse",jsonObject);
            }
        });
    }

    //退出房间
    public void logoutRoom(){
        setMethodCallback("logoutRoom", mZegoLiveRoom.logoutRoom(),0);
    }

    //-------------------------------------预览界面--------------------------------------

    //开始预览
    public void startPreview(View view, final Boolean zoom, String channelIndex){
        setMethodCallback("setPreviewView", mZegoLiveRoom.setPreviewView(view),0);
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        final int finalChannel = channel;
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                JSONObject jsonObject = new JSONObject();
                mMessageHandle.Callback("viewTouch",JSON.toJSON(motionEvent));
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!zoom){
                            return true;
                        }
                        //Log.i(TAG, "test: move!!!");
                        /**
                         * 首先判断按下手指的个数是不是大于两个。
                         * 如果大于两个则执行以下操作（即图片的缩放操作）。
                         */
                        if (motionEvent.getPointerCount() >= 2) {

                            float offsetX = motionEvent.getX(0) - motionEvent.getX(1);
                            float offsetY = motionEvent.getY(0) - motionEvent.getY(1);
                            /**
                             * 原点和滑动后点的距离差
                             */
                            mCurrentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                            if (mLastDistance < 0) {
                                mLastDistance = mCurrentDistance;
                            } else {
                                mMaxZoomValue =   ZegoCamera.getCamMaxZoomFactor(finalChannel);

                                /**
                                 * 如果当前滑动的距离（currentDistance）比最后一次记录的距离（lastDistance）相比大于5英寸（也可以为其他尺寸），
                                 * 那么现实图片放大
                                 */
                                if (mCurrentDistance - mLastDistance > 10) {
                                    //Log.i(TAG, "test: 放大！！！");
                                    mCurrentZoomValue+=0.2;
                                    if(mCurrentZoomValue > mMaxZoomValue) {
                                        mCurrentZoomValue = mMaxZoomValue;
                                    }

                                    ZegoCamera.setCamZoomFactor(mCurrentZoomValue,finalChannel);

                                    mLastDistance = mCurrentDistance;
                                    /**
                                     * 如果最后的一次记录的距离（lastDistance）与当前的滑动距离（currentDistance）相比小于5英寸，
                                     * 那么图片缩小。
                                     */
                                } else if (mLastDistance - mCurrentDistance > 10) {
                                    //Log.i(TAG, "test: 缩小！！！");
                                    mCurrentZoomValue-=0.2;
                                    if(mCurrentZoomValue < 0) {
                                        mCurrentZoomValue = 0;
                                    }
                                    ZegoCamera.setCamZoomFactor(mCurrentZoomValue,finalChannel);
                                    mLastDistance = mCurrentDistance;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        setMethodCallback("startPreview", mZegoLiveRoom.startPreview(channel),0);
    }

    // 停止预览
    public void stopPreview(){
        setMethodCallback("stopPreview", mZegoLiveRoom.stopPreview(),0);
    }

    //预览填充类型
    public void setPreviewMode(String mode){
        int mMode = 1;
        switch (mode.toUpperCase()){
            case "SCALEASPECTFIT":
                mMode = 0;
                break;
            case "SCALETOFILL":
                mMode = 2;
                break;
        }
        setMethodCallback("setPreviewMode", mZegoLiveRoom.setPreviewViewMode(mMode),0);
    }

    //-------------------------------------推流拉流--------------------------------------
    //推流1
    public void startPublishing(String streamId,String title,String flag,String extraInfo){
        int mFlag = 0;
        switch (flag.toUpperCase()){
            case "SINGLEANCHOR":
                mFlag = 4;
                break;
            case "MIXSTREAM":
                mFlag = 2;
                break;
        }
        setMethodCallback("startPublishing", mZegoLiveRoom.startPublishing(streamId,title,mFlag,extraInfo),0);

    }

    //推流2
    public void startPublishing2(String streamId,String title,String flag,String channelIndex){
        int mFlag = 0;
        switch (flag.toUpperCase()){
            case "SINGLEANCHOR":
                mFlag = 4;
                break;
            case "MIXSTREAM":
                mFlag = 2;
                break;
        }
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        setMethodCallback("startPublishing2", mZegoLiveRoom.startPublishing2(streamId,title,mFlag,channel),0);
    }

    //停止推流
    public void stopPublishing(){
        setMethodCallback("stopPublishing", mZegoLiveRoom.stopPublishing(),0);
    }

    //拉流
    public void startPlayingStream(String streamId,View view){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                JSONObject jsonObject = new JSONObject();
                mMessageHandle.Callback("viewTouch",JSON.toJSON(motionEvent));
                return true;
            }
        });
        setMethodCallback("startPlayingStream", mZegoLiveRoom.startPlayingStream(streamId,view),0);
    }

    //设置拉流视图模式
    public void setPlayingViewMode(String streamId,String mode){
        int mMode = 1;
        switch (mode.toUpperCase()){
            case "SCALEASPECTFIT":
                mMode = 0;
                break;
            case "SCALETOFILL":
                mMode = 2;
                break;
        }
        setMethodCallback("setPlayingViewMode", mZegoLiveRoom.setViewMode(mMode,streamId),0);
    }

    //更新拉流视图
    public void updatePlayView(String streamId,View view){
        setMethodCallback("updatePlayView", mZegoLiveRoom.updatePlayView(streamId,view),0);
    }

    //停止拉流
    public void stopPlayingStream(String streamId){
        setMethodCallback("stopPlayingStream", mZegoLiveRoom.stopPlayingStream(streamId),0);
    }

    //-------------------------------------IM--------------------------------------
    //发送消息
    public void sendRoomMessage(String type,String category,String content){
        int mType = 1;
        switch (type.toUpperCase()){
            case "FILE":
                mType = 3;
                break;
            case "OTHERTYPE":
                mType = 100;
                break;
            case "PICTURE":
                mType = 2;
                break;
        }
        int mCategory = 1;
        switch (category.toUpperCase()){
            case "SYSTEM":
                mCategory = 2;
                break;
            case "LIKE":
                mCategory = 3;
                break;
            case "GIFT":
                mCategory = 4;
                break;
            case "OTHERCATEGORY":
                mCategory = 100;
                break;
        }
        Boolean ret = mZegoLiveRoom.sendRoomMessage(mType, mCategory, content, new IZegoRoomMessageCallback() {
            @Override
            public void onSendRoomMessage(int i, String s, long l) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",i);
                jsonObject.put("String",s);
                jsonObject.put("long",l);
                setMethodCallback("sendRoomMessage", true,i);
            }
        });
        if(!ret){
            setMethodCallback("sendRoomMessage", false,0);
        }
    }

    //-------------------------------------录制--------------------------------------
    //开始录制
    public void startRecord(String channelIndex,String recordType,String storagePath,String recordFormat){
        ZegoMediaRecordChannelIndex  channel = ZegoMediaRecordChannelIndex.MAIN;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = ZegoMediaRecordChannelIndex.AUX;
        }

        ZegoMediaRecordType type = ZegoMediaRecordType.BOTH;
        switch (recordType.toUpperCase()){
            case "AUDIO":
                type = ZegoMediaRecordType.AUDIO;
                break;
            case "VIDEO":
                type = ZegoMediaRecordType.VIDEO;
                break;
        }

        ZegoMediaRecordFormat format = ZegoMediaRecordFormat.MP4;
        if(recordFormat.toUpperCase().equals("FLV")){
            format = ZegoMediaRecordFormat.FLV;
        }
        setMethodCallback("startRecord",  mZegoMediaRecorder.startRecord(channel,type,storagePath,true,1000, format),0);
    }

    //停止录制
    public void stopRecord(String channelIndex){
        ZegoMediaRecordChannelIndex  channel = ZegoMediaRecordChannelIndex.MAIN;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = ZegoMediaRecordChannelIndex.AUX;
        }
        setMethodCallback("stopRecord",  mZegoMediaRecorder.stopRecord(channel),0);
    }

    //-------------------------------------截图----------------------------------------
    //截图
    public void takePreviewSnapshot(String path,String channelIndex){
        if(path == ""){
            path = "/sdcard/1.jpg";
        }
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        final String finalPath = path;
        mZegoLiveRoom.takePreviewSnapshot(new IZegoSnapshotCompletionCallback2() {
            @Override
            public void onZegoSnapshotCompletion(int i, Bitmap bitmap) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("state",true);
                jsonObject.put("path",saveBitmap(finalPath,bitmap));
                mMessageHandle.Callback("takePicture", jsonObject);
            }
        },channel);
    }

    //保存图片
    public String saveBitmap(String path,Bitmap mBitmap) {
        if(path == ""){
            return "";
        }
        File filePic;
        try {
            filePic = new File(path);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        return filePic.getAbsolutePath();
    }

    //-------------------------------------美颜滤镜----------------------------------------
    //开启美颜
    public void enableBeautify(){
        setMethodCallback("enableBeautify",  mZegoLiveRoom.enableBeautifying(ZegoBeauty.NONE | ZegoBeauty.POLISH | ZegoBeauty.SKIN_WHITEN | ZegoBeauty.SHARPEN |ZegoBeauty.POLISH | ZegoBeauty.WHITEN),0);
    }

    //关闭美颜
    public void disableBeautify(){
        setMethodCallback("disableBeautify",  mZegoLiveRoom.enableBeautifying(0),0);
    }


    // 设置美颜美白的亮度修正参数
    public void setWhitenFactor(float f){
        mBeautify.whitenFactor = f;
        setMethodCallback("setWhitenFactor",  mZegoLiveRoom.setWhitenFactor(f),0);
    }

    // 设置美颜磨皮的采样步长
    public void setPolishStep(float f){
        mBeautify.polishStep = f;
        setMethodCallback("setPolishStep",  mZegoLiveRoom.setPolishStep(f),0);
    }

    // 设置锐化参数
    public void setPolishFactor(float f){
        mBeautify.polishFactor = f;
        setMethodCallback("setPolishFactor",  mZegoLiveRoom.setPolishFactor(f),0);
    }

    // 设置美颜采样颜色阈值
    public void setSharpenFactor(float f){
        mBeautify.sharpenFactor = f;
        setMethodCallback("setSharpenFactor", mZegoLiveRoom.setSharpenFactor(f),0);
    }

    // 设置滤镜
    public void setFilter(String filter){
        int mFilter = 0;
        switch (filter.toUpperCase()){
            case "LOMO":
                mFilter = 1;
                break;
            case "BLACKWHITE":
                mFilter = 2;
                break;
            case "OLDSTYLE":
                mFilter = 3;
                break;
            case "GOTHIC":
                mFilter = 4;
                break;
            case "SHARPCOLOR":
                mFilter = 5;
                break;
            case "FADE":
                mFilter = 6;
                break;
            case "WINE":
                mFilter = 7;
                break;
            case "LIME":
                mFilter = 8;
                break;
            case "ROMANTIC":
                mFilter = 9;
                break;
            case "HALO":
                mFilter = 10;
                break;
            case "BLUE":
                mFilter = 11;
                break;
            case "ILLUSION":
                mFilter = 12;
                break;
            case "DARK":
                mFilter = 13;
                break;
        }
        setMethodCallback("setFilter",  mZegoLiveRoom.setFilter(mFilter),0);
    }




    //-------------------------------------音量回声--------------------------------------
    //麦克风开关
    public void enableMic(boolean enable,String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        mZegoLiveRoom.muteAudioPublish(!enable,channel);
        setMethodCallback("enableMic", mZegoLiveRoom.enableMic(enable),enable?1:0);
    }

    //混音开关
    public void enableAux(boolean enable){
        setMethodCallback("enableMic", mZegoAudioAux.enableAux(enable),enable?1:0);
    }

    //混音音量
    public void setAuxVolume(int volume){
        mZegoAudioAux.setAuxVolume(volume);
        mZegoAudioAux.setAuxPublishVolume(volume);
    }

    //音量降噪
    public void enableNoiseSuppress(boolean enable){
        mZegoLiveRoom.enableNoiseSuppress(enable);
    }

    //音量降噪模式
    public void setNoiseSuppressMode(String mode){
        int mMode = 1;
        switch (mode.toUpperCase()){
            case "LOW":
                mMode = 0;
                break;
            case "HIGH":
                mMode = 2;
                break;
        }
        mZegoLiveRoom.setNoiseSuppressMode(mMode);
    }

    //回声消除
    public void enableAEC(boolean enable){
        mZegoLiveRoom.enableAEC(enable);
        mZegoLiveRoom.enableAECWhenHeadsetDetected(enable);
    }

    //回声消除模式
    public void setAECMode(String mode){
        int mMode = 0;
        switch (mode.toUpperCase()){
            case "MEDIUM":
                mMode = 1;
                break;
            case "SOFT":
                mMode = 2;
                break;
        }
        mZegoLiveRoom.setAECMode(mMode);
    }

    //变声
    public void setVoiceChangerParam(String mode){
         float mMode = ZegoAudioProcessing.ZegoVoiceChangerCategory.NONE;
         switch (mode.toUpperCase()){
             case "MEN_TO_CHILD":
                 mMode = ZegoAudioProcessing.ZegoVoiceChangerCategory.MEN_TO_CHILD;
                 break;
             case "MEN_TO_WOMEN":
                 mMode = ZegoAudioProcessing.ZegoVoiceChangerCategory.MEN_TO_WOMEN;
                 break;
             case "WOMEN_TO_CHILD":
                 mMode = ZegoAudioProcessing.ZegoVoiceChangerCategory.WOMEN_TO_CHILD;
                 break;
             case "WOMEN_TO_MEN":
                 mMode = ZegoAudioProcessing.ZegoVoiceChangerCategory.WOMEN_TO_MEN;
                 break;
         }
         ZegoAudioProcessing.setVoiceChangerParam(mMode);
    }

    //-------------------------------------镜头相关--------------------------------------
    //切换摄像头
    public void setFrontCam(Boolean bool,String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        setMethodCallback("setFrontCam", mZegoLiveRoom.setFrontCam(bool,channel),bool?1:0);
    }


    //设置app朝向
    public void setAppOrientation(int orientation,String channelIndex){
        int rotation = 0;
        switch (orientation){
            case 90:
                rotation = 1;
                break;
            case 180:
                rotation = 2;
                break;
            case 270:
                rotation = 3;
                break;
        }
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        setMethodCallback("setAppOrientation", mZegoLiveRoom.setAppOrientation(rotation,channel),rotation);
    }

    //摄像头开关
    public void enableCamera(boolean enable, String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        mZegoLiveRoom.muteVideoPublish(!enable,channel);
        setMethodCallback("enableCamera", mZegoLiveRoom.enableCamera(enable,channel),enable?1:0);
    }

    //设置镜像
    public void setVideoMirrorMode(String mode,String channelIndex){
        int mMode = 2;
        switch (mode.toUpperCase()){
            case "BOTH":
                mMode = 1;
                break;
            case "PREVIEW":
                mMode = 0;
                break;
            case "PUBLISH":
                mMode = 3;
                break;
        }

        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        setMethodCallback("enablePreviewMirror", mZegoLiveRoom.setVideoMirrorMode(mMode,channel),mMode);
    }


    //获取摄像头支持最大变焦倍数
    public float getCamMaxZoomFactor(String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        return ZegoCamera.getCamMaxZoomFactor(channel);
    }

    //设置镜头变焦倍数
    public void setCamZoomFactor(float zoom,String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        ZegoCamera.setCamZoomFactor(zoom,channel);
    }


    //-------------------------------------水印--------------------------------------
    //添加水印
    public void setWaterMarkImagePath(String path,int x,int y,int w,int h,String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }

        Bitmap bitmap = getBitmap(path);
        if(bitmap == null){
            setMethodCallback("setWaterMarkImagePath",false,0);
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //设置想要的大小
        float newWidth = w == 0?(float)h/height * width:w;
        float newHeight = h == 0?(float)w/width * height:h;

        Rect rect = new Rect(x,y,(int)(x + newWidth),(int)(y + newHeight) * 5);

        setMethodCallback("setWaterMarkImagePath", ZegoLiveRoom.setWaterMarkImagePath("file:" + path),1);

        ZegoLiveRoom.setPublishWaterMarkRect(rect);
        ZegoLiveRoom.setPreviewWaterMarkRect(rect);
    }

    //读取本地图片为bitmap
    public Bitmap getBitmap(String path){
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e){
            return null;
        }
        return bitmap;
    }

    //-------------------------------------其他杂项--------------------------------------
    //闪光灯开关
    public void enableTorch(boolean enable,String channelIndex){
        int channel = 0;
        if(channelIndex.toUpperCase().equals("AUX")){
            channel = 1;
        }
        setMethodCallback("enableTorch", mZegoLiveRoom.enableTorch(enable,channel),enable?1:0);
    }

    //硬件编码开关
    public void requireHardwareEncoder(Boolean bool){
        setMethodCallback("requireHardwareEncoder",ZegoLiveRoom.requireHardwareEncoder(bool),bool?1:0);
    }

    //硬件解码开关
    public void requireHardwareDecoder(Boolean bool){
        setMethodCallback("requireHardwareDecoder",ZegoLiveRoom.requireHardwareDecoder(bool),bool?1:0);
    }

    //回调输出
    public void setMethodCallback(String flag,Boolean bool,int code) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("state",bool);
        jsonObject.put("code",code);
        mMessageHandle.Callback(flag,jsonObject);
    }

}
