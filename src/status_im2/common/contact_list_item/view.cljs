(ns status-im2.common.contact-list-item.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im2.common.contact-list-item.style :as style]
            [utils.address :as utils.address]
            [status-im2.common.home.actions.view :as actions]))

(defn open-chat [chat-id]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
  (rf/dispatch [:search/home-filter-changed nil])
  (rf/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))

(defn contact-item [item]
  (let [{:keys [public-key ens-verified added? images]} item
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
        photo-path (when (seq images) (rf/sub [:chats/photo-path public-key]))
        current-pk (rf/sub [:multiaccount/public-key])]
    [rn/touchable-opacity (merge {:style         (style/container)
                                  :on-press      #(open-chat public-key)
                                  :on-long-press #(when-not (= current-pk public-key)
                                                    (rf/dispatch [:bottom-sheet/show-sheet
                                                                  {:content (fn [] [actions/actions item])}]))})
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
      [quo/text {:style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (utils.address/get-shortened-address public-key)]]
     (when-not (= current-pk public-key)
       [rn/touchable-opacity {:style          {:position :absolute
                                               :right    20}
                              :active-opacity 1
                              :on-press       #(rf/dispatch [:bottom-sheet/show-sheet
                                                             {:content (fn [] [actions/actions item])}])}
        [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]])]))
