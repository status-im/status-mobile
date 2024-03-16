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
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(defn- single-token-gating
  [{:keys [blur? token-value token-symbol]}]
  [rn/view {:style {:flex-direction :row}}
   [text/text {:style {:margin-right 3}}
    (i18n/label :t/hold-to-post-1)]
   [token-tag/view
    {:size         :size-24
     :option       false
     :blur?        blur?
     :token-value  token-value
     :token-symbol token-symbol}]
   [text/text {:style {:margin-left 3}}
    (i18n/label :t/hold-to-post-2)]])

(defn- token-group
  [blur? idx group]
  ^{:key idx}
  [rn/view {:style style/token-group}
   (when-not (= idx 0)
     [text/text {:style {:margin-right 3}}
      (string/lower-case (i18n/label :t/or))])
   [preview-list/view
    {:type  :tokens
     :blur? blur?
     :size  :size-24} group]])

(defn- multiple-token-gating
  [{:keys [token-groups blur?]}]
  (let [visible-token-groups (take 2 token-groups)
        extra-groups?        (-> token-groups count (> 2))]
    [rn/view {:style {:flex-direction :row}}
     [text/text (i18n/label :t/hold-to-post-1)]
     [rn/view {:style {:flex-direction :row}}
      (map-indexed (partial token-group blur?) visible-token-groups)]
     (when extra-groups?
       [:<>
        [text/text {:style {:margin-horizontal 3}}
         (string/lower-case (i18n/label :t/or))]
        [number-tag/view
         {:size   :size-24
          :number "9999" ;; show the options tag
          :blur?  blur?
          :type   :rounded}]])
     [text/text {:style {:margin-left 3}}
      (i18n/label :t/hold-to-post-2)]]))

(defn- view-internal
  [{:keys [on-press blur? type container-style] :as props}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/shadow
     {:offset      [0 4]
      :start-color (colors/theme-colors colors/neutral-100-opa-5 colors/neutral-100-opa-60)
      :distance    30
      :style       {:align-self :stretch}}
     [rn/view {:style (merge (style/container blur? theme) container-style)}
      [button/button
       {:type      :ghost
        :size      24
        :on-press  on-press
        :icon-left (when (= type :action)
                     (:action-icon props))}
       (condp = type
         :action                (:action-label props)
         :single-token-gating   [single-token-gating
                                 (select-keys props [:token-value :token-symbol :blur?])]
         :multiple-token-gating [multiple-token-gating
                                 (select-keys props [:token-groups :blur?])])]]]))
(def view (schema/instrument #'view-internal component-schema/?schema))

