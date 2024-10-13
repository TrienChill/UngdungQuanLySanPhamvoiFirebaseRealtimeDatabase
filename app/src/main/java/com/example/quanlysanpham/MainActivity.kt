package com.example.quanlysanpham

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quanlysanpham.ui.theme.QuanlysanphamTheme
class MainActivity : ComponentActivity() {
    private lateinit var productRepository: ProductRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productRepository = ProductRepository()

        // Di chuyển các biến trạng thái ra ngoài
        var productList by mutableStateOf<List<Product>?>(null)
        var isLoading by mutableStateOf(true)
        var showAddProductScreen by mutableStateOf(false)
        var editingProduct by mutableStateOf<Product?>(null)

        // Lấy dữ liệu từ Firebase
        productRepository.getAllProducts { products ->
            productList = products
            isLoading = false
        }

        setContent {
            when {
                showAddProductScreen -> {
                    AddProductScreen(onAddProduct = { product ->
                        productRepository.addProduct(product)
                        showAddProductScreen = false
                    })
                }

                editingProduct != null -> {
                    ProductDetailScreen(
                        product = editingProduct!!,
                        onUpdateProduct = { updatedProduct ->
                            productRepository.updateProduct(updatedProduct)
                            editingProduct = null
                        },
                        onDeleteProduct = { productId ->
                            productRepository.deleteProduct(productId)
                            editingProduct = null
                        },
                        onBack = {
                            editingProduct = null
                        }
                    )
                }

                else -> {
                    ProductListScreen(
                        products = productList ?: emptyList(),
                        isLoading = isLoading,
                        onAddClick = { showAddProductScreen = true },
                        onProductClick = { product ->
                            editingProduct = product
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ProductListScreen(products: List<Product>, isLoading: Boolean, onAddClick: () -> Unit, onProductClick: (Product) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onAddClick) {
            Text("Thêm sản phẩm")
        }

        if (isLoading) {
            Text("Đang tải dữ liệu...", modifier = Modifier.padding(16.dp))
        } else if (products.isEmpty()) {
            Text("Không có sản phẩm nào để hiển thị.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(products) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(product) }
                            .padding(16.dp)
                    ) {
                        Text(text = product.name, modifier = Modifier.weight(1f))
                        Text(text = "${product.price} đ")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductScreen(onAddProduct: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên sản phẩm") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = price,
            onValueChange = {
                price = it
                isError = price.toDoubleOrNull() == null
            },
            label = { Text("Giá sản phẩm") },
            isError = isError
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả sản phẩm") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (name.isNotEmpty() && !isError) {
                onAddProduct(Product(id = "", name = name, price = price.toDouble(), description = description))
            }
        }) {
            Text("Thêm sản phẩm")
        }
    }
}


@Composable
fun ProductDetailScreen(
    product: Product,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var description by remember { mutableStateOf(product.description) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = price, onValueChange = { price = it }, label = { Text("Giá") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") })
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                val updatedProduct = product.copy(name = name, price = price.toDouble(), description = description)
                onUpdateProduct(updatedProduct)
                onBack() // Quay lại sau khi cập nhật
            }) {
                Text("Cập nhật sản phẩm")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onDeleteProduct(product.id)
                onBack() // Quay lại sau khi xóa
            }) {
                Text("Xóa sản phẩm")
            }
        }
    }
}




