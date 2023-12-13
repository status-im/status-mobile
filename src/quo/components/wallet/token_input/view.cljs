(ns quo.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.dividers.divider-line.view :as divider-line]
    [quo.components.markdown.text :as text]
    [quo.components.tags.network-tags.view :as network-tag]
    [quo.components.utilities.token.view :as token]
    [quo.components.wallet.token-input.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.common :as common]
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
  [{external-value :value}]
  (let [width             (:width (rn/get-window))
        value             (reagent/atom nil)
        crypto?           (reagent/atom true)
        input-ref         (atom nil)
        controlled-input? (some? external-value)]
    (fn [{:keys          [theme token currency conversion networks title customization-color
                          on-change-text on-swap container-style show-keyboard?]
          :or            {show-keyboard? true}
          external-value :value}]
      [rn/view
       {:style (merge
                (style/main-container width)
                container-style)}
       [rn/view {:style style/amount-container}
        [rn/pressable
         {:on-press #(when @input-ref (.focus ^js @input-ref))
          :style    {:flex-direction :row
                     :flex-grow      1
                     :align-items    :flex-end}}
         [token/view {:token token :size :size-32}]
         [rn/text-input
          (cond-> {:auto-focus               true
                   :ref                      #(reset! input-ref %)
                   :placeholder              "0"
                   :placeholder-text-color   (colors/theme-colors colors/neutral-40
                                                                  colors/neutral-50
                                                                  theme)
                   :keyboard-type            :numeric
                   :max-length               12
                   :on-change-text           (fn [v]
                                               (when-not controlled-input?
                                                 (reset! value v))
                                               (when on-change-text
                                                 (on-change-text v)))
                   :style                    (style/text-input theme)
                   :selection-color          customization-color
                   :show-soft-input-on-focus show-keyboard?}
            controlled-input?       (assoc :value external-value)
            (not controlled-input?) (assoc :default-value @value))]
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
          :on-press            (fn []
                                 (swap! crypto? not)
                                 (when on-swap
                                   (on-swap @crypto?)))
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
