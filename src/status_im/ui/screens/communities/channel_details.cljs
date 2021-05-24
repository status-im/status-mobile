(ns status-im.ui.screens.communities.channel-details
  (:require [quo.core :as quo]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.i18n.i18n :as i18n]
            [clojure.string :as string]
            [status-im.communities.core :as communities]))

(defn view []
  (let [{:keys [chat-id]} (<sub [:get-screen-params])
        current-chat (<sub [:chat-by-id chat-id])
        {:keys [chat-name color description community-id]} current-chat
        {:keys [admin]} (<sub [:communities/community community-id])]
    [:<>
     [quo/animated-header {:left-accessories  [{:icon                :main-icons/arrow-left
                                                :accessibility-label :back-button
                                                :on-press            #(>evt [:navigate-back])}]
                           :right-accessories (when admin [{:icon                :edit
                                                            :accessibility-label :invite-button
                                                            :on-press            #(>evt [::communities/edit-channel-pressed
                                                                                         community-id
                                                                                         chat-name
                                                                                         description
                                                                                         color])}])
                           :extended-header   (profile-header/extended-header
                                               {:title    chat-name
                                                :color    color
                                                :subtitle (i18n/label :t/public-channel)})
                           :use-insets        true}
      (when-not (string/blank? description)
        [:<>
         [quo/list-footer {:color :main}
          description]
         [quo/separator {:style {:margin-vertical 8}}]])]]))