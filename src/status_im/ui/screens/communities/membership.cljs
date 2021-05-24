(ns status-im.ui.screens.communities.membership
  (:require [quo.react-native :as rn]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.communities.core :as communities]
            [status-im.constants :as constants]))

(def options {constants/community-on-request-access
              {:title       :t/membership-approval
               :description :t/membership-approval-description}
              constants/community-invitation-only-access
              {:title       :t/membership-invite
               :description :t/membership-invite-description}
; disabled for now
;              constants/community-rule-ens-only
;              {:title       :t/membership-ens
;               :description :t/membership-ens-description}
              constants/community-no-membership-access
              {:title       :t/membership-free
               :description :t/membership-free-description}})

(defn option [{:keys [title description]} {:keys [selected on-select]}]
  [:<>
   [quo/list-item {:title     (i18n/label title)
                   :size      :small
                   :accessory :radio
                   :active    selected
                   :on-press  on-select}]
   [quo/list-footer
    (i18n/label description)]
   [quo/separator {:style {:margin-vertical 8}}]])

(defn membership []
  (let [{:keys [membership]} (<sub [:communities/create])]
    [:<>
     [rn/scroll-view {}
      (doall
       (for [[id o] options]
         ^{:key (str "option-" id)}
         [option o {:selected  (= id membership)
                    :on-select #(>evt [::communities/create-field :membership id])}]))]
     [toolbar/toolbar
      {:show-border? true
       :center       [quo/button {:type     :secondary
                                  :on-press #(>evt [:navigate-back])}
                      (i18n/label :t/done)]}]]))
