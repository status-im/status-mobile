(ns quo.components.wallet.token-input.view
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
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

(defn fiat-format
  [currency num-value conversion]
  (str (get common/currency-label currency) (.toFixed (* num-value conversion) 2)))

(defn crypto-format
  [num-value conversion crypto-decimals token]
  (str (.toFixed (/ num-value conversion) (or crypto-decimals 2))
       " "
       (string/upper-case (or (clj->js token) ""))))

(defn calc-value
  [{:keys [crypto? currency token value conversion crypto-decimals]}]
  (let [num-value (if (string? value)
                    (or (parse-double value) 0)
                    value)]
    (if crypto?
      (fiat-format currency num-value conversion)
      (crypto-format num-value conversion crypto-decimals token))))

(defn- data-info
  [{:keys [theme token crypto-decimals conversion networks title crypto? currency amount error?]}]
  [rn/view {:style style/data-container}
   [network-tag/view
    {:networks networks
     :title    title
     :status   (when error? :error)}]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  (style/fiat-amount theme)}
    (calc-value {:crypto?         crypto?
                 :currency        currency
                 :token           token
                 :value           amount
                 :conversion      conversion
                 :crypto-decimals crypto-decimals})]])

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
    {:max-length  12
     :style       style/text-input-dimensions
     :editable    false
     :placeholder "0"
     :opacity     0
     :value       value}]
   [token-name-text theme text]])

(defn input-section
  [{:keys [on-change-text value value-atom on-selection-change]}]
  (let [input-ref               (atom nil)
        set-ref                 #(reset! input-ref %)
        focus-input             #(when-let [ref ^js @input-ref]
                                   (.focus ref))
        controlled-input?       (some? value)
        handle-on-change-text   (fn [v]
                                  (when-not controlled-input?
                                    (reset! value-atom v))
                                  (when on-change-text
                                    (on-change-text v)))
        handle-selection-change (fn [^js e]
                                  (when on-selection-change
                                    (-> e
                                        (oops/oget "nativeEvent.selection")
                                        (js->clj :keywordize-keys true)
                                        (on-selection-change))))]
    (fn [{:keys [theme token customization-color show-keyboard? crypto? currency value error?
                 selection]
          :or   {show-keyboard? true}}]
      [rn/pressable
       {:on-press focus-input
        :style    {:flex 1}}
       [token/view
        {:token token
         :size  :size-32}]
       [rn/view {:style style/text-input-container}
        [rn/text-input
         (cond-> {:style                    (style/text-input theme error?)
                  :placeholder-text-color   (style/placeholder-text theme)
                  :auto-focus               true
                  :ref                      set-ref
                  :placeholder              "0"
                  :keyboard-type            :numeric
                  :max-length               12
                  :on-change-text           handle-on-change-text
                  :selection-color          customization-color
                  :show-soft-input-on-focus show-keyboard?
                  :on-selection-change      handle-selection-change
                  :selection                (clj->js selection)}
           controlled-input?       (assoc :value value)
           (not controlled-input?) (assoc :default-value @value-atom))]]
       [token-label
        {:theme theme
         :text  (if crypto? token currency)
         :value (if controlled-input? value @value-atom)}]])))

(defn- view-internal
  []
  (let [width      (:width (rn/get-window))
        value-atom (reagent/atom nil)
        crypto?    (reagent/atom true)]
    (fn [{:keys [theme container-style value on-swap] :as props}]
      (let [handle-on-swap (fn []
                             (swap! crypto? not)
                             (when on-swap (on-swap @crypto?)))]
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
                 :amount  (or value @value-atom))]]))))

(def view (quo.theme/with-theme view-internal))
