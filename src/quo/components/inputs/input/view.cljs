(ns quo.components.inputs.input.view
  (:require [oops.core :as oops]
            [quo.components.icon :as icon]
            [quo.components.inputs.input.style :as style]
            [quo.components.markdown.text :as text]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.core :as reagent]))

(defn- label-&-counter
  [{:keys [label current-chars char-limit variant-colors]}]
  [rn/view
   {:accessibility-label :input-labels
    :style               style/texts-container}
   [rn/view {:style style/label-container}
    [text/text
     {:style  (style/label-color variant-colors)
      :weight :medium
      :size   :paragraph-2}
     label]]
   (when-let [count-text (some->> char-limit
                                  (str current-chars "/"))]
     [rn/view {:style style/counter-container}
      [text/text
       {:style  (style/counter-color current-chars char-limit variant-colors)
        :weight :regular
        :size   :paragraph-2}
       count-text]])])

(defn- left-accessory
  [{:keys [variant-colors small? icon-name]}]
  [rn/view
   {:accessibility-label :input-icon
    :style               (style/left-icon-container small?)}
   [icon/icon icon-name (style/icon variant-colors)]])

(defn- right-accessory
  [{:keys [variant-colors small? disabled? on-press icon-style-fn icon-name]}]
  [rn/touchable-opacity
   {:accessibility-label :input-right-icon
    :style               (style/right-icon-touchable-area small?)
    :disabled            disabled?
    :on-press            on-press}
   [icon/icon icon-name (icon-style-fn variant-colors)]])

(defn- right-button
  [{:keys [variant-colors colors-by-status small? disabled? on-press text]}]
  [rn/touchable-opacity
   {:accessibility-label :input-button
    :style               (style/button variant-colors small?)
    :disabled            disabled?
    :on-press            on-press}
   [rn/text {:style (style/button-text colors-by-status)}
    text]])

(def ^:private custom-props
  "Custom properties that must be removed from properties map passed to InputText."
  [:type :blur? :theme :error? :right-icon :left-icon :disabled? :small? :button
   :label :char-limit :on-char-limit-reach :icon-name :multiline? :on-focus :on-blur
   :container-style])

