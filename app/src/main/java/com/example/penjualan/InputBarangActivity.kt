package com.example.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator

class InputBarangActivity : AppCompatActivity() {

    private lateinit var tvKode: TextView
    private lateinit var etNamaBarang: EditText
    private lateinit var etHarga: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnScanBarcode: Button
    private lateinit var tvInformasiDetail: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_barang)

        tvKode = findViewById(R.id.tvKode)
        etNamaBarang = findViewById(R.id.etNamaBarang)
        etHarga = findViewById(R.id.etHarga)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnScanBarcode = findViewById(R.id.btnScanBarcode)
        val btnEdit: Button = findViewById(R.id.btnEdit)
        val btnHapus: Button = findViewById(R.id.btnHapus)
        tvInformasiDetail = findViewById(R.id.tvInformasiDetail)

        btnSimpan.setOnClickListener {
            saveDataToDatabase()
        }

        btnScanBarcode.setOnClickListener {
            startBarcodeScanner()
        }
        // Aksi untuk tombol Edit
        btnEdit.setOnClickListener {
            editDataInDatabase()
        }
        btnHapus.setOnClickListener {
            deleteDataInDatabase()
        }
    }

    private fun saveDataToDatabase() {
        val kode = tvKode.text.toString().trim()
        val namaBarang = etNamaBarang.text.toString().trim()
        val hargaText = etHarga.text.toString().trim()

        // Validasi input wajib diisi
        if (kode.isEmpty()) {
            Toast.makeText(this, "Kode barang tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }
        if (namaBarang.isEmpty()) {
            Toast.makeText(this, "Nama barang tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }
        if (hargaText.isEmpty()) {
            Toast.makeText(this, "Harga barang tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val harga = hargaText.toDoubleOrNull()
        if (harga == null) {
            Toast.makeText(this, "Harga harus berupa angka!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Periksa apakah kode sudah ada di database
        db.collection("barang").whereEqualTo("kode", kode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Jika kode sudah ada
                    Toast.makeText(this, "Kode sudah ada di database!", Toast.LENGTH_SHORT).show()

                    // Tampilkan informasi detail
                    tvInformasiDetail.text = """
                Informasi Detail: (Tidak Tersimpan karena kode sudah ada)
                Kode: $kode
                Nama Barang: -
                Harga Barang: -
            """.trimIndent()
                } else {
                    // Jika kode belum ada, tambahkan data baru
                    val barangData = hashMapOf(
                        "kode" to kode,
                        "nama_barang" to namaBarang,
                        "harga_barang" to harga
                    )

                    db.collection("barang")
                        .add(barangData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data berhasil disimpan ke Firebase!", Toast.LENGTH_SHORT).show()

                            // Tampilkan informasi detail
                            tvInformasiDetail.text = """
                        Informasi Detail: (Berhasil Menyimpan)
                        Kode: $kode
                        Nama Barang: $namaBarang
                        Harga Barang: $harga
                    """.trimIndent()

                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memeriksa kode: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }




    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Arahkan kamera ke barcode")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }
    private fun editDataInDatabase() {
        val kode = tvKode.text.toString().trim()
        val newNamaBarang = etNamaBarang.text.toString().trim()
        val newHargaText = etHarga.text.toString().trim()

        if (kode.isEmpty()) {
            Toast.makeText(this, "Kode barang tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("barang").whereEqualTo("kode", kode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()
                    val docId = document.id

                    val currentNamaBarang = document.getString("nama_barang") ?: ""
                    val currentHargaBarang = document.getDouble("harga_barang") ?: 0.0

                    val updatedData = hashMapOf(
                        "nama_barang" to if (newNamaBarang.isNotEmpty()) newNamaBarang else currentNamaBarang,
                        "harga_barang" to if (newHargaText.isNotEmpty()) newHargaText.toDouble() else currentHargaBarang
                    )

                    db.collection("barang").document(docId)
                        .update(updatedData as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                            // Tampilkan informasi detail
                            tvInformasiDetail.text = """
                            Informasi Detail:
                            (Berhasil Memperbarui)
                            Kode: $kode
                            Nama Barang: ${if (newNamaBarang.isNotEmpty()) "diubah $currentNamaBarang menjadi $newNamaBarang" else currentNamaBarang}
                            Harga Barang: ${if (newHargaText.isNotEmpty()) "diubah $currentHargaBarang menjadi ${newHargaText.toDouble()}" else currentHargaBarang}
                        """.trimIndent()

                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Barang dengan kode tersebut tidak ditemukan!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteDataInDatabase() {
        val kode = tvKode.text.toString().trim()

        if (kode.isEmpty()) {
            Toast.makeText(this, "Kode barang tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("barang").whereEqualTo("kode", kode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val currentNamaBarang = document.getString("nama_barang") ?: ""
                        val currentHargaBarang = document.getDouble("harga_barang") ?: 0.0

                        db.collection("barang").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()

                                // Tampilkan informasi detail
                                tvInformasiDetail.text = """
                                Informasi Detail:
                                (Berhasil Dihapus)
                                Kode: $kode
                                Nama Barang: $currentNamaBarang
                                Harga Barang: $currentHargaBarang
                            """.trimIndent()

                                clearFields()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal menghapus data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Barang dengan kode tersebut tidak ditemukan!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show()
            } else {
                tvKode.text = result.contents
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun clearFields() {
        tvKode.text = ""
        etNamaBarang.text.clear()
        etHarga.text.clear()
    }
}
