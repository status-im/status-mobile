(ns status-im2.contexts.quo-preview.preview
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [clojure.string :as string]
            [status-im2.contexts.quo-preview.common :as common]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]
            [quo2.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.style :as style]
            utils.number)
  (:require-macros status-im2.contexts.quo-preview.preview))

(defn- label-view
  [_ label]
  [rn/view {:style style/label-container}
   [rn/text {:style (style/label)}
    label]])

(defn- humanize
  [k]
  ;; We explicitly convert `k` to string because sometimes it's a number and
  ;; Clojure would throw an exception.
  (-> (if (keyword? k) k (str k))
      camel-snake-kebab/->kebab-case-keyword
      name
      (string/replace "-" " ")
      string/capitalize))

(defn- key->boolean-label
  [k]
  (let [label (humanize k)]
    (if (string/ends-with? label "?")
      label
      (str label "?"))))

(defn- key->text-label
  [k]
  (str (humanize k) ":"))

(defn- customizer-boolean
  [{:keys [label state] :as args}]
  (let [label       (or label (key->boolean-label (:key args)))
        field-value (reagent/cursor state [(:key args)])
        active?     @field-value]
    [rn/view {:style style/field-row}
     [label-view state label]
     [rn/view {:style (style/boolean-container)}
      [rn/pressable
       {:style    (style/boolean-button {:active? active? :left? true})
        :on-press #(reset! field-value true)}
       [rn/text {:style (style/field-text active?)}
        "True"]]
      [rn/pressable
       {:style    (style/boolean-button {:active? (not active?) :left? false})
        :on-press #(reset! field-value false)}
       [rn/text {:style (style/field-text (not active?))}
        "False"]]]]))

(defn- customizer-text
  [{:keys [label state limit suffix] :as args}]
  (let [label       (or label (key->text-label (:key args)))
        field-value (reagent/cursor state [(:key args)])]
    [rn/view {:style style/field-row}
     [label-view state label]
     [rn/view {:style style/field-column}
      [rn/text-input
       (merge
        {:value               @field-value
         :show-cancel         false
         :style               (style/field-container false)
         :keyboard-appearance (quo.theme/theme-value :light :dark)
         :on-change-text      (fn [text]
                                (reset! field-value (if (and suffix
                                                             (> (count text) (count @field-value)))
                                                      (str (string/replace text suffix "") suffix)
                                                      text))
                                (reagent/flush))}
        (when limit
          {:max-length limit}))]]]))

(defn- customizer-number
  [{:keys [label state default] :as args}]
  (let [label       (or label (key->text-label (:key args)))
        field-value (reagent/cursor state [(:key args)])]
    [rn/view {:style style/field-row}
     [label-view state label]
     [rn/view {:style style/field-column}
      [rn/text-input
       (merge
        {:value               (str @field-value)
         :show-cancel         false
         :style               (style/field-container false)
         :keyboard-appearance (quo.theme/theme-value :light :dark)
         :on-change-text      (fn [text]
                                (reset! field-value (utils.number/parse-int text default))
                                (reagent/flush))})]]]))

(defn- find-selected-option
  [id v]
  (first (filter #(= (:key %) id) v)))

(defn- customizer-select-modal
  [{:keys [open options field-value]}]
  [rn/modal
   {:visible                @open
    :on-request-close       #(reset! open false)
    :status-bar-translucent true
    :transparent            true
    :animation              :slide}
   [rn/view {:style (style/modal-overlay)}
    [rn/view {:style (style/modal-container)}
     [rn/scroll-view {:shows-vertical-scroll-indicator false}
      (doall
       (for [{k :key v :value} options
             :let              [v (or v (humanize k))]]
         ^{:key k}
         [rn/pressable
          {:style    (style/select-option (= @field-value k))
           :on-press (fn []
                       (reset! open false)
                       (reset! field-value k))}
          [rn/text {:style (style/field-text (= @field-value k))}
           v]]))]
     [rn/view {:style (style/footer)}
      [rn/pressable
       {:style    (style/select-button)
        :on-press (fn []
                    (reset! field-value nil)
                    (reset! open false))}
       [rn/text {:style (style/field-text false)}
        "Clear"]]
      [rn/view {:style {:width 16}}]
      [rn/touchable-opacity
       {:style    (style/select-button)
        :on-press #(reset! open false)}
       [rn/text {:style (style/field-text false)}
        "Close"]]]]]])

(defn- customizer-select-button
  [{:keys [open selected-option]}]
  [rn/pressable
   {:style    (style/select-container)
    :on-press #(reset! open true)}
   [rn/text
    {:style           (style/field-select)
     :number-of-lines 1}
    (if selected-option
      (or (:value selected-option) (humanize (:key selected-option)))
      "Select option")]
   [rn/view
    [quo/icon :i/chevron-right]]])

(defn- customizer-select
  []
  (let [open (reagent/atom nil)]
    (fn [{:keys [label state options] :as args}]
      (let [label           (or label (key->text-label (:key args)))
            field-value     (reagent/cursor state [(:key args)])
            selected-option (find-selected-option @field-value options)]
        [rn/view {:style style/field-row}
         [label-view state label]
         [rn/view {:style style/field-column}
          [customizer-select-modal
           {:open        open
            :options     options
            :field-value field-value}]
          [customizer-select-button {:open open :selected-option selected-option}]]]))))

(defn customizer
  [state descriptors]
  [rn/view
   {:style {:flex-shrink        1
            :padding-horizontal 20}}
   (doall
    (for [desc descriptors
          :let [descriptor (merge desc {:state state})]]
      ^{:key (:key desc)}
      [:<>
       (case (:type desc)
         :boolean [customizer-boolean descriptor]
         :text    [customizer-text descriptor]
         :number  [customizer-number descriptor]
         :select  [customizer-select descriptor]
         nil)]))])

(defn customization-color-option
  ([]
   (customization-color-option {}))
  ([opts]
   (merge {:key     :customization-color
           :type    :select
           :options (->> colors/customization
                         keys
                         sort
                         (map (fn [k]
                                {:key k :value (string/capitalize (name k))})))}
          opts)))

(defn blur-view
  [{:keys [show-blur-background? image height blur-view-props style]} & children]
  [rn/view
   {:style {:flex             1
            :padding-vertical 16}}
   (when show-blur-background?
     [rn/view
      {:style {:height        (or height 100)
               :border-radius 16
               :overflow      :hidden}}
      [rn/image
       {:source (or image (resources/get-mock-image :dark-blur-bg))
        :style  {:height "100%" :width "100%"}}]
      [blur/view
       (merge {:style         {:position :absolute
                               :top      0
                               :bottom   0
                               :left     0
                               :right    0}
               :blur-amount   10
               :overlay-color (colors/theme-colors colors/white-opa-70 colors/neutral-80-opa-80)}
              blur-view-props)]])
   (into [rn/view
          {:style (merge {:position           :absolute
                          :top                32
                          :padding-horizontal 16}
                         style)}]
         children)])

(defn- f-preview-container
  [{:keys [state descriptor blur? blur-dark-only?
           component-container-style
           blur-container-style blur-view-props blur-height show-blur-background?]
    :or   {blur-height 200}}
   & children]
  (rn/use-effect (fn []
                   (when blur-dark-only?
                     (if blur?
                       (quo.theme/set-theme :dark)
                       (quo.theme/set-theme :light))))
                 [blur? blur-dark-only?])
  [rn/view
   {:style {:top  (safe-area/get-top)
            :flex 1}}
   [common/navigation-bar]
   [rn/scroll-view
    {:style                           (style/panel-basic)
     :shows-vertical-scroll-indicator false}
    [rn/pressable {:on-press rn/dismiss-keyboard!}
     (when descriptor
       [rn/view {:style style/customizer-container}
        [customizer state descriptor]])
     (if blur?
       [rn/view {:style (merge style/component-container component-container-style)}
        (into [blur-view
               {:show-blur-background? show-blur-background?
                :height                blur-height
                :style                 (merge {:width     "100%"
                                               :flex-grow 1}
                                              (when-not show-blur-background?
                                                {:padding-horizontal 0
                                                 :top                0})
                                              blur-container-style)
                :blur-view-props       (merge {:blur-type (quo.theme/get-theme)}
                                              blur-view-props)}]
              children)]
       (into [rn/view {:style (merge style/component-container component-container-style)}]
             children))]]])

(defn preview-container
  [& args]
  (into [:f> f-preview-container] args))
