package com.bebetter.bemora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.rememberNavController
import com.bebetter.bemora.navigation.BeMoraBottomBar
import com.bebetter.bemora.navigation.BeMoraNavGraph
import com.bebetter.bemora.ui.theme.BeMoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BeMoraTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BeMoraBottomBar(
                            navController = navController
                        )
                    }
                ) { innerPadding ->

                    BeMoraNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}