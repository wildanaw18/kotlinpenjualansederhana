package com.example.penjualan

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnInputBarang: Button = findViewById(R.id.btnInputBarang)
        val btnLihatDataBarang: Button = findViewById(R.id.btnLihatDataBarang)
        val btnKeluar: Button = findViewById(R.id.btnKeluar)

        // Tombol Input Barang
        btnInputBarang.setOnClickListener {
            val intent = Intent(this, InputBarangActivity::class.java)
            startActivity(intent)
        }

        // Tombol Lihat Data Barang
        btnLihatDataBarang.setOnClickListener {
            startBarcodeScanner()
        }

        // Tombol Keluar
        btnKeluar.setOnClickListener {
            Toast.makeText(this, "Terima kasih!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Fungsi untuk memulai scanner barcode
    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Arahkan kamera ke barcode")
        integrator.setCameraId(0) // Gunakan kamera belakang
        integrator.setBeepEnabled(true) // Bunyi saat barcode terdeteksi
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    // Fungsi untuk menangani hasil scan
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show()
            } else {
                // Jika scan berhasil, ambil data dari Firebase berdasarkan kode
                val scannedCode = result.contents
                fetchDataFromDatabase(scannedCode)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Fungsi untuk mengambil data dari Firebase berdasarkan kode
    private fun fetchDataFromDatabase(kode: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .whereEqualTo("kode", kode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val namaBarang = document.getString("nama_barang") ?: "Tidak diketahui"
                        val hargaBarang = document.getDouble("harga_barang") ?: 0.0
                        showPopup(kode, namaBarang, hargaBarang)
                    }
                } else {
                    Toast.makeText(this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Fungsi untuk menampilkan popup informasi barang
    private fun showPopup(kode: String, namaBarang: String, hargaBarang: Double) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_item_info)

        // Ambil referensi komponen dari layout popup
        val tvKode: TextView = dialog.findViewById(R.id.tvKode)
        val tvNamaBarang: TextView = dialog.findViewById(R.id.tvNamaBarang)
        val tvHargaBarang: TextView = dialog.findViewById(R.id.tvHargaBarang)
        val btnCloseModal: TextView = dialog.findViewById(R.id.btnCloseModal)

        // Atur teks dengan data yang diambil
        tvKode.text = "Kode: $kode"
        tvNamaBarang.text = "Nama Barang: $namaBarang"
        tvHargaBarang.text = "Harga Barang: Rp ${hargaBarang.toInt()}"

        // Fungsi tombol tutup
        btnCloseModal.setOnClickListener {
            dialog.dismiss()
        }

        // Tampilkan popup
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
