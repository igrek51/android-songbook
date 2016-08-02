package igrek.songbook.logic.filetree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import igrek.songbook.filesystem.Files;
import igrek.songbook.logic.exceptions.NoParentDirException;
import igrek.songbook.output.Output;

public class FileTreeManager {

    private Files files;

    private String currentPath;
    private String currentFileName = null;

    private List<FileItem> items;

    public FileTreeManager(Files files, String startPath) {
        this.files = files;
        if(files.isDirectory(startPath)) {
            currentPath = startPath;
        }else{
            Output.warn("Brak początkowego folderu: " + startPath);
            currentPath = "/";
        }
        updateCurrentPath();
    }

    public FileTreeManager(Files files) {
        this(files, "/");
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
        List<File> fileList = files.listFiles(currentPath);
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
            return files.openFileString(filePath);
        } catch (IOException e) {
            Output.error(e);
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
