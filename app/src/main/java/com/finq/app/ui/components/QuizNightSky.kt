package com.finq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.finq.app.ui.theme.BgBase
import com.finq.app.ui.theme.Lime
import kotlin.random.Random

/**
 * 퀴즈 화면의 밤하늘 배경.
 *
 * 홈의 밤 풍경과 같은 언어지만 **언덕·나무·별똥별이 없고 움직이지도 않는다**.
 * 퀴즈는 긴 지문을 읽는 화면이라 배경이 움직이면 글자와 싸운다.
 *
 * 이 배경은 장식이 아니라 **유리 선지가 성립하기 위한 조건**이다. 뒤가 균일하면
 * 알파를 낮춰도 비침이 생기지 않고 채도 낮은 불투명 면이 될 뿐이다. 비침을 만드는
 * 것은 별이 아니라 앰비언트 블롭 — 넓은 밝기 기울기다. 별은 점이라 카드 한 장
 * 안에서 밝기 차를 만들지 못한다.
 *
 * 블롭 두 개는 **선지 영역에 걸리도록** 아래쪽에 둔다. 위로 올리면 질문 글자에
 * 색이 물든다.
 */
@Composable
fun QuizNightSky(modifier: Modifier = Modifier) {
    val stars = remember {
        val rnd = Random(SKY_SEED)
        List(STAR_COUNT) {
            Star(
                x = rnd.nextFloat(),
                // 제곱 분포 — 위쪽이 성기고 아래로 갈수록 촘촘하다.
                y = rnd.nextFloat() * rnd.nextFloat(),
                radiusDp = 0.9f + rnd.nextFloat() * 1.4f,
                alpha = 0.25f + rnd.nextFloat() * (STAR_ALPHA_MAX - 0.25f),
            )
        }
    }
    val nightTop = lerp(BgBase, Color.Black, 0.62f)
    Canvas(modifier) {
        drawRect(
            brush = Brush.verticalGradient(0f to nightTop, 0.72f to BgBase, 1f to BgBase),
            size = size,
        )
        listOf(
            Triple(0.16f, 0.40f, Lime),
            Triple(0.88f, 0.62f, BlobBlue),
        ).forEach { (cx, cy, color) ->
            val center = Offset(cx * size.width, cy * size.height)
            val radius = size.minDimension * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = BLOB_ALPHA), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
        // 아래로 갈수록 블롭을 잠재운다. 문항이 짧으면 선지 아래가 통째로 비는데,
        // 거기 남은 발광은 가리킬 것이 없어 그냥 초록 안개로 보인다.
        drawRect(
            brush = Brush.verticalGradient(
                0.5f to Color.Transparent,
                1f to BgBase,
            ),
            size = size,
        )
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radiusDp.dp.toPx() / 2f,
                center = Offset(star.x * size.width, star.y * size.height * 0.85f),
            )
        }
    }
}

private data class Star(val x: Float, val y: Float, val radiusDp: Float, val alpha: Float)

private const val SKY_SEED = 20260808
private const val STAR_COUNT = 56
private const val STAR_ALPHA_MAX = 0.95f
private const val BLOB_ALPHA = 0.18f
private val BlobBlue = Color(0xFF4A7FD4)
