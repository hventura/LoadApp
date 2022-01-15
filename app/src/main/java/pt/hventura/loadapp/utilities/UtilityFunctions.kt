package pt.hventura.loadapp.utilities

import java.net.MalformedURLException
import java.net.URL
import kotlin.math.min

class UtilityFunctions {

    /**
     * Got this from here:
     * https://stackoverflow.com/questions/11575943/parse-file-name-from-url-before-downloading-the-file
     * Not being use for now.
     * */
    fun getFileNameFromURL(url: String?): String {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
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

        // calculate the end index
        val endIndex = min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }

}