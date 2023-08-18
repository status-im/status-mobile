(ns quo2.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo2.components.buttons.button.view :as button]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [quo2.foundations.common :as common]
    [quo2.components.wallet.token-input.style :as style]))

(defn calc-value
  [crypto? currency token value conversion]
  (if crypto?
    (str (get common/currency-label currency) (.toFixed (* value conversion) 2))
    (str (.toFixed (/ value conversion) 2) " " (string/upper-case (clj->js token)))))

(defn- view-internal
  []
  (let [width     (:width (rn/get-window))
        value     (reagent/atom 0)
        crypto?   (reagent/atom true)
        input-ref (atom nil)]
    (fn [{:keys [theme token currency conversion]}]
      [rn/view {:style (style/main-container width)}
       [rn/view {:style style/amount-container}
        [rn/pressable
         {:on-press #(when @input-ref (.focus ^js @input-ref))
          :style    {:flex-direction :row
                     :flex-grow      1
                     :align-items    :flex-end}}
         [rn/image
          {:style  style/token
           :source (resources/get-token token)}]
         [rn/text-input
          {:ref                    #(reset! input-ref %)
           :placeholder            "0"
           :placeholder-text-color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
           :keyboard-type          :numeric
           :max-length             12
           :default-value          @value
           :on-change-text         #(reset! value %)
           :style                  style/text-input}]
         [text/text
          {:size   :paragraph-2
           :weight :semi-bold
           :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
          (string/upper-case (clj->js (if @crypto? token currency)))]]
        [button/button
         {:icon                true
          :size                32
          :on-press            #(swap! crypto? not)
          :type                :outline
          :accessibility-label :reorder}
         :i/reorder]]
       [rn/view {:style (style/divider width theme)}]
       [rn/view {:style style/data-container}
        [text/text "[WIP] NETWORK TAG"]
        [text/text
         {:size   :paragraph-2
          :weight :medium
          :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
         (calc-value @crypto? currency token @value conversion)]]])))

(def view (quo.theme/with-theme view-internal))
