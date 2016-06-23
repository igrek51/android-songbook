package igrek.todotree.system.files;

import java.io.File;

public class PathBuilder {
    String pathstr;

    public PathBuilder(String pathstr) {
        this.pathstr = cutSlashFromEnd(pathstr);
    }


    /**
     * konstruktor kopiujący
     * @param src
     */
    public PathBuilder(PathBuilder src) {
        this.pathstr = src.pathstr;
    }

    @Override
    public String toString() {
        return pathstr;
    }

    public File getFile(){
        return new File(pathstr);
    }

    public static String cutSlashFromBeginning(String pathstr) {
        while (pathstr.length() > 0 && pathstr.charAt(0) == '/') {
            pathstr = pathstr.substring(1);
        }
        return pathstr;
    }

    public static String cutSlashFromEnd(String pathstr) {
        while (pathstr.length() > 0 && pathstr.charAt(pathstr.length() - 1) == '/') {
            pathstr = pathstr.substring(0, pathstr.length() - 1);
        }
        return pathstr;
    }

    public static String trimSlash(String pathstr){
        while (pathstr.length() > 0 && pathstr.charAt(0) == '/') {
            pathstr = pathstr.substring(1);
        }
        while (pathstr.length() > 0 && pathstr.charAt(pathstr.length() - 1) == '/') {
            pathstr = pathstr.substring(0, pathstr.length() - 1);
        }
        return pathstr;
    }

    public static String removeExtension(String pathStr){
        int lastDot = cutSlashFromEnd(pathStr).lastIndexOf(".");
        int lastSlash = cutSlashFromEnd(pathStr).lastIndexOf("/");
        if(lastDot < 0) return pathStr; //nie ma kropki
        if(lastDot < lastSlash) return pathStr; //kropka jest przed ostatnim slashem
        return pathStr.substring(0, lastDot);
    }

    public PathBuilder append(String pathstr) {
        String newPathstr = cutSlashFromEnd(this.pathstr) + "/" + trimSlash(pathstr);
        return new PathBuilder(newPathstr);
    }

    public PathBuilder parent(){
        if(pathstr.equals("/")) return null;
        PathBuilder copy = new PathBuilder(this);
        int lastSlash = cutSlashFromEnd(copy.pathstr).lastIndexOf("/");
        if(lastSlash < 0) return null;
        copy.pathstr = copy.pathstr.substring(0, lastSlash);
        return copy;
    }
}
