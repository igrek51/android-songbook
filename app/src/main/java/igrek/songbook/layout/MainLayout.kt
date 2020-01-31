package igrek.songbook.layout

import android.view.View

interface MainLayout {

    fun getLayoutResourceId(): Int

    fun showLayout(layout: View)

    fun onBackClicked()

    fun onLayoutExit()

}
