(ns status-im2.common.contact-list-item.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.contexts.chat.home.chat-list-item.style :as style]
            [utils.address :as utils.address]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo.react :as react]))

(defn open-chat
  [chat-id]
  (let [view-id (rf/sub [:view-id])]
    (when (= view-id :shell-stack)
      (rf/dispatch [:dismiss-keyboard])
      (rf/dispatch [:chat.ui/show-profile chat-id])
      (rf/dispatch [:search/home-filter-changed nil]))))

(defn action-icon
  [{:keys [public-key] :as item} {:keys [icon start-a-new-chat? group] :as extra-data}
   user-selected? on-toggle]
  (let [{:keys [contacts admins]} group
        member?                   (contains? contacts public-key)
        current-pk                (rf/sub [:multiaccount/public-key])
        admin?                    (get admins current-pk)
        checked?                  (reagent/atom (if start-a-new-chat?
                                                  user-selected?
                                                  member?))
        on-check                  (fn [selected]
                                    (if start-a-new-chat?
                                      (on-toggle true @checked? public-key)
                                      (if-not member?
                                        (if selected
                                          (rf/dispatch [:select-participant public-key true])
                                          (rf/dispatch [:deselect-participant public-key true]))
                                        (if selected
                                          (rf/dispatch [:undo-deselect-member public-key true])
                                          (rf/dispatch [:deselect-member public-key true])))))]
    [:f>
     (fn []
       [rn/touchable-opacity
        {:on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                  {:content (fn [] [actions/actions item extra-data])}])
         :style    {:position :absolute
                    :right    20}}
        (if (= icon :options)
          [quo/icon :i/options
<<<<<<< HEAD
           {:size  20
            :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
=======
           {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
>>>>>>> 17d3ad846 (Formatting)
          (react/use-memo
           (fn []
             [quo/checkbox
              {:default-checked?    @checked?
               :accessibility-label :contact-toggle-check
               :disabled?           (and member? (not admin?))
<<<<<<< HEAD
               :on-change           on-check}])
=======
               :on-change           (fn [selected]
                                      (if start-a-new-chat?
                                        (on-toggle true @checked? public-key)
                                        (if-not member?
                                          (if selected
                                            (rf/dispatch [:select-participant public-key true])
                                            (rf/dispatch [:deselect-participant public-key true]))
                                          (if selected
                                            (rf/dispatch [:undo-deselect-member public-key true])
                                            (rf/dispatch [:deselect-member public-key true])))))}])
>>>>>>> 17d3ad846 (Formatting)
           [checked?]))])]))

(defn contact-list-item
  [item _ _ {:keys [start-a-new-chat? on-toggle] :as extra-data}]
  (let [{:keys [public-key ens-verified added? images]} item
        display-name                                    (first (rf/sub
                                                                [:contacts/contact-two-names-by-identity
                                                                 public-key]))
        photo-path                                      (when (seq images)
                                                          (rf/sub [:chats/photo-path public-key]))
        current-pk                                      (rf/sub [:multiaccount/public-key])
        online?                                         (rf/sub [:visibility-status-updates/online?
                                                                 public-key])
        user-selected?                                  (rf/sub [:is-contact-selected? public-key])]
    [rn/touchable-opacity
     (merge
      {:style               (style/container)
       :accessibility-label :contact
       :active-opacity      1
       :on-press            #(if start-a-new-chat?
                               (on-toggle true user-selected? public-key)
                               (open-chat public-key))
       :on-long-press       #(rf/dispatch [:bottom-sheet/show-sheet
                                           {:content (fn [] [actions/actions item extra-data])}])})
     [quo/user-avatar
      {:full-name         display-name
       :profile-picture   photo-path
       :status-indicator? true
       :online?           online?
       :size              :small
       :ring?             false}]
     [rn/view {:style {:margin-left 8}}
      [rn/view {:style {:flex-direction :row}}
       [quo/text {:weight :semi-bold} display-name]
       (if ens-verified
         [rn/view {:style {:margin-left 5 :margin-top 4}}
          [quo/icon :i/verified
           {:no-color true :size 12 :color (colors/theme-colors colors/success-50 colors/success-60)}]]
         (when added?
           [rn/view {:style {:margin-left 5 :margin-top 4}}
            [quo/icon :i/contact
             {:no-color true
              :size     12
              :color    (colors/theme-colors colors/primary-50 colors/primary-60)}]]))]
      [quo/text
       {:size  :paragraph-1
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (utils.address/get-shortened-address public-key)]]
     (when-not (= current-pk public-key)
       [action-icon item extra-data user-selected? on-toggle])]))
