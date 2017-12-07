package com.hxf.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangqing on 12/7/17.
 */
public class Utils {

    public static List<File> getFileList(String strPath, List<File> filelist, String ext) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath(), filelist, ext); // 获取文件绝对路径
                } else if (fileName.endsWith(ext)) { // 判断文件名是否以txt结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }

        }
        return filelist;
    }


    public static void getFiles(String path, String ext) {
        File f = new File(path);

        FileFilter ff = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                // TODO Auto-generated method stub
                String s = pathname.getName().toLowerCase();

                if (s.endsWith(ext)) {
                    return true;
                }

                return false;
            }
        };

        File[] flist = f.listFiles(ff);

        if (flist == null) {
            System.out.println("the flist is null");
            return;
        }

        for (File fs : flist) {
            System.out.println(fs);
        }

    }

/*
//    hdfs utils
    public  static void  createdirs(String path){
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(path);
        fs.create(path);
        fs.close();




           Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        Path path = new Path("/user/hadoop/data/20130710");
        fs.delete(path);
        fs.close();




          Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path("/user/hadoop/data/write.txt");
        FSDataOutputStream out = fs.create(path);
        out.writeUTF("da jia hao,cai shi zhen de hao!");
        fs.close();



          Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path("/user/hadoop/data/write.txt");

        if(fs.exists(path)){
            FSDataInputStream is = fs.open(path);
            FileStatus status = fs.getFileStatus(path);
            byte[] buffer = new byte[Integer.parseInt(String.valueOf(status.getLen()))];
            is.readFully(0, buffer);
            is.close();
            fs.close();
            System.out.println(buffer.toString());
        }




          Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        Path src = new Path("/home/hadoop/word.txt");
        Path dst = new Path("/user/hadoop/data/");
        fs.copyFromLocalFile(src, dst);
        fs.close();


          Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        Path path = new Path("/user/hadoop/data/word.txt");
        fs.delete(path);
        fs.close();
    }








     public static void getFile(Path path,FileSystem fs) throws IOException {

        FileStatus[] fileStatus = fs.listStatus(path);
        for(int i=0;i<fileStatus.length;i++){
            if(fileStatus[i].isDir()){
                Path p = new Path(fileStatus[i].getPath().toString());
                getFile(p,fs);
            }else{
                System.out.println(fileStatus[i].getPath().toString());
            }
        }
        }


         /**
     * 查找某个文件在HDFS集群的位置
     * @Title:
     * @Description:
     * @param
     * @return
     * @throws
     */
/*
public static void getFileLocal() throws IOException{
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    Path path = new Path("/user/hadoop/data/write.txt");

    FileStatus status = fs.getFileStatus(path);
    BlockLocation[] locations = fs.getFileBlockLocations(status, 0, status.getLen());

    int length = locations.length;
    for(int i=0;i<length;i++){
        String[] hosts = locations[i].getHosts();
        System.out.println("block_" + i + "_location:" + hosts[i]);
    }
}


 /**
     * HDFS集群上所有节点名称信息
     * @Title:
     * @Description:
     * @param
     * @return
     * @throws
     */
/*
public static void getHDFSNode() throws IOException{
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);

    DistributedFileSystem  dfs = (DistributedFileSystem)fs;
    DatanodeInfo[] dataNodeStats = dfs.getDataNodeStats();

    for(int i=0;i<dataNodeStats.length;i++){
        System.out.println("DataNode_" + i + "_Node:" + dataNodeStats[i].getHostName());
    }

}


    */

    public static void main(String[] args) {
        ArrayList<File> list = new ArrayList<File>();
//        getFileList("/home/fangqing/Downloads", list, "pdf");
        getFiles("/home/fangqing/Downloads", "pdf");
//        System.out.println(list.toString());
    }
}
