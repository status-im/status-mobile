(ns status-im2.contexts.communities.actions.token-gating.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]))

(defn token-requirements
  [id]
  (rf/dispatch [:communities/check-permissions-to-join-community id])
  (fn []
    (let [{:keys [can-request-access?
                  number-of-hold-tokens tokens]} (rf/sub [:community/token-gated-overview id])]
      [rn/view {:style {:padding-horizontal 12}}
       [quo/text {:weight :medium}
        (if can-request-access?
          (i18n/label :t/you-eligible-to-join)
          (i18n/label :t/you-not-eligible-to-join))]
       [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hold-number-of-hold-tokens-of-these
                      {:number-of-hold-tokens number-of-hold-tokens})
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list {:tokens tokens}]])))
