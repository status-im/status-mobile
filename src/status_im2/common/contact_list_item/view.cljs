(ns status-im2.common.contact-list-item.view
  (:require [quo2.foundations.colors :as colors]
            [status-im2.contexts.chat.home.chat-list-item.style :as style]
            [utils.re-frame :as rf]
            [status-im2.common.home.actions.view :as actions]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [utils.address :as utils.address]))

(defn open-chat [chat-id]
  (let [view-id (rf/sub [:view-id])]
    (when (= view-id :shell-stack)
      (rf/dispatch [:dismiss-keyboard])
      (if platform/android?
        (rf/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
        (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))
      (rf/dispatch [:search/home-filter-changed nil])
      (rf/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))))

(defn action-icon [{:keys [public-key] :as item} {:keys [icon group added] :as extra-data}]
  (let [{:keys [contacts]} group
        member? (contains? contacts public-key)]
    [rn/touchable-opacity {:on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                                    {:content (fn [] [actions/actions item extra-data])}])
                           :style    {:position :absolute
                                      :right    20}}
     (if (= icon :options)
       [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
       [quo/checkbox {:default-checked? member?
                      :on-change        (fn [selected] (if selected
                                                         (swap! added conj public-key)
                                                         (reset! added (remove #(= % public-key) @added))))}])]))

(defn contact-list-item [item _ _ extra-data]
  (let [{:keys [public-key ens-verified added? images]} item
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
        photo-path   (when (seq images) (rf/sub [:chats/photo-path public-key]))
        current-pk   (rf/sub [:multiaccount/public-key])]
    [rn/touchable-opacity (merge {:style          (style/container)
                                  :active-opacity 1
                                  :on-press       #(open-chat public-key)
                                  :on-long-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                                                 {:content (fn [] [actions/actions item extra-data])}])})
     [quo/user-avatar {:full-name         display-name
                       :profile-picture   photo-path
                       :status-indicator? true
                       :online?           true
                       :size              :small
                       :ring?             false}]
     [rn/view {:style {:margin-left 8}}
      [rn/view {:style {:flex-direction :row}}
       [quo/text {:weight :semi-bold} display-name]
       (if ens-verified
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [quo/icon :i/verified {:no-color true :size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when added?
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [quo/icon :i/contact {:no-color true :size 12 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))]
      [quo/text {:size  :paragraph-1
                 :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (utils.address/get-shortened-address public-key)]]
     (when-not (= current-pk public-key)
       [action-icon item extra-data])]))

