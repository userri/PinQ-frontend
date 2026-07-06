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
 * @param horizontalPaddingDp 배너를 감싸는 부모의 좌우 패딩 합계 (적응형 폭 계산용)
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    horizontalPaddingDp: Int = 0,
) {
    // @Preview 렌더링에서는 AdView 를 만들 수 없으므로 그리지 않는다
    if (LocalInspectionMode.current) return

    val adWidthDp = LocalConfiguration.current.screenWidthDp - horizontalPaddingDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(
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
