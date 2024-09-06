(ns status-im.common.alert-banner.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.safe-area :as safe-area]
            [status-im.common.alert-banner.style :as style]
            [status-im.constants :as constants]
            [utils.re-frame :as rf]))

(defn get-colors-map
  [theme]
  {:alert
   {:background-color (colors/resolve-color :warning theme 20)
    :text-color       (colors/resolve-color :warning theme)}
   :error
   {:background-color (colors/resolve-color :danger theme 20)
    :text-color       (colors/resolve-color :danger theme)}})

(defn- banner
  [{:keys [text type second-banner? colors-map on-press]}]
  [rn/pressable
   {:on-press #(when on-press (on-press))
    :style    (when second-banner? style/second-banner-wrapper)}
   [hole-view/hole-view
    {:style (style/hole-view (get-in colors-map [type :background-color]))
     :holes (if second-banner?
              []
              [{:x            0
                :y            constants/alert-banner-height
                :width        (int (:width (rn/get-window)))
                :height       constants/alert-banner-height
                :borderRadius style/border-radius}])}
    [quo/text
     {:size   :paragraph-2
      :weight :medium
      :style  {:color (get-in colors-map [type :text-color])}}
     text]]])

(defn view
  []
  (let [banners       (rf/sub [:alert-banners])
        hide-banners? (rf/sub [:alert-banners/hide?])
        theme         (quo.theme/use-theme)
        banners-count (count banners)
        alert-banner  (:alert banners)
        error-banner  (:error banners)
        safe-area-top (safe-area/get-top)
        colors-map    (get-colors-map theme)]
    (when-not hide-banners?
      [hole-view/hole-view
       ;; required for fix flicker issue https://github.com/status-im/status-mobile/issues/19490
       {:style {:padding-bottom 0.5}
        :holes [{:x            0
                 :y            (+ safe-area-top (* constants/alert-banner-height banners-count))
                 :width        (int (:width (rn/get-window)))
                 :height       constants/alert-banner-height
                 :borderRadius style/border-radius}]}
       [rn/view {:style {:background-color colors/neutral-100}}
        [rn/view
         {:style {:height           safe-area-top
                  :background-color (get-in colors-map
                                            [(if error-banner :error :alert) :background-color])}}]
        (when error-banner
          [banner
           (assoc error-banner
                  :colors-map     colors-map
                  :second-banner? false)])
        (when alert-banner
          [banner
           (assoc alert-banner
                  :colors-map     colors-map
                  :second-banner? (= 2 banners-count))])]])))
