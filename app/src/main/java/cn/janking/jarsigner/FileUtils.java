package cn.janking.jarsigner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileUtils {
    /**
     * 复制文件
     */
    public static void copyFileToFile(InputStream inputStream, String toFile) throws IOException {
        FileOutputStream outputStream;
        outputStream = new FileOutputStream(getExistFile(toFile));
        copyFileToFile(inputStream, outputStream);
    }

    /**
     * 复制文件到文件
     * 内部具体实现
     */
    private static void copyFileToFile(InputStream fromFile, OutputStream toFile) throws IOException {
        //把读取到的内容写入新文件
        //把字节数组设置大一些   1*1024*1024=1M
        byte[] bs = new byte[1 * 1024 * 1024];
        int count = 0;
        while ((count = fromFile.read(bs)) != -1) {
            toFile.write(bs, 0, count);
        }
        //关闭流
        fromFile.close();
        toFile.flush();
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    public static File getExistFile(String filePath) throws IOException {
        return getExistFile(new File(filePath));
    }

    /**
     * 获取一个File类型的文件，如果不存在，尝试创建
     */
    public static File getExistFile(File file) throws IOException {
        File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!file.exists()) {
            file.createNewFile();//有路径才能创建文件
        } else if (file.isDirectory()) {
            file.delete();
            file.createNewFile();
        }
        return file;
    }

    /**
     * Return the extension of file.
     *
     * @param file The file.
     * @return the extension of file
     */
    public static String getFileExtension(final File file) {
        if (file == null) return "";
        return getFileExtension(file.getPath());
    }

    /**
     * Return the extension of file.
     *
     * @param filePath The path of file.
     * @return the extension of file
     */
    public static String getFileExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }


    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
