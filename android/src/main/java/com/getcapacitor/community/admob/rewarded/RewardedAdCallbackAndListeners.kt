package com.getcapacitor.community.admob.rewarded

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AbMobPluginError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.common.util.BiConsumer

object RewardedAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardAdPluginEvents.Rewarded, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): RewardedAdLoadCallback {
        return object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                AdRewardExecutor.mRewardedAd = ad
                AdRewardExecutor.mRewardedAd.fullScreenContentCallback = FullscreenPluginCallback(
                        RewardAdPluginEvents, notifyListenersFunction)


                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardAdPluginEvents.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AbMobPluginError(adError)

                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

}