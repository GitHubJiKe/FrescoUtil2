package com.ypf.frescoutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.PriorityThreadFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/4/14.
 */
public class FrescoUtil {
    private static FrescoUtil instance = null;

    /**
     * 基本设置
     *
     * @param context 上下文对象
     */
    private FrescoUtil(Context context) {
        Fresco.initialize(context);
    }

    /**
     * 带有缓存的设置
     *
     * @param context       上下文对象
     * @param diskCachePath 缓存路径
     */
    private FrescoUtil(Context context, String diskCachePath) {
        Fresco.initialize(context, getImagePipelineConfig(context, diskCachePath));
    }

    /**
     * @param context 上下文对象
     * @param args    缓存路径(一个参数，需要缓存才写，不需要，不写)
     * @return 返回FrescoUtil对象
     */
    public static FrescoUtil getInstance(Context context, String... args) {
        if (instance == null) {
            String path = args[0];
            if (TextUtils.isEmpty(path)) {
                instance = new FrescoUtil(context);
            } else {
                instance = new FrescoUtil(context, path);
            }
        }
        return instance;
    }

    /**
     * @param context       上下文对象
     * @param diskCachePath 缓存路径
     * @return ImagePipelineConfig对象
     */
    private ImagePipelineConfig getImagePipelineConfig(Context context, String diskCachePath) {
        Supplier<MemoryCacheParams> bitmapCacheParamsSupplier = new Supplier<MemoryCacheParams>() {
            private final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams((int) (16 * 1024 * 1024), // Max total size of elements in the cache
                    256, // Max entries in the cache
                    (int) (16 * 1024 * 1024),
                    128, // Max length of eviction queue
                    384); // Max cache entry size

            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        ExecutorSupplier executorSupplier = new ExecutorSupplier() {
            private final ThreadFactory backgroundThreadFactory = new PriorityThreadFactory(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            private final Executor mBackgroundTasksExecutor = Executors.newFixedThreadPool(8, backgroundThreadFactory);
            private final Executor mDecodeExecutor = Executors.newFixedThreadPool(4, backgroundThreadFactory);
            private final Executor mLightweightBackgroundTasksExecutor = Executors.newFixedThreadPool(6, backgroundThreadFactory);
            private final Executor mLocalStorageReadWriteExecutor = Executors.newFixedThreadPool(2);

            @Override
            public Executor forBackgroundTasks() {
                return mBackgroundTasksExecutor;
            }

            @Override
            public Executor forDecode() {
                return mDecodeExecutor;
            }

            @Override
            public Executor forLightweightBackgroundTasks() {
                return mLightweightBackgroundTasksExecutor;
            }

            @Override
            public Executor forLocalStorageRead() {
                return mLocalStorageReadWriteExecutor;
            }

            @Override
            public Executor forLocalStorageWrite() {
                return mLocalStorageReadWriteExecutor;
            }

        };
        DiskCacheConfig mainDiskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(new File(diskCachePath))
                .setBaseDirectoryName("cache")
                .setMaxCacheSize(16 * 1024 * 1024)
                .setMaxCacheSizeOnLowDiskSpace(8 * 1024 * 1024)
                .setMaxCacheSizeOnVeryLowDiskSpace(2 * 1024 * 1024)
                .setVersion(0)
                .build();
        DiskCacheConfig smallImageDiskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(new File(diskCachePath + "small"))
                .setBaseDirectoryName("cache")
                .setMaxCacheSize(16 * 1024 * 1024)
                .setMaxCacheSizeOnLowDiskSpace(8 * 1024 * 1024)
                .setMaxCacheSizeOnVeryLowDiskSpace(2 * 1024 * 1024)
                .setVersion(0)
                .build();
        int maxConnectionSum = 3;
        long aliveDuration = 60000;
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectionPool(new ConnectionPool(maxConnectionSum, aliveDuration));
        okHttpClient.setConnectTimeout(5000, TimeUnit.MILLISECONDS);
        okHttpClient.setReadTimeout(3000, TimeUnit.MILLISECONDS);
        okHttpClient.setWriteTimeout(2000, TimeUnit.MILLISECONDS);
        return OkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient)
                .setBitmapMemoryCacheParamsSupplier(bitmapCacheParamsSupplier)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .setDownsampleEnabled(true)
                .setExecutorSupplier(executorSupplier)
                .setMainDiskCacheConfig(mainDiskCacheConfig)
                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();

    }

    /**
     * @param simpleDraweeView view
     * @param url              图片URL
     * @param imageType        显示类型
     */
    public void displayPicFromNet(SimpleDraweeView simpleDraweeView, String url, ImageRequest.ImageType imageType, boolean isgif) {
        if (url.compareTo(checkTag(simpleDraweeView, url)) != 0) {
            simpleDraweeView.setTag(url);//设置tag为了避免重复加载相同url的图片

            DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(getImageRequest(url, imageType))//设置请求
                    .setTapToRetryEnabled(true)//点击再次加载
                    .setOldController(simpleDraweeView.getController())
                    .setAutoPlayAnimations(isgif)
                    .build();
            simpleDraweeView.setController(draweeController);
        }
    }

    /**
     * @param simpleDraweeView view
     * @param url              图片URL
     * @param imageType        显示类型
     * @param listener         外部监听器
     */
    public void displayPicFromNetWithListener(SimpleDraweeView simpleDraweeView, String url, ImageRequest.ImageType imageType, ControllerListener<? super ImageInfo> listener, boolean isgif) {
        if (url.compareTo(checkTag(simpleDraweeView, url)) != 0) {
            simpleDraweeView.setTag(url);//设置tag为了避免重复加载相同url的图片

            DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(getImageRequest(url, imageType))//设置请求
                    .setTapToRetryEnabled(true)//点击再次加载
                    .setOldController(simpleDraweeView.getController())
                    .setControllerListener(listener)
                    .setAutoPlayAnimations(isgif)
                    .build();
            simpleDraweeView.setController(draweeController);
        }
    }

    /**
     * @param simpleDraweeView view
     * @param url              图片URL
     * @return 新的URL
     */
    private String checkTag(SimpleDraweeView simpleDraweeView, String url) {
        String viewDispalyUrl = "";

        Object tagObject = simpleDraweeView.getTag();
        if (tagObject != null) {
            viewDispalyUrl = (String) tagObject;
        }
        return viewDispalyUrl;
    }

    /**
     * @param simpleDraweeView view
     * @param url              图片URL
     * @param imageType        显示类型
     */
    public void displayPicFromLocal(SimpleDraweeView simpleDraweeView, String url, ImageRequest.ImageType imageType, boolean isgif) {
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(getImageRequest(url, imageType))
                .setOldController(simpleDraweeView.getController())
                .setAutoPlayAnimations(isgif)
                .build();
        simpleDraweeView.setController(draweeController);
    }

    /**
     * @param simpleDraweeView view
     * @param pacakgeName      包名 例如："res://com.ypf.frescoutil/"
     * @param id               资源ＩＤ
     */
    public void displayPicFromResource(SimpleDraweeView simpleDraweeView, String pacakgeName, int id, boolean isgif) {
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(getImageRequest(pacakgeName + id, ImageRequest.ImageType.SMALL))
                .setOldController(simpleDraweeView.getController())
                .setAutoPlayAnimations(isgif)
                .build();
        simpleDraweeView.setController(draweeController);
    }

    /**
     * 优先请求本地，本地没有请求网络
     *
     * @param simpleDraweeView view
     * @param localUri         本地图片URL 例如：file://
     * @param networkUri       网络图片URL
     * @param imageType        显示类型
     */
    public void displayPicFromNetOrLocal(SimpleDraweeView simpleDraweeView, String localUri, String networkUri, ImageRequest.ImageType imageType, boolean isgif) {
        ImageRequest[] imageRequests = {getImageRequest(localUri, imageType), getImageRequest(networkUri, imageType)};//请求数组
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setFirstAvailableImageRequests(imageRequests)//设置第一个可见的请求
                .setOldController(simpleDraweeView.getController())
                .setAutoPlayAnimations(isgif)
                .build();
        simpleDraweeView.setController(draweeController);
    }

    /**
     * @param url       图片URL
     * @param imageType 显示类型
     * @return ImageRequest对象
     */
    private ImageRequest getImageRequest(String url, ImageRequest.ImageType imageType) {
        return ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setImageType(imageType)//设置加载类型
                .build();
    }

    /**
     * 关闭Fresco
     */
    public void shutdown() {
        Fresco.shutDown();
    }

}
