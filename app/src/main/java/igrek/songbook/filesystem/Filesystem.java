package igrek.songbook.filesystem;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import igrek.songbook.logger.Logs;
import igrek.songbook.logic.controller.services.IService;

public class Filesystem implements IService {

    Activity activity;
    private String pathToExtSD;

    public Filesystem(Activity activity) {
        this.activity = activity;
        pathSDInit();
    }

    private void pathSDInit() {
        pathToExtSD = "/storage/extSdCard";
        if (!exists(pathToExtSD)) {
            pathToExtSD = Environment.getExternalStorageDirectory().toString();
        }
    }

    public PathBuilder pathSD() {
        return new PathBuilder(pathToExtSD);
    }

    public String internalAppDirectory() {
        return activity.getFilesDir().toString();
    }

    public List<String> listDir(String path) {
        List<String> lista = new ArrayList<>();
        File f = new File(path);
        File file[] = f.listFiles();
        for (File aFile : file) {
            lista.add(aFile.getName());
        }
        return lista;
    }

    public List<String> listDir(PathBuilder path) {
        return listDir(path.toString());
    }

    public List<File> listFiles(String path) {
        List<File> files = new ArrayList<>();
        File f = new File(path);
        File file[] = f.listFiles();
        if (file == null) {
            Logs.warn("file array null for path: " + path);
            return files;
        }
        for (File aFile : file) {
            files.add(aFile);
        }
        return files;
    }

    public List<File> listFiles(PathBuilder path) {
        return listFiles(path.toString());
    }

    public byte[] openFile(String filename) throws IOException {
        RandomAccessFile f = new RandomAccessFile(new File(filename), "r");
        int length = (int) f.length();
        byte[] data = new byte[length];
        f.readFully(data);
        f.close();
        return data;
    }

    public String openFileString(String filename) throws IOException {
        byte[] bytes = openFile(filename);
        CharsetDetector charsetDetector = new CharsetDetector();
        String charsetName = charsetDetector.detect(bytes);
        bytes = charsetDetector.repair(bytes, charsetName);
        return new String(bytes, charsetName);
    }

    public void saveFile(String filename, byte[] data) throws IOException {
        File file = new File(filename);
        FileOutputStream fos;
        fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public void saveFile(String filename, String str) throws IOException {
        saveFile(filename, str.getBytes());
    }

    public boolean isDirectory(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    public boolean isFile(String path) {
        File f = new File(path);
        return f.exists() && f.isFile();
    }

    public boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }

    public boolean delete(String path) {
        File file = new File(path);
        return file.delete();
    }

    public boolean delete(PathBuilder path) {
        return delete(path.toString());
    }

    public void copy(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
}
