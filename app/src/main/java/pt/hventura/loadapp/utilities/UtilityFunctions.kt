package pt.hventura.loadapp.utilities

import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

object UtilityFunctions {

    /**
     * Got this from here:
     * https://stackoverflow.com/questions/11575943/parse-file-name-from-url-before-downloading-the-file
     * Hope there is no warm in using this :)
     * */
    fun getFileNameFromURL(url: String?): String {
        if (url == null) {
            return ""
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return ""
        }
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // find end index for &
        var firstAndPos = url.indexOf('&')
        if (firstAndPos == -1) {
            firstAndPos = length
        }

        // calculate the end index
        val endIndexQMHASH = minOf(lastQMPos, lastHashPos)
        val endIndex = minOf(endIndexQMHASH, firstAndPos)
        return url.substring(startIndex, endIndex)
    }


    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}