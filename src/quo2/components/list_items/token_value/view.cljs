(ns quo2.components.list-items.token-value.view
  (:require
    [clojure.string :as string]
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.foundations.common :as common]
    [quo2.components.list-items.token-value.style :as style]))

(defn- internal-view
  [{:keys [theme customization-color state status token metrics? values]}]
  (let [bg-opacity                                                      (case state
                                                                          :active  10
                                                                          :pressed 5
                                                                          0)
        {:keys [crypto-value fiat-value percentage-change fiat-change]} values]
    [rn/view
     {:style               (style/container customization-color bg-opacity theme)
      :accessibility-label :container}
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      [rn/image
       {:source (resources/tokens token)
        :style  {:width  32
                 :height 32}}]
      [rn/view {:style {:margin-left 8}}
       [text/text {:weight :semi-bold} (common/token-label token)]
       [text/text
        {:size  :paragraph-2
         :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
        (str crypto-value " " (string/upper-case (clj->js token)))]]]
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

(def view (quo.theme/with-theme internal-view))
