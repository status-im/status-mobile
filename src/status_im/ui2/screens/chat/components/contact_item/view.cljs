(ns status-im.ui2.screens.chat.components.contact-item.view
  (:require [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.foundations.typography :as typography]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo.react-native :as rn]
            [status-im.utils.utils :refer [get-shortened-address]]
            [quo.platform :as platform]
            [status-im.ui2.screens.chat.components.message-home-item.style :as style]))



(defn open-chat [chat-id]
  (>evt [:dismiss-keyboard])
  (if platform/android?
    (>evt [:chat.ui/navigate-to-chat-nav2 chat-id])
    (>evt [:chat.ui/navigate-to-chat chat-id]))
  (>evt [:search/home-filter-changed nil])
  (>evt [:accept-all-activity-center-notifications-from-chat chat-id]))

(defn contact-item [item]
  (let [{:keys [public-key]} item
        display-name (first (<sub [:contacts/contact-two-names-by-identity public-key]))
        photo-path   (if-not (empty? (:images item)) (<sub [:chats/photo-path public-key]) nil)]
    [rn/touchable-opacity (merge {:style   (style/container)
                                  :on-press #(open-chat public-key)})
     [user-avatar/user-avatar {:full-name         display-name
                               :profile-picture   photo-path
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :ring?             false}]
     [rn/view {:style {:margin-left 8}}
      [rn/view {:style {:flex-direction :row}}
       [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold
                               {:color (colors/theme-colors colors/neutral-100 colors/white)})}
        display-name]
       (if (:ens-verified item)
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [icons/icon :main-icons2/verified {:size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when (:added? item)
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [icons/icon :main-icons2/contact {:size 12 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))]
      [rn/text {:style (merge typography/paragraph-2 typography/font-regular
                              {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})}
       (get-shortened-address public-key)]]
     [rn/touchable-opacity {:style          {:position :absolute
                                             :right    20}
                            :active-opacity 1} ; TODO: on-long-press to be added when contact bottom sheet is implemented
      [icons/icon :main-icons2/options {:size 20 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]]))

