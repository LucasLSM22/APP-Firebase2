package com.example.myapplication

// Importações necessárias
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Atividade principal da aplicação
class MainActivity : ComponentActivity() {
    val db: FirebaseFirestore = Firebase.firestore // Conexão com o banco de dados Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tema da aplicação
            MyApplicationTheme {
                // Tela principal da aplicação
                Surface(
                    modifier = Modifier.fillMaxSize(), // Ocupar a tela toda
                    color = MaterialTheme.colorScheme.background // Cor de fundo
                ) {
                    app(db = db) // Chama a função que cria a interface
                }
            }
        }
    }
}

// Função que desenha a interface
@Composable
fun app(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") } // Variável para armazenar o nome
    var telefone by remember { mutableStateOf("") } // Variável para armazenar o telefone
    var clientes by remember { mutableStateOf(listOf<Map<String, String>>()) } // Lista de clientes

    // Coluna para organizar os itens na tela
    Column(
        Modifier
            .fillMaxWidth() // Usar toda a largura
            .padding(16.dp), // Espaçamento interno
        horizontalAlignment = Alignment.CenterHorizontally // Alinhar os itens ao centro
    ) {
        Spacer(modifier = Modifier.height(20.dp)) // Espaço vertical

        // Carregar e exibir uma imagem
        val image: Painter = painterResource(id = R.drawable.jao)
        Image(painter = image, contentDescription = "Imagem", modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp)) // Mais espaço

        // Exibir nome e turma
        Text(text = "Nome: Lucas | Turma: 3DS", modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(20.dp))

        // Campo para inserir o nome
        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth(0.3f) // Definir largura da coluna
            ) {
                Text(text = "Nome:") // Texto
            }
            Column {
                TextField(
                    value = nome, // Variável para armazenar o texto
                    onValueChange = { nome = it }, // Atualiza o valor quando o texto muda
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp) // Campo com cantos arredondados
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre campos

        // Campo para inserir o telefone
        Row(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth(0.3f)
            ) {
                Text(text = "Telefone:")
            }
            Column {
                TextField(
                    value = telefone, // Variável para telefone
                    onValueChange = { telefone = it }, // Atualiza o valor quando o texto muda
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para cadastrar cliente
        Button(
            onClick = {
                if (nome.isNotEmpty() && telefone.isNotEmpty()) { // Verifica se os campos não estão vazios
                    val pessoa = hashMapOf(
                        "nome" to nome,
                        "telefone" to telefone
                    )
                    db.collection("Clientes").add(pessoa) // Adiciona no Firestore
                        .addOnSuccessListener { documentReference ->
                            Log.d("Firestore", "Cliente adicionado: ${documentReference.id}")
                            nome = "" // Limpa o campo nome
                            telefone = "" // Limpa o campo telefone
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Erro ao adicionar cliente", e)
                        }
                }
            },
            modifier = Modifier
                .width(200.dp) // Largura do botão
                .padding(8.dp)
        ) {
            Text(text = "Cadastrar") // Texto do botão
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Busca a lista de clientes do Firestore
        LaunchedEffect(Unit) {
            db.collection("Clientes")
                .get()
                .addOnSuccessListener { documents ->
                    val listaClientes = mutableListOf<Map<String, String>>()
                    for (document in documents) {
                        listaClientes.add(
                            mapOf(
                                "id" to document.id,
                                "nome" to "${document.data["nome"]}",
                                "telefone" to "${document.data["telefone"]}"
                            )
                        )
                    }
                    clientes = listaClientes // Atualiza a lista de clientes
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Erro ao buscar clientes: ", exception)
                }
        }

        // Exibir lista de clientes usando LazyColumn
        LazyColumn {
            items(clientes) { cliente ->
                Row(Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.weight(0.5f)) {
                        Text(text = "Nome: ${cliente["nome"]}") // Exibe o nome
                    }
                    Column(Modifier.weight(0.5f)) {
                        Text(text = "Telefone: ${cliente["telefone"]}") // Exibe o telefone
                    }
                }
            }
        }
    }
}
