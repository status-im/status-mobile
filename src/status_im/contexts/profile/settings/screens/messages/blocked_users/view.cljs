(ns status-im.contexts.profile.settings.screens.messages.blocked-users.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.contact-list-item.view :as contact-list-item]
            [status-im.contexts.profile.contact.unblock-contact.view :as unblock-contact]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn list-item
  [user _ _ _]
  [contact-list-item/contact-list-item
   {:accessory {:type  :custom
                :child [quo/button
                        {:on-press            #(rf/dispatch [:show-bottom-sheet
                                                             {:theme :dark
                                                              :content
                                                              (fn [] [unblock-contact/view user])}])
                         :type                :outline
                         :size                24
                         :background          :blur
                         :customization-color :blue}
                        (i18n/label :t/unblock)]}}
   user])

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [contacts (rf/sub [:contacts/blocked])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top {:title (i18n/label :t/blocked-users)}]
     [rn/flat-list
      {:data                contacts
       :key-fn              :key
       :render-fn           list-item
       :accessibility-label :contacts-list}]]))
