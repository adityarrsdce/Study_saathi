package com.babu.appp.screen


import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

data class ResumeData(
    val name: String,
    val email: String,
    val phone: String,
    val education: String,
    val skills: String
)

@Composable
fun ResumeScreen() {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Resume Builder", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Skills") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val data = ResumeData(name, email, phone, education, skills)
                savePdf(context, data)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate PDF")
        }

        Spacer(Modifier.height(16.dp))

        Text("Preview:", style = MaterialTheme.typography.titleMedium)
        ResumeTemplate(
            ResumeData(name, email, phone, education, skills)
        )
    }
}

@Composable
fun ResumeTemplate(data: ResumeData) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = data.name, style = MaterialTheme.typography.headlineSmall)
        Text(text = data.email)
        Text(text = data.phone)
        Spacer(Modifier.height(8.dp))
        Text("Education: ${data.education}")
        Text("Skills: ${data.skills}")
    }
}

fun savePdf(context: Context, data: ResumeData) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(1080, 1920, 1).create()
    val page = pdfDocument.startPage(pageInfo)

    val composeView = ComposeView(context).apply {
        setContent {
            ResumeTemplate(data)
        }
        measure(
            View.MeasureSpec.makeMeasureSpec(pageInfo.pageWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(pageInfo.pageHeight, View.MeasureSpec.EXACTLY)
        )
        layout(0, 0, pageInfo.pageWidth, pageInfo.pageHeight)
        draw(page.canvas)
    }

    pdfDocument.finishPage(page)

    val file = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
        "MyResume.pdf"
    )
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    Toast.makeText(context, "PDF Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
}
