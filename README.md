# Airlines Manager Finder

Airlines Manager Finder adalah aplikasi Android yang memudahkan pengguna untuk mencari, memantau, dan mengelola informasi maskapai penerbangan secara real-time. Aplikasi ini dilengkapi dengan berbagai fitur interaktif dan antarmuka yang intuitif untuk pengalaman pengguna yang optimal.

---

## 📌 Fitur Utama

- **WebView Terintegrasi:** Menampilkan halaman maskapai penerbangan dengan pengaturan lengkap.
- **Autentikasi PIN:** Fitur keamanan PIN saat masuk dan keluar aplikasi (reset 3 menit jika salah 3x).
- **Progress Bar:** Indikator loading WebView dengan warna merah/hijau sesuai versi.
- **Login & Registrasi Dummy:** Mendukung login dan pembuatan akun dummy (email & password).
- **Menu Toolbar Lengkap:**
  - Logout
  - Ubah warna toolbar (19 pilihan warna, disimpan)
  - Tentang aplikasi
  - Kebijakan privasi
  - Mode gelap/terang
  - Set/hapus PIN
- **Multi WebView:** Mendukung beberapa WebView dalam satu aplikasi (fitur premium).
- **Rotasi Layar Otomatis:** Mendukung tampilan lanskap & potret secara fleksibel.
- **Fitur IPTV (opsional):** Menggunakan file M3U dari folder `assets`.
- **Splash Screen:** Tampilan awal aplikasi yang menarik selama 5 detik.

---

## 🛠️ Teknologi & Tools

- Bahasa: **Kotlin**
- Platform: **Android**
- Integrasi:
  - Firebase Auth (opsional untuk login)
  - SharedPreferences untuk penyimpanan PIN & warna toolbar
  - WebView API Android

---

## ⚡ Cara Instalasi

1. Clone repository ini:

```bash
git clone https://github.com/rossusuan-lab/Airlines-Manager-Finder.git
