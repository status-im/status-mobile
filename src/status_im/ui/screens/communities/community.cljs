(ns status-im.ui.screens.communities.community
  (:require [status-im.ui.components.topbar :as topbar]
            [quo.react-native :as rn]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.communities.core :as communities]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.home.views.inner-item :as inner-item]
            [status-im.constants :as constants]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.utils.core :as utils]))

(defn toolbar-content [id display-name color members]
  [rn/view {:style {:flex           1
                    :align-items    :center
                    :flex-direction :row}}
   [rn/view {:padding-right 10}
    (if (= id constants/status-community-id)
      [rn/image {:source (resources/get-image :status-logo)
                 :style  {:width  40
                          :height 40}}]
      [chat-icon.screen/chat-icon-view-toolbar
       id
       true
       display-name
       (or color (rand-nth colors/chat-colors))])]
   [rn/view {:style {:flex 1 :justify-content :center}}
    [quo/text {:number-of-lines     1
               :accessibility-label :community-name-text}
     display-name]
    [quo/text {:number-of-lines 1
               :size            :small
               :color           :secondary}
     (i18n/label-pluralize members :t/community-members {:count members})]]])

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn community-actions [{:keys [id admin description]}]
  [:<>
   [quo/list-item
    {:title    (get-in description [:identity :display-name])
     :on-press #(hide-sheet-and-dispatch [:navigate-to :community-management {:community-id id}])
     :chevron  true
     :icon     (if (= id constants/status-community-id)
                 [rn/image {:source (resources/get-image :status-logo)
                            :style  {:width  40
                                     :height 40}}]
                 [chat-icon.screen/chat-icon-view-chat-sheet
                  (get-in description [:identity :display-name])
                  true
                  (get-in description [:identity :display-name])
                  (get-in description [:identity :color] (rand-nth colors/chat-colors))])}]
   (when (and config/communities-management-enabled? admin)
     [:<>
      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/export-key)
        :accessibility-label :community-export-key
        :icon                :main-icons/objects
        :on-press            #(hide-sheet-and-dispatch [::communities/export-pressed id])}]
      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/create-channel)
        :accessibility-label :community-create-channel
        :icon                :main-icons/channel
        :on-press            #(hide-sheet-and-dispatch [::communities/create-channel-pressed id])}]
      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/invite-people)
        :icon                :main-icons/share
        :accessibility-label :community-invite-people
        :on-press            #(>evt [::communities/invite-people-pressed id])}]])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/leave-community)
     :accessibility-label :leave
     :icon                :main-icons/arrow-left
     :on-press            #(do
                             (>evt [:bottom-sheet/hide])
                             (>evt [:navigate-to :home])
                             (>evt [::communities/leave id]))}]])

(defn welcome-blank-page []
  [rn/view {:style {:padding 16 :flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [quo/text {:align :center
              :color :secondary}
    (i18n/label :t/welcome-blank-message)]])

(defn community-chat-item [home-item]
  [inner-item/home-list-item home-item])

(defn community-chat-list [chats]
  (if (empty? chats)
    [welcome-blank-page]
    [list/flat-list
     {:key-fn                       :chat-id
      :content-container-style      {:padding-vertical 8}
      :keyboard-should-persist-taps :always
      :data                         chats
      :render-fn                    community-chat-item
      :footer                       [rn/view {:height 68}]}]))

(defn community-channel-list [id]
  (let [chats (<sub [:chats/by-community-id id])
        chats (cond->> chats
                (= id constants/status-community-id)
                (map #(assoc % :color colors/blue)))]
    [community-chat-list chats]))

(defn channel-preview-item [{:keys [id color identity]}]
  (let [color (if (= id constants/status-community-id)
                colors/blue
                color)]
    [quo/list-item
     {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                  id true (:display-name identity) color false false]
      :title                     [rn/view {:flex-direction :row
                                           :flex           1
                                           :padding-right  16
                                           :align-items    :center}
                                  [icons/icon :main-icons/tiny-group
                                   {:color           colors/black
                                    :width           15
                                    :height          15
                                    :container-style {:width        15
                                                      :height       15
                                                      :margin-right 2}}]
                                  [quo/text {:weight              :medium
                                             :accessibility-label :chat-name-text
                                             :ellipsize-mode      :tail
                                             :number-of-lines     1}
                                   (utils/truncate-str (:display-name identity) 30)]]
      :title-accessibility-label :chat-name-text
      :subtitle                  (:description identity)}]))

(defn community-channel-preview-list [_ description]
  (let [chats (reduce-kv
               (fn [acc k v]
                 (conj acc (assoc v :id (name k))))
               []
               (get-in description [:chats]))]
    [list/flat-list
     {:key-fn                       :id
      :content-container-style      {:padding-vertical 8}
      :keyboard-should-persist-taps :always
      :data                         chats
      :render-fn                    channel-preview-item}]))

(defn community [route]
  (let [{:keys [community-id]} (get-in route [:route :params])
        {:keys [id description joined admin]
         :as   community}      (<sub [:communities/community community-id])]
    [rn/view {:style {:flex 1}}

     [topbar/topbar
      {:content           [toolbar-content id
                           (get-in description [:identity :display-name])
                           (get-in description [:identity :color])
                           (count (get description :members))]
       :right-accessories (when (or admin joined)
                            [{:icon                :main-icons/more
                              :accessibility-label :community-menu-button
                              :on-press
                              #(>evt [:bottom-sheet/show-sheet
                                      {:content (fn []
                                                  [community-actions community])
                                       :height  256}])}])}]
     (if joined
       [community-channel-list id]
       [community-channel-preview-list id description])
     (when-not joined
       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button {:on-press #(>evt [::communities/join id])
                                    :type     :secondary}
                        (i18n/label :t/join)]}])]))
