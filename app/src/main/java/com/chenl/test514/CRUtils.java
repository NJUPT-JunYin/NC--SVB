package com.chenl.test514;

import java.util.ArrayList;
import java.util.Random;

public class CRUtils {
    int uselessCt = 0;
    double BLoss1;
    double BLoss2;
    double CLoss1;
    double CLoss2;
    int w ;
    int u;
    int totalSlot;

    int[] avgDelay1 = new int[4];
    int[] avgDelay2 = new int[4];
    double[] avgScore1 = new double[4];
    int[] actionTime1 = new int[4];
    double[] avgScore2 = new double[4];
    int[] actionTime2 = new int[4];

    //缓冲区
    int[] tails1;//编码窗口尾指针，0开始
    int[] tails2;//编码窗口尾指针，0开始
    int[][] buf1;//U1 缓冲区 4行 w列
    int[][] buf2;//U2 缓冲区

    //丢包队列
    int[][] lossMap1;
    int[] firstLossPoint1;
    int[] lastLossPoint1;
    int[] seenPoint1;

    int[][] lossMap2;
    int[] firstLossPoint2;
    int[] lastLossPoint2;
    int[] seenPoint2;


    //ack值
    int[] ack1 = new int[4];
    int[] ack2 = new int[4];

    ArrayList<ArrayList<Integer>> lostList1 = new ArrayList<>();
    ArrayList<ArrayList<Integer>> lostList2 = new ArrayList<>();

    public void initBroad(double BLoss1,double BLoss2,double CLoss1,double CLoss2,int w,int u,int totalSlot){
        this.BLoss1 = BLoss1;
        this.BLoss2 = BLoss2;

        this.CLoss1 = CLoss1;
        this.CLoss2 = CLoss2;

        this.w = w;
        this.u = u;
        this.totalSlot = totalSlot;

        this.buf1 = new int[4][w];
        this.buf2 = new int[4][w];

        this.tails1 = new int[4];
        this.tails2 = new int[4];

        lossMap1 = new int[4][totalSlot+1];
        firstLossPoint1 = new int[4];
        lastLossPoint1 = new int[4];
        seenPoint1 = new int[4];

        lossMap2 = new int[4][totalSlot+1];
        firstLossPoint2 = new int[4];
        lastLossPoint2 = new int[4];
        seenPoint2 = new int[4];

        for (int i = 0; i < 4; i++) {
            ArrayList<Integer> list1 = new ArrayList<>();
            ArrayList<Integer> list2 = new ArrayList<>();
            lostList1.add(list1);
            lostList2.add(list2);
        }

    }
    public boolean isLost(double probability){
        double rand = new Random().nextDouble();
        return rand < probability;
    }

