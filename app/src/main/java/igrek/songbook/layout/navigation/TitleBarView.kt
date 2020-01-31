package igrek.songbook.layout.navigation

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import javax.inject.Inject


class TitleBarView : RelativeLayout {

    @Inject
    lateinit var activity: Lazy<AppCompatActivity>
    @Inject
    lateinit var navigationMenuController: Lazy<NavigationMenuController>

    private var title: String? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        parseAttrs(attrs)
        initView(context)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val attrsArray = activity.get().obtainStyledAttributes(attrs, R.styleable.TitleBarView)

        title = attrsArray.getText(R.styleable.TitleBarView_title)?.toString()

        attrsArray.recycle()
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.component_titlebar, this)

        if (title != null) {
            val screenTitleTextView: TextView = findViewById(R.id.screenTitleTextView)
            screenTitleTextView.text = title
        }

        val toolbar1 = findViewById<Toolbar>(R.id.toolbar1)
        activity.get().setSupportActionBar(toolbar1)
        val actionBar = activity.get().supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.get().navDrawerShow() }
    }

}
