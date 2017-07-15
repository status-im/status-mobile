(ns status-im.chat.views.api.choose-contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as str]
            [status-im.chat.constants :as const]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.utils.listview :as lw]))

(defn- select-contact [arg-index bot-db-key {:keys [name] :as contact}]
  (let [contact (select-keys contact [:address :whisper-identity :name :photo-path :dapp?])
        name    (str/replace name (re-pattern const/arg-wrapping-char) "")]
    (dispatch [:set-command-argument [arg-index name true]])
    (dispatch [:set-in-bot-db {:path  [:public (keyword bot-db-key)]
                               :value contact}])
    (dispatch [:select-next-argument])))

(defn render-row [arg-index bot-db-key]
  (fn [contact _ _]
    (list-item
      ^{:key contact}
      [contact-view {:contact  contact
                     :on-press #(select-contact arg-index bot-db-key contact)}])))

(defview choose-contact-view [{title      :title
                               arg-index  :index
                               bot-db-key :bot-db-key}]
  [contacts [:contacts-filtered :people-in-current-chat]]
  [view {:flex 1}
   [text {:style {:font-size      14
                  :color          "rgb(147, 155, 161)"
                  :padding-top    12
                  :padding-left   16
                  :padding-right  16
                  :padding-bottom 12}}
    title]
   [list-view {:dataSource                (lw/to-datasource contacts)
               :enableEmptySections       true
               :renderRow                 (render-row arg-index bot-db-key)
               :bounces                   false
               :keyboardShouldPersistTaps :always
               :renderSeparator           renderers/list-separator-renderer}]])