package igrek.songbook.logic.filetree;

public class FileItem {

    private String filename = "";
    private boolean folder = false;
    private FileItem parent = null;

    public FileItem(FileItem parent, String filename, boolean isFolder) {
        this.parent = parent;
        this.filename = filename;
        this.folder = isFolder;
    }

    public static FileItem file(FileItem parent, String filename){
        return new FileItem(parent, filename, false);
    }

    public static FileItem folder(FileItem parent, String filename){
        return new FileItem(parent, filename, true);
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileItem getParent() {
        return parent;
    }

    public void setParent(FileItem parent) {
        this.parent = parent;
    }

    public boolean isFolder() {
        //TODO
        return false;
    }

    public String getPath(){
        //TODO rekurencyjnie od rodzica
        return "/";
    }

    @Override
    public String toString() {
        if (!isFolder()) {
            return filename;
        } else {
            return " [" + filename + "]";
        }
    }
}
