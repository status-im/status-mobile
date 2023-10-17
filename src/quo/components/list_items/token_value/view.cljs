(ns quo.components.list-items.token-value.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
    [quo.components.list-items.token-value.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.foundations.common :as common]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- internal-view
  []
  (let [state (reagent/atom :default)]
    (fn [{:keys [theme customization-color status token metrics? values on-press]}]
      (let [bg-opacity                                                      (case @state
                                                                              :active  10
                                                                              :pressed 5
                                                                              0)
            {:keys [crypto-value fiat-value percentage-change fiat-change]} values]
        [rn/pressable
         {:style               (style/container customization-color bg-opacity theme)
          :on-press-in         #(reset! state :pressed)
          :on-press-out        #(reset! state :default)
          :on-press            (fn []
                                 (reset! state :active)
                                 (js/setTimeout #(reset! state :default) 300)
                                 on-press)
          :accessibility-label :container}
         [rn/view
          {:style {:flex-direction :row
                   :align-items    :center
                   :flex           1}}
          [rn/image
           {:source (resources/get-token token)
            :style  {:width  32
                     :height 32}}]
          [rn/view {:style {:margin-left 8}}
           [text/text {:weight :semi-bold} (common/token-label token)]
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
                 (style/arrow-icon status theme)]])])]]))))

(def view (quo.theme/with-theme internal-view))
