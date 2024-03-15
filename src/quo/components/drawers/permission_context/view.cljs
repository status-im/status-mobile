(ns quo.components.drawers.permission-context.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.permission-context.schema :as component-schema]
    [quo.components.drawers.permission-context.style :as style]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.tags.number-tag.view :as number-tag]
    [quo.components.tags.token-tag.view :as token-tag]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(defn- token-group
  [idx group]
  ^{:key idx}
  [rn/view
   {:style {:flex-direction :row
            :margin-left    3}}
   (when-not (= idx 0)
     [text/text {:style {:margin-right 3}} (string/lower-case (i18n/label :t/or))])
   [preview-list/view {:type :tokens :size :size-24} group]])

(defn- view-internal
  [{:keys [on-press blur? type container-style] :as props}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style (merge (style/container blur? theme) container-style)}
     [button/button
      {:type      :ghost
       :size      24
       :on-press  on-press
       :icon-left (when (= type :action)
                    (:action-icon props))}
      (condp = type
        :action                (:action-label props)
        :single-token-gating   [rn/view {:style {:flex-direction :row}}
                                [text/text {:style {:margin-right 3}} (i18n/label :t/hold-to-post-1)]
                                [token-tag/view
                                 {:size         :size-24
                                  :option       false
                                  :blur?        blur?
                                  :token-value  (:token-value props)
                                  :token-symbol (:token-symbol props)}]
                                [text/text {:style {:margin-left 3}} (i18n/label :t/hold-to-post-2)]]
        :multiple-token-gating [rn/view {:style {:flex-direction :row}}
                                [text/text (i18n/label :t/hold-to-post-1)]
                                [rn/view
                                 {:style {:flex-direction :row}}
                                 (map-indexed token-group
                                              (->> props
                                                   :token-groups
                                                   (take 2)))]
                                (when (-> props :token-groups count (> 2))
                                  [:<>
                                   [text/text {:style {:margin-horizontal 3}}
                                    (string/lower-case (i18n/label :t/or))]
                                   [number-tag/view
                                    {:size :size-24 :number "9999" :blur? blur? :type :rounded}]])
                                [text/text {:style {:margin-left 3}}
                                 (i18n/label :t/hold-to-post-2)]])]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
