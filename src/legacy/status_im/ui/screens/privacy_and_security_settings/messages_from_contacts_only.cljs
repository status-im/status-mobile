(ns legacy.status-im.ui.screens.privacy-and-security-settings.messages-from-contacts-only
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]
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
     [list.item/list-item
      {:active    (not messages-from-contacts-only)
       :accessory :radio
       :title     (i18n/label :t/anyone)
       :on-press  #(re-frame/dispatch [::messages-from-contacts-only-switched false])}]
     [list.item/list-item
      {:active             messages-from-contacts-only
       :accessory          :radio
       :title              (i18n/label :t/contacts)
       :subtitle           (i18n/label :t/messages-from-contacts-only-subtitle)
       :subtitle-max-lines 4
       :on-press           #(re-frame/dispatch [::messages-from-contacts-only-switched true])}]]))
