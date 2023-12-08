(ns status-im2.contexts.communities.actions.addresses-for-permissions.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.communities.actions.addresses-for-permissions.style :as style]
            [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- page-top ;; TODO:: Make this a common component
  [{:keys [community-name logo-uri]}]
  [rn/view {:style style/page-top}
   [quo/text
    {:size   :heading-2
     :weight :semi-bold}
    (i18n/label :t/addresses-for-permissions)]
   [quo/context-tag
    {:type            :community
     :size            24
     :community-logo  logo-uri
     :community-name  community-name
     :container-style {:margin-top 8}}]])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])]
    [rn/view {:style style/container}
     [page-top
      {:community-name name
       :logo-uri       (get-in images [:thumbnail :uri])}]
    ]))
