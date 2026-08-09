package com.finq.app.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.finq.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * 인라인 적응형 배너 광고.
 *
 * - 광고 단위 ID 는 BuildConfig.ADMOB_BANNER_UNIT_ID 로 주입 —
 *   debug 는 구글 공식 테스트 광고, release 는 실제 광고 단위.
 * - 광고가 채워지지 않으면(no-fill) 높이 0 으로 접혀 빈 공간을 남기지 않는다.
 *   (비공개 테스트 기간에는 앱이 스토어에 게시 전이라 no-fill 이 정상이다)
 *
 * **인라인(inline)과 앵커(anchored)를 자리에 맞게 쓴다.** 인라인은 높이가 자유로워
 * 스크롤 콘텐츠 안에서만 맞고, 화면 하단에 고정하는 자리에 쓰면 세로를 1/3 까지
 * 먹는다(목록에서 실제로 그랬다). 하단 고정에는 높이가 정해진 앵커형을 쓴다.
 *
 * @param horizontalPaddingDp 배너를 감싸는 부모의 좌우 패딩 합계 (적응형 폭 계산용)
 * @param anchored 화면 하단에 고정하는 자리인가. 기본은 스크롤 콘텐츠용 인라인.
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    horizontalPaddingDp: Int = 0,
    anchored: Boolean = false,
) {
    // @Preview 렌더링에서는 AdView 를 만들 수 없으므로 그리지 않는다
    if (LocalInspectionMode.current) return

    val adWidthDp = LocalConfiguration.current.screenWidthDp - horizontalPaddingDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    if (anchored)
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
                    else
                        AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidthDp)
                )
                adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { adView -> adView.destroy() },
    )
}
