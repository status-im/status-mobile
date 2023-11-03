(ns quo.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.dividers.divider-line.view :as divider-line]
    [quo.components.markdown.text :as text]
    [quo.components.tags.network-tags.view :as network-tag]
    [quo.components.wallet.token-input.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.common :as common]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn calc-value
  [crypto? currency token value conversion]
  (let [num-value (if (string? value) (parse-double (or value "0")) value)]
    (if crypto?
      (str (get common/currency-label currency) (.toFixed (* num-value conversion) 2))
      (str (.toFixed (/ num-value conversion) 2) " " (string/upper-case (or (clj->js token) ""))))))

(defn- view-internal
  []
  (let [width     (:width (rn/get-window))
        value     (reagent/atom nil)
        crypto?   (reagent/atom true)
        input-ref (atom nil)]
    (fn [{:keys          [theme token currency conversion networks title customization-color
                          show-keyboard?]
          :or            {show-keyboard? true}
          external-value :value}]
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
          (cond-> {:ref                    #(reset! input-ref %)
                   :placeholder            "0"
                   :accessibility-label :token-input
                   :placeholder-text-color (colors/theme-colors colors/neutral-40
                                                                colors/neutral-50
                                                                theme)
                   :keyboard-type          :numeric
                   :max-length             12
                   :on-change-text         #(reset! value %)
                   :style                  (style/text-input theme)
                   :selection-color        customization-color
                   :showSoftInputOnFocus   show-keyboard?}
            external-value (assoc :value external-value))]
         [text/text
          {:size   :paragraph-2
           :weight :semi-bold
           :style  {:color          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
                    :margin-right   8
                    :padding-bottom 2}}
          (string/upper-case (or (clj->js (if @crypto? token currency)) ""))]]
        [button/button
         {:icon                true
          :icon-only?          true
          :size                32
          :on-press            #(swap! crypto? not)
          :type                :outline
          :accessibility-label :reorder}
         :i/reorder]]
       [divider-line/view {:container-style {:margin-vertical 8}}]
       [rn/view {:style style/data-container}
        [network-tag/view {:networks networks :title title}]
        [text/text
         {:size   :paragraph-2
          :weight :medium
          :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
         (calc-value @crypto? currency token (or external-value @value) conversion)]]])))

(def view (quo.theme/with-theme view-internal))
