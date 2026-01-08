package com.example.terminal_test_app.platform.checkout

import android.content.Context
import android.content.Intent
import com.example.terminal_test_app.domain.model.CheckoutPage
import com.example.terminal_test_app.domain.repository.CheckoutLauncher
import com.example.terminal_test_app.platform.webview.CheckoutWebViewActivity

class AndroidCheckoutLauncher(
    private val context: Context
) : CheckoutLauncher {

    override fun openCheckout(page: CheckoutPage) {
        val intent = Intent(context, CheckoutWebViewActivity::class.java)
        intent.putExtra("url", page.url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
