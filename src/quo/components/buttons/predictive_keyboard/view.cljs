(ns quo.components.buttons.predictive-keyboard.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.buttons.predictive-keyboard.style :as style]
    [quo.components.info.info-message.view :as info-message]
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]))

(def ^:private gradients
  {:light [(colors/alpha colors/neutral-40 0.05) (colors/alpha colors/neutral-40 0)]
   :dark  [(colors/alpha colors/neutral-80 0.7) (colors/alpha colors/neutral-80 0)]
   :blur  [colors/white-opa-5 colors/white-opa-0]})

(defn- word-component
  [word _ _ {:keys [on-press]}]
  [button/button
   {:type       :grey
    :background :blur
    :size       32
    :on-press   #(on-press word)}
   word])

(defn- separator
  []
  [rn/view {:style {:width 8}}])

(defn view
  "Options
   - `type` `:words`/`:error`/`:info`/`:empty`.
   - `blur?` Boolean to enable blur background support.
   - `text`  error/info text.
   - `words` List of words to display in the keyboard.
   - `on-press` Callback called when a word is pressed `(fn [word])`
   - `theme` :light or :dark, received from with-theme HOC."
  [{:keys [type blur? text words on-press container-style] :or {container-style {}}}]
  (let [theme (quo.context/use-theme)]
    [linear-gradient/linear-gradient
     {:style               (assoc container-style :flex-direction :row)
      :accessibility-label :predictive-keyboard
      :colors              (if blur?
                             (gradients :blur)
                             (colors/theme-colors (gradients :light) (gradients :dark) theme))}
     [rn/view {:style (style/wrapper type)}
      (case type
        :words
        [rn/flat-list
         {:keyboard-should-persist-taps      :always
          :data                              words
          :content-container-style           style/word-list
          :render-fn                         word-component
          :render-data                       {:on-press on-press}
          :shows-horizontal-scroll-indicator false
          :separator                         [separator]
          :horizontal                        true
          :key-fn                            str}]

        :error
        [info-message/view
         {:icon   :i/alert
          :size   :default
          :status :error}
         text]

        :info
        [info-message/view
         (merge {:icon   :i/info
                 :size   :default
                 :status (if (= type :error) :error :default)}
                (when blur?
                  {:text-color colors/white-opa-70
                   :icon-color colors/white-opa-40}))
         text]
        nil)]]))
