package com.louis.user_center.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/13 22:49
 */
@Slf4j
public class ImportExcel {
    /**
     * 不交给IOC容器管理 直接执行main方法
     * @param args
     */
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String filePath = "D:\\桌面\\附件2.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取3000条数据 然后返回过来 直接调用使用数据就行
        synchronousRead(filePath);
    }
    /**
     * 监听器读取
     * 1. 创建excel对应的实体对象 参照{@link Info}
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，参照{@link DemoDataListener}
     * 3. 直接读
     */
    public static void readByListener(String filePath){
        EasyExcel.read(filePath, Info.class, new DemoDataListener()).sheet().doRead();
    }
    public static void synchronousRead(String filePath){
        List<Info> infoList = EasyExcel.read(filePath).head(Info.class).sheet().doReadSync();
        int cnt = 0;
        for (Info info : infoList) {
            System.out.println(info);
            cnt++;
        }
        System.out.println(cnt);
    }
}
