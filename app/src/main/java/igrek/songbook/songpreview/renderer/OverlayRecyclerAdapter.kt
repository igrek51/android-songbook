package igrek.songbook.songpreview.renderer

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView

class OverlayRecyclerAdapter(private val songPreview: SongPreview) : RecyclerView.Adapter<OverlayRecyclerAdapter.OverlayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        // Scrolling in Android is so fukced up!!
        // The only workaround seems to be make the almost indefinite views and put the scroll somewhere in the middle
        val height = Integer.MAX_VALUE / itemCount

        val overlayView = View(parent.context)
        overlayView.layoutParams = ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height)
        overlayView.minimumHeight = height
        overlayView.setOnClickListener { songPreview.onClick() }
        overlayView.setOnTouchListener(songPreview)

        return OverlayViewHolder(overlayView)
    }

    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {}

    override fun getItemCount(): Int {
        return 3
    }

    class OverlayViewHolder(v: View) : RecyclerView.ViewHolder(v)
}