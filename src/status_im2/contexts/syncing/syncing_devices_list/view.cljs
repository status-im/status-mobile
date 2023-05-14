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

(defn render-device
  [{:keys [device
           this-device?
           type]}]
  [rn/view {:style style/device-container}
   [rn/view {:style style/icon-container}
    [quo/icon
     (if (= type :mobile) :i/mobile :i/desktop)
     {:color colors/white}]]
   [rn/view {:style style/device-details}
    [quo/text
     {:accessibility-label :device-name
      :weight              :medium
      :size                :paragraph-1
      :style               {:color colors/white}}
     device]
    [not-implemented/not-implemented
     [quo/text
      {:accessibility-label :next-back-up
       :size                :paragraph-2
       :style               {:color colors/white-opa-40}}
      "Next backup in 04:36:12"]]
    (when this-device?
      [rn/view {:style style/tag-container}
       [quo/status-tag
        {:size           :small
         :status         {:type :positive}
         :no-icon?       true
         :label          (i18n/label :t/this-device)
         :override-theme :dark}]])]])

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
     [render-device
      {:device       "iPhone 11"
       :this-device? true
       :type         :mobile}]]]])
