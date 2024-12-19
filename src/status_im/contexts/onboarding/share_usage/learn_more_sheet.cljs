(ns status-im.contexts.onboarding.share-usage.learn-more-sheet
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.privacy.view :as privacy]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private bullet-points-data
  [[:t/help-us-improve-status-bullet-point-1 true]
   [:t/help-us-improve-status-bullet-point-2 true]
   [:t/help-us-improve-status-bullet-point-3 false]
   [:t/help-us-improve-status-bullet-point-4 false]
   [:t/help-us-improve-status-bullet-point-5 false]])

(defn- privacy-policy-text
  []
  [rn/view {:style {:margin-horizontal 20 :margin-vertical 8}}
   [quo/text
    [quo/text
     {:style {:color colors/white-opa-50}
      :size  :paragraph-2}
     (i18n/label :t/more-details-in-privacy-policy-1-onboarding)]
    [quo/text
     {:size     :paragraph-2
      :weight   :bold
      :on-press #(rf/dispatch [:show-bottom-sheet {:content privacy/privacy-statement :shell? true}])}
     (i18n/label :t/more-details-in-privacy-policy-2)]]])

(defn- bullet-points
  []
  [:<>
   (for [[label collected?] bullet-points-data]
     ^{:key label}
     [quo/markdown-list
      {:description     (i18n/label label)
       :blur?           true
       :type            :custom-icon
       :container-style {:padding-vertical 5}
       :icon            (if collected? :i/check-circle :i/clear)
       :icon-props      (if collected?
                          {:no-color true}
                          {:size 20 :color colors/danger-60 :color-2 colors/white})}])])

(defn view
  []
  [:<>
   [quo/drawer-top
    {:title (i18n/label :t/help-us-improve-status)}]
   [quo/text
    {:size  :paragraph-1
     :style {:padding-horizontal 20}}
    (i18n/label :t/help-us-improve-status-subtitle)]
   [rn/view {:style {:padding-vertical 8}}
    [bullet-points]]
   [privacy-policy-text]])
