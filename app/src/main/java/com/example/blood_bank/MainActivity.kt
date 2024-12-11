package com.example.blood_bank

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.blood_bank.ui.theme.Blood_BankTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var donorsViewModel: DonorsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = Room.databaseBuilder(
            applicationContext,
            DonorsDatabase::class.java,
            "donors_db"
        ).fallbackToDestructiveMigration().build()

        val donorDao = db.donorsDao()
        donorsViewModel = DonorsViewModel(donorDao)

        setContent {
            Blood_BankTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mainScreen") {
                    composable("mainScreen") { MainScreen(navController) }
                    composable("donorScreen") { DonorsScreen(donorsViewModel) }
                    composable("adminLoginScreen") { AdminLoginScreen(navController) }
                    composable("adminDashboard") { AdminDashboard(donorsViewModel) }
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("donorScreen") }) {
            Text("Donor")
        }
        Button(onClick = { navController.navigate("adminLoginScreen") }) {
            Text("Admin")
        }
    }
}

@Composable
fun DonorsScreen(viewModel: DonorsViewModel) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    val context = LocalContext.current

    var showToast by remember { mutableStateOf(false) }

    Column {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
        TextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth") })
        TextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
        TextField(
            value = lastDonationDate,
            onValueChange = { lastDonationDate = it },
            label = { Text("Last Donation Date") }
        )
        TextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") })
        TextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") })

        Button(onClick = {
            if (name.isNotEmpty() && bloodGroup.isNotEmpty()) {
                viewModel.addDonor(
                    Donors(
                        name = name,
                        location = location,
                        dob = dob,
                        age = age.toIntOrNull() ?: 0,
                        lastBloodDonation = lastDonationDate,
                        bloodGroup = bloodGroup,
                        mobile = mobile
                    )
                )
                showToast = true
            }
        }) {
            Text("Save")
        }
    }

    if (showToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Donor saved", Toast.LENGTH_SHORT).show()
            showToast = false // Reset after showing
        }
    }
}


class DonorsViewModel(private val donorDao: DonorsDAO) : ViewModel() {
    private val _donors = MutableStateFlow<List<Donors>>(emptyList())
    val donors: StateFlow<List<Donors>> = _donors

    fun addDonor(donor: Donors) {
        viewModelScope.launch {
            donorDao.insertDonors(donor)
        }
    }

    fun getDonorsByBloodGroup(bloodGroup: String) {
        viewModelScope.launch {
            _donors.value = donorDao.getDonorsByBloodGroup(bloodGroup)
        }
    }
}

@Composable
fun AdminLoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = {
            if (username == "abcdAdmin" && password == "123Admin") {
                navController.navigate("adminDashboard")
            } else {
                Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Login")
        }
    }
}

@Composable
fun AdminDashboard(viewModel: DonorsViewModel) {
    var bloodGroup by remember { mutableStateOf("") }
    val donors by viewModel.donors.collectAsState()

    Column {
        TextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") })

        Button(onClick = { viewModel.getDonorsByBloodGroup(bloodGroup) }) {
            Text("Filter")
        }

        LazyColumn {
            items(donors) { donor ->
                Text("Name: ${donor.name}, Blood Group: ${donor.bloodGroup}")
            }
        }
    }
}
