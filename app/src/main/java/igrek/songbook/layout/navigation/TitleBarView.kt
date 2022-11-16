package igrek.songbook.layout.navigation


import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.songbook.R
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class TitleBarView(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val activity by LazyExtractor(appCompatActivity)
    private val navigationMenuController by LazyExtractor(navigationMenuController)

    private var title: String? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        appFactory.appCompatActivity,
        appFactory.navigationMenuController
    )

    init {
        parseAttrs(attrs)
        initView(context)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val attrsArray = activity.obtainStyledAttributes(attrs, R.styleable.TitleBarView)

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
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }
    }

}
