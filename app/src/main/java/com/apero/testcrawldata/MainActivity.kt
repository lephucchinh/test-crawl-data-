package com.apero.testcrawldata

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.apero.testcrawldata.ui.theme.TestCrawlDataTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TestCrawlDataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeamCompositionScreen()
                }
            }
        }
    }
}

@Composable
fun TeamCompositionScreen() {
    var teams by remember { mutableStateOf<List<TeamInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val result = withContext(Dispatchers.IO) {
                crawlData()
            }
            teams = result
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(teams) { team ->
                        TeamCard(team = team)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamCard(team: TeamInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.rank,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = team.playStyle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = team.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Champions:",
                style = MaterialTheme.typography.titleSmall
            )

            team.champions.forEach { champion ->
                ChampionItem(champion = champion)
            }
        }
    }
}

@Composable
fun ChampionItem(champion: Champion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Champion Image
            if (champion.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(champion.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = champion.name,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit,
                    onError = {
                        Log.e("ImageError", "Failed to load champion image: ${champion.imageUrl}")
                    }
                )
            } else {
                // Placeholder when no image URL
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = champion.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = champion.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (champion.championClass.isNotEmpty()) {
                        Text(
                            text = "Class $champion.championClass",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (champion.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        champion.items.forEach { item ->
                            if (item.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.name,
                                    modifier = Modifier.size(24.dp),
                                    contentScale = ContentScale.Fit,
                                    onError = {
                                        Log.e(
                                            "ImageError",
                                            "Failed to load item image: ${item.imageUrl}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun crawlData(): List<TeamInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://tftactics.gg/tierlist/team-comps/").get()
            val teamElements = doc.select("div.team-portrait")

            teamElements.map { teamElement ->
                val rank = teamElement.selectFirst("div.team-rank")?.text().orEmpty()
                val name = teamElement.selectFirst("div.team-name-elipsis")?.ownText().orEmpty()
                val playStyle = teamElement.selectFirst("div.team-playstyle")?.text().orEmpty()

                Log.d("TeamInfo", "Team: $name (Rank: $rank, Style: $playStyle)")

                val championAnchors = teamElement.select("div.team-characters > a.characters-item")

                val champions = championAnchors.mapNotNull { championEl ->
                    val championName = championEl.selectFirst("div.team-character-name")?.text()?.trim()
                        ?: championEl.selectFirst("img.character-icon")?.attr("alt")?.trim()
                        ?: ""

                    val championImage = championEl.selectFirst("div.character-wrapper img.character-icon")
                        ?.attr("src")?.trim() ?: ""

                    // Get champion class (c1, c2, c3, c4)
                    val championClass = championEl.classNames()
                        .find { it.startsWith("c") }
                        ?.substring(1) ?: ""

                    Log.d("ChampionDebug", "Champion: $championName (Class: $championClass)")

                    // Get items for this champion
                    val itemElements = championEl.select("div.character-items > a.characters-item")
                    val items = itemElements.mapNotNull { itemEl ->
                        Log.d("dkm", "$itemEl: ")
                        val itemImage = itemEl.selectFirst("div.character-wrapper img.character-icon")?.attr("src")?.trim() ?: ""
                        val itemName = itemEl.selectFirst("div.character-wrapper img.character-icon")?.attr("alt")?.trim() ?: ""
                        
                        if (itemName.isNotEmpty() && itemImage.isNotEmpty()) {
                            Log.d("Item", "Champion: $championName - Item: $itemName ($itemImage)")
                            ChampionItem(itemName, itemImage)
                        } else null
                    }

                    Log.d("Champion", "Created: $championName with ${items.size} items")
                    items.forEachIndexed { index, item ->
                        Log.d("ChampionItems", "Item ${index + 1}: ${item.name}")
                    }

                    Champion(
                        name = championName,
                        imageUrl = championImage,
                        items = items,
                        championClass = championClass
                    )
                }

                Log.d("TeamParsed", "Team $name - Champions: ${champions.size}")
                TeamInfo(rank, name, playStyle, champions)
            }
        } catch (e: Exception) {
            Log.e("CrawlError", "Error crawling data: ${e.message}", e)
            throw e
        }
    }
}

data class ChampionItem(
    val name: String,
    val imageUrl: String
)

data class Champion(
    val name: String,
    val imageUrl: String,
    val items: List<ChampionItem>,
    val championClass: String = ""
)

data class TeamInfo(
    val rank: String,
    val name: String,
    val playStyle: String,
    val champions: List<Champion>
)