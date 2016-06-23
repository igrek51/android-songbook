package igrek.todotree.logic.datatree;

import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import igrek.todotree.logic.datatree.serializer.TreeSerializer;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.settings.Config;
import igrek.todotree.settings.preferences.Preferences;
import igrek.todotree.system.files.Files;
import igrek.todotree.system.files.PathBuilder;
import igrek.todotree.system.output.Output;

public class TreeManager {
    private TreeItem rootItem;
    private TreeItem currentItem;

    private TreeItem editItem = null;
    private Integer newItemPosition = null;

    private List<Integer> selectedPositions = null;

    private TreeSerializer treeSerializer = new TreeSerializer();

    private List<TreeItem> clipboard = null;

    public TreeManager() {
        rootItem = new TreeItem(null, "root");
        currentItem = rootItem;
        editItem = null;
    }

    public TreeItem getRootItem() {
        return rootItem;
    }

    public void setRootItem(TreeItem rootItem) {
        this.rootItem = rootItem;
        this.currentItem = rootItem;
    }

    public TreeItem getCurrentItem() {
        return currentItem;
    }

    public TreeItem getEditItem() {
        return editItem;
    }

    public void setEditItem(TreeItem editItem) {
        this.editItem = editItem;
        this.newItemPosition = null;
    }

    public void setNewItemPosition(Integer newItemPosition) {
        this.editItem = null;
        this.newItemPosition = newItemPosition;
    }

    public Integer getNewItemPosition() {
        return newItemPosition;
    }

    //  NAWIGACJA

    public void goUp() throws NoSuperItemException {
        if (currentItem == rootItem) {
            throw new NoSuperItemException();
        } else if (currentItem.getParent() == null) {
            throw new IllegalStateException("parent = null. To się nie powinno zdarzyć");
        } else {
            currentItem = currentItem.getParent();
        }
    }

    public void goInto(int childIndex) {
        goTo(currentItem.getChild(childIndex));
    }

    public void goTo(TreeItem child) {
        currentItem = child;
    }

    public void goToRoot() {
        goTo(rootItem);
    }

    //  DODAWANIE / USUWANIE ELEMENTÓW

    public void addToCurrent(TreeItem newItem) {
        currentItem.add(newItem);
    }

    public void deleteFromCurrent(int location) {
        currentItem.remove(location);
    }

    //  EDYCJA

    public void saveItemContent(TreeItem item, String content) {
        item.setContent(content);
    }

    public void saveItemContent(String content) {
        if (editItem != null) {
            editItem.setContent(content);
        }
    }

    //  ZAPIS / ODCZYT Z PLIKU

