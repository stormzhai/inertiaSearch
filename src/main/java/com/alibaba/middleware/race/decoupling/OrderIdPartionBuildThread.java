package com.alibaba.middleware.race.decoupling;

import com.alibaba.middleware.race.RaceConf;
import com.alibaba.middleware.race.codec.HashKeyHash;
import com.alibaba.middleware.race.models.comparableKeys.ComparableKeysByOrderId;
import com.alibaba.middleware.race.storage.IndexPartition;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiyuanbupt on 7/26/16.
 * orderid 索引创建线程
 */
public class OrderIdPartionBuildThread extends PartionBuildThread<ComparableKeysByOrderId>{

    public OrderIdPartionBuildThread(AtomicInteger nRemain, CountDownLatch sendFinishSingle){
        super(nRemain,sendFinishSingle);
        this.keysQueue = DiskLocQueues.comparableKeysByOrderId;
        this.myPartions = indexNameSpace.mOrderPartion;
    }

    @Override
    protected void putIndexToPartion(ComparableKeysByOrderId comparableKeysByOrderId) {
        int hashCode = HashKeyHash.hashKeyHash(comparableKeysByOrderId.hashCode());
        myPartions.get(hashCode).addKey(comparableKeysByOrderId);
    }

    /**
     * 重构,按照partion 的数目分为多路执行
     */
    @Override
    protected void createBPlusTree() {
        for(Map.Entry<Integer,IndexPartition<ComparableKeysByOrderId>> entry:myPartions.entrySet()){
            entry.getValue().merageAndBuildMe();
        }
    }
}
