package igrek.songbook.songpreview.quickmenu

import android.view.View
import android.widget.Button
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import javax.inject.Inject

class QuickMenuTranspose {

    @Inject
    lateinit var chordsTransposerManager: Lazy<ChordsTransposerManager>
    @Inject
    lateinit var uiResourceService: UiResourceService

    var isVisible = false
        set(visible) {
            field = visible
            if (visible) {
                quickMenuView!!.visibility = View.VISIBLE
                updateTranspositionText()
            } else {
                quickMenuView!!.visibility = View.GONE
            }
        }
    private var quickMenuView: View? = null
    private var transposedByLabel: TextView? = null

    /**
     * @return is feature active - has impact on song preview (panel may be hidden)
     */
    val isFeatureActive: Boolean
        get() = chordsTransposerManager.get().isTransposed

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun setQuickMenuView(quickMenuView: View) {
        this.quickMenuView = quickMenuView

        transposedByLabel = quickMenuView.findViewById(R.id.transposedByLabel)

        val transposeM5Button = quickMenuView.findViewById<Button>(R.id.transposeM5Button)
        transposeM5Button.setOnClickListener {
            chordsTransposerManager.get()
                    .onTransposeEvent(-5)
        }

        val transposeM1Button = quickMenuView.findViewById<Button>(R.id.transposeM1Button)
        transposeM1Button.setOnClickListener {
            chordsTransposerManager.get()
                    .onTransposeEvent(-1)
        }

        val transpose0Button = quickMenuView.findViewById<Button>(R.id.transpose0Button)
        transpose0Button.setOnClickListener {
            chordsTransposerManager.get()
                    .onTransposeResetEvent()
        }

        val transposeP1Button = quickMenuView.findViewById<Button>(R.id.transposeP1Button)
        transposeP1Button.setOnClickListener {
            chordsTransposerManager.get()
                    .onTransposeEvent(+1)
        }

        val transposeP5Button = quickMenuView.findViewById<Button>(R.id.transposeP5Button)
        transposeP5Button.setOnClickListener {
            chordsTransposerManager.get()
                    .onTransposeEvent(+5)
        }

        updateTranspositionText()
    }

    private fun updateTranspositionText() {
        val semitonesDisplayName = chordsTransposerManager.get().transposedByDisplayName
        val transposedByText = uiResourceService.resString(R.string.transposed_by_semitones, semitonesDisplayName)
        transposedByLabel!!.text = transposedByText
    }

    fun onTransposedEvent() {
        if (isVisible) {
            updateTranspositionText()
        }
    }

}
