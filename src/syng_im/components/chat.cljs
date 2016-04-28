(ns syng-im.components.chat
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [reagent.core :as r]
            [syng-im.components.chat.chat-message :refer [chat-message]]
            [syng-im.components.chat.chat-message-new :refer [chat-message-new]]))


(defn contacts-by-identity [contacts]
  (->> contacts
       (map (fn [{:keys [identity] :as contact}]
              [identity contact]))
       (into {})))

(defn add-msg-color [{:keys [from] :as msg} contact-by-identity]
  (if (= "system" from)
    (assoc msg :text-color "#4A5258"
               :background-color "#D3EEEF")
    (let [{:keys [text-color background-color]} (get contact-by-identity from)]
      (assoc msg :text-color text-color
                 :background-color background-color))))

(defn chat [{:keys [navigator]}]
  (let [messages (subscribe [:get-chat-messages])
        chat     (subscribe [:get-current-chat])]
    (fn []
      (let [msgs                @messages
            ;_                 (log/debug "messages=" msgs)
            datasource          (to-realm-datasource msgs)
            contacts            (:contacts @chat)
            contact-by-identity (contacts-by-identity contacts)]
        [view {:style {:flex            1
                       :backgroundColor "#eef2f5"}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:logo             res/logo-icon
                             :title            (or (@chat :name)
                                                   "Chat name")
                             :titleColor       "#4A5258"
                             :subtitle         "Last seen just now"
                             :subtitleColor    "#AAB2B2"
                             :navIcon          res/nav-back-icon
                             :style            {:backgroundColor "white"
                                                :height          56
                                                :elevation       2}
                             :actions          (when (and (:group-chat @chat)
                                                          (:is-active @chat))
                                                 [{:title        "Add Contact to chat"
                                                   :icon         res/add-icon
                                                   :showWithText true}
                                                  {:title        "Remove Contact from chat"
                                                   :icon         res/trash-icon
                                                   :showWithText true}
                                                  {:title        "Leave Chat"
                                                   :icon         res/leave-icon
                                                   :showWithText true}])
                             :onActionSelected (fn [position]
                                                 (case position
                                                   0 (dispatch [:show-add-participants navigator])
                                                   1 (dispatch [:show-remove-participants navigator])
                                                   2 (dispatch [:leave-group-chat navigator])))
                             :onIconClicked    (fn []
                                                 (nav-pop navigator))}])
         [list-view {:dataSource            datasource
                     :renderScrollComponent (fn [props]
                                              (invertible-scroll-view nil))
                     :renderRow             (fn [row section-id row-id]
                                              (let [msg (-> (js->clj row :keywordize-keys true)
                                                            (add-msg-color contact-by-identity))]
                                                (r/as-element [chat-message msg])))
                     :style                 {:backgroundColor "white"}}]
         (when (:is-active @chat)
           [chat-message-new])]))))
