package igrek.todotree.logic.datatree.serializer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.exceptions.NoMatchingBracketException;

public class TreeSerializer {

    public TreeSerializer() {

    }

    //  WCZYTYWANIE Z PLIKU

    public TreeItem loadTree(String data) throws ParseException {
        TreeItem rootItem = new TreeItem(null, "root");
        if (!data.isEmpty()) {
            //wyłuskanie wierszy
            String[] lines = data.split("\n");
            List<String> linesList = new ArrayList<>();
            //obcięcie białych znaków
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    linesList.add(trimmed);
                }
            }
            loadTreeItems(rootItem, linesList);
        }
        return rootItem;
    }


    /**
     * ładuje zawartość elementów z tekstowych wierszy i dodaje do wybranego elementu
     *
     * @param parent element, do którego dodane odczytane potomki
     * @param lines  lista wierszy, z których zostaną dodane elementy
     * @throws ParseException
     */
    private void loadTreeItems(TreeItem parent, List<String> lines) throws ParseException {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equals("{")) {
                try {
                    int closingBracketIndex = findClosingBracket(lines, i);
                    //jeśli cokolwiek jest w środku bloku
                    if (closingBracketIndex - i >= 2) {
                        List<String> subLines = lines.subList(i + 1, closingBracketIndex);
                        TreeItem lastChild = parent.getLastChild();
                        if (lastChild == null) {
                            throw new ParseException("Brak pasującego elementu przed otwarciem nawiasu", i);
                        }
                        loadTreeItems(lastChild, subLines);
                    }
                    //przeskoczenie już przeanalizowanych wierszy
                    i = closingBracketIndex;
                } catch (NoMatchingBracketException ex) {
                    throw new ParseException("Nie odnaleziono pasującego nawiasu domykającego", i);
                }
            } else if (line.equals("}")) {
                //nawiasy domykające zostały już przeanalizowane
                throw new ParseException("Nadmiarowy nawias domykający", i);
            } else {
                parent.add(line);
            }
        }
    }

    /**
     * @param lines      lista wierszy
     * @param startIndex indeks wiersza będącego klamrą otwierającą
     * @return indeks wiersza będącego pasującą klamrą zamykającą
     * @throws NoMatchingBracketException jeśli nie znaleziono poprawnego nawiasu domykającego
     */
    private int findClosingBracket(List<String> lines, int startIndex) throws NoMatchingBracketException {
        int bracketDepth = 1;
        for (int j = startIndex + 1; j < lines.size(); j++) {
            String line = lines.get(j);
            if (line.equals("{")) {
                bracketDepth++;
            } else if (line.equals("}")) {
                bracketDepth--;
                if (bracketDepth == 0) {
                    return j;
                }
            }
        }
        throw new NoMatchingBracketException();
    }


    //  ZAPIS DO PLIKU

    private void saveTreeItems(TreeItem parent, int level, StringBuilder output) {
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) indentBuilder.append("\t");
        String indents = indentBuilder.toString();

        output.append(indents);
        output.append(parent.getContent());
        output.append("\n");
        if (!parent.isEmpty()) {
            output.append(indents);
            output.append("{\n");
            for (TreeItem item : parent.getChildren()) {
                saveTreeItems(item, level + 1, output);
            }
            output.append(indents);
            output.append("}\n");
        }
    }

    public String saveTree(TreeItem root) {
        StringBuilder output = new StringBuilder();
        for (TreeItem child : root.getChildren()) {
            saveTreeItems(child, 0, output);
        }
        return output.toString();
    }

}
