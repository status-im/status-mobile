(ns status-im.contexts.preview.quo.drawers.permission-drawers
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :action}
              {:key :single-token-gating}
              {:key :multiple-token-gating}]}
   {:key  :blur?
    :type :boolean}])

(def single-token-gating-props
  [{:token-value  "50"
    :token-symbol "SNT"}
   {:token-value  "0.01"
    :token-symbol "ETH"}])

(def multiple-token-gating-props
  [{:token-groups [[:eth :knc :snt :rare :mana]]}
   {:token-groups [[:snt :eth :knc]
                   [:eth :knc :snt :rare :mana]]}
   {:token-groups [[:snt :eth :knc :rare :mana :snt]
                   [:eth :knc :snt :rare :mana]
                   [:rare :mana]]}])

(def action-props
  [{:action-label "You sure you know this guy?"
    :action-icon  :i/pending-state}
   {:action-label "Join community to post"
    :action-icon  :i/communities}])

(defn view
  []
  (let [state (reagent/atom {:type  :multiple-token-gating
                             :blur? true})
        blur? (reagent/cursor state [:blur?])
        type  (reagent/cursor state [:type])]
    (fn []
      [preview/preview-container
       {:state                     state
        :blur?                     @blur?
        :show-blur-background?     true
        :component-container-style {:padding-horizontal 0}
        :blur-container-style      {:padding-horizontal 0}
        :descriptor                descriptor}
       (->> (condp = @type
              :action                action-props
              :single-token-gating   single-token-gating-props
              :multiple-token-gating multiple-token-gating-props)
            (map-indexed (fn [idx props]
                           [:<>
                            ^{:key idx}
                            [quo/permission-context
                             (merge @state props {:container-style {:margin-bottom 8}})]])))])))
