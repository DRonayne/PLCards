package com.darach.plcards.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.Log
import androidx.core.content.FileProvider
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.ui.my_xi.Formation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareHelper @Inject constructor() {


    private fun downloadImage(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    fun shareTeamFormation(context: Context, players: Map<Int, CardModel>, formation: Formation) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the formation image
                val formationBitmap = createTeamFormationImage(players, formation)

                withContext(Dispatchers.Main) {
                    shareFormationImage(context, formation, formationBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    shareFormationTextOnly(context, formation, players)
                }
            }
        }
    }

    private fun createTeamFormationImage(
        players: Map<Int, CardModel>,
        formation: Formation
    ): Bitmap {
        val width = 1080
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Create football pitch background
        val pitchGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#2E7D32"),
                Color.parseColor("#388E3C"),
                Color.parseColor("#43A047")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        val pitchPaint = Paint().apply { shader = pitchGradient }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pitchPaint)

        // Draw pitch markings
        val linePaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Outer boundary
        canvas.drawRect(80f, 120f, width - 80f, height - 200f, linePaint)

        // Center circle
        val centerX = width / 2f
        val centerY = (height - 200f + 120f) / 2f
        canvas.drawCircle(centerX, centerY, 120f, linePaint)

        // Center line
        canvas.drawLine(80f, centerY, width - 80f, centerY, linePaint)

        // Goal areas
        val goalWidth = 200f
        val goalHeight = 80f
        val goalX = centerX - goalWidth / 2f

        // Top goal area
        canvas.drawRect(goalX, 120f, goalX + goalWidth, 120f + goalHeight, linePaint)

        // Bottom goal area
        canvas.drawRect(
            goalX,
            height - 200f - goalHeight,
            goalX + goalWidth,
            height - 200f,
            linePaint
        )

        // Formation title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 72f
            isAntiAlias = true
            setShadowLayer(8f, 4f, 4f, Color.parseColor("#80000000"))
        }
        canvas.drawText(formation.displayName, centerX, 80f, titlePaint)

        // Calculate positions for each row
        val pitchTop = 200f
        val pitchBottom = height - 280f
        val pitchHeight = pitchBottom - pitchTop
        val rowSpacing = pitchHeight / 4f // 4 rows: GK, DEF, MID, FWD

        // Draw players
        val playerSize = 120f
        val nameTextSize = 32f

        // Goalkeeper (bottom)
        val gkY = pitchBottom - rowSpacing / 2f
        players[0]?.let { player ->
            drawPlayerOnPitch(canvas, player, centerX, gkY, playerSize, nameTextSize)
        }

        // Defenders
        val defY = pitchBottom - rowSpacing * 1.5f
        val defSpacing = (width - 200f) / (formation.defenders + 1)
        for (i in 0 until formation.defenders) {
            val defX = 100f + defSpacing * (i + 1)
            players[1 + i]?.let { player ->
                drawPlayerOnPitch(canvas, player, defX, defY, playerSize, nameTextSize)
            }
        }

        // Midfielders
        val midY = pitchBottom - rowSpacing * 2.5f
        val midSpacing = (width - 200f) / (formation.midfielders + 1)
        for (i in 0 until formation.midfielders) {
            val midX = 100f + midSpacing * (i + 1)
            players[1 + formation.defenders + i]?.let { player ->
                drawPlayerOnPitch(canvas, player, midX, midY, playerSize, nameTextSize)
            }
        }

        // Forwards
        val fwdY = pitchBottom - rowSpacing * 3.5f
        val fwdSpacing = (width - 200f) / (formation.forwards + 1)
        for (i in 0 until formation.forwards) {
            val fwdX = 100f + fwdSpacing * (i + 1)
            players[1 + formation.defenders + formation.midfielders + i]?.let { player ->
                drawPlayerOnPitch(canvas, player, fwdX, fwdY, playerSize, nameTextSize)
            }
        }

        // Add watermark
        val watermarkPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 36f
            isAntiAlias = true
            alpha = 200
        }
        canvas.drawText("PLCards", width - 60f, height - 60f, watermarkPaint)

        return bitmap
    }

    private fun drawPlayerOnPitch(
        canvas: Canvas,
        player: CardModel,
        x: Float,
        y: Float,
        playerSize: Float,
        nameTextSize: Float
    ) {
        // Try to download and draw player image
        val playerImage = try {
            downloadImage(player.cardImageUrl)
        } catch (e: Exception) {
            null
        }

        // Draw circular background
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x, y - playerSize / 3f, playerSize / 2f, backgroundPaint)

        // Draw border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#1976D2")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawCircle(x, y - playerSize / 3f, playerSize / 2f, borderPaint)

        if (playerImage != null) {
            // Create circular clip for the image
            val circularImage = Bitmap.createBitmap(
                (playerSize - 12f).toInt(),
                (playerSize - 12f).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val imageCanvas = Canvas(circularImage)

            // Draw circular path
            val path = Path()
            path.addCircle(
                (playerSize - 12f) / 2f,
                (playerSize - 12f) / 2f,
                (playerSize - 12f) / 2f,
                Path.Direction.CW
            )
            imageCanvas.clipPath(path)

            // Scale and draw the player image
            val scaledImage = Bitmap.createScaledBitmap(
                playerImage,
                (playerSize - 12f).toInt(),
                (playerSize - 12f).toInt(),
                true
            )
            imageCanvas.drawBitmap(scaledImage, 0f, 0f, null)

            // Draw the circular image on main canvas
            canvas.drawBitmap(
                circularImage,
                x - (playerSize - 12f) / 2f,
                y - playerSize / 3f - (playerSize - 12f) / 2f,
                null
            )
        } else {
            // Draw player initials if no image
            val initialsPaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = playerSize / 3f
                isAntiAlias = true
            }
            val initials =
                player.playerName.split(" ").take(2).joinToString("") { it.first().toString() }
                    .uppercase()
            canvas.drawText(
                initials,
                x,
                y - playerSize / 3f + initialsPaint.textSize / 3f,
                initialsPaint
            )
        }

        // Draw player name
        val namePaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = nameTextSize
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000"))
        }

        // Use last name only for better fit
        val lastName = player.playerName.split(" ").lastOrNull() ?: player.playerName
        canvas.drawText(lastName, x, y + playerSize / 3f, namePaint)
    }

    private fun shareFormationImage(
        context: Context,
        formation: Formation,
        formationBitmap: Bitmap
    ) {
        try {
            // Save to cache directory
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val imageFile = File(cacheDir, "my_xi_${formation.displayName.replace("-", "")}.jpg")
            val fileOutputStream = FileOutputStream(imageFile)
            formationBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.close()

            // Get URI using FileProvider
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Create share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out my ${formation.displayName} formation in PLCards!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch chooser
            val chooserIntent = Intent.createChooser(shareIntent, "Share My XI")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

        } catch (e: IOException) {
            e.printStackTrace()
            // Fallback to text-only sharing
            shareFormationTextOnly(context, formation, emptyMap())
        }
    }

    private fun shareFormationTextOnly(
        context: Context,
        formation: Formation,
        players: Map<Int, CardModel>
    ) {
        val shareText = buildString {
            append("My ${formation.displayName} formation in PLCards:\n\n")

            // Add players by position if available
            if (players.isNotEmpty()) {
                val sortedPlayers = players.toList().sortedBy { it.first }
                sortedPlayers.forEach { (position, card) ->
                    val positionName = when {
                        position == 0 -> "GK"
                        position <= formation.defenders -> "DEF"
                        position <= formation.defenders + formation.midfielders -> "MID"
                        else -> "FWD"
                    }
                    append("$positionName: ${card.playerName} (${card.team})\n")
                }
            }

            append("\nBuilt with PLCards!")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share My XI")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    fun shareCardDetails(context: Context, cardModel: CardModel) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the card details image
                val cardBitmap = createCardDetailsImage(cardModel)

                withContext(Dispatchers.Main) {
                    shareCardImage(context, cardModel, cardBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    shareCardTextOnly(context, cardModel)
                }
            }
        }
    }

    private fun createCardDetailsImage(cardModel: CardModel): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Create gradient background
        val backgroundGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#1E1E1E"),
                Color.parseColor("#2D2D2D"),
                Color.parseColor("#1A1A1A")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        val backgroundPaint = Paint().apply { shader = backgroundGradient }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Card image area with blur effect background
        val cardImageSize = 500f
        val cardImageX = (width - cardImageSize) / 2f
        val cardImageY = 100f

        // Create blurred background
        val blurPaint = Paint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            alpha = 60
        }

        // Download and process card image
        val cardImage = try {
            downloadImage(cardModel.cardImageUrl)
        } catch (e: Exception) {
            null
        }

        if (cardImage != null) {
            // Create blurred background version
            val blurredBg = Bitmap.createScaledBitmap(cardImage, width, height, true)
            canvas.drawBitmap(blurredBg, 0f, 0f, blurPaint)

            // Draw main card image with rounded corners
            RectF(
                cardImageX,
                cardImageY,
                cardImageX + cardImageSize,
                cardImageY + cardImageSize * 1.4f
            )
            val cornerRadius = 24f

            // Create rounded bitmap
            val roundedBitmap = Bitmap.createBitmap(
                cardImageSize.toInt(),
                (cardImageSize * 1.4f).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val roundedCanvas = Canvas(roundedBitmap)
            val path = Path().apply {
                addRoundRect(
                    RectF(0f, 0f, cardImageSize, cardImageSize * 1.4f),
                    cornerRadius,
                    cornerRadius,
                    Path.Direction.CW
                )
            }
            roundedCanvas.clipPath(path)

            val scaledCardImage = Bitmap.createScaledBitmap(
                cardImage,
                cardImageSize.toInt(),
                (cardImageSize * 1.4f).toInt(),
                true
            )
            roundedCanvas.drawBitmap(scaledCardImage, 0f, 0f, null)

            // Draw shadow
            val shadowPaint = Paint().apply {
                color = Color.BLACK
                alpha = 100
                maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawRoundRect(
                RectF(
                    cardImageX + 8f,
                    cardImageY + 8f,
                    cardImageX + cardImageSize + 8f,
                    cardImageY + cardImageSize * 1.4f + 8f
                ),
                cornerRadius, cornerRadius, shadowPaint
            )

            // Draw main card image
            canvas.drawBitmap(roundedBitmap, cardImageX, cardImageY, null)
        }

        // Player name
        val playerNamePaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 64f
            isAntiAlias = true
            setShadowLayer(8f, 4f, 4f, Color.parseColor("#80000000"))
        }
        canvas.drawText(
            cardModel.playerName,
            width / 2f,
            cardImageY + cardImageSize * 1.4f + 80f,
            playerNamePaint
        )

        // Team name
        val teamPaint = Paint().apply {
            color = Color.parseColor("#B0BEC5")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 42f
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000"))
        }
        canvas.drawText(
            cardModel.team,
            width / 2f,
            cardImageY + cardImageSize * 1.4f + 140f,
            teamPaint
        )

        // Season under team name (only if not WC2002)
        if (cardModel.season != "WC2002") {
            val seasonPaint = Paint().apply {
                color = Color.parseColor("#90CAF9")
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 36f
                isAntiAlias = true
                setShadowLayer(4f, 2f, 2f, Color.parseColor("#80000000"))
            }
            canvas.drawText(
                cardModel.season,
                width / 2f,
                cardImageY + cardImageSize * 1.4f + 190f,
                seasonPaint
            )
        }

        // Add watermark
        val watermarkPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
            isAntiAlias = true
            alpha = 160
        }
        canvas.drawText("PLCards", width - 60f, height - 60f, watermarkPaint)

        return bitmap
    }

    private fun shareCardImage(context: Context, cardModel: CardModel, cardBitmap: Bitmap) {
        try {
            // Save to cache directory
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val imageFile = File(cacheDir, "card_${cardModel.playerName.replace(" ", "_")}.jpg")
            val fileOutputStream = FileOutputStream(imageFile)
            cardBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.close()

            // Get URI using FileProvider
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Create share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out this ${cardModel.playerName} card from ${cardModel.team} in PLCards!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch chooser
            val chooserIntent = Intent.createChooser(shareIntent, "Share Card")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

        } catch (e: IOException) {
            e.printStackTrace()
            // Fallback to text-only sharing
            shareCardTextOnly(context, cardModel)
        }
    }

    private fun shareCardTextOnly(context: Context, cardModel: CardModel) {
        val shareText = buildString {
            append("${cardModel.playerName} - ${cardModel.team}\n\n")
            if (cardModel.season != "WC2002") {
                append("Season: ${cardModel.season}\n")
            }
            append("\nShared from PLCards!")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Card")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}