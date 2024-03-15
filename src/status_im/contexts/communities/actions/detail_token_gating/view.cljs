(ns status-im.contexts.communities.actions.detail-token-gating.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.detail-token-gating.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [id]
  (let [permissions (rf/sub [:community/token-permissions id])
        role-text   (fn [role]
                      (i18n/label
                       (communities.utils/role->translation-key role
                                                                :t/token-master)))]

    [rn/view {:style style/container}
     (map (fn [{:keys [tokens role satisfied?]}]
            (when (seq tokens)
              ^{:key role}
              [rn/view {:style {:margin-bottom 20}}
               [quo/text {:weight :medium}
                (if satisfied?
                  (i18n/label :t/you-eligible-to-join-as {:role (role-text role)})
                  (i18n/label :t/you-not-eligible-to-join))]
               [quo/text {:size :paragraph-2 :style {:padding-bottom 8}}
                (if satisfied?
                  (i18n/label :t/you-hodl)
                  (i18n/label :t/you-must-hold))]
               [quo/token-requirement-list
                {:tokens          tokens
                 :container-style {:padding-horizontal 20}}]]))
          permissions)]))
