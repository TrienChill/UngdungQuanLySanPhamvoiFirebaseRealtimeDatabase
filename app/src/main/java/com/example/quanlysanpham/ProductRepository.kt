package com.example.quanlysanpham

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductRepository {
    private val database = FirebaseDatabase.getInstance().getReference("products")

    // Lấy tất cả sản phẩm từ Firebase
    fun getAllProducts(onProductsLoaded: (List<Product>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                onProductsLoaded(productList) // Trả về danh sách sản phẩm
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }

    fun addProduct(product: Product) {
        val productId = database.push().key // Firebase tạo key dưới dạng String
        if (productId != null) {
            val newProduct = product.copy(id = productId)
            database.child(productId).setValue(newProduct)
        }
    }



    // Cập nhật sản phẩm
    fun updateProduct(product: Product) {
        database.child(product.id).setValue(product)
    }

    // Xóa sản phẩm
    fun deleteProduct(productId: String) {
        database.child(productId).removeValue()
    }
}