    //接收广播数据
    /*
     * user : 1,2
     * layer: [0~3]
     * */
    public void receiveB(int user,int layer,int packetNo){

        if(user == 1){
            //未丢失
            if(!isLost(BLoss1)){

                succB(1,layer,packetNo);

                //缓冲区操作
                //发生丢包
                if(tails1[layer] >= w - 1){
                    //删除后是否影响协作对方
                    if(buf1[layer][0] != 0 && lossMap2[layer][buf1[layer][0]] == 0){//删除后对方会丢包
                        lostList2.get(layer).add(buf1[layer][0]);
                        lossMap2[layer][buf1[layer][0]] = 1;
//                        if(buf1[layer][0] > firstLossPoint2[layer] && buf1[layer][0] < lastLossPoint2[layer]){
//                            int tmp = firstLossPoint2[layer];
//                            while(tmp >= firstLossPoint2[layer] && tmp <= buf1[layer][0]){
//                                if(lossMap2[layer][tmp] == 0){
//                                    lossMap2[layer][tmp] = 1;
//                                    lostList2.get(layer).add(tmp);
//                                }
//                                tmp++;
//                            }
//                            while(tmp >= buf1[layer][0] && tmp <= lastLossPoint2[layer]){//找到全局丢包之后的第一个局部丢包位置
//                                if(lossMap2[layer][tmp] == 0){
//                                    firstLossPoint2[layer] = tmp;
//                                    break;
//                                }
//                                tmp++;
//                            }
//                        }
                    }
                    //删除头数据
                    for (int i = 0; i < tails1[layer]; i++) {
                        buf1[layer][i] = buf1[layer][i+1];
                    }

                    buf1[layer][tails1[layer]] = 0;

                }
                buf1[layer][tails1[layer]] = packetNo;//存入缓冲区
                //1. 广播
                //2. 每次广播有丢包时
                //last~first之间全局丢包删除
                /*
                1.两个loss中该包全为0
                2.一个不为0，但不在该用户buf中
                * */

            }else{
                //ack?
                //发生丢包
                lossB(user, layer, packetNo);
            }
        }else{
            //未丢失
            if(!isLost(BLoss2)){

                succB(2,layer,packetNo);

                //缓冲区操作

                //发生丢包
                if(tails2[layer] >= w - 1){
                    //删除后是否影响协作对方
                    if(buf2[layer][0] != 0 && lossMap1[layer][buf2[layer][0]] == 0){//删除后对方会丢包
                        lostList1.get(layer).add(buf2[layer][0]);
                        lossMap1[layer][buf2[layer][0]] = 1;
//                        if(buf2[layer][0] >= firstLossPoint1[layer] && buf2[layer][0] <= lastLossPoint1[layer]){
//                            int tmp = firstLossPoint1[layer];
//                            while(tmp >= firstLossPoint1[layer] && tmp <= buf2[layer][0]){
//                                if(lossMap1[layer][tmp] == 0){
//                                    lossMap1[layer][tmp] = 1;
//                                    lostList1.get(layer).add(tmp);
//                                }
//                                tmp++;
//                            }
//                            while(tmp >= buf2[layer][0] && tmp <= lastLossPoint1[layer]){//找到全局丢包之后的第一个局部丢包位置
//                                if(lossMap1[layer][tmp] == 0){
//                                    firstLossPoint1[layer] = tmp;
//                                    break;
//                                }
//                                tmp++;
//                            }
//                        }
                    }
                    //删除头数据
                    for (int i = 0; i < tails2[layer]; i++) {
                        buf2[layer][i] = buf2[layer][i+1];
                    }
                    buf2[layer][tails2[layer]] = 0;
                }
                buf2[layer][tails2[layer]] = packetNo;
            }else{
                //发生丢包
                lossB(user, layer, packetNo);
            }
        }
    }
    public void succB(int user,int layer,int packetNo){
        switch (user){
            case 1:
                lossMap1[layer][packetNo] = 1;
            case 2:
                lossMap2[layer][packetNo] = 1;
            default:
                break;
        }
    }
    public void lossB(int user,int layer,int packetNo){
        switch (user){
            case 1:
                lossMap1[layer][packetNo] = 0;
            case 2:
                lossMap2[layer][packetNo] = 0;
            default:
                break;
        }
    }

    //接收协作数据
    public void sentC(int receiveUser,int layer,int nowSlot){

        switch (receiveUser){
            //2 --> 1
            case 1:
                if(!isLost(CLoss1)){
                    //2.编码包对1的解码 双指针 两指针重合时解码
                    //0.根据ack移动缓冲区指针
                    //3.更新ack
                    int ack = 0;
                    while(ack < w && buf2[layer][ack] != 0){
                        if(lossMap1[layer][buf2[layer][ack]] == 0){//看见一个包
                            seenPoint1[layer] = buf2[layer][ack];
                            break;
                        }
                        ack++;
                    }
                    if(seenPoint1[layer] >= lastLossPoint1[layer]){
                        //解码成功
                        //计算解码时延
                        for (int i = firstLossPoint1[layer]; i <= lastLossPoint1[layer]; i++) {
                            if(lossMap1[layer][i] == 0){
                                lossMap1[layer][i] = 1;//解码成功置1
                                avgDelay1[layer] += (nowSlot - i);
                            }
                        }

                        //seen，first，last移至最新的0
                        while(lastLossPoint1[layer] <= nowSlot){
                            if(lossMap1[layer][lastLossPoint1[layer]] == 0){
                                break;
                            }
                            lastLossPoint1[layer]++;
                        }
                        seenPoint1[layer] = 0;
                        firstLossPoint1[layer] = lastLossPoint1[layer];

                    }
                    ack2[layer] = ack;
                    removeTails(receiveUser, ack2, layer);
                }

            case 2:
                if(!isLost(CLoss2)){
                    //2.编码包对1的解码 双指针 两指针重合时解码
                    //0.根据ack移动缓冲区指针
                    //3.更新ack
                    int ack = 0;
                    //
                    if(buf1[layer][0] > lastLossPoint2[layer] && seenPoint2[layer] < lastLossPoint2[layer]){
                        for (int i = firstLossPoint2[layer]; i <= lastLossPoint2[layer] ; i++) {
                            if(lossMap2[layer][i] == 0){
                                lostList2.get(layer).add(i);
                                lossMap2[layer][i] = 1;
                            }
                        }
                        seenPoint2[layer] = 0;
                        firstLossPoint2[layer] = 0;
                        lastLossPoint2[layer] = 0;
                        ack = w;
                    }else{
                        if(lastLossPoint2[layer] == 0){
                            ack = w;
                        }else{
                            for (int i = 0; i < w; i++) {
                                if((lossMap2[layer][buf1[layer][i]] == 0) && (buf1[layer][i] > seenPoint2[layer])){
                                    seenPoint2[layer] = buf1[layer][ack];
                                    ack++;
                                    break;
                                } else {
                                    ack++;
                                }
                            }
                            for (int i = ack; i < w; i++) {
                                if(lossMap2[layer][buf1[layer][i]] == 1){
                                    ack++;
                                }else {
                                    break;
                                }
                            }
                            if(seenPoint2[layer] == lastLossPoint2[layer]){
                                //解码成功
                                //计算解码时延
                                for (int i = firstLossPoint2[layer]; i <= lastLossPoint2[layer]; i++) {
                                    if(lossMap2[layer][i] == 0){
                                        lossMap2[layer][i] = 1;//解码成功置1
                                        avgDelay2[layer] += (nowSlot - i);
                                    }
                                }

                                //seen，first，last至0
                                seenPoint2[layer] = 0;
                                firstLossPoint2[layer] = 0;
                                lastLossPoint2[layer] = 0;
                                ack = w;
                            }

                        }
                    }
                    ack1[layer] = ack;
                    removeTails(receiveUser, ack1, layer);
                }
            default:
                break;
        }
    }
    public void removeTails(int user,int[] ack, int layer){
        switch (user){
            case 1:
                if(ack[layer] != 0){
                    for (int j = 0; j < w-ack[layer]; j++) {
                        buf1[layer][j] = buf1[layer][j+ack[layer]];
                    }
                    for (int j = w-ack[layer]; j < w; j++) {
                        buf1[layer][j] = 0;
                    }
                }
            case 2:
                if(ack[layer] != 0){
                    for (int j = 0; j < w-ack[layer]; j++) {
                        buf2[layer][j] = buf2[layer][j+ack[layer]];
                    }
                    for (int j = w-ack[layer]; j < w; j++) {
                        buf2[layer][j] = 0;
                    }
                }
            default:
                break;
        }
    }

