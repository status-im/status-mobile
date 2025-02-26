(ns status-im.contexts.keycard.unblock.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.common.view :as common.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn success-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/keycard-unblocked)}]
   [rn/view {:style {:flex 1}}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/done)]]])

(defn ready-to-unblock
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/ready-to-unblock-keycard)}]
   [rn/view {:style {:flex 1}}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/scan-keycard)
     :button-one-props {:on-press #(rf/dispatch [:keycard/unblock])}}]])

(defn sheet
  []
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/unblock-keycard-recovery)}]
     [quo/text
      {:style {:padding-horizontal 20
               :padding-vertical   8}}
      (i18n/label :t/unblock-keycard-instructions)]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/continue)
       :button-one-props {:customization-color
                          customization-color
                          :on-press
                          (fn []
                            (rf/dispatch [:hide-bottom-sheet])
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch
                             [:open-modal :screen/use-recovery-phrase-dark
                              {:on-success #(rf/dispatch [:keycard/unblock.phrase-entered %])}]))}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press #(rf/dispatch [:hide-bottom-sheet])}}]]))
