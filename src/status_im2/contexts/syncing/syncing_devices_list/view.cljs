(ns status-im2.contexts.syncing.syncing-devices-list.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.syncing.syncing-devices-list.style :as style]
            [status-im2.common.not-implemented :as not-implemented]
            [utils.re-frame :as rf]))

;;TODO remove mock data (#https://github.com/status-im/status-mobile/issues/15142)
(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?   true
     :mid-section  {:type :text-only :main-text ""}
     :left-section {:type                :grey
                    :icon                :i/arrow-left
                    :icon-override-theme :dark
                    :on-press            #(rf/dispatch [:navigate-back])}}]])

(defn view
  []
  [rn/view {:style style/container-main}
   [navigation-bar]
   [rn/view {:style style/page-container}
    [rn/view {:style style/title-container}
     [quo/text
      {:size   :heading-1
       :weight :semi-bold
       :style  {:color colors/white}} (i18n/label :t/syncing)]
     [quo/button
      {:size     32
       :icon     true
       :on-press #(rf/dispatch [:navigate-to :settings-setup-syncing])}
      :i/add]]
    [rn/view {:style style/devices-container}
     [device/render-device
      {:name         "iPhone 11"
       :this-device? true
       :device-type  :mobile}]]]])
