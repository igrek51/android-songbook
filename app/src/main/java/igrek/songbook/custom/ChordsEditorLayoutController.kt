package igrek.songbook.custom

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class ChordsEditorLayoutController : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var customSongService: Lazy<CustomSongService>
    @Inject
    lateinit var customSongEditLayoutController: Lazy<CustomSongEditLayoutController>
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    private var contentEdit: EditText? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun showLayout(layout: View) {
        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
        // navigation menu button
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        contentEdit = layout.findViewById(R.id.songContentEdit)

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener(object : SafeClickListener() {
            override fun onClick() {
                returnNewContent()
            }
        })

        val addChordButton = layout.findViewById<Button>(R.id.addChordButton)
        addChordButton.setOnClickListener(object : SafeClickListener() {
            override fun onClick() {
                onAddChordClick()
            }
        })

        // TODO buttons: copying, pasting, mutliple clipbords (for chords), auto chords finding, changing notation,
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.CUSTOM_SONG_EDIT
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.chords_editor
    }

    override fun onBackClicked() {
        returnNewContent()
    }

    private fun returnNewContent() {
        val content = contentEdit?.text.toString()
        layoutController.showCustomSong()
        customSongEditLayoutController.get().setSongContent(content)
    }

    override fun onLayoutExit() {
        softKeyboardService.hideSoftKeyboard()
    }

    private fun onAddChordClick() {
        var edited = contentEdit!!.text.toString()
        var selStart = contentEdit!!.selectionStart
        var selEnd = contentEdit!!.selectionEnd

        val before = edited.substring(0, selStart)
        val after = edited.substring(selEnd)

        // if there's nonempty selection
        if (selStart < selEnd) {

            val selected = edited.substring(selStart, selEnd)
            edited = "$before[$selected]$after"
            selStart++
            selEnd++

        } else { // just single cursor

            // if it's the end of line AND there is no space before
            if ((after.isEmpty() || after.startsWith("\n")) && !before.isEmpty() && !before.endsWith(" ")) {
                // insert missing space
                edited = "$before []$after"
                selStart += 2
            } else {
                edited = "$before[]$after"
                selStart += 1
            }
            selEnd = selStart

        }

        contentEdit!!.setText(edited)
        contentEdit!!.setSelection(selStart, selEnd)
        contentEdit!!.requestFocus()
    }

    fun setContent(content: String) {
        contentEdit?.setText(content)
    }
}
