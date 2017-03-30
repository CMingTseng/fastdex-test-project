package com.dx168.fastdex.build.snapshoot.api;

import com.dx168.fastdex.build.snapshoot.utils.SerializeUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.$Gson$Types;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by tong on 17/3/29.
 */
public abstract class Snapshoot<DIFF_INFO extends BaseDiffInfo,ITEM_INFO extends ItemInfo> implements STSerializable<Snapshoot<DIFF_INFO,ITEM_INFO>> {
    @Expose
    private Class<DIFF_INFO> diffInfoClass = null;

    public Collection<ITEM_INFO> itemInfos;

    @Expose
    public Map<String,ITEM_INFO> fileItemInfoMap;

    public Snapshoot() {
        createEmptyItemInfos();
    }

    protected void createEmptyItemInfos() {
        itemInfos = new HashSet<>();
    }

    /**
     * 添加内容
     * @param itemInfo
     */
    protected void addItemInfo(ITEM_INFO itemInfo) {
        itemInfos.add(itemInfo);
        if (fileItemInfoMap == null) {
            fileItemInfoMap = new HashMap<>();
        }
        fileItemInfoMap.put(itemInfo.getUniqueKey(),itemInfo);
    }

    /**
     * 获取所有的内容
     * @return
     */
    protected Collection<ITEM_INFO> getAllItemInfo() {
        return itemInfos;
    }

    /**
     * 通过索引获取内容
     * @param uniqueKey
     * @return
     */
    protected ITEM_INFO getItemInfoByUniqueKey(String uniqueKey) {
        ITEM_INFO itemInfo = null;
        if (fileItemInfoMap != null) {
            itemInfo = fileItemInfoMap.get(uniqueKey);
        }
        if (itemInfo == null) {
            for (ITEM_INFO info : itemInfos) {
                if (uniqueKey.equals(info.getUniqueKey())) {
                    if (fileItemInfoMap == null) {
                        fileItemInfoMap = new HashMap<>();
                    }
                    fileItemInfoMap.put(itemInfo.getUniqueKey(),info);
                    itemInfo = info;
                    break;
                }
            }
        }
        return itemInfo;
    }

    /**
     * 创建空的对比结果集
     * @return
     */
    protected Collection<DIFF_INFO> createEmptyDiffInfos() {
        return new HashSet<DIFF_INFO>();
    }

    /**
     * 获取对比结果类的类型
     * @return
     */
    protected Class<DIFF_INFO> getDiffInfoType() {
        if (diffInfoClass == null) {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class)
            {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            Type result = $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
            diffInfoClass = (Class<DIFF_INFO>) result;
        }
        return diffInfoClass;
    }

    /**
     * 创建一项内容的对比结果
     * @param status
     * @param now
     * @param old
     * @return
     */
    protected DIFF_INFO createDiffInfo(Status status, ITEM_INFO now, ITEM_INFO old) {
        Class<DIFF_INFO> clazz = getDiffInfoType();
        try {
            DIFF_INFO diffInfo = clazz.newInstance();
            diffInfo.status = status;
            diffInfo.now = now;
            diffInfo.old = old;

            switch (status) {
                case ADD:
                case MODIFIED:
                    diffInfo.uniqueKey = now.getUniqueKey();
                    break;
                case DELETE:
                    diffInfo.uniqueKey = old.getUniqueKey();
                    break;
            }

            return diffInfo;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 把一项内容的对比结果添加到结果集中
     * @param diffInfos
     * @param diffInfo
     */
    protected void addDiffInfo(Collection<DIFF_INFO> diffInfos,DIFF_INFO diffInfo) {
        diffInfos.add(diffInfo);
    }

    /**
     * 扫描变化项和删除项
     * @param diffInfos
     * @param otherSnapshoot
     * @param deletedItemInfos
     * @param increasedItemInfos
     */
    protected void scanFromDeletedAndIncreased(Collection<DIFF_INFO> diffInfos, Snapshoot<DIFF_INFO,ITEM_INFO> otherSnapshoot, Set<ITEM_INFO> deletedItemInfos, Set<ITEM_INFO> increasedItemInfos) {
        if (deletedItemInfos != null) {
            for (ITEM_INFO itemInfo : deletedItemInfos) {
                addDiffInfo(diffInfos,createDiffInfo(Status.DELETE,null,itemInfo));
            }
        }
        if (increasedItemInfos != null) {
            for (ITEM_INFO itemInfo : increasedItemInfos) {
                addDiffInfo(diffInfos,createDiffInfo(Status.ADD,itemInfo,null));
            }
        }
    }

    @Override
    public Snapshoot<DIFF_INFO,ITEM_INFO> load(InputStream inputStream) throws IOException {
        return SerializeUtils.load(inputStream,getClass());
    }

    @Override
    public void serializeTo(OutputStream outputStream) throws IOException {
        SerializeUtils.serializeTo(outputStream,this);
    }

    /**
     * 对比快照
     * @param otherSnapshoot
     * @return
     */
    public Collection<DIFF_INFO> diff(Snapshoot<DIFF_INFO,ITEM_INFO> otherSnapshoot) {
        //获取删除项
        Set<ITEM_INFO> deletedItemInfos = new HashSet<>();
        deletedItemInfos.addAll(otherSnapshoot.getAllItemInfo());
        deletedItemInfos.removeAll(getAllItemInfo());

        //新增项
        Set<ITEM_INFO> increasedItemInfos = new HashSet<>();
        increasedItemInfos.addAll(getAllItemInfo());
        increasedItemInfos.removeAll(otherSnapshoot.getAllItemInfo());

        //需要检测是否变化的列表
        Set<ITEM_INFO> needDiffFileInfos = new HashSet<>();
        needDiffFileInfos.addAll(getAllItemInfo());
        needDiffFileInfos.addAll(otherSnapshoot.getAllItemInfo());
        needDiffFileInfos.removeAll(deletedItemInfos);
        needDiffFileInfos.removeAll(increasedItemInfos);


        Collection<DIFF_INFO> diffInfos = createEmptyDiffInfos();
        scanFromDeletedAndIncreased(diffInfos,otherSnapshoot,deletedItemInfos,increasedItemInfos);

        for (ITEM_INFO itemInfo : needDiffFileInfos) {
            ITEM_INFO now = itemInfo;
            String uniqueKey = itemInfo.getUniqueKey();
            if (uniqueKey == null || uniqueKey.length() == 0) {
                throw new RuntimeException("UniqueKey can not be null or empty!!");
            }
            ITEM_INFO old = otherSnapshoot.getItemInfoByUniqueKey(uniqueKey);
            if (now.diff(old)) {
                addDiffInfo(diffInfos,createDiffInfo(Status.MODIFIED,now,old));
            }
        }
        return diffInfos;
    }
}