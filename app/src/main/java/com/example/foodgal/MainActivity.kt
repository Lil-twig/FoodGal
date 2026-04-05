package com.example.foodgal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.foodgal.ui.navigation.NavGraph
import com.example.foodgal.ui.theme.FoodGalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodGalTheme {
                NavGraph()
            }
        }
    }
}
