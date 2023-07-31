(ns quo2.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo2.components.buttons.button.view :as button]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [reagent.core :as reagent]))

(def currency-label
  {:eur "€"
   :usd "$"})

(defn calc-value
  [crypto? currency token value conversion]
  (if crypto?
    (str (get currency-label currency) (* value conversion))
    (str (/ value conversion) " " (string/upper-case (clj->js token)))))

(defn- view-internal
  [{:keys [theme token currency conversion]}]
  (let [width   (:width (rn/get-window))
        value   (reagent/atom 0)
        crypto? (reagent/atom true)]
    (fn []
      [rn/view {:style {:padding-vertical 8
                        :width            width}}
       [rn/view {:style {:padding-horizontal 20
                         :padding-bottom     4
                         :height             36
                         :flex-direction     :row
                         :justify-content    :space-between}}
        [rn/view {:style {:flex-direction :row
                          :align-items    :flex-end}}
         [rn/image {:style  {:width  32
                             :height 32}
                    :source (resources/get-token token)}]
         [rn/text-input {:placeholder            "0"
                         :placeholder-text-color (colors/theme-colors colors/neutral-40 colors/neutral-50)
                         :max-length             12
                         :default-value          @value
                         :on-change-text         #(reset! value %)
                         :style                  {:font-size    27
                                                  :font-weight  "600"
                                                  :line-height  32
                                                  :margin-left  4
                                                  :margin-right (if platform/ios? 6 4)
                                                  :padding      0
                                                  :text-align   :center
                                                  :height       "100%"}}]
         [text/text {:size   :paragraph-2
                     :weight :semi-bold
                     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}} (string/upper-case (clj->js (if @crypto? token currency)))]]
        [button/button
         {:icon                true
          :size                32
          :on-press            #(swap! crypto? not)
          :type                :outline
          :accessibility-label :reorder}
         :i/reorder]]
       [rn/view {:style {:height           1
                         :width            width
                         :background-color (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
                         :margin-vertical  8}}]
       [rn/view {:style {:padding-top        4
                         :padding-horizontal 20
                         :height             28
                         :flex-direction     :row
                         :justify-content    :space-between}}
        [text/text "[WIP] NETWORK TAG"]
        [text/text {:size   :paragraph-2
                    :weight :medium
                    :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
         (calc-value @crypto? currency token @value conversion)]]])))

(def view (quo.theme/with-theme view-internal))