    public void loadRootTree(Files files, Preferences preferences) {
        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);
        Output.log("Wczytywanie bazy danych z pliku: " + dbFilePath.toString());
        if (!files.exists(dbFilePath.toString())) {
            Output.log("Plik z bazą danych nie istnieje. Domyślna pusta baza danych.");
            return;
        }
        try {
            String fileContent = files.openFileString(dbFilePath.toString());
            TreeItem rootItem = treeSerializer.loadTree(fileContent);
            setRootItem(rootItem);
            Output.log("Wczytano bazę danych.");
        } catch (IOException | ParseException e) {
            Output.error(e);
        }
    }

    public void saveRootTree(Files files, Preferences preferences) {
        saveBackupFile(files, preferences);
        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);
        Output.log("Zapisywanie bazy danych do pliku: " + dbFilePath.toString());
        try {
            String output = treeSerializer.saveTree(getRootItem());
            files.saveFile(dbFilePath.toString(), output);
            Output.log("Zapisano bazę danych.");
        } catch (IOException e) {
            Output.error(e);
        }
    }

    //  BACKUP

    public void saveBackupFile(Files files, Preferences preferences) {
        if (Config.backup_num == 0) return;
        SimpleDateFormat sdfr = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss", Locale.ENGLISH);
        //usunięcie starych plików
        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);
        PathBuilder dbDirPath = dbFilePath.parent();

        List<String> children = files.listDir(dbDirPath);
        List<Pair<String, Date>> backups = new ArrayList<>();
        //rozpoznanie plików backup i odczytanie ich dat
        for (String child : children) {
            if (child.startsWith(Config.backup_file_prefix)) {
                String dateStr = PathBuilder.removeExtension(child).substring(Config.backup_file_prefix.length());
                Date date = null;
                try {
                    date = sdfr.parse(dateStr);
                } catch (ParseException e) {
                    Output.log("Niepoprawny format daty w nazwie pliku: " + child);
                }
                backups.add(new Pair<>(child, date));
            }
        }

        //posortowanie po datach malejąco
        Collections.sort(backups, new Comparator<Pair<String, Date>>() {
            @Override
            public int compare(Pair<String, Date> a, Pair<String, Date> b) {
                if (a.second == null) return +1;
                if (b.second == null) return -1;
                return -a.second.compareTo(b.second);
            }
        });

        //usunięcie najstarszych plików
        for (int i = Config.backup_num - 1; i < backups.size(); i++) {
            Pair<String, Date> pair = backups.get(i);
            PathBuilder toRemovePath = dbDirPath.append(pair.first);
            files.delete(toRemovePath);
            Output.log("Usunięto stary backup: " + toRemovePath.toString());
        }

        //zapisanie nowego backupa
        PathBuilder backupPath = dbDirPath.append(Config.backup_file_prefix + sdfr.format(new Date()));
        try {
            files.copy(new File(dbFilePath.toString()), new File(backupPath.toString()));
            Output.log("Utworzono backup: " + backupPath.toString());
        } catch (IOException e) {
            Output.error(e);
        }
    }

    //  ZMIANA KOLEJNOŚCI

    public void replace(TreeItem parent, int pos1, int pos2) {
        if (pos1 == pos2) return;
        if (pos1 < 0 || pos2 < 0) throw new IllegalArgumentException("position < 0");
        if (pos1 >= parent.size() || pos2 >= parent.size()) {
            throw new IllegalArgumentException("position >= size");
        }
        TreeItem item1 = parent.getChild(pos1);
        TreeItem item2 = parent.getChild(pos2);
        //wstawienie na pos1
        parent.remove(pos1);
        parent.add(pos1, item2);
        //wstawienie na pos2
        parent.remove(pos2);
        parent.add(pos2, item1);
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w górę
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    public void moveUp(TreeItem parent, int position) {
        if (position <= 0) return;
        replace(parent, position, position - 1);
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w dół
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    public void moveDown(TreeItem parent, int position) {
        if (position >= parent.size() - 1) return;
        replace(parent, position, position + 1);
    }

    /**
     * przesuwa element z pozycji o określoną liczbę pozycji
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     * @param step     liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     * @return nowa pozycja elementu
     */
    public int move(TreeItem parent, int position, int step) {
        int targetPosition = position + step;
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= parent.size()) targetPosition = parent.size() - 1;
        while (position < targetPosition) {
            moveDown(parent, position);
            position++;
        }
        while (position > targetPosition) {
            moveUp(parent, position);
            position--;
        }
        return targetPosition;
    }

    /**
     * przesuwa element z pozycji do wybranego miejsca
     *
     * @param parent         przodek przesuwanego elementu
     * @param position       pozycja elementu przed przesuwaniem
     * @param targetPosition pozycja elemetnu po przesuwaniu
     */
    public void moveTo(TreeItem parent, int position, int targetPosition) {
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= parent.size()) targetPosition = parent.size() - 1;
        while (position < targetPosition) {
            moveDown(parent, position);
            position++;
        }
        while (position > targetPosition) {
            moveUp(parent, position);
            position--;
        }
    }

    public void moveToEnd(TreeItem parent, int position) {
        moveTo(parent, position, parent.size() - 1);
    }

    public void moveToBegining(TreeItem parent, int position) {
        moveTo(parent, position, 0);
    }

    //  OBCINANIE NIEDOZWOLONYCH ZNAKÓW

    /**
     * obcięcie białych znaków na początku i na końcu, usunięcie niedozwolonych znaków
     *
     * @param content zawartość elementu
     * @return zawartość z obciętymi znakami
     */
    public String trimContent(String content) {
        final String WHITE_CHARS = " ";
        final String INVALID_CHARS = "{}[]\n\t";
        //usunięcie niedozwolonych znaków ze środka
        for (int i = 0; i < content.length(); i++) {
            if (isCharInSet(content.charAt(i), INVALID_CHARS)) {
                content = content.substring(0, i) + content.substring(i + 1);
                i--;
            }
        }
        //obcinanie białych znaków na początku
        while (content.length() > 0 && isCharInSet(content.charAt(0), WHITE_CHARS)) {
            content = content.substring(1);
        }
        //obcinanie białych znaków na końcu
        while (content.length() > 0 && isCharInSet(content.charAt(content.length() - 1), WHITE_CHARS)) {
            content = content.substring(0, content.length() - 1);
        }
        return content;
    }

    private boolean isCharInSet(char c, String set) {
        for (int i = 0; i < set.length(); i++) {
            if (set.charAt(i) == c) return true;
        }
        return false;
    }

    //  ZAZNACZANIE ELEMENTÓW

    public List<Integer> getSelectedItems() {
        return selectedPositions;
    }

    public int getSelectedItemsCount() {
        if (selectedPositions == null) return 0;
        return selectedPositions.size();
    }

    public boolean isSelectionMode() {
        if (selectedPositions == null) return false;
        return selectedPositions.size() > 0;
    }

    public void startSelectionMode() {
        selectedPositions = new ArrayList<>();
    }

    public void cancelSelectionMode() {
        selectedPositions = null;
    }

    public void setItemSelected(int position, boolean selectedState) {
        if (!isSelectionMode()) {
            startSelectionMode();
        }
        if (selectedState == true) {
            if (isItemSelected(position)) {
                return;
            } else {
                selectedPositions.add(position);
            }
        } else {
            if (isItemSelected(position)) {
                selectedPositions.remove(new Integer(position));
                if (selectedPositions.isEmpty()) {
                    selectedPositions = null;
                }
            } else {
                return;
            }
        }
    }

    public boolean isItemSelected(int position) {
        for (Integer pos : selectedPositions) {
            if (pos.intValue() == position) {
                return true;
            }
        }
        return false;
    }

    public void toggleItemSelected(int position) {
        setItemSelected(position, !isItemSelected(position));
    }

    //  SCHOWEK

    public List<TreeItem> getClipboard() {
        return clipboard;
    }

    public int getClipboardSize() {
        if (clipboard == null) return 0;
        return clipboard.size();
    }

    public boolean isClipboardEmpty() {
        if (clipboard == null) return true;
        return clipboard.size() == 0;
    }

    public void clearClipboard() {
        clipboard = null;
    }

    public void addToClipboard(TreeItem item) {
        if (clipboard == null) {
            clipboard = new ArrayList<>();
        }
        clipboard.add(new TreeItem(item));
    }

    public void recopyClipboard() {
        if (clipboard != null) {
            ArrayList<TreeItem> newClipboard = new ArrayList<>();
            for (TreeItem item : clipboard) {
                newClipboard.add(new TreeItem(item));
            }
            clipboard = newClipboard;
        }
    }
}
