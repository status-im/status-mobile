(ns status-im.ui.screens.privacy-and-security-settings.messages-from-contacts-only
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]))

(fx/defn handle-messages-from-contacts-only-switched
  {:events [::messages-from-contacts-only-switched]}
  [cofx value]
  (multiaccounts.update/multiaccount-update cofx
                                            :messages-from-contacts-only value {}))

(views/defview messages-from-contacts-only []
  (views/letsubs [{:keys [messages-from-contacts-only]} [:multiaccount]]
    [react/view {:margin-top 8}
     [quo/list-item
      {:active    (not messages-from-contacts-only)
       :accessory :radio
       :title     (i18n/label :t/anyone)
       :on-press  #(re-frame/dispatch [::messages-from-contacts-only-switched false])}]
     [quo/list-item
      {:active messages-from-contacts-only
       :accessory :radio
       :title (i18n/label :t/contacts)
       :subtitle (i18n/label :t/messages-from-contacts-only-subtitle)
       :subtitle-max-lines 4
       :on-press #(re-frame/dispatch [::messages-from-contacts-only-switched true])}]]))
