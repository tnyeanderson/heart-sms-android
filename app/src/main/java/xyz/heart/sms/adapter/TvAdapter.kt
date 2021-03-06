package xyz.heart.sms.adapter

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import xyz.heart.sms.fragment.message.MessageInstanceManager
import xyz.heart.sms.shared.data.model.Conversation

class TvAdapter(val conversations: List<Conversation>) : ArrayObjectAdapter() {

    override fun get(index: Int): Any {
        val conversation = conversations[index]

        val customFragmentAdapter = ArrayObjectAdapter()
        customFragmentAdapter.add(MessageInstanceManager.newInstance(conversation))

        return ListRow(HeaderItem(conversation.title), customFragmentAdapter)
    }
}