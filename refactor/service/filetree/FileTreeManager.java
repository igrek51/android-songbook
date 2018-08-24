package igrek.songbook.service.filetree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import igrek.songbook.domain.filetree.FileItem;
import igrek.songbook.filesystem.Filesystem;
import igrek.songbook.logger.Logger;
import igrek.songbook.service.controller.AppController;
import igrek.songbook.domain.exception.NoParentDirException;

public class FileTreeManager {

    private String currentPath = null;
    private String currentFileName = null;

    private List<FileItem> items;

    public FileTreeManager(String homePath) {

        Filesystem filesystem = AppController.getService(Filesystem.class);

        currentPath = null;
        setCurrentPathIfNotSet(filesystem, homePath);
        if (currentPath == null) {
            Logger.warn("not existing starting directory: " + homePath + ", getting default");
        }
        setCurrentPathIfNotSet(filesystem, filesystem.pathSD().toString());
        setCurrentPathIfNotSet(filesystem, "/");

        updateCurrentPath();
    }

    private void setCurrentPathIfNotSet(Filesystem filesystem, String path) {
        if (currentPath == null) {
            if (path != null && filesystem.isDirectory(path)) {
                currentPath = path;
            }
        }
    }

    public static String trimEndSlash(String str) {
        while (!str.isEmpty() && str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public String getCurrentDirName() {
        if (currentPath.equals("/")) return currentPath;
        String path = trimEndSlash(currentPath);
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) return null;
        return path.substring(lastSlash + 1);
    }

    private String getParent() {
        if (currentPath.equals("/")) return null;
        String path = trimEndSlash(currentPath);
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) return null;
        path = path.substring(0, lastSlash);
        if(path.isEmpty()) path = "/";
        return path;
    }

    //  NAWIGACJA

    public void goUp() throws NoParentDirException {
        String parent = getParent();
        if (parent == null) {
            throw new NoParentDirException();
        }
        updateCurrentPath(parent);
    }

    public void goInto(String dir) {
        String path = trimEndSlash(currentPath) + "/" + trimEndSlash(dir);
        updateCurrentPath(path);
    }

    public void goTo(String path) {
        if(path.equals("/")){
            updateCurrentPath(path);
            return;
        }
        updateCurrentPath(trimEndSlash(path));
    }

    public void goToRoot() {
        currentPath = "/";
        updateCurrentPath();
    }

    private void updateCurrentPath(String currentPath) {
        this.currentPath = currentPath;
        updateCurrentPath();
    }

    private void updateCurrentPath() {

        Filesystem filesystem = AppController.getService(Filesystem.class);

        List<File> fileList = filesystem.listFiles(currentPath);
        items = new ArrayList<>();

        for (File f : fileList) {
            if (f.isDirectory()) {
                items.add(FileItem.directory(f.getName()));
            } else if (f.isFile()) {
                items.add(FileItem.file(f.getName()));
            }
        }

        Collections.sort(items, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem lhs, FileItem rhs) {
                if(lhs.isDirectory() && rhs.isRegularFile()){
                    return -1;
                }
                if(lhs.isRegularFile() && rhs.isDirectory()){
                    return +1;
                }
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });
    }

    public List<FileItem> getItems() {
        return items;
    }

    public String getCurrentFilePath(String filename){
        return trimEndSlash(currentPath) + "/" + trimEndSlash(filename);
    }

    public String getCurrentPath() {
        return trimEndSlash(currentPath);
    }

    public String getFileContent(String filePath){
        try {
            Filesystem filesystem = AppController.getService(Filesystem.class);
            return filesystem.openFileString(filePath);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }
}
