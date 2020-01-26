package igrek.songbook.layout.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import igrek.songbook.R
import igrek.songbook.info.logger.LoggerFactory




class TitleBarFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.component_titlebar, container, false)

        LoggerFactory.logger.debug(arguments)
        LoggerFactory.logger.debug(savedInstanceState)
        val title: String? = arguments?.getString("title")
        if (title != null) {
            val screenTitleTextView: TextView = view.findViewById(igrek.songbook.R.id.screenTitleTextView)
            screenTitleTextView.text = title
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerFactory.logger.debug(savedInstanceState)
    }
}
