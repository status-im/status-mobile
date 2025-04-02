(ns quo.components.list-items.market-token.view
  (:require
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icon]
    [quo.components.list-items.market-token.schema :as component-schema]
    [quo.components.list-items.market-token.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [re-frame.core :as rf]
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
                             on-press))]
    [rn/pressable
     {:style               (style/container customization-color bg-opacity theme)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :on-long-press       on-long-press
      :accessibility-label :market-token-container}
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center
               :flex           1}}
      [counter/view
       {:type            :outline
        :max-value       99999
        :container-style {:margin-right 8}}
       token-rank]
      [token/view {:token token :size :size-32}]
      [rn/view {:style {:margin-left 8}}
       [text/text {:weight :semi-bold} token-name]
       [text/text
        {:size  :paragraph-2
         :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
        market-cap]]]
     [rn/view
      {:style {:align-items     :flex-end
               :justify-content :space-between}}
      [text/text
       {:weight :medium
        :size   :paragraph-2} price]
      (when percentage-change
        [rn/view
         {:style {:flex-direction :row
                  :align-items    :center}}
         [text/text
          {:size  :paragraph-2
           :style (style/percentage-text percentage-change theme)}
          (str percentage-change "%")]
         [rn/view
          {:style               {:margin-left 4}
           :accessibility-label :arrow-icon}
          [icon/icon (if (pos? percentage-change) :i/positive :i/negative)
           (style/arrow-icon percentage-change theme)]]])]]))

(def view (schema/instrument #'internal-view component-schema/?schema))
