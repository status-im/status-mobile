(ns quo.components.drawers.permission-context.view
  (:require
    [clojure.string :as string]
    [quo.components.blur.view :as blur]
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.permission-context.schema :as component-schema]
    [quo.components.drawers.permission-context.style :as style]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.tags.number-tag.view :as number-tag]
    [quo.components.tags.token-tag.view :as token-tag]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.shadow :as shadow]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(defn- single-token-gating
  [{:keys [blur? token-value token-symbol]}]
  [rn/view
   {:style               {:flex-direction :row}
    :accessibility-label :permission-context-single-token}
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
   (when-not (zero? idx)
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
    [rn/view
     {:style               {:flex-direction :row}
      :accessibility-label :permission-context-multiple-token}
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
  [{:keys [on-press blur? container-style] :as props}]
  (let [theme        (quo.theme/use-theme)
        context-type (:type props)]
    [shadow/view
     {:offset       [0 4]
      :paint-inside false
      :start-color  (colors/theme-colors colors/neutral-100-opa-8 colors/neutral-100-opa-60 theme)
      :distance     25
      :style        {:align-self :stretch}}
     [rn/view {:style (merge (style/container blur? theme) container-style)}
      (when blur?
        [blur/view
         {:style         style/blur-container
          :blur-amount   20
          :blur-radius   (if platform/ios? 20 10)
          :overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)
          :blur-type     :transparent}])
      [button/button
       {:type      :ghost
        :size      24
        :on-press  on-press
        :icon-left (when (= context-type :action)
                     (:action-icon props))}
       (condp = context-type
         :action                (:action-label props)
         :single-token-gating   [single-token-gating
                                 (select-keys props [:token-value :token-symbol :blur?])]
         :multiple-token-gating [multiple-token-gating
                                 (select-keys props [:token-groups :blur?])])]]]))

(def view (schema/instrument #'view-internal component-schema/?schema))

