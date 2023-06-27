(ns quo2.components.buttons.predictive-keyboard.view
  (:require [react-native.core :as rn]
            [quo2.components.buttons.predictive-keyboard.style :as style]
            [quo2.components.info.info-message :as info-message]
            [quo2.theme :as theme]
            [react-native.linear-gradient :as linear-gradient]
            [quo2.foundations.colors :as colors]
            [quo2.components.buttons.button.view :as button]))

(def ^:private gradients
  {:light [(colors/alpha colors/neutral-40 0.05) (colors/alpha colors/neutral-40 0)]
   :dark  [(colors/alpha colors/neutral-80 0.7) (colors/alpha colors/neutral-80 0)]
   :blur  [colors/white-opa-5 colors/white-opa-0]})

(defn- word-component
  [word _ _ {:keys [on-press]}]
  [button/button
   {:type     :blur-bg
    :size     32
    :on-press #(on-press word)}

   word])

(defn- separator
  []
  [rn/view {:style {:width 8}}])

(defn- view-internal
  "Options
   - `type` `:words`/`:error`/`:info`/`:empty`.
   - `blur?` Boolean to enable blur background support.
   - `text`  error/info text.
   - `words` List of words to display in the keyboard.
   - `on-press` Callback called when a word is pressed `(fn [word])`
   - `theme` :light or :dark, received from with-theme HOC."
  [{:keys [type blur? text words on-press theme]}]
  [linear-gradient/linear-gradient
   {:style               {:flex-direction :row}
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
      [info-message/info-message
       {:icon :i/info
        :size :default
        :type :error}
       text]

      :info
      [info-message/info-message
       (merge {:icon :i/info
               :size :default
               :type (if (= type :error) :error :default)}
              (when blur?
                {:text-color colors/white-opa-70
                 :icon-color colors/white-opa-70}))
       text]
      nil)]])

(def view (theme/with-theme view-internal))
