(ns status-im2.contexts.communities.actions.addresses-for-permissions.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.communities.actions.addresses-for-permissions.style :as style]
            [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])]
    [rn/view {:style style/container}

     [quo/drawer-top
      {:type                 :context-tag
       :title                (i18n/label :t/addresses-for-permissions)
       :button-icon          :i/info
       :community-name       name
       :community-logo       (get-in images [:thumbnail :uri])
       :customization-color  color
       :on-button-press      #(rf/dispatch [:navigate-to-within-stack [:community :community :id id]])
       :on-button-long-press #(rf/dispatch [:navigate-to-within-stack
                                            [:community :community :id id]])}]]))
