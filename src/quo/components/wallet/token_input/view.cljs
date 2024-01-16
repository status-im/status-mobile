(ns quo.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.dividers.divider-line.view :as divider-line]
    [quo.components.markdown.text :as text]
    [quo.components.tags.network-tags.view :as network-tag]
    [quo.components.utilities.token.view :as token]
    [quo.components.wallet.token-input.style :as style]
    [quo.foundations.common :as common]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn calc-value
  [crypto? currency token value conversion]
  (let [num-value (if (string? value)
                    (or (parse-double value) 0)
                    value)]
    (if crypto?
      (str (get common/currency-label currency) (.toFixed (* num-value conversion) 2))
      (str (.toFixed (/ num-value conversion) 2)
           " "
           (string/upper-case (or (clj->js token) ""))))))

(defn- data-info
  [{:keys [theme conversion networks title crypto? currency token amount]}]
  [rn/view {:style style/data-container}
   [network-tag/view {:networks networks :title title}]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  (style/fiat-amount theme)}
    (calc-value crypto? currency token amount conversion)]])

(defn- token-name-text
  [theme text]
  [text/text
   {:style  (style/token-name theme)
    :size   :paragraph-2
    :weight :semi-bold}
   (string/upper-case (or (clj->js text) ""))])

(defn- token-label
  [{:keys [theme value text]}]
  [rn/view
   {:style          style/token-label-container
    :pointer-events :none}
   [rn/text-input
    {:auto-focus true
     :max-length 12
     :style      style/text-input-dimensions
     :editable   false
     :opacity    0
     :value      value}]
   [token-name-text theme text]])

(defn input-section
  [{:keys [on-change-text value value-atom]}]
  (let [input-ref             (atom nil)
        set-ref               #(reset! input-ref %)
        focus-input           #(when-let [ref ^js @input-ref]
                                 (.focus ref))
        controlled-input?     (some? value)
        handle-on-change-text (fn [v]
                                (when-not controlled-input?
                                  (reset! value-atom v))
                                (when on-change-text
                                  (on-change-text v)))]
    (fn [{:keys [theme token customization-color show-keyboard? crypto? currency value]
          :or   {show-keyboard? true}}]
      [rn/pressable
       {:on-press focus-input
        :style    {:flex 1}}
       [token/view
        {:token token
         :size  :size-32}]
       [rn/view {:style style/text-input-container}
        [rn/text-input
         (cond-> {:style                    (style/text-input theme)
                  :placeholder-text-color   (style/placeholder-text theme)
                  :auto-focus               true
                  :ref                      set-ref
                  :placeholder              "0"
                  :keyboard-type            :numeric
                  :max-length               12
                  :on-change-text           handle-on-change-text
                  :selection-color          customization-color
                  :show-soft-input-on-focus show-keyboard?}
           controlled-input?       (assoc :value value)
           (not controlled-input?) (assoc :default-value @value-atom))]]
       [token-label
        {:theme theme
         :text  (if crypto? token currency)
         :value (if controlled-input? value @value-atom)}]])))

(defn- view-internal
  [{:keys [on-swap]}]
  (let [width          (:width (rn/get-window))
        value-atom     (reagent/atom nil)
        crypto?        (reagent/atom true)
        handle-on-swap (fn []
                         (swap! crypto? not)
                         (when on-swap
                           (on-swap @crypto?)))]
    (fn [{:keys [theme container-style value] :as props}]
      [rn/view {:style (merge (style/main-container width) container-style)}
       [rn/view {:style style/amount-container}
        [input-section
         (assoc props
                :value-atom value-atom
                :crypto?    @crypto?)]
        [button/button
         {:icon                true
          :icon-only?          true
          :size                32
          :on-press            handle-on-swap
          :type                :outline
          :accessibility-label :reorder}
         :i/reorder]]
       [divider-line/view {:container-style (style/divider theme)}]
       [data-info
        (assoc props
               :crypto? @crypto?
               :amount  (or value @value-atom))]])))

(def view (quo.theme/with-theme view-internal))
