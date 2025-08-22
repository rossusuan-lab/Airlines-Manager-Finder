package com.airlinesmanagerfinder

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import com.google.firebase.database.FirebaseDatabase
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webView2: WebView
    private lateinit var currentWebView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private lateinit var preferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var clockText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var pinAttempts = 0
    private var isLocked = false
    private var lockResetHandler: Handler? = null
    private var lockResetRunnable: Runnable? = null

    private val colors = arrayOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4",
        "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107",
        "#FF9800", "#FF5722", "#795548", "#9E9E9E", "#607D8B", "#000000", "#0000FF",
        "#00FF00", "#6F00FF", "#00FFFF", "#7CFC00", "#FFD700", "#00BFFF", "#4169E1",
        "#CD853F", "#800000", "#8F00FF", "#808000", "#008000", "#FF0123", "#008000",
        "#1E90FF", "#607D8B", "#795548", "#E91E63", "#000000", "#00008B", "#006400",
        "#C0C0C0", "#FFFFFF", "#A52A2A", "#6F00FF", "#ADFF2F", "#808080", "#228B22",
        "#00CED1", "#FF8C00", "#0A0FA6", "#FF7F50", "#6495ED", "#E6E6FA", "#6B8E23",
        "#8A2BE2", "#FFA07A", "#8B0000", "#FF8C00", "#00FA9A", "#48D1CC", "#191970",
        "#006400", "#483D8B", "#696969", "#FFFAF0", "#ADD8E6", "#90EE90", "#0000CD",
        "#4169E1", "#00FF7F", "#9ACD32", "#B22222", "#228B22", "#008B8B", "#8A2BE2",
    )

    // --- Link grup / developer ---
    private val whatsappGroup = "https://chat.whatsapp.com/LY8ptOPGnJgFT2KN6Tcjiy?mode=ac_t"
    private val facebookGroup = "https://www.facebook.com/groups/1926257111506201/?ref=share&mibextid=NSMWBT"
    private val developerSocial = "https://whatsapp.com/channel/0029Vb6TLnoKWEKuSHaKIR2X"
    private val githubRepo = "https://github.com/rossusuan-lab/AM-Finder/releases"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        preferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        clockText = findViewById(R.id.toolbarClock)

        startClockUpdater()
    

        progressBar = findViewById(R.id.progressBar)
        progressBar.indeterminateDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)

        fullscreenContainer = findViewById(R.id.fullscreen_container)

        applyToolbarColor(preferences.getString("toolbarColor", "#8A2BE2")!!)
        applyThemeMode(preferences.getBoolean("darkMode", false))

        webView = findViewById(R.id.webView)
        webView2 = findViewById(R.id.webView2)

        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            checkPIN()
        } else {
            showLoginDialog()
        }

        setupWebViews()
    }

   // ===================== Login / Register =====================
private fun showLoginDialog() {
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val emailInput = EditText(context).apply { hint = "Email" }
        val passInput = EditText(context).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        addView(emailInput)
        addView(passInput)

        AlertDialog.Builder(this@MainActivity)
            .setTitle("Login")
            .setView(this)
            .setPositiveButton("Login") { _, _ ->
                loginWithFirebase(emailInput.text.toString(), passInput.text.toString())
            }
            .setNeutralButton("Lupa Password") { _, _ ->
                val email = emailInput.text.toString()
                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Link reset password dikirim ke email.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Gagal kirim reset password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Masukkan email untuk reset password", Toast.LENGTH_LONG).show()
                    showLoginDialog()
                }
            }
            .setNegativeButton("Daftar") { _, _ -> showRegisterDialog() }
            .setCancelable(false)
            .show()
    }
}

private fun loginWithFirebase(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser
            if (user != null && user.isEmailVerified) {
                checkPIN()
            } else {
                Toast.makeText(this, "Verifikasi email terlebih dahulu.", Toast.LENGTH_LONG).show()
                auth.signOut()
                showLoginDialog()
            }
        } else {
            Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            showLoginDialog()
        }
    }
}

