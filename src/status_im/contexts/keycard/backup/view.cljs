(ns status-im.contexts.keycard.backup.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.common.view :as common.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn ready-to-add
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/ready-add-keypair-keycard)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-migration)}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/scan-keycard)
     :button-one-props {:on-press #(rf/dispatch [:keycard/backup.ready-to-add-connect])}}]])

(defn success-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/backup-keycard-created)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-backup-positive)}]
   [rn/view {:style {:padding-horizontal 20 :padding-vertical 12}}
    [quo/button {:on-press events-helper/navigate-back}
     (i18n/label :t/done)]]])

(defn not-empty-view
  []
  (let [{:keys [on-press]} (rf/sub [:get-screen-params])]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            (i18n/label :t/keycard-not-empty)
       :description      :text
       :description-text (i18n/label :t/scan-empty-keycard)}]
     [rn/image
      {:resize-mode :contain
       :style       {:flex 1 :width (:width (rn/get-window))}
       :source      (resources/get-image :keycard-not-empty)}]
     [common.view/tips]
     [rn/view {:style {:padding-horizontal 20}}
      [quo/button {:on-press on-press}
       (i18n/label :t/try-again)]]]))

(defn scan-empty
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/scan-empty-keycard)
     :description      :text
     :description-text (i18n/label :t/backup-empty-keycard-only)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :check-your-keycard)}]
   [common.view/tips]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/ready-to-scan)
     :button-one-props {:on-press #(rf/dispatch [:keycard/backup.scan-empty-card])}}]])

(defn sheet
  [{:keys [on-continue]}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/create-backup-keycard)}]
     [quo/text
      {:style {:padding-horizontal 20
               :padding-vertical   8}}
      (i18n/label :t/backup-keycard-instructions)]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/continue)
       :button-one-props {:customization-color customization-color
                          :on-press            (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (on-continue))}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press #(rf/dispatch [:hide-bottom-sheet])}}]]))
