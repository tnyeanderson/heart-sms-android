package xyz.heart.sms.adapter.view_holder

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import xyz.heart.sms.R
import xyz.heart.sms.shared.data.ArticlePreview
import xyz.heart.sms.shared.data.MimeType
import xyz.heart.sms.shared.data.Settings
import xyz.heart.sms.shared.data.model.Message
import xyz.heart.sms.shared.util.ColorUtils
import xyz.heart.sms.shared.util.DensityUtil
import xyz.heart.sms.shared.util.ImageUtils
import xyz.heart.sms.shared.util.listener.ForcedRippleTouchListener

class WearableMessageViewHolder(itemView: View, color: Int, type: Int, private val timestampHeight: Int)
    : RecyclerView.ViewHolder(itemView) {

    val title: TextView? by lazy { itemView.findViewById<View>(R.id.title) as TextView? }
    val timestamp: TextView? by lazy { itemView.findViewById<View>(R.id.timestamp) as TextView? }
    val message: TextView? by lazy { itemView.findViewById<View>(R.id.message) as TextView? }
    val contact: TextView? by lazy { itemView.findViewById<View>(R.id.contact) as TextView? }
    val image: ImageView? by lazy { itemView.findViewById<View>(R.id.image) as ImageView? }
    val clippedImage: ImageView? by lazy { itemView.findViewById<View>(R.id.clipped_image) as ImageView? }
    val messageHolder: View? by lazy { itemView.findViewById<View>(R.id.message_holder) }

    var messageId: Long = 0
    var data: String? = null
    var mimeType: String? = null
    var color = -1
    var textColor = -1

    private var primaryColor = -1
    private var accentColor = -1


    private val clickListener = View.OnClickListener {
        if (timestamp == null) {
            return@OnClickListener
        }

        val animator: ValueAnimator
        if (timestamp!!.height > 0) {
            animator = ValueAnimator.ofInt(timestampHeight, 0)
            animator.interpolator = AccelerateInterpolator()
        } else {
            animator = ValueAnimator.ofInt(0, timestampHeight)
            animator.interpolator = DecelerateInterpolator()
        }

        val params = timestamp!!.layoutParams
        animator.addUpdateListener { animation ->
            params.height = animation.animatedValue as Int
            timestamp?.requestLayout()
        }

        animator.duration = 100
        animator.start()
    }

    init {
        message?.textSize = Settings.largeFont.toFloat()
        contact?.textSize = Settings.smallFont.toFloat()
        contact?.height = DensityUtil.spToPx(contact?.context, Settings.mediumFont)

        timestamp?.textSize = Settings.smallFont.toFloat()
        timestamp?.height = DensityUtil.spToPx(timestamp?.context, Settings.mediumFont)

        val useGlobalThemeColor = Settings.useGlobalThemeColor
        if (color != -1 && messageHolder != null || useGlobalThemeColor && type == Message.TYPE_RECEIVED) {
            if (useGlobalThemeColor) {
                this.color = Settings.mainColorSet.color
            } else {
                this.color = color
            }

            textColor = if (!ColorUtils.isColorDark(this.color)) {
                itemView.context.resources.getColor(R.color.darkText)
            } else {
                itemView.context.resources.getColor(R.color.lightText)
            }

            message?.setTextColor(textColor)
            messageHolder?.backgroundTintList = ColorStateList.valueOf(this.color)
        }

        image?.setOnClickListener {
            if (mimeType != null && MimeType.isVcard(mimeType!!)) {
                var uri = Uri.parse(message!!.text.toString())
                if (message!!.text.toString().contains("file://")) {
                    uri = ImageUtils.createContentUri(itemView.context, uri)
                }

                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(uri, MimeType.TEXT_VCARD)
                itemView.context.startActivity(intent)
            } else if (mimeType != null && mimeType == MimeType.MEDIA_ARTICLE) {
                startArticle()
            }
        }

        messageHolder?.setOnClickListener(clickListener)
        message?.setOnClickListener(clickListener)
        message?.setOnTouchListener(ForcedRippleTouchListener(message!!))
        message?.isHapticFeedbackEnabled = false
    }

    fun setColors(color: Int, accentColor: Int) {
        this.primaryColor = color
        this.accentColor = accentColor
    }

    private fun startArticle() {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(primaryColor)
        builder.setShowTitle(true)
        val customTabsIntent = builder.build()

        val preview = ArticlePreview.build(data!!)
        if (preview != null) {
            customTabsIntent.launchUrl(itemView.context, Uri.parse(preview.webUrl))
        }
    }
}
