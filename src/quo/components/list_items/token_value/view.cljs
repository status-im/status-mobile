(ns quo.components.list-items.token-value.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
    [quo.components.list-items.token-value.schema :as component-schema]
    [quo.components.list-items.token-value.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- internal-view
  [{:keys [customization-color status token metrics? values on-press on-long-press token-name]}]
  (let [theme                 (quo.context/use-theme)
        [state set-state]     (rn/use-state :default)
        bg-opacity            (case state
                                :active  10
                                :pressed 5
                                0)
        {:keys [crypto-value
                fiat-value
                percentage-change
                fiat-change]} values
        on-press-in           (rn/use-callback #(set-state :pressed))
        on-press-out          (rn/use-callback #(set-state :default))
        on-press              (rn/use-callback
                               (fn []
                                 (set-state :active)
                                 (js/setTimeout #(set-state :default) 300)
                                 (when on-press
                                   (on-press))))]
    [rn/pressable
     {:style               (style/container customization-color bg-opacity theme)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :on-long-press       on-long-press
      :accessibility-label :container}
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center
               :flex           1}}
      [token/view {:token token :size :size-32}]
      [rn/view {:style {:margin-left 8}}
       [text/text {:weight :semi-bold} token-name]
       [text/text
        {:size  :paragraph-2
         :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
        (str crypto-value " " (if token (string/upper-case (clj->js token)) ""))]]]
     [rn/view
      {:style {:align-items     :flex-end
               :justify-content :space-between}}
      [text/text
       {:weight :medium
        :size   :paragraph-2} fiat-value]
      (when metrics?
        [rn/view
         {:style {:flex-direction :row
                  :align-items    :center}}
         [text/text
          {:size  :paragraph-2
           :style (style/metric-text status theme)} (str percentage-change "%")]
         [rn/view {:style (style/dot-divider status theme)}]
         [text/text
          {:size  :paragraph-2
           :style (style/metric-text status theme)} fiat-change]
         (when (not= status :empty)
           [rn/view
            {:style               {:margin-left 4}
             :accessibility-label :arrow-icon}
            [icon/icon (if (= status :positive) :i/positive :i/negative)
             (style/arrow-icon status theme)]])])]]))

(def view (schema/instrument #'internal-view component-schema/?schema))
