(ns quo.components.inputs.input.view
  (:require [oops.core :as oops]
            [quo.components.icon :as icon]
            [quo.components.inputs.input.style :as style]
            [quo.components.markdown.text :as text]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.platform :as platform]))

(defn- label-line
  [{:keys [label label-right current-chars char-limit variant-colors theme]}]
  [rn/view
   {:accessibility-label :input-labels
    :style               style/texts-container}
   [rn/view {:style style/label-container}
    [text/text
     {:style  (style/label-color variant-colors)
      :weight :medium
      :size   :paragraph-2}
     label]]
   (when label-right
     [rn/view {:style style/right-label-container}
      [text/text
       {:style  (style/label-color variant-colors)
        :weight :regular
        :size   :paragraph-2}
       label-right]])
   (when char-limit
     (let [count-text (str current-chars "/" char-limit)]
       [rn/view {:style style/right-label-container}
        [text/text
         {:style  (style/counter-color {:current-chars  current-chars
                                        :char-limit     char-limit
                                        :variant-colors variant-colors
                                        :theme          theme})
          :weight :regular
          :size   :paragraph-2}
         count-text]]))])

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
  [:type :blur? :error? :right-icon :left-icon :disabled? :small? :button
   :label :char-limit :on-char-limit-reach :icon-name :multiline? :on-focus :on-blur
   :container-style :input-container-style :ref :placeholder])

(defn- base-input
  [{:keys [blur? error? right-icon left-icon disabled? small? button
           label char-limit multiline? clearable? on-focus on-blur container-style input-container-style
           on-change-text on-char-limit-reach weight default-value on-clear placeholder label-right]
    :as   props}]
  (let [theme                  (quo.theme/use-theme)
        ref                    (rn/use-ref-atom nil)
        on-ref                 (rn/use-callback (fn [value]
                                                  (when (:ref props)
                                                    ((:ref props) value))
                                                  (reset! ref value)))
        [status set-status]    (rn/use-state :default)
        internal-on-focus      (rn/use-callback #(set-status :focus))
        internal-on-blur       (rn/use-callback #(set-status :default))
        [multiple-lines?
         set-multiple-lines]   (rn/use-state false)
        on-content-size-change (rn/use-callback
                                (fn [event]
                                  (let [height     (oops/oget event "nativeEvent.contentSize.height")
                                        ;; In Android height comes with padding
                                        min-height (if platform/android? 40 22)]
                                    (if (> height min-height)
                                      (set-multiple-lines true)
                                      (set-multiple-lines false)))))
        [char-count
         set-char-count]       (rn/use-state (count default-value))
        on-change-text         (rn/use-callback
                                (fn [new-text]
                                  (when on-change-text (on-change-text new-text))
                                  (let [amount-chars (count new-text)]
                                    (set-char-count amount-chars)
                                    (when (and (>= amount-chars char-limit) on-char-limit-reach)
                                      (on-char-limit-reach amount-chars)))))
        status-kw              (cond
                                 disabled? :disabled
                                 error?    :error
                                 :else     status)
        colors-by-status       (style/status-colors status-kw blur? theme)
        variant-colors         (style/variants-colors blur? theme)
        clear-on-press         (rn/use-callback (fn []
                                                  (.clear ^js @ref)
                                                  (when on-clear (on-clear)))
                                                [on-clear])
        right-icon             (or right-icon
                                   (when clearable?
                                     {:style-fn  style/clear-icon
                                      :icon-name :i/clear
                                      :on-press  clear-on-press}))
        clean-props            (apply dissoc props custom-props)
        ;; Workaround for android cursor overlapping placeholder
        ;; https://github.com/facebook/react-native/issues/27687
        modified-placeholder   (if platform/android? (str "\u2009" placeholder) placeholder)]
    [rn/view {:style container-style}
     (when (or label char-limit label-right)
       [label-line
        {:variant-colors variant-colors
         :label          label
         :label-right    label-right
         :current-chars  char-count
         :char-limit     char-limit
         :theme          theme}])
     [rn/view
      {:style (merge (style/input-container colors-by-status small? disabled?) input-container-style)}
      (when-let [{:keys [icon-name]} left-icon]
        [left-accessory
         {:variant-colors variant-colors
          :small?         small?
          :icon-name      icon-name}])
      [rn/text-input
       (cond-> {:ref                    on-ref
                :style                  (style/input colors-by-status small? multiple-lines? weight)
                :accessibility-label    :input
                :placeholder-text-color (:placeholder colors-by-status)
                :keyboard-appearance    theme
                :cursor-color           (:cursor variant-colors)
                :editable               (not disabled?)
                :placeholder            modified-placeholder
                :on-focus               (fn []
                                          (when on-focus (on-focus))
                                          (internal-on-focus))
                :on-blur                (fn []
                                          (when on-blur (on-blur))
                                          (internal-on-blur))}
         :always    (merge clean-props)
         multiline? (assoc :multiline              true
                           :on-content-size-change on-content-size-change)
         char-limit (assoc :on-change-text on-change-text))]
      (when-let [{:keys [on-press icon-name style-fn]} right-icon]
        [right-accessory
         {:variant-colors variant-colors
          :small?         small?
          :disabled?      disabled?
          :icon-style-fn  style-fn
          :icon-name      icon-name
          :on-press       (fn []
                            (when clearable? (set-char-count 0))
                            (on-press))}])
      (when-let [{:keys [on-press text]} button]
        [right-button
         {:colors-by-status colors-by-status
          :variant-colors   variant-colors
          :small?           small?
          :disabled?        disabled?
          :on-press         on-press
          :text             text}])]]))

(defn- password-input
  [{:keys [default-shown?]
    :or   {default-shown? false}
    :as   props}]
  (let [[password-shown?
         set-password-shown] (rn/use-state default-shown?)
        on-press             (rn/use-callback #(set-password-shown (not password-shown?))
                                              [password-shown?])]
    [base-input
     (assoc props
            :accessibility-label :password-input
            :auto-capitalize     :none
            :auto-complete       :password
            :secure-text-entry   (not password-shown?)
            :right-icon          {:style-fn  style/password-icon
                                  :icon-name (if password-shown? :i/hide-password :i/reveal)
                                  :on-press  on-press})]))

(defn input
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
  - :label-right - Additional label aligned to the right
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
  [{:keys [type icon-name]
    :or   {type :text}
    :as   props}]
  (let [base-props (cond-> props
                     icon-name (assoc-in [:left-icon :icon-name] icon-name))]
    (if (= type :password)
      [password-input base-props]
      [base-input base-props])))
