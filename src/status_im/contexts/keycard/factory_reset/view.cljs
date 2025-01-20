(ns status-im.contexts.keycard.factory-reset.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- reset-card
  []
  (rf/dispatch
   [:keycard/factory-reset
    {:on-success (fn []
                   (rf/dispatch [:navigate-back])
                   (rf/dispatch [:keycard/disconnect])
                   (rf/dispatch [:open-modal :screen/keycard.factory-reset.success]))
     :on-failure (fn []
                   (rf/dispatch [:navigate-back])
                   (rf/dispatch [:keycard/disconnect])
                   (rf/dispatch [:open-modal :screen/keycard.factory-reset.fail]))}]))

(defn- connect-and-reset
  [key-uid]
  (rf/dispatch
   [:keycard/connect
    {:key-uid    key-uid
     :on-success reset-card
     :on-error   (fn [error]
                   (if (or (= error :keycard/error.keycard-frozen)
                           (= error :keycard/error.keycard-locked)
                           (= error :keycard/error.keycard-unpaired))
                     (reset-card)
                     (do
                       (rf/dispatch [:navigate-back])
                       (if (= error :keycard/error.keycard-wrong-profile)
                         (do
                           (rf/dispatch [:keycard/disconnect])
                           (rf/dispatch [:open-modal :screen/keycard.different-card]))
                         (rf/dispatch [:keycard/on-application-info-error error])))))}]))

(defn success-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-reset-success)
     :description      :text
     :description-text (i18n/label :t/keycard-empty-ready)}]
   [rn/view {:style {:flex 1}}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/done)]]])

(defn failed-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/keycard-reset-failed)}]
   [rn/view {:style {:flex 1}}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/try-again)]]])

(defn sheet
  []
  (let [customization-color    (rf/sub [:profile/customization-color])
        key-uid                (rf/sub [:keycard/key-uid])
        [checked? set-checked] (rn/use-state false)]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/factory-reset-keycard)}]
     [quo/text
      {:style {:padding-horizontal 20
               :padding-top        8
               :padding-bottom     8}}
      (i18n/label :t/factory-reset-warning)]
     [quo/disclaimer
      {:checked?        checked?
       :container-style {:margin-horizontal 20}
       :on-change       #(set-checked (not checked?))}
      (i18n/label :t/key-pair-erased)]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/continue)
       :button-one-props {:disabled?           (not checked?)
                          :customization-color customization-color
                          :on-press            (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (connect-and-reset key-uid))}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press (fn []
                                      (rf/dispatch [:hide-bottom-sheet]))}}]]))
