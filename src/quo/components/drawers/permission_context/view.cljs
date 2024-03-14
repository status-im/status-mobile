(ns quo.components.drawers.permission-context.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.permission-context.style :as style]
    [quo.components.tags.token-tag.view :as token-tag]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [on-press background type] :as props}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style (style/container background theme)}
     [button/button
      {:type      :ghost
       :size      24
       :on-press  on-press
       :icon-left (when (= type :action)
                    (:action-icon props))}
      (condp = type
        :action              (:action-label props)
        :single-token-gating [token-tag/view
                              {:size         :size-24
                               :blur?        (= background :blur)
                               :token-value  (:token-value props)
                               :token-symbol (:token-symbol props)}])]]))

(def ?base
  [:map {:closed true}
   [:type [:enum :action :single-token-gating]]
   [:background {:optional true} [:enum :blur :none]]
   [:on-press {:optional true} :function]])

(def ?action
  [:map {:closed true}
   [:action-label :string]
   [:action-icon [:qualified-keyword {:namespace :i}]]])

(def ?single-token-gating
  [:map {:closed true}
   [:token-value :string]
   [:token-symbol :string]])

(def ?schema
  [:=>
   [:cat
    [:multi {:dispatch :type}
     [:action [:merge ?base ?action]]
     [:single-token-gating [:merge ?base ?single-token-gating]]]]
   :any])

