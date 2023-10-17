(ns status-im.ui.screens.profile.components.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.ui.components.core :as quo]
    [status-im.ui.components.react :as react]
    [status-im.ui.screens.profile.components.styles :as styles]
    [utils.i18n :as i18n]))

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
