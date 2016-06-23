package igrek.todotree.gui.views.edititem;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.system.output.Output;

public class EditItemGUI {

    private GUI gui;
    private EditText etEditItem;
    private Button buttonSaveItem;
    private String lastEditText;
    private int quickInsertMode = 0; //0 - tekst, 1 - godzina, 2 - data
    private String quickInserted;
    private boolean quickInsertFlag = false;

    public EditItemGUI(GUI gui, final TreeItem item, TreeItem parent) {
        this.gui = gui;
        init(item, parent);
    }

    public EditText getEtEditItem() {
        return etEditItem;
    }

    public void init(final TreeItem item, TreeItem parent) {
        View editItemContentLayout = gui.setMainContentLayout(R.layout.edit_item_content);

        etEditItem = (EditText) editItemContentLayout.findViewById(R.id.etEditItemContent);
        //przycisk zapisu
        buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.buttonSaveItem);
        //przycisk zapisz i dodaj nowy
        Button buttonSaveAndAdd = (Button) editItemContentLayout.findViewById(R.id.buttonSaveAndAddItem);

        gui.setTitle(parent.getContent());

        if (item != null) { //edycja
            etEditItem.setText(item.getContent());
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSavedEditedItem(item, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(item, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
        } else { //nowy element
            etEditItem.setText("");
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSavedNewItem(etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(null, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
        }

        //przycisk anuluj
        Button buttonEditCancel = (Button) editItemContentLayout.findViewById(R.id.buttonEditCancel);
        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gui.getGuiListener().onCancelEditedItem(item);
            }
        });

        //przyciski zmiany kursora i zaznaczenia
        ImageButton quickEditGoBegin = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoBegin);
        quickEditGoBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(-2);
            }
        });
        ImageButton quickEditGoLeft = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoLeft);
        quickEditGoLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(-1);
            }
        });
        ImageButton quickEditSelectAll = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditSelectAll);
        quickEditSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEditSelectAll();
            }
        });
        ImageButton quickEditGoRight = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoRight);
        quickEditGoRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(+1);
            }
        });
        ImageButton quickEditGoEnd = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoEnd);
        quickEditGoEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(+2);
            }
        });

        //klawiatura dodawania godziny
        Button buttonEditInsertTime = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertTime);
        buttonEditInsertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickInsertKeyboardToggle(1);
            }
        });

        //klawiatura dodawania daty
        Button buttonEditInsertDate = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertDate);
        buttonEditInsertDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickInsertKeyboardToggle(2);
            }
        });

        //dodawanie przedziału
        Button buttonEditInsertRange = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertRange);
        buttonEditInsertRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickInsertRange();
            }
        });

        //numer
        Button buttonEditInsertNumber = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertNumber);
        buttonEditInsertNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickInsertKeyboardToggle(3);
            }
        });

        quickInsertReset();

        etEditItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                if (quickInsertFlag) return;
                String currentEdit = cs.toString();
                if (currentEdit.equals(lastEditText)) { //brak zmian
                    return;
                }
                if (before - count == 1 && count > 0) { //backspace
                    return;
                }
                // jakieś zjebane zachowanie
                if (start == 0 && count > 2 && count == currentEdit.length() && before == currentEdit.length() - 1) {
                    return;
                }
                if (start == 0 && count > 4 && count == currentEdit.length() && before == currentEdit.length() - 2) {
                    return;
                }
                quickInsertFlag = true;
                CharSequence diffCs = cs.subSequence(start, start + count);
                if (diffCs.length() > 0) {
                    char diff = diffCs.charAt(diffCs.length() - 1);
                    quickInsert(diff);
                }
                quickInsertFlag = false;
            }

            @Override
            public void beforeTextChanged(CharSequence cs, int start, int count, int after) {
                if (quickInsertFlag) return;
                lastEditText = etEditItem.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable cs) { }

        });

        etEditItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    quickInsertOK();
                    return true;
                }
                return false;
            }
        });

        //focus na końcu edytowanego tekstu
        etEditItem.requestFocus();
        quickEdit(+2);
        gui.showSoftKeyboard(etEditItem);
    }

    private void quickEdit(int direction) {
        if (direction == -2) { //na początek
            etEditItem.setSelection(0);
        } else if (direction == +2) { //na koniec
            etEditItem.setSelection(etEditItem.getText().length());
        } else if (direction == -1 || direction == +1) {
            int selStart = etEditItem.getSelectionStart();
            int selEnd = etEditItem.getSelectionEnd();
            if (selStart == selEnd) { //brak zaznaczenia
                selStart += direction;
                if (selStart < 0) selStart = 0;
                if (selStart > etEditItem.getText().length())
                    selStart = etEditItem.getText().length();
                etEditItem.setSelection(selStart);
            } else { //zaznaczenie wielu znaków
                //poszerzenie zaznaczenia
                if (direction == -1) { //w lewo
                    selStart--;
                    if (selStart < 0) selStart = 0;
                } else if (direction == +1) { //w prawo
                    selEnd++;
                    if (selEnd > etEditItem.getText().length())
                        selEnd = etEditItem.getText().length();
                }
                etEditItem.setSelection(selStart, selEnd);
            }
        }
    }

    private void quickEditSelectAll() {
        etEditItem.setSelection(0, etEditItem.getText().length());
    }

    private void quickInsert(char key) {
        if (quickInsertMode != 0) {
            quickInserted += key;
            if (quickInsertMode == 1) { //godzina
                if (quickInserted.length() >= 4) {
                    quickInsertFinish();
                    return;
                }
            } else if (quickInsertMode == 2) { //data
                if (quickInserted.length() >= 6) {
                    quickInsertFinish();
                    return;
                }
            }
        }
    }

    private void quickInsertFinish() {
        String edited = etEditItem.getText().toString();
        int cursor = etEditItem.getSelectionStart();
        if (quickInsertMode == 1) { //godzina
            if (quickInserted.length() >= 3) { // 01:02, 1:02
                edited = insertAt(edited, ":", cursor - 2);
                cursor++;
                etEditItem.setText(edited);
            }
        } else if (quickInsertMode == 2) { //data
            if (quickInserted.length() >= 5) { // 01.02.93, 1.02.93
                edited = insertAt(edited, ".", cursor - 4);
                cursor++;
                edited = insertAt(edited, ".", cursor - 2);
                cursor++;
                etEditItem.setText(edited);
            } else if (quickInserted.length() >= 3) { // 01.02, 1.02
                edited = insertAt(edited, ".", cursor - 2);
                cursor++;
                etEditItem.setText(edited);
            }
        } else if (quickInsertMode == 3) { //liczba lub waluta

        }
        quickInsertReset();
        etEditItem.setSelection(cursor, cursor);
    }

    private void quickInsertOK() {
        if (quickInsertMode == 0) { //zapis przyciskiem OK
            buttonSaveItem.performClick();
        } else {
            if (quickInsertMode == 1) { //godzina
                quickInsertFinish();
            } else if (quickInsertMode == 2) { //data
                quickInsertFinish();
            } else if (quickInsertMode == 3) { //liczba lub waluta
                quickInsertFinish();
            }
        }
    }

    private void quickInsertKeyboardToggle(int mode) {
        int selEnd = etEditItem.getSelectionEnd();
        int selStart = etEditItem.getSelectionStart();
        if (quickInsertMode == mode) {
            quickInsertReset();
        } else {
            if(mode == 3){
                //TODO zjebane jest wpisywanie kilku liczb (ujemnych / przecinkowych), przecinek zamiast kropki, wstawianie "-" i "," w osobnych przyciskach jako tekst
                etEditItem.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }else {
                etEditItem.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
            quickInsertMode = mode;
            quickInserted = "";
        }
        etEditItem.setSelection(selStart, selEnd);
    }

    private void quickInsertReset() {
        etEditItem.setInputType(InputType.TYPE_CLASS_TEXT);
        etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
        quickInsertMode = 0;
        quickInserted = "";
        quickInsertFlag = false;
    }

    private String insertAt(String str, String c, int offset) {
        if (offset < 0) offset = 0;
        if (offset > str.length()) offset = str.length();
        String before = str.substring(0, offset);
        String after = str.substring(offset);
        return before + c + after;
    }

    private void quickInsertRange() {
        if (quickInsertMode == 1) { //godzina
            quickInsertFinish();
        } else if (quickInsertMode == 2) { //data
            quickInsertFinish();
        } else if (quickInsertMode == 3) { //liczba lub waluta
            quickInsertFinish();
        }
        String edited = etEditItem.getText().toString();
        int selStart = etEditItem.getSelectionStart();
        int selEnd = etEditItem.getSelectionEnd();
        String before = edited.substring(0, selStart);
        String after = edited.substring(selEnd);
        //bez podwójnej spacji przed "-"
        if (before.length() > 0 && before.charAt(before.length() - 1) == ' ') {
            edited = before + "- " + after;
            selStart += 2;
        } else {
            edited = before + " - " + after;
            selStart += 3;
        }
        etEditItem.setText(edited);
        etEditItem.setSelection(selStart, selStart);
    }
}
