(ns status-im.ui2.screens.chat.components.contact-item.view
  (:require [quo2.foundations.typography :as typography]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo.react-native :as rn]
            [status-im.utils.utils :as utils]
            [quo.platform :as platform]
            [quo2.components.markdown.text :as text]
            [status-im.ui2.screens.chat.components.message-home-item.style :as style]
            [utils.re-frame :as rf]
            [status-im.ui2.screens.chat.actions :as actions]
            [quo2.components.selectors.selectors :as selectors]))

(defn open-chat [chat-id]
  (let [view-id (rf/sub [:view-id])]
    (when (= view-id :shell-stack)
      (rf/dispatch [:dismiss-keyboard])
      (if platform/android?
        (rf/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
        (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))
      (rf/dispatch [:search/home-filter-changed nil])
      (rf/dispatch [:accept-all-activity-center-notifications-from-chat chat-id]))))

;(defn action-icon [{:keys [public-key] :as item} {:keys [icon group] :as extra-data}]
;  (let [{:keys [contacts]} group
;        member? (contains? contacts public-key)]
;    [rn/touchable-opacity {:style          (merge {:position :absolute
;                                                   :right    20}
;                                                  (when (= icon :check)
;                                                    (if member?
;                                                      {:width           20
;                                                       :height          20
;                                                       :border-radius   6
;                                                       :justify-content :center
;                                                       :align-items     :center
;                                                       :background-color (colors/theme-colors colors/primary-50 colors/primary-60)}
;                                                      {:width           20
;                                                       :height          20
;                                                       :border-radius   6
;                                                       :border-width 1
;                                                       :border-color (colors/theme-colors colors/neutral-20 colors/neutral-80)
;                                                       :background-color (colors/theme-colors colors/white colors/neutral-80-opa-40)})))
;                           :active-opacity 1
;                           :on-press       (if (= icon :options)
;                                             #(rf/dispatch [:bottom-sheet/show-sheet
;                                                            {:content (fn [] [actions/actions item extra-data])}])
;                                             #(println "other"))}
;     (if (= icon :options)
;       [icons/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
;       [icons/icon :i/check-large {:size 12 :color colors/white}])]))

(defn action-icon [{:keys [public-key] :as item} {:keys [icon group added] :as extra-data}]
  (let [{:keys [contacts]} group
        member? (contains? contacts public-key)]
    (if (= icon :options)
      [icons/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
      [rn/view {:style {:position :absolute
                        :right    20}}
       [selectors/checkbox {:default-checked? member?
                            :on-change        (fn [selected] (if selected
                                                               (swap! added conj public-key)
                                                               (reset! added (filter #(-> % (not= public-key)) @added))))}]])))

(defn contact-item [item extra-data]
  (let [{:keys [public-key ens-verified added? images]} item
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
<<<<<<< HEAD
        photo-path   (when (seq images) (rf/sub [:chats/photo-path public-key]))
        current-pk   (rf/sub [:multiaccount/public-key])]
    [rn/touchable-opacity (merge {:style         (style/container)
                                  :on-press      #(open-chat public-key)
<<<<<<< HEAD
                                  :on-long-press #(when-not (= current-pk public-key)
                                                    (rf/dispatch [:bottom-sheet/show-sheet
                                                                  {:content (fn [] [actions/actions item])}]))})
=======
                                  :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                                                {:content (fn [] [actions/actions item extra-data])}])})
>>>>>>> 25441811e... feat: group details screen 2
=======
        photo-path   (when (seq images) (rf/sub [:chats/photo-path public-key]))]
    [rn/touchable-opacity (merge {:style          (style/container)
                                  :active-opacity 1
                                  :on-press       #(open-chat public-key)
                                  :on-long-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                                                 {:content (fn [] [actions/actions item extra-data])}])})
>>>>>>> faae21626... updates
     [user-avatar/user-avatar {:full-name         display-name
                               :profile-picture   photo-path
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :ring?             false}]
     [rn/view {:style {:margin-left 8}}
      [rn/view {:style {:flex-direction :row}}
       [text/text {:style (merge typography/paragraph-1 typography/font-semi-bold
                                 {:color (colors/theme-colors colors/neutral-100 colors/white)})}
        display-name]
       (if ens-verified
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [icons/icon :i/verified {:no-color true :size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when added?
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [icons/icon :i/contact {:no-color true :size 12 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]]))]
      [text/text {:size  :paragraph-1
                  :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (utils/get-shortened-address public-key)]]
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
     (when-not (= current-pk public-key)
       [rn/touchable-opacity {:style          {:position :absolute
                                               :right    20}
                              :active-opacity 1
                              :on-press       #(rf/dispatch [:bottom-sheet/show-sheet
                                                             {:content (fn [] [actions/actions item])}])}
        [icons/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]])]))
=======
     [rn/touchable-opacity {:style          {:position :absolute
                                             :right    20}
                            :active-opacity 1
                            :on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                                     {:content (fn [] [actions/actions item extra-data])}])}
      [icons/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]))
>>>>>>> 4b20ea02d... feat: group details screen
=======
     [rn/touchable-opacity {:style          (merge {:position :absolute
                                                    :right    20}
                                                   (when (= icon :check)
                                                     {:background-color (colors/theme-colors colors/primary-50 colors/primary-60)
                                                      :width 20
                                                      :height 20
                                                      :border-radius 6
                                                      :justify-content :center
                                                      :align-items :center}))
                            :active-opacity 1
                            :on-press       (if (= icon :options)
                                              #(rf/dispatch [:bottom-sheet/show-sheet
                                                             {:content (fn [] [actions/actions item extra-data])}])
                                              #(println "other"))}
      (if (= icon :options)
        [icons/icon :i/options {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
        [icons/icon :i/check-large {:size 12 :color colors/white}])]]))
>>>>>>> 6bc845a97... updates
=======
     [action-icon item extra-data]]))
>>>>>>> faae21626... updates

