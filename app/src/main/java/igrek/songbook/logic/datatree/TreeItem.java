package igrek.todotree.logic.datatree;

import java.util.ArrayList;
import java.util.List;

public class TreeItem {

    private String content = "";
    private List<TreeItem> children;
    private TreeItem parent = null;

    public TreeItem(TreeItem parent, String content) {
        children = new ArrayList<>();
        this.parent = parent;
        this.content = content;
    }

    /**
     * konstruktor kopiujący (razem z zawartością), element nadrzędny nie ma rodzica!
     * @param source    źródłowy element
     */
    public TreeItem(TreeItem source) {
        this.content = source.content;
        this.children = new ArrayList<>();
        for(TreeItem sourceChild : source.children){
            TreeItem newChild = new TreeItem(sourceChild);
            newChild.parent = this;
            this.children.add(newChild);
        }
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<TreeItem> getChildren() {
        return children;
    }

    public void setParent(TreeItem parent) {
        this.parent = parent;
    }

    public TreeItem getParent() {
        return parent;
    }

    public TreeItem getChild(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index < 0");
        }
        if (index >= children.size()) {
            throw new IndexOutOfBoundsException("index > size = " + children.size());
        }
        return children.get(index);
    }

    public int getChildIndex(TreeItem child){
        for(int i=0; i<children.size(); i++){
            if(children.get(i) == child){
                return i;
            }
        }
        return -1;
    }

    public int getIndexInParent(){
        if(parent == null) return -1;
        return parent.getChildIndex(this);
    }

    public TreeItem getLastChild() {
        if (children.isEmpty()) return null;
        return children.get(children.size() - 1);
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void add(TreeItem newItem) {
        children.add(newItem);
    }

    public void add(int location, TreeItem newItem) {
        children.add(location, newItem);
    }

    public TreeItem add(String content) {
        TreeItem newItem = new TreeItem(this, content);
        children.add(newItem);
        return newItem;
    }

    public TreeItem add(int location, String content) {
        TreeItem newItem = new TreeItem(this, content);
        children.add(location, newItem);
        return newItem;
    }

    public void remove(int location) {
        children.remove(location);
    }

    public boolean remove(TreeItem item) {
        return children.remove(item);
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return content;
        } else {
            return content + " [" + children.size() + "]";
        }
    }
}
