(ns status-im.ui.screens.communities.invite
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [re-frame.core :as re-frame]
            [status-im.communities.core :as communities]
            [status-im.ui.components.topbar :as topbar]))

(defn invite []
  (let [user-pk (reagent/atom "")]
    (fn []
      [:<>
       [topbar/topbar {:title (i18n/label :t/community-invite-title)}]
       [rn/scroll-view {:style                   {:flex 1}
                        :content-container-style {:padding-vertical 16}}
        [rn/view {:style {:padding-horizontal 16}}
         [quo/text-input
          {:label          (i18n/label :t/enter-user-pk)
           :placeholder    (i18n/label :t/enter-user-pk)
           :on-change-text #(reset! user-pk %)
           :auto-focus     true}]]]
       [toolbar/toolbar
        {:show-border? true
         :center
         [quo/button {:disabled (= "" user-pk)
                      :type     :secondary
                      :on-press #(re-frame/dispatch [::communities/invite-people-confirmation-pressed @user-pk])}
          (i18n/label :t/invite)]}]])))
