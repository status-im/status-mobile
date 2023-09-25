(ns status-im2.contexts.quo-preview.drawers.documentation-drawers
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key  :show-button?
    :type :boolean}
   {:key  :button-label
    :type :text}
   {:key  :shell?
    :type :boolean}])

(defn documentation-content
  [theme]
  [quo/text {:style {:color (colors/theme-colors colors/neutral-100 colors/white theme)}}
   "Group chats are conversations of more than two people. To invite someone to a group chat, you need to have them on your Status contact list."])

(defn documentation-content-full
  [theme]
  (let [text-color (colors/theme-colors colors/neutral-100 colors/white theme)
        text-style {:color text-color :margin-bottom 10}]
    [rn/view
     [quo/text {:style text-style}
      "Group chats are conversations of more than two people. To invite someone to a group chat, you need to have them on your Status contact list."]
     [quo/text {:style text-style}
      "Group chats are different to communities, as they're meant to unite smaller groups of people or be centred around specific topics. For more information about group chats, see Understand group chats."]
     [quo/text {:size :paragraph-1 :weight :semi-bold :style {:margin-top 16 :color text-color}}
      "What to expect"]
     [quo/text {:style text-style}
      "You can invite up to 20 members to your group chat. If you need more, consider creating a community."]
     [quo/text {:style text-style}
      "Once you create your group chat, you can customize it and add members, as well as remove them."]
     [quo/text {:style text-style}
      "Group chats are always end-to-end encrypted with secure cryptographic keys. Only the group chat members will have access to the messages in it. Status doesn't have the keys and can't access any messages by design."]
     [quo/text {:size :paragraph-1 :weight :semi-bold :style {:margin-top 16 :color text-color}}
      "What to expect"]
     [quo/text {:style text-style}
      "You can invite up to 20 members to your group chat. If you need more, consider creating a community."]
     [quo/text {:style text-style}
      "Once you create your group chat, you can customize it and add members, as well as remove them."]
     [quo/text {:style text-style}
      "Group chats are always end-to-end encrypted with secure cryptographic keys. Only the group chat members will have access to the messages in it. Status doesn't have the keys and can't access any messages by design."]
     [quo/text {:size :paragraph-1 :weight :semi-bold :style {:margin-top 16 :color text-color}}
      "What to expect"]
     [quo/text {:style text-style}
      "You can invite up to 20 members to your group chat. If you need more, consider creating a community."]
     [quo/text {:style text-style}
      "Once you create your group chat, you can customize it and add members, as well as remove them."]
     [quo/text {:style text-style}
      "Group chats are always end-to-end encrypted with secure cryptographic keys. Only the group chat members will have access to the messages in it. Status doesn't have the keys and can't access any messages by design."]
     [quo/text {:size :paragraph-1 :weight :semi-bold :style {:margin-top 16 :color text-color}}
      "What to expect"]
     [quo/text {:style text-style}
      "You can invite up to 20 members to your group chat. If you need more, consider creating a community."]
     [quo/text {:style text-style}
      "Once you create your group chat, you can customize it and add members, as well as remove them."]
     [quo/text {:style text-style}
      "Group chats are always end-to-end encrypted with secure cryptographic keys. Only the group chat members will have access to the messages in it. Status doesn't have the keys and can't access any messages by design."]
     [quo/text {:size :paragraph-1 :weight :semi-bold :style {:margin-top 16 :color text-color}}
      "What to expect"]
     [quo/text {:style text-style}
      "You can invite up to 20 members to your group chat. If you need more, consider creating a community."]
     [quo/text {:style text-style}
      "Once you create your group chat, you can customize it and add members, as well as remove them."]
     [quo/text {:style text-style}
      "Group chats are always end-to-end encrypted with secure cryptographic keys. Only the group chat members will have access to the messages in it. Status doesn't have the keys and can't access any messages by design."]]))

(defn documentation-drawer
  [title show-button? button-label expanded? shell?]
  [quo/documentation-drawers
   {:title           title
    :show-button?    (and show-button? (not @expanded?))
    :button-label    button-label
    :button-icon     :info
    :shell?          shell?
    :on-press-button #(swap! expanded? not)}
   (if @expanded?
     [documentation-content-full (when shell? :dark)]
     [documentation-content (when shell? :dark)])])

(defn view
  []
  (let [state        (reagent/atom {:title        "Create a group chat"
                                    :button-label "Read more"})
        title        (reagent/cursor state [:title])
        show-button? (reagent/cursor state [:show-button?])
        button-label (reagent/cursor state [:button-label])
        shell?       (reagent/cursor state [:shell?])
        expanded?    (reagent/atom false)]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/button
        {:container-style {:margin-horizontal 40
                           :margin-bottom     20}
         :on-press        #(rf/dispatch [:show-bottom-sheet
                                         {:content     (constantly [documentation-drawer @title
                                                                    @show-button?
                                                                    @button-label expanded? @shell?])
                                          :expandable? @show-button?
                                          :shell?      @shell?
                                          :expanded?   @expanded?}])}
        "Open drawer"]
       [documentation-drawer @title @show-button? @button-label expanded?]])))
