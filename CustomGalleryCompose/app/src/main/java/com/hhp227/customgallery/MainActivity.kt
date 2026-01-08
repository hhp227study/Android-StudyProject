package com.hhp227.customgallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomGalleryApp()
        }
    }
}

@Composable
fun CustomGalleryApp() {
    MaterialTheme {
        Surface {
            Text("Hello Custom Gallery!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomGalleryPreview() {
    CustomGalleryApp()
}