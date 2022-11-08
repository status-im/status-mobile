(ns status-im.ui2.screens.chat.components.contact-bottom-sheet.view
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react-native :as rn]
            [quo2.components.drawers.action-drawers :refer [divider]]
            [quo2.components.list-items.menu-item :as quo2.menu-item]
            [re-frame.core :as rf]))

(defn hide-sheet-and-dispatch [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn contact-bottom-sheet [{:keys [public-key] :as contact}]
  [rn/view
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/view-profile)
     :accessibility-label "view-profile"
     :icon                :profile
     :on-press            #(hide-sheet-and-dispatch [:chat.ui/show-profile public-key])}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/remove-from-contacts)
     :accessibility-label "remove-from-contacts"
     :icon                :remove-user
     :on-press            #(hide-sheet-and-dispatch [:contact.ui/remove-contact-pressed contact])}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/rename)
     :accessibility-label "rename"
     :icon                :edit
     :on-press            #(println "TODO: to be implemented, requires design input")}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/show-qr)
     :accessibility-label "show-qr"
     :icon                :qr-code
     :on-press            #(println "TODO: to be implemented, requires design input")}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/share-profile)
     :accessibility-label "share-profile"
     :icon                :share
     :on-press            #(println "TODO: to be implemented, requires design input")}]
   [divider]
   [quo2.menu-item/menu-item
    {:type                :danger
     :title               (i18n/label :t/mark-untrustworthy)
     :accessibility-label "mark-untrustworthy"
     :icon                :alert
     :on-press            #(println "TODO: to be implemented, probably requires status-go impl. and design input")}]
   [quo2.menu-item/menu-item
    {:type                :danger
     :title               (i18n/label :t/block-user)
     :accessibility-label "block-user"
     :icon                :block
     :on-press            #(println "TODO: to be implemented, requires design input")}]
   ])
