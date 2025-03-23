(ns status-im.contexts.keycard.change-pin.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.common.view :as common.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn change-pin-confirmation-sheet
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/change-pin-keycard)
    :shell? true}
   [rn/view
    [quo/text {:size :paragraph-2}
     (i18n/label :t/change-pin-keycard-message)]
    [quo/bottom-actions
     {:actions          :two-actions
      :container-style  {:margin-horizontal -20}
      :blur?            true
      :button-one-label (i18n/label :t/continue)
      :button-one-props {:on-press #(rf/dispatch [:keycard/change-pin.enter-current-pin])}
      :button-two-label (i18n/label :t/cancel)
      :button-two-props {:on-press events-helper/hide-bottom-sheet
                         :type     :grey}}]]])

(defn ready-to-change-pin
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/ready-to-change-pin)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-change-pin)}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/scan-keycard)
     :button-one-props {:on-press #(rf/dispatch
                                    [:keycard/change-pin.verify-current-pin-and-continue])}}]])

(defn pin-change-success
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-pin-changed)
     :description      :text
     :description-text (i18n/label :t/keycard-pin-changed-description)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-change-pin-positive)}]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/done)
     :button-one-props {:on-press events-helper/navigate-back}}]])

(defn pin-change-failed
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-pin-change-failed)
     :description      :text
     :description-text (i18n/label :t/keycard-pin-change-failed-description)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-change-pin-negative)}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/try-again)
     :button-one-props {:on-press (fn []
                                    (rf/dispatch [:navigate-back])
                                    (rf/dispatch [:open-modal :screen/keycard.ready-to-change-pin]))}}]])
