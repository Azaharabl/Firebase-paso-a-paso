package com.example.autentificaciontutorialv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.autentificaciontutorialv1.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    //variable para el binding
    private lateinit var binding: ActivityMainBinding

    //variable para inicio de sesion
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()  //configuracion del metodo de autentificacion
        initListeners() //boton
    }



    //metodo de autentificacion
    private fun configAuth() {
        //inicialiar las variables
        firebaseAuth = FirebaseAuth.getInstance()

        //iniciamos el listener para cuando nos autentifiquemos
        authStateListener = FirebaseAuth.AuthStateListener {

            if (it.currentUser != null){ //si el usuario ya esta autenticado
                supportActionBar?.title = it.currentUser?.displayName  //ponemos el nombre del usuario en la toolbar
                binding.textInit.visibility = View.VISIBLE  //haer visible...
            }else {
                //si el usuario no esta autenticado entonces
                //crear la lista de todas las formas de autentificacion
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),    //email
                    AuthUI.IdpConfig.GoogleBuilder().build(), //google
                    AuthUI.IdpConfig.PhoneBuilder().build()   //phone
                    )


                //lanzar il intent para mostrar todas las formas de logueo
                resultLauncher.launch(//este bloque es el intent para mostrar el logeado
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build())
            }
        }
    }
    //variable para evaluar el resultado del intent
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){
            val user = FirebaseAuth.getInstance().currentUser //datos del usuario identificado

            if (user != null){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
        }else {
            if(response == null){ //el usuario a pulsado hacia atras para salir de la APP
                Toast.makeText(this, "Adios....", Toast.LENGTH_SHORT).show()
                finish()
            }else { //se debe tratar los errores de conexion
                response.error?.let{
                    if(it.errorCode == ErrorCodes.NO_NETWORK){
                        Toast.makeText(this, "Sin red", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Codigo error: ${it.errorCode}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // ciclo de vida
    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener (authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener (authStateListener)
    }

    private fun initListeners() {

        binding.salirButton.setOnClickListener {
            AuthUI.getInstance().signOut(this) //salir de la session
                .addOnSuccessListener {
                    Toast.makeText(this, "Session cerrada", Toast.LENGTH_SHORT).show()
                }
            configAuth()

        }

    }

}