private fun showRegisterDialog() {
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL

        val usernameInput = EditText(context).apply { hint = "Nama Pengguna" }
        val phoneInput = EditText(context).apply { 
            hint = "Nomor HP" 
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val emailInput = EditText(context).apply { hint = "Email" }
        val passInput = EditText(context).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        addView(usernameInput)
        addView(phoneInput)
        addView(emailInput)
        addView(passInput)

        AlertDialog.Builder(this@MainActivity)
            .setTitle("Daftar Akun")
            .setView(this)
            .setPositiveButton("Daftar") { _, _ ->
                val email = emailInput.text.toString()
                val password = passInput.text.toString()
                val username = usernameInput.text.toString()
                val phone = phoneInput.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && phone.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Simpan data tambahan ke Realtime Database
                                val userId = auth.currentUser?.uid
                                val db = FirebaseDatabase.getInstance().reference
                                val userMap = mapOf(
                                    "username" to username,
                                    "phone" to phone,
                                    "email" to email
                                )
                                userId?.let {
                                    db.child("users").child(it).setValue(userMap)
                                }

                                // Kirim verifikasi email
                                auth.currentUser?.sendEmailVerification()
                                Toast.makeText(this@MainActivity, "Email verifikasi dikirim. Silakan cek email.", Toast.LENGTH_LONG).show()
                                showLoginDialog()
                            } else {
                                Toast.makeText(this@MainActivity, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                showRegisterDialog()
                            }
                        }
                } else {
                    Toast.makeText(this@MainActivity, "Lengkapi semua field", Toast.LENGTH_LONG).show()
                    showRegisterDialog()
                }
            }
            .setNegativeButton("Batal") { _, _ -> showLoginDialog() }
            .setCancelable(false)
            .show()
    }
}

 private fun showAccountDialog() {
    val userId = auth.currentUser?.uid ?: return
    val db = FirebaseDatabase.getInstance().reference

    db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
        val username = snapshot.child("username").value?.toString() ?: "-"
        val phone = snapshot.child("phone").value?.toString() ?: "-"
        val email = snapshot.child("email").value?.toString() ?: auth.currentUser?.email ?: "-"

        AlertDialog.Builder(this)
            .setTitle("Profil Saya")
            .setMessage("Nama Pengguna: $username\nNomor HP: $phone\nEmail: $email")
            .setPositiveButton("OK", null)
            .show()
    }.addOnFailureListener {
        Toast.makeText(this, "Gagal memuat data akun", Toast.LENGTH_SHORT).show()
    }
}

