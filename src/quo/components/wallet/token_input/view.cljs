(ns quo.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.dividers.divider-line.view :as divider-line]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.components.wallet.token-input.schema :as component-schema]
    [quo.components.wallet.token-input.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- token-name-text
  [theme text]
  [text/text
   {:style  (style/token-name theme)
    :size   :paragraph-2
    :weight :semi-bold}
   (string/upper-case (or (clj->js text) ""))])

(defn input-section
  [{:keys [token-symbol on-token-press value error? on-swap currency-symbol show-token-icon?
           swappable?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:style    {:width          "100%"
                 :flex-direction :row}
      :on-press on-token-press}
     (when show-token-icon?
       [token/view
        {:token token-symbol
         :size  :size-32}])
     [rn/view {:style style/input-container}
      [rn/text-input
       {:style                  (style/text-input theme error?)
        :placeholder-text-color (style/placeholder-text theme)
        :placeholder            "0"
        :keyboard-type          :numeric
        :editable               false
        :value                  value}]
      [token-name-text theme currency-symbol]]
     (when swappable?
       [button/button
        {:icon                true
         :icon-only?          true
         :size                32
         :on-press            #(when on-swap (on-swap))
         :type                :outline
         :accessibility-label :reorder}
        :i/reorder])]))

(defn- view-internal
  [{:keys [token-symbol
           value
           on-token-press
           error?
           container-style
           on-swap
           converted-value
           hint-component
           show-token-icon?
           currency-symbol
           swappable?]
    :or   {show-token-icon? true
           swappable?       true}}]
  (let [theme (quo.theme/use-theme)
        width (:width (rn/get-window))]
    [rn/view {:style (merge (style/main-container width) container-style)}
     [rn/view {:style style/amount-container}
      [input-section
       {:theme            theme
        :token-symbol     token-symbol
        :on-token-press   on-token-press
        :value            value
        :error?           error?
        :on-swap          on-swap
        :currency-symbol  currency-symbol
        :show-token-icon? show-token-icon?
        :swappable?       swappable?}]]
     [divider-line/view {:container-style (style/divider theme)}]
     [rn/view {:style style/data-container}
      hint-component
      (when swappable?
        [text/text
         {:size   :paragraph-2
          :weight :medium
          :style  (style/converted-amount theme)}
         converted-value])]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
