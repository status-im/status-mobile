(ns status-im.chat.views.api.choose-contact
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]))

(defn- render-contact [arg-index bot-db-key]
  (fn [contact]
    [contact-view {:contact  contact
                   :on-press #(re-frame/dispatch
                                [:set-contact-as-command-argument {:arg-index arg-index
                                                                   :bot-db-key bot-db-key
                                                                   :contact contact}])}]))


(defview choose-contact-view [{title      :title
                               arg-index  :index
                               bot-db-key :bot-db-key}]
  (letsubs [contacts [:people-in-current-chat]]
    [react/view
     [react/text {:style {:font-size      14
                          :color          "rgb(147, 155, 161)"
                          :padding-top    12
                          :padding-left   16
                          :padding-right  16
                          :padding-bottom 12}}
      title]
     [list/flat-list {:data                      contacts
                      :key-fn                    :address
                      :render-fn                 (render-contact arg-index bot-db-key)
                      :enableEmptySections       true
                      :keyboardShouldPersistTaps :always
                      :bounces                   false}]]))