(defn- base-input
  [{:keys [on-change-text on-char-limit-reach weight default-value]}]
  (let [status              (reagent/atom :default)
        internal-on-focus   #(reset! status :focus)
        internal-on-blur    #(reset! status :default)
        multiple-lines?     (reagent/atom false)
        set-multiple-lines! #(let [height     (oops/oget % "nativeEvent.contentSize.height")
                                   ;; In Android height comes with padding
                                   min-height (if platform/android? 40 22)]
                               (if (> height min-height)
                                 (reset! multiple-lines? true)
                                 (reset! multiple-lines? false)))
        char-count          (reagent/atom (count default-value))
        update-char-limit!  (fn [new-text char-limit]
                              (when on-change-text (on-change-text new-text))
                              (let [amount-chars (count new-text)]
                                (reset! char-count amount-chars)
                                (when (and (>= amount-chars char-limit) on-char-limit-reach)
                                  (on-char-limit-reach amount-chars))))]
    (fn [{:keys [blur? theme error? right-icon left-icon disabled? small? button
                 label char-limit multiline? clearable? on-focus on-blur container-style]
          :as   props}]
      (let [status-kw        (cond
                               disabled? :disabled
                               error?    :error
                               :else     @status)
            colors-by-status (style/status-colors status-kw blur? theme)
            variant-colors   (style/variants-colors blur? theme)
            clean-props      (apply dissoc props custom-props)]
        [rn/view {:style container-style}
         (when (or label char-limit)
           [label-&-counter
            {:variant-colors variant-colors
             :label          label
             :current-chars  @char-count
             :char-limit     char-limit}])
         [rn/view {:style (style/input-container colors-by-status small? disabled?)}
          (when-let [{:keys [icon-name]} left-icon]
            [left-accessory
             {:variant-colors variant-colors
              :small?         small?
              :icon-name      icon-name}])
          [rn/text-input
           (cond-> {:style                  (style/input colors-by-status small? @multiple-lines? weight)
                    :accessibility-label    :input
                    :placeholder-text-color (:placeholder colors-by-status)
                    :keyboard-appearance    (quo.theme/theme-value :light :dark theme)
                    :cursor-color           (:cursor variant-colors)
                    :editable               (not disabled?)
                    :on-focus               (fn []
                                              (when on-focus (on-focus))
                                              (internal-on-focus))
                    :on-blur                (fn []
                                              (when on-blur (on-blur))
                                              (internal-on-blur))}
             :always    (merge clean-props)
             multiline? (assoc :multiline              true
                               :on-content-size-change set-multiple-lines!)
             char-limit (assoc :on-change-text #(update-char-limit! % char-limit)))]
          (when-let [{:keys [on-press icon-name style-fn]} right-icon]
            [right-accessory
             {:variant-colors variant-colors
              :small?         small?
              :disabled?      disabled?
              :icon-style-fn  style-fn
              :icon-name      icon-name
              :on-press       (fn []
                                (when clearable? (reset! char-count 0))
                                (on-press))}])
          (when-let [{:keys [on-press text]} button]
            [right-button
             {:colors-by-status colors-by-status
              :variant-colors   variant-colors
              :small?           small?
              :disabled?        disabled?
              :on-press         on-press
              :text             text}])]]))))

(defn- password-input
  [{:keys [default-shown?]
    :or   {default-shown? false}}]
  (let [password-shown? (reagent/atom default-shown?)]
    (fn [props]
      [base-input
       (assoc props
              :accessibility-label :password-input
              :auto-capitalize     :none
              :auto-complete       :password
              :secure-text-entry   (not @password-shown?)
              :right-icon          {:style-fn  style/password-icon
                                    :icon-name (if @password-shown? :i/hide-password :i/reveal)
                                    :on-press  #(swap! password-shown? not)})])))

(defn input-internal
  "This input supports the following properties:
  - :type - Can be `:text`(default) or `:password`.
  - :blur? - Boolean to set the blur color variant.
  - :theme - Can be `light` or `:dark`.
  - :small? - Boolean to specify if this input is rendered in its small version.
  - :multiline? - Boolean to specify if this input support multiple lines.
  - :icon-name - The name of an icon to display at the left of the input.
  - :error? - Boolean to specify it this input marks an error.
  - :disabled? - Boolean to specify if this input is disabled or not.
  - :clearable? - Boolean to specify if this input has a clear button at the end.
  - :on-clear - Function executed when the clear button is pressed.
  - :button - Map containing `:on-press` & `:text` keys, if provided renders a button
  - :label - A string to set as label for this input.
  - :char-limit - A number to set a maximum char limit for this input.
  - :on-char-limit-reach - Function executed each time char limit is reached or exceeded.
  - :default-shown? - boolean to show password input initially
  and supports the usual React Native's TextInput properties to control its behaviour:
  - :value
  - :default-value
  - :on-change
  - :on-change-text
  ...
  "
  [{:keys [type clearable? on-clear on-change-text icon-name]
    :or   {type :text}
    :as   props}]
  (let [base-props (cond-> props
                     icon-name      (assoc-in [:left-icon :icon-name] icon-name)
                     clearable?     (assoc :right-icon
                                           {:style-fn  style/clear-icon
                                            :icon-name :i/clear
                                            :on-press  #(when on-clear (on-clear))})
                     on-change-text (assoc :on-change-text
                                           (fn [new-text]
                                             (on-change-text new-text)
                                             (reagent/flush))))]
    (if (= type :password)
      [password-input base-props]
      [base-input base-props])))

(def input (quo.theme/with-theme input-internal))
