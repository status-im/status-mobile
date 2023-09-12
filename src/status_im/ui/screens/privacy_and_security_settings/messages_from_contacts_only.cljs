(ns status-im.ui.screens.privacy-and-security-settings.messages-from-contacts-only
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]))

(rf/defn handle-messages-from-contacts-only-switched
  {:events [::messages-from-contacts-only-switched]}
  [cofx value]
  (multiaccounts.update/multiaccount-update cofx
                                            :messages-from-contacts-only
                                            value
                                            {}))

(views/defview messages-from-contacts-only-view
  []
  (views/letsubs [{:keys [messages-from-contacts-only]} [:profile/profile]]
    [react/view {:margin-top 8}
     [quo/list-item
      {:active    (not messages-from-contacts-only)
       :accessory :radio
       :title     (i18n/label :t/anyone)
       :on-press  #(re-frame/dispatch [::messages-from-contacts-only-switched false])}]
     [quo/list-item
      {:active             messages-from-contacts-only
       :accessory          :radio
       :title              (i18n/label :t/contacts)
       :subtitle           (i18n/label :t/messages-from-contacts-only-subtitle)
       :subtitle-max-lines 4
       :on-press           #(re-frame/dispatch [::messages-from-contacts-only-switched true])}]]))
