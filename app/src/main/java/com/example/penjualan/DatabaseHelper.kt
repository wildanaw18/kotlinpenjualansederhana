package com.example.penjualan

import com.google.firebase.firestore.FirebaseFirestore

class DatabaseHelper {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Fungsi untuk menambahkan data ke koleksi "barang"
    fun addBarang(
        hargaBarang: String,
        kode: String,
        namaBarang: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val data = hashMapOf(
            "harga_barang" to hargaBarang,
            "kode" to kode,
            "nama_barang" to namaBarang
        )

        db.collection("barang")
            .add(data)
            .addOnSuccessListener { documentReference ->
                onComplete(true, "Data berhasil ditambahkan dengan ID: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // Fungsi untuk membaca semua data dari koleksi "barang"
    fun getAllBarang(onComplete: (Boolean, List<Map<String, Any>>?, String?) -> Unit) {
        db.collection("barang")
            .get()
            .addOnSuccessListener { documents ->
                val dataList = mutableListOf<Map<String, Any>>()
                for (document in documents) {
                    dataList.add(document.data)
                }
                onComplete(true, dataList, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, null, exception.message)
            }
    }

    // Fungsi untuk memperbarui data berdasarkan ID dokumen
    fun updateBarang(
        docId: String,
        hargaBarang: String,
        kode: String,
        namaBarang: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val updatedData = hashMapOf(
            "harga_barang" to hargaBarang,
            "kode" to kode,
            "nama_barang" to namaBarang
        )

        db.collection("barang").document(docId)
            .update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                onComplete(true, "Data berhasil diperbarui!")
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // Fungsi untuk menghapus data berdasarkan ID dokumen
    fun deleteBarang(docId: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("barang").document(docId)
            .delete()
            .addOnSuccessListener {
                onComplete(true, "Data berhasil dihapus!")
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }
}