private fun startClockUpdater() {
        timeRunnable = object : Runnable {
            override fun run() {
                val currentTime = Calendar.getInstance().time
                val formatter = SimpleDateFormat("EEE, dd MMM yyyy - HH:mm", Locale.getDefault())
                clockText.text = formatter.format(currentTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timeRunnable)
    }
    
    // ===================== PIN =====================
    private fun checkPIN() {
        if (preferences.contains("pin")) showPinDialog() else loadCurrentWebView()
    }

    private fun showPinDialog() {
        if (isLocked) {
            Toast.makeText(this, "Terlalu banyak percobaan. Coba lagi nanti.", Toast.LENGTH_LONG).show()
            finish(); return
        }

        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Masukkan PIN")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if (input.text.toString() == preferences.getString("pin", "")) {
                    pinAttempts = 0
                    loadCurrentWebView()
                } else {
                    pinAttempts++
                    if (pinAttempts >= 2) {
                        isLocked = true
                        Toast.makeText(this, "Terlalu banyak percobaan. Aplikasi terkunci.", Toast.LENGTH_LONG).show()
                        startUnlockCountdown()
                        finish()
                    } else {
                        Toast.makeText(this, "PIN salah!", Toast.LENGTH_SHORT).show()
                        showPinDialog()
                    }
                }
            }
            .show()
    }

    private fun showExitPinDialog() {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Masukkan PIN untuk keluar")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                if (input.text.toString() == preferences.getString("pin", "")) {
                    finish()
                } else {
                    Toast.makeText(this, "PIN salah!", Toast.LENGTH_SHORT).show()
                    showExitPinDialog()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ===================== Setup WebViews =====================
    private fun applyLanguage(lang: String) {
    val locale = when (lang) {
        "id" -> java.util.Locale("id")      // Bahasa Indonesia
        "es" -> java.util.Locale("es")      // Bahasa Spanyol
        "ar" -> java.util.Locale("ar")      // Bahasa Arab
        else -> java.util.Locale("en")      // Default: Inggris
    }

    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)

    // Reload WebView agar bahasa baru diterapkan
    currentWebView.reload()
}

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViews() {
        setupWebView(webView, "https://destinations.noway.info/")
        setupWebView(webView2, "https://finder.airlines-manager.com/")
        loadCurrentWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(web: WebView, url: String) {
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (!isOnline()) web.loadUrl("file:///android_asset/offline.html")
            }
        }

        if (isOnline()) web.loadUrl(url)
        else web.loadUrl("file:///android_asset/offline.html")

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                fullscreenContainer.visibility = View.VISIBLE
                fullscreenContainer.addView(view)
                webView.visibility = View.GONE
                webView2.visibility = View.GONE
                supportActionBar?.hide()
            }

            override fun onHideCustomView() {
                fullscreenContainer.visibility = View.GONE
                fullscreenContainer.removeView(customView)
                customView = null
                webView.visibility = View.VISIBLE
                webView2.visibility = View.GONE
                supportActionBar?.show()
                customViewCallback?.onCustomViewHidden()
            }
        }
    }

  private fun loadCurrentWebView() {
   // Tampilkan WebView pertama
    webView.visibility = View.VISIBLE
    webView2.visibility = View.GONE
    currentWebView = webView       // ✅ WebView aktif

// Tampilkan WebView kedua
   webView.visibility = View.GONE
   webView2.visibility = View.VISIBLE
   currentWebView = webView2      // ✅ WebView aktif
}
    // ===================== Network Check =====================
    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ===================== Lock Reset =====================
    private fun startUnlockCountdown() {
        lockResetHandler = Handler(Looper.getMainLooper())
        lockResetRunnable = Runnable {
            isLocked = false
            pinAttempts = 0
            Toast.makeText(this, "PIN direset. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
        }
        lockResetHandler?.postDelayed(lockResetRunnable!!, 2 * 60 * 1000)
    }

    // ===================== Back & Lifecycle =====================
    override fun onBackPressed() {
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitPinDialog()
        }
    }

    override fun onDestroy() {
        lockResetHandler?.removeCallbacks(lockResetRunnable!!)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        webView2.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView2.onResume()
    }

    // ---------------- Menu Toolbar -------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.toolbar_menu, menu)

    // Ambil bahasa yang dipilih dari SharedPreferences
    val lang = preferences.getString("language", "id") ?: "id"

    when (lang) {
        "id" -> {
            menu.findItem(R.id.menu_logout).title = "Keluar"
            menu.findItem(R.id.action_account).title = "Akun Saya"
            menu.findItem(R.id.menu_theme).title = "Mode Gelap / Terang"
            menu.findItem(R.id.menu_color).title = "Ubah Warna Toolbar"
            menu.findItem(R.id.menu_about).title = "Tentang Aplikasi"
            menu.findItem(R.id.menu_privacy).title = "Kebijakan Privasi"
            menu.findItem(R.id.menu_set_pin).title = "Set PIN"
            menu.findItem(R.id.menu_delete_pin).title = "Hapus PIN"
            menu.findItem(R.id.menu_webview1).title = "WebView 1"
            menu.findItem(R.id.menu_webview2).title = "WebView 2"
            menu.findItem(R.id.menu_lang_id).title = "Bahasa Indonesia"
            menu.findItem(R.id.menu_lang_en).title = "Bahasa Inggris"
            menu.findItem(R.id.menu_lang_es).title = "Bahasa Spanyol"
            menu.findItem(R.id.menu_lang_ar).title = "Bahasa Arab"
            menu.findItem(R.id.menu_whatsapp).title = "Grup WhatsApp"
            menu.findItem(R.id.menu_facebook).title = "Grup Facebook"
            menu.findItem(R.id.menu_dev_sosmed).title = "Developer Social"
            menu.findItem(R.id.menu_update_github).title = "Update GitHub"
            menu.findItem(R.id.menu_check_update).title = "Cek Update"
        }
        "en" -> {
            menu.findItem(R.id.menu_logout).title = "Logout"
            menu.findItem(R.id.action_account).title = "My Account"
            menu.findItem(R.id.menu_theme).title = "Dark / Light Mode"
            menu.findItem(R.id.menu_color).title = "Change Toolbar Color"
            menu.findItem(R.id.menu_about).title = "About App"
            menu.findItem(R.id.menu_privacy).title = "Privacy Policy"
            menu.findItem(R.id.menu_set_pin).title = "Set PIN"
            menu.findItem(R.id.menu_delete_pin).title = "Delete PIN"
            menu.findItem(R.id.menu_webview1).title = "WebView 1"
            menu.findItem(R.id.menu_webview2).title = "WebView 2"
            menu.findItem(R.id.menu_lang_id).title = "Indonesian"
            menu.findItem(R.id.menu_lang_en).title = "English"
            menu.findItem(R.id.menu_lang_es).title = "Spanish"
            menu.findItem(R.id.menu_lang_ar).title = "Arabic"
            menu.findItem(R.id.menu_whatsapp).title = "WhatsApp Group"
            menu.findItem(R.id.menu_facebook).title = "Facebook Group"
            menu.findItem(R.id.menu_dev_sosmed).title = "Developer Social"
            menu.findItem(R.id.menu_update_github).title = "GitHub Update"
            menu.findItem(R.id.menu_check_update).title = "Check Update"
        }
        "es" -> {
            menu.findItem(R.id.menu_logout).title = "Cerrar Sesión"
            menu.findItem(R.id.action_account).title = "Mi cuenta"
            menu.findItem(R.id.menu_theme).title = "Modo Oscuro / Claro"
            menu.findItem(R.id.menu_color).title = "Cambiar Color Toolbar"
            menu.findItem(R.id.menu_about).title = "Acerca de la App"
            menu.findItem(R.id.menu_privacy).title = "Política de Privacidad"
            menu.findItem(R.id.menu_set_pin).title = "Establecer PIN"
            menu.findItem(R.id.menu_delete_pin).title = "Eliminar PIN"
            menu.findItem(R.id.menu_webview1).title = "WebView 1"
            menu.findItem(R.id.menu_webview2).title = "WebView 2"
            menu.findItem(R.id.menu_lang_id).title = "Indonesio"
            menu.findItem(R.id.menu_lang_en).title = "Inglés"
            menu.findItem(R.id.menu_lang_es).title = "Español"
            menu.findItem(R.id.menu_lang_ar).title = "Árabe"
            menu.findItem(R.id.menu_whatsapp).title = "Grupo WhatsApp"
            menu.findItem(R.id.menu_facebook).title = "Grupo Facebook"
            menu.findItem(R.id.menu_dev_sosmed).title = "Red Social Dev"
            menu.findItem(R.id.menu_update_github).title = "Actualizar GitHub"
            menu.findItem(R.id.menu_check_update).title = "Comprobar Actualización"
        }
        "ar" -> {
            menu.findItem(R.id.menu_logout).title = "تسجيل خروج"
            menu.findItem(R.id.action_account).title = "حسابي"
            menu.findItem(R.id.menu_theme).title = "الوضع الداكن / الفاتح"
            menu.findItem(R.id.menu_color).title = "تغيير لون شريط الأدوات"
            menu.findItem(R.id.menu_about).title = "حول التطبيق"
            menu.findItem(R.id.menu_privacy).title = "سياسة الخصوصية"
            menu.findItem(R.id.menu_set_pin).title = "تعيين PIN"
            menu.findItem(R.id.menu_delete_pin).title = "حذف PIN"
            menu.findItem(R.id.menu_webview1).title = "WebView 1"
            menu.findItem(R.id.menu_webview2).title = "WebView 2"
            menu.findItem(R.id.menu_lang_id).title = "الإندونيسية"
            menu.findItem(R.id.menu_lang_en).title = "الإنجليزية"
            menu.findItem(R.id.menu_lang_es).title = "الإسبانية"
            menu.findItem(R.id.menu_lang_ar).title = "العربية"
            menu.findItem(R.id.menu_whatsapp).title = "مجموعة واتساب"
            menu.findItem(R.id.menu_facebook).title = "مجموعة فيسبوك"
            menu.findItem(R.id.menu_dev_sosmed).title = "وسائل التواصل للمطور"
            menu.findItem(R.id.menu_update_github).title = "تحديث GitHub"
            menu.findItem(R.id.menu_check_update).title = "فحص التحديث"
        }
    }

    return true
}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_logout -> auth.signOut().also { recreate() }
            R.id.menu_theme -> {
                val darkMode = !preferences.getBoolean("darkMode", false)
                preferences.edit { putBoolean("darkMode", darkMode) }
                applyThemeMode(darkMode); recreate()
            }
            R.id.menu_color -> {
                AlertDialog.Builder(this)
                    .setTitle("Pilih Warna Toolbar")
                    .setItems(colors) { _, which ->
                        val selectedColor = colors[which]
                        applyToolbarColor(selectedColor)
                        preferences.edit { putString("toolbarColor", selectedColor) }
                    }.show()
            }
            R.id.menu_about -> {
            val currentVersion = try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                pInfo.versionName
            } catch (e: Exception) {
                "1.0"
            }

            AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage("Airlines Manager Finder adalah aplikasi praktis untuk mencari, membandingkan, dan mengelola informasi maskapai penerbangan. Pengguna dapat menemukan rute, harga tiket, jadwal, serta fasilitas maskapai dengan cepat. Fitur utama meliputi pencarian maskapai, perbandingan layanan, detail jadwal penerbangan, favorit, notifikasi promo, dan informasi lengkap maskapai—all in one app.\nVersi: $currentVersion")
                .setPositiveButton("OK", null)
                .show()
        }
        
        R.id.action_account -> {
            showAccountDialog()
            true
        }

        R.id.menu_privacy -> {
            AlertDialog.Builder(this)
                .setTitle("Kebijakan Privasi")
                .setMessage("Airlines Manager Finder menghargai privasi pengguna. Aplikasi ini hanya mengumpulkan data yang diperlukan untuk memberikan layanan, seperti pencarian maskapai dan preferensi pengguna. Data tidak dibagikan kepada pihak ketiga tanpa izin dan disimpan dengan aman. Dengan menggunakan aplikasi ini, pengguna menyetujui kebijakan privasi ini. \n © 2025 All Rights Reserved")
                .setPositiveButton("OK", null)
                .show()
        }
            R.id.menu_whatsapp -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappGroup)))
            R.id.menu_facebook -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebookGroup)))
            R.id.menu_dev_sosmed -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(developerSocial)))
            R.id.menu_update_github -> updateApkGithub()
            R.id.menu_check_update -> checkUpdateGithub()
            R.id.menu_set_pin -> {
            val input = EditText(this).apply {
                hint = "PIN Baru"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
            AlertDialog.Builder(this)
                .setTitle("Set PIN Baru")
                .setView(input)
                .setPositiveButton("Simpan") { _, _ ->
                    val pin = input.text.toString()
                    if (pin.isNotEmpty()) {
                        preferences.edit { putString("pin", pin) }
                        Toast.makeText(this, "PIN disimpan", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
            R.id.menu_delete_pin -> { preferences.edit { remove("pin") }; Toast.makeText(this,"PIN dihapus",Toast.LENGTH_SHORT).show() }

            // -------- Menu Baru: Multi WebView -----------
            R.id.menu_webview1 -> {
    webView.visibility = View.VISIBLE
    webView2.visibility = View.GONE
    currentWebView = webView   // ✅ assign WebView
}

R.id.menu_webview2 -> {
    webView.visibility = View.GONE
    webView2.visibility = View.VISIBLE
    currentWebView = webView2  // ✅ assign WebView
}

            // -------- Menu Baru: Multi Bahasa -----------
            R.id.menu_lang_id -> { preferences.edit { putString("language","id") }; applyLanguage("id"); recreate() }
            R.id.menu_lang_en -> { preferences.edit { putString("language","en") }; applyLanguage("en"); recreate() }
            R.id.menu_lang_es -> { preferences.edit { putString("language","es") }; applyLanguage("es"); recreate() }
            R.id.menu_lang_ar -> { preferences.edit { putString("language","ar") }; applyLanguage("ar"); recreate() }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyToolbarColor(color: String) { toolbar.setBackgroundColor(Color.parseColor(color)); toolbar.setTitleTextColor(Color.WHITE) }
    private fun applyThemeMode(dark:Boolean) { AppCompatDelegate.setDefaultNightMode(if(dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }

    // ---------------- Update & Cek versi via GitHub -------------------
    private fun checkUpdateGithub() {
        Toast.makeText(this, "Cek update dari GitHub...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = URL("https://api.github.com/repos/rossusuan-lab/AM-Finder/releases/latest").readText()
                val latest = JSONObject(json).getString("tag_name")
                withContext(Dispatchers.Main) {
                    val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
                    if (latest != currentVersion) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Update Tersedia")
                            .setMessage("Versi terbaru: $latest\nVersi saat ini: $currentVersion")
                            .setPositiveButton("Update") { _, _ -> updateApkGithub() }
                            .setNegativeButton("Nanti", null)
                            .show()
                    } else {
                        Toast.makeText(this@MainActivity, "Aplikasi sudah versi terbaru", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal cek update: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateApkGithub() {
        Toast.makeText(this, "Update APK otomatis dari GitHub...", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubRepo))
        startActivity(intent)
    }
}