(ns quo.components.list-items.market-token.view
  (:require
    [clojure.string :as string]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icon]
    [quo.components.list-items.market-token.schema :as component-schema]
    [quo.components.list-items.market-token.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.context :as quo.context]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- internal-view
  [{:keys [token token-name token-rank market-cap price percentage-change customization-color
           on-press on-long-press]}]
  (let [theme             (quo.context/use-theme)
        [state set-state] (rn/use-state :default)
        bg-opacity        (case state
                            :active  10
                            :pressed 5
                            0)
        on-press-in       (rn/use-callback #(set-state :pressed))
        on-press-out      (rn/use-callback #(set-state :default))
        on-press          (rn/use-callback
                           (fn []
                             (set-state :active)
                             (js/setTimeout #(set-state :default) 300)
                             (when on-press
                               (on-press))))
        token-short-name  (if token (string/upper-case (clj->js token)) "")]
    [rn/pressable
     {:style               (style/container customization-color bg-opacity theme)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :on-long-press       on-long-press
      :accessibility-label :market-token-container}
     [rn/view
      {:style style/left-side}
      [counter/view
       {:type            :outline
        :max-value       99999
        :container-style {:margin-right 8}}
       token-rank]
      [token/view {:token token :size :size-32}]
      [rn/view style/left-text-block
       [rn/text
        ;; If we place 2 texts of different styles within parent _view_ component they won't be
        ;; aligned to the same baseline. So they should be a children of the same _text_ component
        ;; to share same basline and look nice. But that means we can't use precise margins anymore
        ;; and have to rely on whitespaces to split 2 texts.
        [text/text style/token-name (str token-name " ")]
        [text/text
         {:weight :medium
          :size   :paragraph-2
          :style  (style/token-short-name theme)} token-short-name]]
       [text/text
        {:size  :paragraph-2
         :style (style/market-cap theme)}
        market-cap]]]
     [rn/view
      {:style style/right-side}
      [text/text
       {:weight :medium
        :size   :paragraph-2} price]
      (when percentage-change
        [rn/view
         {:style style/percentage-container}
         [text/text
          {:size  :paragraph-2
           :style (style/percentage-text percentage-change theme)}
          (str percentage-change "%")]
         [rn/view
          {:style               style/arrow
           :accessibility-label :arrow-icon}
          [icon/icon (if (pos? percentage-change) :i/positive :i/negative)
           (style/arrow-icon percentage-change theme)]]])]]))

(def view (schema/instrument #'internal-view component-schema/?schema))
