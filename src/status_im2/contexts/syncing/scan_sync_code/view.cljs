(ns status-im2.contexts.syncing.scan-sync-code.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.syncing.scan-sync-code.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.scan-qr-code.view :as scan-qr-code]))


(defn- header
  [active-tab title]
  [:<>
   [rn/view {:style style/header-container}
    [quo/button
     {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :close-sign-in-by-syncing
      :override-theme      :dark
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/arrow-left]
    [quo/button
     {:before              :i/info
      :type                :blur-bg
      :size                32
      :accessibility-label :find-sync-code
      :override-theme      :dark
      :on-press            #(js/alert "Yet to be implemented")}
     (i18n/label :t/find-sync-code)]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-text}
    title]
   [quo/text
    {:size   :paragraph-1
     :weight :regular
     :style  style/header-sub-text}
    (i18n/label :t/synchronise-your-data-across-your-devices)]
   [rn/view {:style style/tabs-container}
    [quo/segmented-control
     {:size           32
      :override-theme :dark
      :blur?          true
      :default-active @active-tab
      :data           [{:id 1 :label (i18n/label :t/scan-sync-qr-code)}
                       {:id 2 :label (i18n/label :t/enter-sync-code)}]
      :on-change      (fn [id]
                        (reset! active-tab id) )}]]])




(defn- scan-qr-code-tab
  []
  [:<>
     [:f> scan-qr-code/view]     
     ])

(defn- enter-sync-code-tab
  []
  [rn/view {:style style/enter-sync-code-container}
   [quo/text
    {:size   :paragraph-1
     :weight :medium
     :style  {:color colors/white}}
    "Yet to be implemented"]])

(defn- bottom-view
  [insets]
  [rn/touchable-without-feedback
   {:on-press #(js/alert "Yet to be implemented")}
   [rn/view
    {:style (style/bottom-container (:bottom insets))}
    [quo/text
     {:size   :paragraph-2
      :weight :medium
      :style  style/bottom-text}
     (i18n/label :t/i-dont-have-status-on-another-device)]]])

(defn f-view
  [{:keys [title show-bottom-view? background]}]
  (let [insets         (safe-area/get-insets)
        active-tab     (reagent/atom 1)
        qr-view-finder (reagent/atom {})]
    (fn []
      (let [scan-qr-code-tab?                (= @active-tab 1)]
      
        [:<>
         background
         [rn/view {:style (style/root-container (:top insets))}
          [header active-tab title]
          (case @active-tab
            1 [scan-qr-code-tab]
            2 [enter-sync-code-tab]
            nil)
          [rn/view {:style style/flex-spacer}]
          (when show-bottom-view? [bottom-view insets])]]))
          ))

(defn view
  [props]
  [:f> f-view props])
