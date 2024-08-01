import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplicationwmsystem.db.DatabaseHelper
import com.example.myapplicationwmsystem.db.GarbageLevelEntry
import com.example.myapplicationwmsystem.fetchGarbageLevelFromThingSpeak
import com.example.myapplicationwmsystem.showNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GarbageLevelScreen(progress: Float, garbageLevel: Int) {
    val progressBarColor = when {
        garbageLevel > 70 -> Color.Red
        garbageLevel in 0..45 -> Color.Green
        else -> Color(0xFFFFA500)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(350.dp)
            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawGauge(progress, progressBarColor)


            val valueTextSize = 30.sp.toPx()
            val labelTextSize = 20.sp.toPx()

            val valueTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = valueTextSize
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }


            val labelTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = labelTextSize
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            }

            val garbageLevelText = "$garbageLevel%"
            val labelText = "garbage level"


            val totalHeight = size.height
            val valueTextOffset = valueTextSize / 2

            drawContext.canvas.nativeCanvas.drawText(
                garbageLevelText,
                size.width / 2,
                totalHeight / 2 - valueTextOffset,
                valueTextPaint
            )

            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                size.width / 2,
                totalHeight / 2 + valueTextOffset + labelTextSize,
                labelTextPaint
            )
        }
    }
}

private fun DrawScope.drawGauge(progress: Float, color: Color) {
    val strokeWidth = 30.dp.toPx()
    val size = size.minDimension
    val radius = size / 2
    val startAngle = -90f
    val sweepAngle = 360f * progress
    val backgroundColor = color.copy(alpha = 0.1f)

    drawArc(
        color = backgroundColor,
        startAngle = startAngle,
        sweepAngle = 360f,
        useCenter = false,
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(strokeWidth)
    )

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(strokeWidth)
    )
}

@Composable
fun DashboardCard(garbageLevel: Int) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(550.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            GarbageLevelScreen(progress = garbageLevel / 100f, garbageLevel = garbageLevel)
            Spacer(modifier = Modifier.height(14.dp))
            LegendSection()
        }
    }
}

@Composable
fun LegendSection() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        LegendItem(Color.Red, "High Garbage Level")
        LegendItem(Color(0xFFFFA500), "Moderate Garbage Level")
        LegendItem(Color.Green, "Low Garbage Level")
    }
}

@Composable
fun LegendItem(color: Color, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = description, style = TextStyle(fontSize = 16.sp))
    }
}
@Composable
fun GarbageLevelScreen(binId: String) {
    var garbageLevel by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val databaseHelper = remember { DatabaseHelper(context) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val fetchedGarbageLevel = fetchGarbageLevelFromThingSpeak(binId)
                    val timestamp = System.currentTimeMillis()

                    saveGarbageLevelToDatabase(databaseHelper, binId, fetchedGarbageLevel, timestamp)

                    garbageLevel = fetchedGarbageLevel

                    if (fetchedGarbageLevel > 80) {
                        showNotification(context, fetchedGarbageLevel)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DashboardCard(garbageLevel = garbageLevel)
    }
}

private fun saveGarbageLevelToDatabase(databaseHelper: DatabaseHelper, binId: String, garbageLevel: Int, timestamp: Long) {
    val entry = GarbageLevelEntry(binId, garbageLevel, timestamp)
    databaseHelper.insertGarbageLevel(entry)
}
