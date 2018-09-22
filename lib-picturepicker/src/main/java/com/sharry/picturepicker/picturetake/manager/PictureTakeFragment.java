package com.sharry.picturepicker.picturetake.manager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.sharry.picturepicker.pricturecrop.manager.CropCallback;
import com.sharry.picturepicker.pricturecrop.manager.PictureCropManager;
import com.sharry.picturepicker.support.utils.FileUtil;
import com.sharry.picturepicker.support.utils.PictureUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Fragment
 */
public class PictureTakeFragment extends Fragment {

    public static final String TAG = PictureTakeFragment.class.getSimpleName();
    public static final String INTENT_ACTION_START_CAMERA = "android.media.action.IMAGE_CAPTURE";

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_TAKE = 0x000222;// 图片选择请求码

    public static PictureTakeFragment newInstance() {
        PictureTakeFragment fragment = new PictureTakeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Context mContext;
    private TakeConfig mConfig;
    private TakeCallback mTakeCallback;
    private File mTempFile;             // Temp file associated with camera.

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * 开始拍照
     */
    public void takePicture(TakeConfig config, TakeCallback callback) {
        this.mConfig = config;
        this.mTakeCallback = callback;
        mTempFile = FileUtil.createTempFileByDestDirectory(config.cameraDirectoryPath);
        Uri tempUri = FileUtil.getUriFromFile(mContext, mConfig.authority, mTempFile);
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CAMERA);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || null == mTakeCallback) return;
        switch (requestCode) {
            case REQUEST_CODE_TAKE:
                try {
                    // 1. 将拍摄后的图片, 压缩到 cameraDestFile 中
                    File cameraDestFile = FileUtil.createCameraDestFile(mConfig.cameraDirectoryPath);
                    PictureUtil.doCompress(mTempFile.getAbsolutePath(), cameraDestFile.getAbsolutePath(), mConfig.cameraDestQuality);
                    // 2. 处理图片裁剪
                    if (mConfig.isCropSupport) {
                        performCropPicture(cameraDestFile.getAbsolutePath());
                    } else {
                        // 3. 回调
                        mTakeCallback.onTakeComplete(cameraDestFile.getAbsolutePath());
                        // 刷新文件管理器
                        FileUtil.freshMediaStore(mContext, cameraDestFile);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Picture compress failed after camera take.", e);
                } finally {
                    mTempFile.delete();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理裁剪
     */
    private void performCropPicture(String cameraFilePath) {
        PictureCropManager.with(mContext)
                .setFileProviderAuthority(mConfig.authority)
                .setCropCircle(mConfig.isCropCircle)
                .setCropSize(mConfig.cropWidth, mConfig.cropHeight)// 期望的尺寸
                .setOriginFile(cameraFilePath)// 需要裁剪的文件路径
                .setCropDirectory(mConfig.cropDirectoryPath)// 裁剪后输出的文件路径
                .setCropQuality(mConfig.cropQuality)// 拍摄后已经压缩一次了, 裁剪时不压缩
                .crop(new CropCallback() {
                    @Override
                    public void onCropComplete(String path) {
                        mTakeCallback.onTakeComplete(path);
                    }
                });
    }

}
