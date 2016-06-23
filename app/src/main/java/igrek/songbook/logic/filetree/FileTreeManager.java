package igrek.songbook.logic.filetree;

import igrek.songbook.logic.exceptions.NoSuperItemException;

public class FileTreeManager {
    private FileItem rootItem;
    private FileItem currentItem;

    public FileTreeManager() {
        rootItem = FileItem.folder(null, "/");
        currentItem = rootItem;
    }

    public FileItem getCurrentItem() {
        return currentItem;
    }

    //  NAWIGACJA

    public void goUp() throws NoSuperItemException {

        //TODO nawigacja przez obcicnanie ostaniego elementu przed slashem

        if (currentItem == rootItem) {
            throw new NoSuperItemException();
        } else if (currentItem.getParent() == null) {
            throw new IllegalStateException("parent = null. To się nie powinno zdarzyć");
        } else {
            currentItem = currentItem.getParent();
        }
    }

    public void goInto(int childIndex) {
        //TODO nawigacja wgłąb przez dodanie slasha i nazwy pliku
//        goTo(currentItem.getChild(childIndex));
    }

    public void goTo(FileItem child) {
        currentItem = child;
    }

    public void goToRoot() {
        goTo(rootItem);
    }
}
