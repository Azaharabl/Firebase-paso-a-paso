
Aqui tendrás todos los pasos que necesitas para implementar la autentificaico de firebasa

1. crea un prollecto en firebase desde la pag wed:

   https://firebase.google.com/

2. accedemos al assistente para que nos ayude a implementar el prollecto 

   tools > firebase > Authentication > Using Kotlin > coonnet to firebase

   tras hacerlo cons saldrá conectado 

3. añadimos dependencias que son las siguientes

   // FirebaseUI for Firebase Auth
   implementation 'com.firebaseui:firebase-ui-auth:8.0.2'
   implementation 'com.google.android.gms:play-services-auth:20.4.0'
   implementation 'androidx.core:core-ktx:+'
   // implementation 'com.google.firebase:firebase-auth-ktx:21.1.0'

   testImplementation 'junit:junit:4.13.2'
   androidTestImplementation 'androidx.test.ext:junit:1.1.4'
   androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.0'


   //para el binding
     buildFeatures {
        viewBinding = true
    }

5. el grandle global tiene que quedar así
   plugins {
   id 'com.android.application' version '7.3.1' apply false
   id 'com.android.library' version '7.3.1' apply false
   id 'org.jetbrains.kotlin.android' version '1.8.0' apply false
   id 'com.google.gms.google-services' version '4.3.14' apply false
   }

   task clean(type: Delete) { delete rootProject.buildDir }

6. empezamos a picar código para la autentificacion

   Main

   1. añadimos las variables que bamos a necesitar
      //variable para el binding
      private lateinit var binding: ActivityMainBinding

     //variable para inicio de sesion
      private lateinit var firebaseAuth: FirebaseAuth
      private lateinit var authStateListener: FirebaseAuth.AuthStateListener

   2. El oncrerate tenemos que hacer el binding y el inicio de la autentificacion

      override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)

           binding = ActivityMainBinding.inflate(layoutInflater)
           setContentView(binding.root)

           configAuth()  //configuracion del metodo de autentificacion
      }
   
   3. hacemos el configAuth donde iniciaremos las variales de autentificacion, listener de este y mostramos 

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
                    AuthUI.IdpConfig.GoogleBuilder().build()) //google

                //lanzar il intent para mostrar todas las formas de logueo
                resultLauncher.launch(//este bloque es el intent para mostrar el logeado
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build())
            }
      }
      }
   
   4. en el ultimo paso creamos el intent que vamos a lanzar y ver si se ha autentificado bien

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

   5. ya tendriamos la autentificacion!! 
   6. para que cuando la app esteen distintos ciclos de vida sobreescribimos las funciones correspondientes

      // ciclo de vida
      override fun onResume() {
      super.onResume()
      firebaseAuth.addAuthStateListener (authStateListener)
      }

       override fun onPause() {
       super.onPause()
       firebaseAuth.removeAuthStateListener (authStateListener)
      }
   
   7. para poder salir lo podemos hacer con un menú o con un boton
      1. boton : creamos una funcion en el create con los listeners y añadimos el dfel boton
         private fun initListeners() {

             binding.salirButton.setOnClickListener {
             AuthUI.getInstance().signOut(this) //salir de la session
                .addOnSuccessListener {
                    Toast.makeText(this, "Session cerrada", Toast.LENGTH_SHORT).show()
                }
             //si salimos pedimos de nuevo la autentificacion
              configAuth()

      } 
   
   8. menú: para un menu tendriasmo primero que hacer un menú con el iten de salir
      y el código sería el siguiente
   
      //evaluar las opciones del menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out ->{
                AuthUI.getInstance().signOut(this) //salir de la session
                    .addOnSuccessListener {
                        Toast.makeText(this, "Session cerrada", Toast.LENGTH_SHORT).show()
                    }
                        //otra forma de comprobar si ha salido de forma correcta
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            binding.textInit.visibility = View.GONE
                        }else {
                            Toast.makeText(this, "no se puede cerrar la session", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
   
   
