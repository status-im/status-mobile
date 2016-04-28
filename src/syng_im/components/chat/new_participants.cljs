(ns syng-im.components.chat.new-participants
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view toolbar-android android? text-input]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-participant-contact :refer [new-participant-contact]]
            [reagent.core :as r]
            [syng-im.navigation :refer [nav-pop]]))

(defn new-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:all-new-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:logo             res/logo-icon
                             :title            "Add Participants"
                             :titleColor       "#4A5258"
                             :style            {:backgroundColor "white"
                                                :height          56
                                                :elevation       2}
                             :actions          [{:title "Add"
                                                 :icon  res/v
                                                 :show  "always"}]
                             :onActionSelected (fn [position]
                                                 (dispatch [:add-new-participants navigator]))
                             :navIcon          res/nav-back-icon
                             :onIconClicked    (fn []
                                                 (nav-pop navigator))}])
         [list-view {:dataSource contacts-ds
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [new-participant-contact (js->clj row :keywordize-keys true) navigator]))
                     :style      {:backgroundColor "white"}}]]))))
