(ns status-im.ui.screens.communities.membership
  (:require [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.communities.core :as communities]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [utils.re-frame :as rf]))

(def options
  {constants/community-on-request-access
   {:title       :t/membership-approval
    :description :t/membership-approval-description}
   constants/community-no-membership-access
   {:title       :t/membership-free
    :description :t/membership-free-description}})

(defn option
  [{:keys [title description]} {:keys [selected on-select]}]
  [:<>
   [quo/list-item
    {:title     (i18n/label title)
     :size      :small
     :accessory :radio
     :active    selected
     :on-press  on-select}]
   [quo/list-footer
    (i18n/label description)]
   [quo/separator {:style {:margin-vertical 8}}]])

(defn membership-view
  []
  (let [{:keys [membership]} (rf/sub [:communities/create])]
    [:<>
     [rn/scroll-view {}
      (doall
       (for [[id o] options]
         ^{:key (str "option-" id)}
         [option o
          {:selected  (= id membership)
           :on-select #(rf/dispatch [::communities/create-field :membership id])}]))]
     [toolbar/toolbar
      {:show-border? true
       :center       [quo/button
                      {:type     :secondary
                       :on-press #(rf/dispatch [:navigate-back])}
                      (i18n/label :t/done)]}]]))
