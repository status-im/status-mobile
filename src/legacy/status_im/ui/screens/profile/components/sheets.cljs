(ns legacy.status-im.ui.screens.profile.components.sheets
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.profile.components.styles :as styles]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview block-contact
  []
  (views/letsubs [{:keys [public-key]} [:popover/popover]
                  in-progress?         (reagent/atom false)]
    [react/view {:style {:padding-top 16 :padding-horizontal 24 :padding-bottom 8}}
     [react/text {:style styles/sheet-text}
      (i18n/label :t/block-contact-details)]
     [react/view {:align-items :center :margin-top 16}
      [quo/button
       {:theme               :negative
        :disabled            @in-progress?
        :loading             @in-progress?
        :accessibility-label :block-contact-confirm
        :on-press            #(do (reset! in-progress? true)
                                  (re-frame/dispatch [:contact.ui/block-contact-confirmed public-key]))}
       (i18n/label :t/block)]
      [react/view {:height 8}]
      [quo/button
       {:type     :secondary
        :disabled @in-progress?
        :on-press #(re-frame/dispatch [:hide-popover])}
       (i18n/label :t/close)]]]))