    public int getLayer(int sentUser){
        return (int) (4 * new Random().nextDouble());
    }
    public int getLayerP(int sentUser){//丢包最多优先
        int max = -1;
        int action = -1;
        for (int i = 0; i < 4; i++) {
            int score = getLossNum(sentUser,i);
            if(max < score){
                action = i;
                max = score;
            }
        }
        System.out.println(action);
        return action;
    }
    public int getLayerNew(int sentUser,int nowSlot){
        double max = -1;
        int action = -1;
        double[] score = new double[4];
        for (int i = 0; i < 4; i++) {
            score[i] = getScore(sentUser,i,nowSlot);
            if(score[i] > max){
                action = i;
                max = score[i];
            }
        }
        switch (sentUser){
            case 1:
                avgScore1[action] += max;
                actionTime1[action] ++;
                if(action == 0 && score[0] == 0){
                    uselessCt++;
                }
            case 2:
                avgScore2[action] += max;
                actionTime2[action] ++;
            default:
                break;
        }
        return action;
    }
    public double getScore(int sentUser,int action,int nowSlot){
        double sum = 0;//a1=4,a2=3
        //优先级系数
        if(getUsefulPacketNum(sentUser,action) != 0){
            sum += (4-action) * 0.6;
            if(canDecodeAtOneC(sentUser,action)){
                sum += 0.1 * (4-action) * (getDelayScore(sentUser, action, nowSlot));
            }
            sum += 0.1 * (4-action) * getAvgChoose(sentUser,action);
        }
//        if(canDecodeAtOneC(sentUser,action)){
//            sum += 0.3 * (4-action)  * (getDelayScore(sentUser, action, nowSlot));
//        }
//        sum += 0.1 * (4-action) * getAvgChoose(sentUser,action);
        return sum;
    }
    public double getAvgChoose(int sentUser, int action){
        switch (sentUser){
            case 1:
                if(actionTime1[action] != 0){
                    return  avgScore1[action] / actionTime1[action];
                }
            case 2:
                if(actionTime2[action] != 0){
                    return avgScore2[action] / actionTime2[action];
                }
            default:
                break;
        }
        return 0;
    }
    public double getDelayScore(int sentUser,int action,int nowSlot){
        int sum = 0;
        switch (sentUser){
            case 1:
                if(firstLossPoint2[action] == 0 || lastLossPoint2[action] == 0){
                    return 0;
                }
                for (int j = firstLossPoint2[action]; j <= lastLossPoint2[action]; j++) {
                    if(lossMap2[action][j] == 0){//看见一个包
                        sum += (nowSlot-j);
                    }
                }
                return sum;
            case 2:
                for (int j = firstLossPoint1[action]; j <= lastLossPoint1[action]; j++) {
                    if(lossMap1[action][j] == 0){//看见一个包
                        sum += (nowSlot-j);
                    }
                }
                return sum;
            default:
                return 0;
        }
    }
    public int getSeenNum(int user,int action){
        int sum = 0;
        switch (user){
            case 1:
                for (int i = 0; i < w; i++) {
                    if(buf1[action][i] == 0 ){
                        break;
                    }
                    if(lossMap2[action][buf1[action][i]] == 0 && buf1[action][i] >= firstLossPoint2[action]
                            && buf1[action][i] <= lastLossPoint2[action]){//看见一个包
                        sum++;
                    }
                }
                return sum;
            case 2:
                for (int i = 0; i < w; i++) {
                    if(buf2[action][i] == 0 ){
                        break;
                    }
                    if(lossMap1[action][buf2[action][i]] == 0 && buf2[action][i] >= firstLossPoint1[action]
                            && buf2[action][i] <= lastLossPoint1[action]){//看见一个包
                        sum++;
                    }
                }
                return sum;
            default:
                return 0;
        }
    }
    public boolean isDecode(int sentUser,int action){
        int ack = 0;
        int seenTmp = seenPoint2[action];
        switch (sentUser){
            case 1:
                while(ack < w && buf1[action][ack] != 0){
                    if(lossMap2[action][buf1[action][ack]] == 0){//看见一个包
                        seenTmp = buf1[action][ack];
                        break;
                    }
                    ack++;
                }
                return seenTmp == lastLossPoint2[action];
            case 2:
                while(ack < w && buf2[action][ack] != 0){
                    if(lossMap1[action][buf2[action][ack]] == 0){//看见一个包
                        seenTmp = buf2[action][ack];
                        break;
                    }
                    ack++;
                }
                return seenTmp == lastLossPoint1[action];
            default:
                return false;
        }
    }
    public int getLossNum(int sentUser,int layer){
        int ans = 0;
        switch (sentUser){
            case 1:
                for (int j = firstLossPoint2[layer]; j <= lastLossPoint2[layer] && j <= totalSlot; j++) {
                    if(lossMap2[layer][j] == 0){
                        ans++;
                    }
                }
                return ans;
            case 2:
                for (int j = firstLossPoint1[layer]; j <= lastLossPoint1[layer] && j <= totalSlot; j++) {
                    if(lossMap1[layer][j] == 0){
                        ans++;
                    }
                }
                return ans;
            default:
                return 0;
        }
    }
    public int getUsefulPacketNum(int sentUser,int layer){
        int sum = 0;
        switch (sentUser){
            case 1:
                for (int i = 0; i < w; i++) {
                    if(buf1[layer][i] == 0){
                        break;
                    }
                    if(lossMap2[layer][buf1[layer][i]] == 0){
                        sum++;
                    }
                }
                return sum;
            case 2:
                for (int i = 0; i < w; i++) {
                    if(buf2[layer][i] == 0){
                        break;
                    }
                    if(lossMap1[layer][buf2[layer][i]] == 0){
                        sum++;
                    }
                }
                return sum;
            default:
                return 0;
        }
    }
    public boolean canDecodeAtOneC(int sentUser,int layer){
        int needPacket = 0;
        int needPacketNo = 0;
        boolean flag = false;
        switch (sentUser){
            case 1:
                if(lastLossPoint2[layer] == 0 || seenPoint2[layer] == 0){
                    return false;
                }
                for (int i = seenPoint2[layer] + 1; i < lastLossPoint2[layer]; i++) {
                    if(lossMap2[layer][i] == 0){
                        needPacket ++;
                        if(needPacket == 1){
                            needPacketNo = i;
                        }
                    }
                }
                for (int i = 0; i < w; i++) {
                    if(buf1[layer][i] == 0){
                        break;
                    }
                    if (buf1[layer][i] == needPacketNo) {
                        flag = true;
                        break;
                    }
                }
                return needPacket == 1 && flag;
            case 2:
                if(lastLossPoint1[layer] == 0 || seenPoint1[layer] == 0){
                    return false;
                }
                for (int i = seenPoint1[layer] + 1; i < lastLossPoint1[layer]; i++) {
                    if(lossMap1[layer][i] == 0){
                        needPacket ++;
                        if(needPacket == 1){
                            needPacketNo = i;
                        }
                    }
                }
                for (int i = 0; i < w; i++) {
                    if(buf2[layer][i] == 0){
                        break;
                    }
                    if (buf2[layer][i] == needPacketNo) {
                        flag = true;
                        break;
                    }
                }
                return needPacket == 1 && flag;
            default:
                return false;
        }
    }
}

