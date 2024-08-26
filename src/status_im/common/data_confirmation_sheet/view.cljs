(ns status-im.common.data-confirmation-sheet.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.data-confirmation-sheet.style :as style]
    [status-im.common.events-helper :as events-helper]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-choice-callback
  [syncing-on-mobile-network?]
  (rf/dispatch [:network/set-syncing-on-mobile-network syncing-on-mobile-network?])
  (events-helper/hide-bottom-sheet))

(def on-wifi-only (partial on-choice-callback false))
(def on-mobile-and-wifi (partial on-choice-callback true))

(defn view
  []
  (let [settings-drawer?           (= (rf/sub [:view-id]) :screen/settings.syncing)
        syncing-on-mobile-network? (rf/sub [:profile/syncing-on-mobile-network?])]
    (rn/use-mount events-helper/dismiss-keyboard)
    [:<>
     [quo/text
      {:weight              :semi-bold
       :size                :heading-2
       :accessibility-label :data-confirmation-sheet-heading
       :style               style/heading}
      (i18n/label (if settings-drawer? :t/sync-and-backup :t/which-connection-to-use))]
     [quo/text
      {:weight              :regular
       :size                :paragraph-1
       :accessibility-label :data-confirmation-sheet-message
       :style               style/message}
      (i18n/label :t/syncing-connection-message)]
     [quo/information-box
      {:type  :default
       :icon  :i/info
       :blur? true
       :style style/warning}
      (i18n/label :t/syncing-wifi-connection-warning)]
     (if settings-drawer?
       [rn/view {:style style/drawer-container}
        [quo/drawer-action
         {:title               (i18n/label :t/mobile-data-and-wifi)
          :state               (when syncing-on-mobile-network? :selected)
          :blur?               true
          :accessibility-label :mobile-data-and-wifi-action
          :icon                :i/connection
          :on-press            on-mobile-and-wifi}]
        [quo/drawer-action
         {:title               (i18n/label :t/wifi-only)
          :state               (when-not syncing-on-mobile-network? :selected)
          :blur?               true
          :accessibility-label :wifi-only-action
          :icon                :i/placeholder
          :on-press            on-wifi-only}]]
       [quo/bottom-actions
        {:actions          :two-actions
         :blur?            true
         :container-style  {:margin-top 12}
         :button-one-label (i18n/label :t/wifi-only)
         :button-one-props {:type     :grey
                            :on-press on-wifi-only}
         :button-two-label (i18n/label :t/mobile-and-wifi)
         :button-two-props {:on-press on-mobile-and-wifi}}])
     (if settings-drawer?
       [quo/divider-line {:container-style {:padding-vertical 8}}]
       [quo/text
        {:weight :regular
         :size   :paragraph-2
         :style  style/settings-subtext}
        (i18n/label :t/you-can-change-later-in-settings)])]))
