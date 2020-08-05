package igrek.songbook.info.errorcheck

import android.view.View

class SafeClickListener(private val onClick: () -> Unit) : View.OnClickListener {

    override fun onClick(var1: View) {
        SafeExecutor {
            onClick.invoke()
        }
    }

}
