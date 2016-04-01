(ns syng-im.components.chats.new-group
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view toolbar-android android? text-input]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-group-contact :refer [new-group-contact]]
            [reagent.core :as r]))

(defn new-group [{:keys [navigator]}]
  (let [contacts   (subscribe [:all-contacts])
        group-name (atom nil)]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:logo             res/logo-icon
                             :title            "New Group Chat"
                             :titleColor       "#4A5258"
                             :style            {:backgroundColor "white"
                                                :height          56
                                                :elevation       2}
                             :actions          [{:title "Create"
                                                 :icon  res/v
                                                 :show  "always"}]
                             :onActionSelected (fn [position]
                                                 (dispatch [:create-new-group navigator]))}])
         [text-input {:underlineColorAndroid "#9CBFC0"
                      :style                 {:marginLeft  5
                                              :marginRight 5
                                              :fontSize    14
                                              :fontFamily  "Avenir-Roman"
                                              :color       "#9CBFC0"}
                      :autoFocus             true
                      :placeholder           "Group Name"
                      :value                 @group-name
                      :onChangeText          (fn [new-text]
                                               (reset! group-name new-text)
                                               (r/flush))
                      :onSubmitEditing       (fn [e]
                                               ;(dispatch [:send-chat-msg @chat-id @text])
                                               (reset! group-name nil))}]
         [list-view {:dataSource contacts-ds
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [new-group-contact (js->clj row :keywordize-keys true) navigator]))
                     :style      {:backgroundColor "white"}}]]))))
